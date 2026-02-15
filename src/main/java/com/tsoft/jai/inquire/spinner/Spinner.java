package com.tsoft.jai.inquire.spinner;

import com.tsoft.jai.anyhow.Result;
import com.tsoft.jai.tokio.sync.oneshot.Oneshot;
import com.tsoft.jai.tokio.time.Interval;
import com.tsoft.jai.tokio.time.Time;
import com.tsoft.jai.tokio.Tokio;
import com.tsoft.jai.tokio.sync.oneshot.Receiver;
import com.tsoft.jai.tokio.sync.oneshot.Sender;
import com.tsoft.jai.tokio.sync.TryRecvError;
import com.tsoft.jai.tokio.sync.mpsc.UnboundedReceiver;
import com.tsoft.jai.tokio.sync.mpsc.UnboundedSender;
import com.tsoft.jai.utils.AbortSignal;
import com.tsoft.jai.utils.base.Tuple;
import com.tsoft.jai.utils.base.TupleN;
import lombok.RequiredArgsConstructor;
import org.jline.keymap.BindingReader;

import java.time.Duration;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

import static com.tsoft.jai.anyhow.Macros.bail;
import static com.tsoft.jai.anyhow.Result.*;
import static com.tsoft.jai.inquire.Inquire.IS_STDOUT_TERMINAL;
import static com.tsoft.jai.inquire.Inquire.terminal;
import static com.tsoft.jai.tokio.Join.join;
import static com.tsoft.jai.tokio.Select.branch;
import static com.tsoft.jai.tokio.sync.mpsc.Unbounded.unboundedChannel;
import static com.tsoft.jai.utils.AbortSignal.pollAbortSignal;
import static com.tsoft.jai.utils.AbortSignal.waitAbortSignal;
import static java.util.concurrent.CompletableFuture.supplyAsync;

@RequiredArgsConstructor
public class Spinner {

    private final UnboundedSender<SpinnerEvent> tx;

    // pub fn create(message: &str) -> (Self, UnboundedReceiver<SpinnerEvent>) {
    //    let (tx, spinner_rx) = mpsc::unbounded_channel();
    //    let spinner = Spinner(tx);
    //    let _ = spinner.set_message(message.to_string());
    //    (spinner, spinner_rx)
    // }
    public static Tuple<Spinner, UnboundedReceiver<SpinnerEvent>> create(String message) {
        Tuple<UnboundedSender<SpinnerEvent>, UnboundedReceiver<SpinnerEvent>> tuple = unboundedChannel();
        UnboundedSender<SpinnerEvent> tx = tuple.first();
        UnboundedReceiver<SpinnerEvent> spinnerRx = tuple.second();
        Spinner spinner = new Spinner(tx);
        spinner.setMessage(message);
        return new Tuple<>(spinner, spinnerRx);
    }

    // pub fn set_message(&self, message: String) -> Result<()> {
    //     self.0.send(SpinnerEvent::SetMessage(message))?;
    //     std::thread::sleep(Duration::from_millis(10));
    //     Ok(())
    // }
    public Result<?> setMessage(String message) {
        tx.send(SpinnerEvent.SetMessage(message));
        Time.sleep(Duration.ofMillis(10));
        return Ok();
    }

    // pub fn stop(&self) {
    //    let _ = self.0.send(SpinnerEvent::Stop);
    //    std::thread::sleep(Duration::from_millis(10));
    // }
    public void stop() {
        tx.send(SpinnerEvent.Stop());
        Time.sleep(Duration.ofMillis(10));
    }

