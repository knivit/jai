package com.tsoft.jai.render;

import com.tsoft.jai.anyhow.Result;
import com.tsoft.jai.client.stream.SseEvent;
import com.tsoft.jai.inquire.spinner.Spinner;
import com.tsoft.jai.render.markdown.MarkdownRender;
import com.tsoft.jai.tokio.sync.mpsc.UnboundedReceiver;
import com.tsoft.jai.utils.AbortSignal;

import java.io.PrintWriter;

import static com.tsoft.jai.anyhow.Result.*;
import static com.tsoft.jai.inquire.Inquire.*;
import static com.tsoft.jai.inquire.spinner.Spinner.spawnSpinner;
import static com.tsoft.jai.utils.AbortSignal.pollAbortSignal;

public class Stream {

    // pub async fn markdown_stream(
    //    rx: UnboundedReceiver<SseEvent>,
    //    render: &mut MarkdownRender,
    //    abort_signal: &AbortSignal,
    // ) -> Result<()> {
    //    enable_raw_mode()?;
    //    let mut stdout = io::stdout();
    //
    //    let ret = markdown_stream_inner(rx, render, abort_signal, &mut stdout).await;
    //
    //    disable_raw_mode()?;
    //
    //    if ret.is_err() {
    //        println!();
    //    }
    //    ret
    // }
    public static Result<?> markdownStream(UnboundedReceiver<SseEvent> rx, MarkdownRender render, AbortSignal abortSignal) {
        enableRawMode();
        PrintWriter stdout = terminal.writer();

        Result<?> ret = markdownStreamInner(rx, render, abortSignal, stdout);

        disableRawMode();

        if (isErr(ret)) {
            println();
        }
        return ret;
    }

    // pub async fn raw_stream(
    //    mut rx: UnboundedReceiver<SseEvent>,
    //    abort_signal: &AbortSignal,
    // ) -> Result<()> {
    //    let mut spinner = Some(spawn_spinner("Generating"));
    //
    //    loop {
    //        if abort_signal.aborted() {
    //            break;
    //        }
    //        if let Some(evt) = rx.recv().await {
    //            if let Some(spinner) = spinner.take() {
    //                spinner.stop();
    //            }
    //
    //            match evt {
    //                SseEvent::Text(text) => {
    //                    print!("{text}");
    //                    stdout().flush()?;
    //                }
    //                SseEvent::Done => {
    //                    break;
    //                }
    //            }
    //        }
    //    }
    //    if let Some(spinner) = spinner.take() {
    //        spinner.stop();
    //    }
    //    Ok(())
    // }
    public static void rawStream(UnboundedReceiver<SseEvent> rx, AbortSignal abortSignal) {

    }

    // async fn markdown_stream_inner(
    //    mut rx: UnboundedReceiver<SseEvent>,
    //    render: &mut MarkdownRender,
    //    abort_signal: &AbortSignal,
    //    writer: &mut Stdout,
    //) -> Result<()> {
    //    let mut buffer = String::new();
    //    let mut buffer_rows = 1;
    //
    //    let columns = terminal::size()?.0;
    //
    //    let mut spinner = Some(spawn_spinner("Generating"));
    //
    //    'outer: loop {
    //        if abort_signal.aborted() {
    //            break;
    //        }
    //        for reply_event in gather_events(&mut rx).await {
    //            if let Some(spinner) = spinner.take() {
    //                spinner.stop();
    //            }
    //
    //            match reply_event {
    //                SseEvent::Text(mut text) => {
    //                    // tab width hacking
    //                    text = text.replace('\t', "    ");
    //
    //                    let mut attempts = 0;
    //                    let (col, mut row) = loop {
    //                        match cursor::position() {
    //                            Ok(pos) => break pos,
    //                            Err(_) if attempts < 3 => attempts += 1,
    //                            Err(e) => return Err(e.into()),
    //                        }
    //                    };
    //
    //                    // Fix unexpected duplicate lines on kitty, see https://github.com/sigoden/aichat/issues/105
    //                    if col == 0 && row > 0 && display_width(&buffer) == columns as usize {
    //                        row -= 1;
    //                    }
    //
    //                    if row + 1 >= buffer_rows {
    //                        queue!(writer, cursor::MoveTo(0, row + 1 - buffer_rows),)?;
    //                    } else {
    //                        let scroll_rows = buffer_rows - row - 1;
    //                        queue!(
    //                            writer,
    //                            terminal::ScrollUp(scroll_rows),
    //                            cursor::MoveTo(0, 0),
    //                        )?;
    //                    }
    //
    //                    // No guarantee that text returned by render will not be re-layouted, so it is better to clear it.
    //                    queue!(writer, terminal::Clear(terminal::ClearType::FromCursorDown))?;
    //
    //                    if text.contains('\n') {
    //                        let text = format!("{buffer}{text}");
    //                        let (head, tail) = split_line_tail(&text);
    //                        let output = render.render(head);
    //                        print_block(writer, &output, columns)?;
    //                        buffer = tail.to_string();
    //                    } else {
    //                        buffer = format!("{buffer}{text}");
    //                    }
    //
    //                    let output = render.render_line(&buffer);
    //                    if output.contains('\n') {
    //                        let (head, tail) = split_line_tail(&output);
    //                        buffer_rows = print_block(writer, head, columns)?;
    //                        queue!(writer, style::Print(&tail),)?;
    //
    //                        // No guarantee the buffer width of the buffer will not exceed the number of columns.
    //                        // So we calculate the number of rows needed, rather than setting it directly to 1.
    //                        buffer_rows += need_rows(tail, columns);
    //                    } else {
    //                        queue!(writer, style::Print(&output))?;
    //                        buffer_rows = need_rows(&output, columns);
    //                    }
    //
    //                    writer.flush()?;
    //                }
    //                SseEvent::Done => {
    //                    break 'outer;
    //                }
    //            }
    //        }
    //
    //        if poll_abort_signal(abort_signal)? {
    //            break;
    //        }
    //    }
    //
    //    if let Some(spinner) = spinner.take() {
    //        spinner.stop();
    //    }
    //    Ok(())
    // }
    private static Result<?> markdownStreamInner(UnboundedReceiver<SseEvent> rx, MarkdownRender render, AbortSignal abortSignal, PrintWriter writer) {
        StringBuilder buffer = new StringBuilder();
        int bufferRows = 1;

        int columns = terminalSize().getColumns();

        Spinner spinner = spawnSpinner("Generating");

        while (true) {
            if (abortSignal.aborted()) {
                break;
            }

            Result<Boolean> res = pollAbortSignal(abortSignal);
            if (isOk(res) && Boolean.TRUE.equals(res.getValue())) {
                break;
            }
        }

        return Ok();
    }
}
