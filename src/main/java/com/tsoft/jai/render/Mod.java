package com.tsoft.jai.render;

import com.tsoft.jai.anyhow.Error;
import com.tsoft.jai.anyhow.Result;
import com.tsoft.jai.client.stream.SseEvent;
import com.tsoft.jai.config.Config;
import com.tsoft.jai.render.markdown.MarkdownRender;
import com.tsoft.jai.render.markdown.RenderOptions;
import com.tsoft.jai.tokio.sync.mpsc.UnboundedReceiver;
import com.tsoft.jai.utils.AbortSignal;

import static com.tsoft.jai.inquire.Inquire.*;
import static com.tsoft.jai.render.Stream.markdownStream;
import static com.tsoft.jai.render.Stream.rawStream;
import static com.tsoft.jai.utils.Mod.errorText;
import static com.tsoft.jai.utils.Mod.prettyError;

public final class Mod {

    // pub async fn render_stream(
    //    rx: UnboundedReceiver<SseEvent>,
    //    config: &GlobalConfig,
    //    abort_signal: AbortSignal,
    // ) -> Result<()> {
    //    let ret = if *IS_STDOUT_TERMINAL && config.read().highlight {
    //        let render_options = config.read().render_options()?;
    //        let mut render = MarkdownRender::init(render_options)?;
    //        markdown_stream(rx, &mut render, &abort_signal).await
    //    } else {
    //        raw_stream(rx, &abort_signal).await
    //    };
    //    ret.map_err(|err| err.context("Failed to reader stream"))
    // }
    public static Result<?> renderStream(UnboundedReceiver<SseEvent> rx, Config config, AbortSignal abortSignal) {
        Result<?> ret;
        if (IS_STDOUT_TERMINAL && config.isHighlight()) {
            RenderOptions renderOptions = config.renderOptions();
            MarkdownRender render = MarkdownRender.init(renderOptions);
            ret = markdownStream(rx, render, abortSignal);
        } else {
            ret = rawStream(rx, abortSignal);
        }
        return ret;
    }

    // pub fn render_error(err: anyhow::Error) {
    //    eprintln!("{}", error_text(&pretty_error(&err)));
    // }
    public static void renderError(Error err) {
        println("{}", errorText(prettyError(err)));
    }

    private Mod() { }
}
