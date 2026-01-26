package com.tsoft.jai.inquire.prompt;

import com.tsoft.jai.anyhow.Result;
import com.tsoft.jai.utils.AbortSignal;
import com.tsoft.jai.utils.base.TupleN;
import lombok.RequiredArgsConstructor;
import org.jline.keymap.BindingReader;

import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import static com.tsoft.jai.anyhow.Macros.bail;
import static com.tsoft.jai.anyhow.Result.*;
import static com.tsoft.jai.inquire.Inquire.IS_STDOUT_TERMINAL;
import static com.tsoft.jai.inquire.Inquire.terminal;
import static com.tsoft.jai.tokio.Join.join;
import static com.tsoft.jai.tokio.Select.branch;
import static com.tsoft.jai.tokio.Select.select;
import static com.tsoft.jai.utils.AbortSignal.waitAbortSignal;
import static java.util.concurrent.CompletableFuture.supplyAsync;

@RequiredArgsConstructor
public class Spinner {

    private final String message;

    private volatile boolean active = false;

    // pub async fn abortable_run_with_spinner<F, T>(
    //    task: F,
    //    message: &str,
    //    abort_signal: AbortSignal,
    // ) -> Result<T>
    // where
    //    F: Future<Output = Result<T>>,
    // {
    //    let (_, spinner_rx) = Spinner::create(message);
    //    abortable_run_with_spinner_rx(task, spinner_rx, abort_signal).await
    public static <T> Result<T> abortableRunWithSpinner(Supplier<Result<T>> task, String message, AbortSignal abortSignal) {
        Spinner spinner = new Spinner(message);
        return spinner.abortableRunWithSpinnerRx(task, abortSignal);
    }

    // pub async fn abortable_run_with_spinner_rx<F, T>(
    //    task: F,
    //    spinner_rx: UnboundedReceiver<SpinnerEvent>,
    //    abort_signal: AbortSignal,
    // ) -> Result<T>
    // where
    //    F: Future<Output = Result<T>>,
    // {
    //    if *IS_STDOUT_TERMINAL {
    //        let (done_tx, done_rx) = oneshot::channel();
    //        let run_task = async {
    //            tokio::select! {
    //                ret = task => {
    //                    let _ = done_tx.send(());
    //                    ret
    //                }
    //                _ = tokio::signal::ctrl_c() => {
    //                    abort_signal.set_ctrlc();
    //                    let _ = done_tx.send(());
    //                    bail!("Aborted!")
    //                },
    //                _ = wait_abort_signal(&abort_signal) => {
    //                    let _ = done_tx.send(());
    //                    bail!("Aborted.");
    //                },
    //            }
    //        };
    //        let (task_ret, spinner_ret) = tokio::join!(
    //            run_task,
    //            run_abortable_spinner(spinner_rx, done_rx, abort_signal.clone())
    //        );
    //        spinner_ret?;
    //        task_ret
    //    } else {
    //        task.await
    //    }
    // }
    public <T> Result<T> abortableRunWithSpinnerRx(Supplier<Result<T>> task, AbortSignal abortSignal) {
        if (IS_STDOUT_TERMINAL) {
            CompletableFuture<Result<T>> runTask = supplyAsync(
                () -> select(
                    branch(supplyAsync(task), ret -> {
                        stopSpinner();
                        return ret;
                    }),
                    branch(supplyAsync(() -> {
                        BindingReader bindingReader = new BindingReader(terminal().reader());
                        try {
                            while (true) {
                                int ch = bindingReader.readCharacter();
                                if (ch == 3) {
                                    return Ok();
                                }
                            }
                        } catch (CancellationException ce) {
                            return Err();
                        }
                    }), ret -> {
                        abortSignal.setCtrlC();
                        stopSpinner();
                        return bail("Aborted.");
                    }),
                    branch(supplyAsync(() -> {
                        waitAbortSignal(abortSignal);
                        return Ok();
                    }), ret -> {
                        stopSpinner();
                        return bail("Aborted.");
                    })
                ));

            TupleN tuple = join(
                runTask,
                runAbortableSpinner(abortSignal)
            );

            Result<T> taskRet = tuple.next();
            Result<T> spinnerRet = tuple.next();
            if (isOk(spinnerRet)) {
                return spinnerRet;
            }
            return taskRet;
        } else {
            return task.get();
        }
    }

    private CompletableFuture<Result<?>> runAbortableSpinner(AbortSignal abortSignal) {
        return supplyAsync(() -> {
            startSpinner(abortSignal);
            return Ok();
        }, ret -> stopSpinner());
    }

    //private BindingReader createCtrlCTerminalReader() {
    //    enum CtrlCOperation {
    //        CANCEL;
    //    }
    //
    //    KeyMap<CtrlCOperation> keyMap = new KeyMap<>();
    //    keyMap.bind(CtrlCOperation.CANCEL, ctrl('C'));//"\u0003"); // Ctrl+C
    //    BindingReader bindingReader = new BindingReader(terminal().reader());
    //}

    private void startSpinner(AbortSignal abortSignal) {
        if (active) {
            return;
        }

        active = true;
        String[] frames = {"⠋", "⠙", "⠹", "⠸", "⠼", "⠴", "⠦", "⠧", "⠇", "⠏"};

        try {
            terminal().writer().write("\033[?25l"); // Hide cursor
            terminal().flush();

            int frameIndex = 0;
            while (active && !abortSignal.aborted()) {
                String frame = frames[frameIndex % frames.length];

                // Move cursor to beginning of line and clear
                terminal().writer().write("\r\033[K");
                terminal().writer().write(frame + " " + message);
                terminal().flush();

                frameIndex++;
                Thread.sleep(25);
            }

            // Clear line
            terminal().writer().write("\r\033[K");
            terminal().flush();
        } catch (Exception e) {
            // Ignore on shutdown
        }
    }

    private void stopSpinner() {
        if (active) {
            terminal().writer().write("\033[?25h"); // Show cursor
            terminal().flush();
        }
        active = false;
    }
}
