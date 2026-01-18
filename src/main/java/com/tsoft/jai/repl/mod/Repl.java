package com.tsoft.jai.repl.mod;

import com.tsoft.jai.config.Config;
import com.tsoft.jai.repl.prompt.ReplPrompt;
import com.tsoft.jai.utils.AbortSignal;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class Repl {

    private Config config;
    //private Reedline editor;
    private ReplPrompt prompt;
    private AbortSignal abortSignal;

    // pub fn init(config: &GlobalConfig) -> Result<Self> {
    //    let editor = Self::create_editor(config)?;
    //
    //    let prompt = ReplPrompt::new(config);
    //    let abort_signal = create_abort_signal();
    //
    //    Ok(Self {
    //        config: config.clone(),
    //        editor,
    //        prompt,
    //        abort_signal,
    //    })
    // }
    public static Repl init(Config config) {
        return null;
    }

    //     pub async fn run(&mut self) -> Result<()> {
    //        if AssertState::False(StateFlags::AGENT | StateFlags::RAG)
    //            .assert(self.config.read().state())
    //        {
    //            print!(
    //                r#"Welcome to {} {}
    //Type ".help" for additional help.
    //"#,
    //                env!("CARGO_CRATE_NAME"),
    //                env!("CARGO_PKG_VERSION"),
    //            )
    //        }
    //
    //        loop {
    //            if self.abort_signal.aborted_ctrld() {
    //                break;
    //            }
    //            let sig = self.editor.read_line(&self.prompt);
    //            match sig {
    //                Ok(Signal::Success(line)) => {
    //                    self.abort_signal.reset();
    //                    match run_repl_command(&self.config, self.abort_signal.clone(), &line).await {
    //                        Ok(exit) => {
    //                            if exit {
    //                                break;
    //                            }
    //                        }
    //                        Err(err) => {
    //                            render_error(err);
    //                            println!()
    //                        }
    //                    }
    //                }
    //                Ok(Signal::CtrlC) => {
    //                    self.abort_signal.set_ctrlc();
    //                    println!("(To exit, press Ctrl+D or enter \".exit\")\n");
    //                }
    //                Ok(Signal::CtrlD) => {
    //                    self.abort_signal.set_ctrld();
    //                    break;
    //                }
    //                _ => {}
    //            }
    //        }
    //        self.config.write().exit_session()?;
    //        Ok(())
    //    }
    public void run() {

    }

    // fn create_editor(config: &GlobalConfig) -> Result<Reedline> {
    //    let completer = ReplCompleter::new(config);
    //    let highlighter = ReplHighlighter::new(config);
    //    let menu = Self::create_menu();
    //    let edit_mode = Self::create_edit_mode(config);
    //    let cursor_config = CursorConfig {
    //        vi_insert: Some(SetCursorStyle::BlinkingBar),
    //        vi_normal: Some(SetCursorStyle::SteadyBlock),
    //        emacs: None,
    //    };
    //    let mut editor = Reedline::create()
    //        .with_completer(Box::new(completer))
    //        .with_highlighter(Box::new(highlighter))
    //        .with_menu(menu)
    //        .with_edit_mode(edit_mode)
    //        .with_cursor_config(cursor_config)
    //        .with_quick_completions(true)
    //        .with_partial_completions(true)
    //        .use_bracketed_paste(true)
    //        .with_validator(Box::new(ReplValidator))
    //        .with_ansi_colors(true);
    //
    //    if let Ok(cmd) = config.read().editor() {
    //        let temp_file = temp_file("-repl-", ".md");
    //        let command = process::Command::new(cmd);
    //        editor = editor.with_buffer_editor(command, temp_file);
    //    }
    //
    //    Ok(editor)
    // }
    public void createEditor(Config config) {

    }
}