    // pub fn spawn_spinner(message: &str) -> Spinner {
    //    let (spinner, mut spinner_rx) = Spinner::create(message);
    //    tokio::spawn(async move {
    //        let mut spinner = SpinnerInner::default();
    //        let mut interval = interval(Duration::from_millis(50));
    //        loop {
    //            tokio::select! {
    //                evt = spinner_rx.recv() => {
    //                    if let Some(evt) = evt {
    //                        match evt {
    //                            SpinnerEvent::SetMessage(message) => {
    //                                spinner.set_message(message)?;
    //                            }
    //                            SpinnerEvent::Stop => {
    //                                spinner.clear_message()?;
    //                                break;
    //                            }
    //                        }
    //
    //                    }
    //                }
    //                _ = interval.tick() => {
    //                    let _ = spinner.step();
    //                }
    //            }
    //        }
    //        Ok::<(), anyhow::Error>(())
    //    });
    //    spinner
    // }
    public static Spinner spawnSpinner(String message) {
        Tuple<Spinner, UnboundedReceiver<SpinnerEvent>> tuple = Spinner.create(message);
        Spinner spinner = tuple.first();
        UnboundedReceiver<SpinnerEvent> spinnerRx = tuple.second();

        Tokio.spawn(() -> {
            SpinnerInner spinnerInner = SpinnerInner.getDefault();
            Interval interval = new Interval(Duration.ofMillis(50));

            AtomicBoolean done = new AtomicBoolean();
            while (!done.get()) {
                Tokio.select(
                    branch(supplyAsync(() -> spinnerRx.recv()),
                        res -> {
                            if (SpinnerEvent.isSetMessage(res)) {
                                SpinnerEvent evt = (SpinnerEvent)res.getValue();
                                String msg = evt.getMessage();
                                spinnerInner.setMessage(msg);
                            } else if (SpinnerEvent.isStop(res)) {
                                spinnerInner.clearMessage();
                                // ? break;
                            }
                            return Ok();
                        }),

                    branch(supplyAsync(() -> interval.tick()),
                        res -> {
                            spinnerInner.step();
                            return Ok();
                        }));
            }
            return Ok();
        });
        return spinner;
    }

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
        Tuple<Spinner, UnboundedReceiver<SpinnerEvent>> tuple = Spinner.create(message);
        UnboundedReceiver<SpinnerEvent> spinnerRx = tuple.second();
        return abortableRunWithSpinnerRx(task, spinnerRx, abortSignal);
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
    public static <T> Result<T> abortableRunWithSpinnerRx(Supplier<Result<T>> task, UnboundedReceiver<SpinnerEvent> spinnerRx, AbortSignal abortSignal) {
        if (IS_STDOUT_TERMINAL) {
            Tuple<Sender<Void>, Receiver<Void>> tuple = Oneshot.channel();
            Sender<Void> doneTx = tuple.first();
            Receiver<Void> doneRx = tuple.second();

            CompletableFuture<Result<T>> runTask = supplyAsync(
                () -> Tokio.select(
                    branch(supplyAsync(task), ret -> {
                        doneTx.send(Ok());
                        return ret;
                    }),

                    branch(supplyAsync(() -> {
                        BindingReader bindingReader = new BindingReader(terminal.reader());
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
                        doneTx.send(Ok());
                        return bail("Aborted.");
                    }),

                    branch(supplyAsync(() -> {
                        waitAbortSignal(abortSignal);
                        return Ok();
                    }), ret -> {
                        doneTx.send(Ok());
                        return bail("Aborted.");
                    })
                ));

            TupleN tupleN = join(
                runTask,
                supplyAsync(() -> runAbortableSpinner(spinnerRx, doneRx, abortSignal))
            );

            Result<T> taskRet = tupleN.next();
            Result<T> spinnerRet = tupleN.next();
            if (isOk(spinnerRet)) {
                return spinnerRet;
            }
            return taskRet;
        } else {
            return task.get();
        }
    }

    // async fn run_abortable_spinner(
    //    mut spinner_rx: UnboundedReceiver<SpinnerEvent>,
    //    mut done_rx: oneshot::Receiver<()>,
    //    abort_signal: AbortSignal,
    // ) -> Result<()> {
    //    let mut spinner = SpinnerInner::default();
    //    loop {
    //        if abort_signal.aborted() {
    //            break;
    //        }
    //
    //        tokio::time::sleep(Duration::from_millis(25)).await;
    //
    //        match done_rx.try_recv() {
    //            Ok(_) | Err(oneshot::error::TryRecvError::Closed) => {
    //                break;
    //            }
    //            _ => {}
    //        }
    //
    //        match spinner_rx.try_recv() {
    //            Ok(SpinnerEvent::SetMessage(message)) => {
    //                spinner.set_message(message)?;
    //            }
    //            Ok(SpinnerEvent::Stop) => {
    //                spinner.clear_message()?;
    //            }
    //            Err(_) => {}
    //        }
    //
    //        if poll_abort_signal(&abort_signal)? {
    //            break;
    //        }
    //
    //        spinner.step()?;
    //    }
    //
    //    spinner.clear_message()?;
    //    Ok(())
    // }
    private static Result<?> runAbortableSpinner(UnboundedReceiver<SpinnerEvent> spinnerRx, Receiver<Void> doneRx, AbortSignal abortSignal) {
        SpinnerInner spinner = SpinnerInner.getDefault();
        while (true) {
            if (abortSignal.aborted()) {
                break;
            }

            Time.sleep(Duration.ofMillis(25));

            Result<?> cRes = doneRx.tryRecv();
            if (isOk(cRes) || (isErr(cRes) && TryRecvError.isClosed(cRes))) {
                break;
            }

            Result<?> sRes = spinnerRx.tryRecv();
            if (isOk(sRes) && SpinnerEvent.isSetMessage(sRes)) {
                spinner.setMessage(((SpinnerEvent)sRes.getValue()).getMessage());
            } else if (isOk(sRes) && SpinnerEvent.isStop(sRes)) {
                spinner.clearMessage();
            }

            Result<Boolean> aRes = pollAbortSignal(abortSignal);
            if (isOk(aRes) && Boolean.TRUE.equals(aRes.getValue())) {
                break;
            }

            spinner.step();
        }

        spinner.clearMessage();
        return Ok();
    }
}
