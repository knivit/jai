package com.tsoft.jai.render;

import com.tsoft.jai.client.stream.SseEvent;
import com.tsoft.jai.render.markdown.MarkdownRender;
import com.tsoft.jai.tokio.sync.mpsc.UnboundedReceiver;
import com.tsoft.jai.utils.AbortSignal;

import java.io.PrintWriter;

import static com.tsoft.jai.inquire.Inquire.enableRawMode;
import static com.tsoft.jai.inquire.Inquire.terminal;

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
    public static void markdownStream(UnboundedReceiver<SseEvent> rx, MarkdownRender render, AbortSignal abortSignal) {
        enableRawMode();
        PrintWriter stdout = terminal().writer();

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
}
