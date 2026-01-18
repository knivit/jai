package com.tsoft.jai;

import com.tsoft.jai.cli.Cli;
import com.tsoft.jai.client.Client;
import com.tsoft.jai.client.model.Model;
import com.tsoft.jai.client.model.ModelType;
import com.tsoft.jai.config.Config;
import com.tsoft.jai.config.Input;
import com.tsoft.jai.config.WorkingMode;
import com.tsoft.jai.config.agent.Agent;
import com.tsoft.jai.function.ToolResult;
import com.tsoft.jai.repl.mod.Repl;
import com.tsoft.jai.utils.AbortSignal;
import com.tsoft.jai.utils.Tuple;
import com.tsoft.jai.utils.command.Shell;

import java.util.Collections;
import java.util.List;

import static com.tsoft.jai.anyhow.Macros.bail;
import static com.tsoft.jai.client.macros.Macros.listModels;
import static com.tsoft.jai.config.Config.TEMP_SESSION_NAME;
import static com.tsoft.jai.config.Role.CODE_ROLE;
import static com.tsoft.jai.config.Role.SHELL_ROLE;
import static com.tsoft.jai.inquire.Inquire.println;
import static com.tsoft.jai.inquire.Inquire.terminal;
import static com.tsoft.jai.utils.CollectionsUtils.isEmpty;
import static com.tsoft.jai.utils.StringUtils.isBlank;
import static com.tsoft.jai.utils.command.Command.SHELL;

public class Main {

    // #[tokio::main]
    // async fn main() -> Result<()> {
    //    load_env_file()?;
    //    let cli = Cli::parse();
    //    let text = cli.text()?;
    //    let working_mode = if cli.serve.is_some() {
    //        WorkingMode::Serve
    //    } else if text.is_none() && cli.file.is_empty() {
    //        WorkingMode::Repl
    //    } else {
    //        WorkingMode::Cmd
    //    };
    //    let info_flag = cli.info
    //        || cli.sync_models
    //        || cli.list_models
    //        || cli.list_roles
    //        || cli.list_agents
    //        || cli.list_rags
    //        || cli.list_macros
    //        || cli.list_sessions;
    //    setup_logger(working_mode.is_serve())?;
    //    let config = Arc::new(RwLock::new(Config::init(working_mode, info_flag).await?));
    //    if let Err(err) = run(config, cli, text).await {
    //        render_error(err);
    //        std::process::exit(1);
    //    }
    //    Ok(())
    // }
    static void main(String[] args) {
        Cli cli = Cli.parse(args);

        String text = cli.text();
        WorkingMode workingMode = WorkingMode.Cmd;
        if (!isBlank(cli.getServe())) {
            workingMode = WorkingMode.Serve;
        } else if (isBlank(text) && cli.getFile().isEmpty()) {
            workingMode = WorkingMode.Repl;
        }

        boolean infoFlag = cli.isInfo()
            || cli.isSyncModels()
            || cli.isListModels()
            || cli.isListRoles()
            || cli.isListAgents()
            || cli.isListRags()
            || cli.isListMacros()
            || cli.isListSessions();

        Config config = Config.init(workingMode, infoFlag, cli.getConfigFile());

        run(config, cli, text);
    }

    // async fn run(config: GlobalConfig, cli: Cli, text: Option<String>) -> Result<()> {
    //    let abort_signal = create_abort_signal();
    //
    //    if cli.sync_models {
    //        let url = config.read().sync_models_url();
    //        return Config::sync_models(&url, abort_signal.clone()).await;
    //    }
    //
    //    if cli.list_models {
    //        for model in list_models(&config.read(), ModelType::Chat) {
    //            println!("{}", model.id());
    //        }
    //        return Ok(());
    //    }
    //    if cli.list_roles {
    //        let roles = Config::list_roles(true).join("\n");
    //        println!("{roles}");
    //        return Ok(());
    //    }
    //    if cli.list_agents {
    //        let agents = list_agents().join("\n");
    //        println!("{agents}");
    //        return Ok(());
    //    }
    //    if cli.list_rags {
    //        let rags = Config::list_rags().join("\n");
    //        println!("{rags}");
    //        return Ok(());
    //    }
    //    if cli.list_macros {
    //        let macros = Config::list_macros().join("\n");
    //        println!("{macros}");
    //        return Ok(());
    //    }
    //
    //    if cli.dry_run {
    //        config.write().dry_run = true;
    //    }
    //
    //    if let Some(agent) = &cli.agent {
    //        let session = cli.session.as_ref().map(|v| match v {
    //            Some(v) => v.as_str(),
    //            None => TEMP_SESSION_NAME,
    //        });
    //        if !cli.agent_variable.is_empty() {
    //            config.write().agent_variables = Some(
    //                cli.agent_variable
    //                    .chunks(2)
    //                    .map(|v| (v[0].to_string(), v[1].to_string()))
    //                    .collect(),
    //            );
    //        }
    //
    //        let ret = Config::use_agent(&config, agent, session, abort_signal.clone()).await;
    //        config.write().agent_variables = None;
    //        ret?;
    //    } else {
    //        if let Some(prompt) = &cli.prompt {
    //            config.write().use_prompt(prompt)?;
    //        } else if let Some(name) = &cli.role {
    //            config.write().use_role(name)?;
    //        } else if cli.execute {
    //            config.write().use_role(SHELL_ROLE)?;
    //        } else if cli.code {
    //            config.write().use_role(CODE_ROLE)?;
    //        }
    //        if let Some(session) = &cli.session {
    //            config
    //                .write()
    //                .use_session(session.as_ref().map(|v| v.as_str()))?;
    //        }
    //        if let Some(rag) = &cli.rag {
    //            Config::use_rag(&config, Some(rag), abort_signal.clone()).await?;
    //        }
    //    }
    //    if cli.list_sessions {
    //        let sessions = config.read().list_sessions().join("\n");
    //        println!("{sessions}");
    //        return Ok(());
    //    }
    //    if let Some(model_id) = &cli.model {
    //        config.write().set_model(model_id)?;
    //    }
    //    if cli.no_stream {
    //        config.write().stream = false;
    //    }
    //    if cli.empty_session {
    //        config.write().empty_session()?;
    //    }
    //    if cli.save_session {
    //        config.write().set_save_session_this_time()?;
    //    }
    //    if cli.info {
    //        let info = config.read().info()?;
    //        println!("{info}");
    //        return Ok(());
    //    }
    //    if let Some(addr) = cli.serve {
    //        return serve::run(config, addr).await;
    //    }
    //    let is_repl = config.read().working_mode.is_repl();
    //    if cli.rebuild_rag {
    //        Config::rebuild_rag(&config, abort_signal.clone()).await?;
    //        if is_repl {
    //            return Ok(());
    //        }
    //    }
    //    if let Some(name) = &cli.macro_name {
    //        macro_execute(&config, name, text.as_deref(), abort_signal.clone()).await?;
    //        return Ok(());
    //    }
    //    if cli.execute && !is_repl {
    //        let input = create_input(&config, text, &cli.file, abort_signal.clone()).await?;
    //        shell_execute(&config, &SHELL, input, abort_signal.clone()).await?;
    //        return Ok(());
    //    }
    //    config.write().apply_prelude()?;
    //    match is_repl {
    //        false => {
    //            let mut input = create_input(&config, text, &cli.file, abort_signal.clone()).await?;
    //            input.use_embeddings(abort_signal.clone()).await?;
    //            start_directive(&config, input, cli.code, abort_signal).await
    //        }
    //        true => {
    //            if !*IS_STDOUT_TERMINAL {
    //                bail!("No TTY for REPL")
    //            }
    //            start_interactive(&config).await
    //        }
    //    }
    // }
    private static void run(Config config, Cli cli, String text) {
        AbortSignal abortSignal = new AbortSignal();

        if (cli.isSyncModels()) {
            // to do
            return;
        }

        if (cli.isListModels()) {
            for (Model model : listModels(config, ModelType.Chat)) {
                println("{}", model.id());
            }
            return;
        }

        if (cli.isListRoles()) {
            String roles = String.join("\n", config.listRoles(true));
            println("{}", roles);
            return;
        }

        if (cli.isListAgents()) {
            String agents = String.join("\n", Agent.listAgents(config));
            println("{}", agents);
            return;
        }

        if (cli.isListRags()) {
            String rags = String.join("\n", config.listRags());
            println("{}", rags);
            return;
        }

        if (cli.isListMacros()) {
            String macros = String.join("\n", config.listMacros());
            println("{}", macros);
            return;
        }

        if (cli.isDryRun()) {
            config.setDryRun(true);
        }

        String agent = cli.getAgent();
        if (!isBlank(agent)) {
            String session = cli.getSession();
            if (isBlank(session)) {
                session = TEMP_SESSION_NAME;
            }
            if (cli.getAgentVariable() != null) {
                config.setAgentVariables(cli.getAgentVariable());
            }
            config.useAgent(agent, session, abortSignal);
            config.setAgentVariables(Collections.emptyMap());
            return;
        } else {
            String prompt = cli.getPrompt();
            if (!isBlank(prompt)) {
                config.usePrompt(prompt);
            } else if (!isBlank(cli.getRole())) {
                config.useRole(cli.getRole());
            } else if (cli.isExecute()) {
                config.useRole(SHELL_ROLE);
            } else if (cli.isCode()) {
                config.useRole(CODE_ROLE);
            }
            if (!isBlank(cli.getSession())) {
                config.useSession(cli.getSession());
            }
            if (!isBlank(cli.getRag())) {
                config.useRag(cli.getRag(), abortSignal);
            }
        }
        if (cli.isListSessions()) {
            String macros = String.join("\n", config.listSessions());
            println("{}", macros);
            return;
        }
        if (!isBlank(cli.getModel())) {
            config.setModel(cli.getModel());
        }
        if (cli.isNoStream()) {
            config.setStream(false);
        }
        if (cli.isEmptySession()) {
            config.emptySession();
        }
        if (cli.isSaveSession()) {
            //config.setSaveSessionThisTime();
        }
        if (cli.isInfo()) {
            String info = config.info();
            println("{}", info);
            return;
        }

        boolean isRepl = WorkingMode.Repl.equals(config.getWorkingMode());
        if (cli.isRebuildRag()) {
            Config.rebuildRag(config, abortSignal);
            if (isRepl) {
                return;
            }
        }
        if (!isBlank(cli.getMacroName())) {
            Config.macroExecute(config, cli.getMacroName(), text, abortSignal);
            return;
        }
        if (cli.isExecute() && !isRepl) {
            Input input = createInput(config, text, cli.getFile(), abortSignal);
            shellExecute(config, SHELL, input, abortSignal);
            return;
        }

        config.applyPrelude();

        if (!isRepl) {
            Input input = createInput(config, text, cli.getFile(), abortSignal);
            input.useEmbeddings(abortSignal);
            startDirective(config, input, cli.isCode(), abortSignal);
        } else {
            if (terminal() == null) {
                bail("No TTY for REPL");
                return;
            }
            startInteractive(config);
        }
    }

    // #[async_recursion::async_recursion]
    // async fn start_directive(
    //    config: &GlobalConfig,
    //    input: Input,
    //    code_mode: bool,
    //    abort_signal: AbortSignal,
    //) -> Result<()> {
    //    let client = input.create_client()?;
    //    let extract_code = !*IS_STDOUT_TERMINAL && code_mode;
    //    config.write().before_chat_completion(&input)?;
    //    let (output, tool_results) = if !input.stream() || extract_code {
    //        call_chat_completions(
    //            &input,
    //            true,
    //            extract_code,
    //            client.as_ref(),
    //            abort_signal.clone(),
    //        )
    //        .await?
    //    } else {
    //        call_chat_completions_streaming(&input, client.as_ref(), abort_signal.clone()).await?
    //    };
    //    config
    //        .write()
    //        .after_chat_completion(&input, &output, &tool_results)?;
    //
    //    if !tool_results.is_empty() {
    //        start_directive(
    //            config,
    //            input.merge_tool_results(output, tool_results),
    //            code_mode,
    //            abort_signal,
    //        )
    //        .await?;
    //    }
    //
    //    config.write().exit_session()?;
    //    Ok(())
    // }
    private static void startDirective(Config config, Input input, boolean codeMode, AbortSignal abortSignal) {
        Client client = input.createClient();
        boolean extractCode = (terminal() == null) && codeMode;
        config.beforeChatCompletion(input);

        Tuple<String, List<ToolResult>> tuple;
        if (!input.stream() || extractCode) {
            tuple = callChatCompletions(input, true, extractCode, client, abortSignal);
        } else {
            tuple = callChatCompletionsStreaming(input, client, abortSignal);
        }
        String output = tuple.first();
        List<ToolResult> toolResults = tuple.second();
        config.afterChatCompletion(input, output, toolResults);

        if (!isEmpty(toolResults)) {
            startDirective(config, input.mergeToolResults(output, toolResults), codeMode, abortSignal);
        }

        config.exitSession();
    }

    // async fn start_interactive(config: &GlobalConfig) -> Result<()> {
    //    let mut repl: Repl = Repl::init(config)?;
    //    repl.run().await
    // }
    private static void startInteractive(Config config) {
        Repl repl = Repl.init(config);
        repl.run();
    }

    // async fn create_input(
    //    config: &GlobalConfig,
    //    text: Option<String>,
    //    file: &[String],
    //    abort_signal: AbortSignal,
    // ) -> Result<Input> {
    //    let input = if file.is_empty() {
    //        Input::from_str(config, &text.unwrap_or_default(), None)
    //    } else {
    //        Input::from_files_with_spinner(
    //            config,
    //            &text.unwrap_or_default(),
    //            file.to_vec(),
    //            None,
    //            abort_signal,
    //        )
    //        .await?
    //    };
    //    if input.is_empty() {
    //        bail!("No input");
    //    }
    //    Ok(input)
    // }
    private static Input createInput(Config config, String text, List<String> file, AbortSignal abortSignal) {
        return null;
    }

    // #[async_recursion::async_recursion]
    // async fn shell_execute(
    //    config: &GlobalConfig,
    //    shell: &Shell,
    //    mut input: Input,
    //    abort_signal: AbortSignal,
    // ) -> Result<()> {
    //    let client = input.create_client()?;
    //    config.write().before_chat_completion(&input)?;
    //    let (eval_str, _) =
    //        call_chat_completions(&input, false, true, client.as_ref(), abort_signal.clone()).await?;
    //
    //    config
    //        .write()
    //        .after_chat_completion(&input, &eval_str, &[])?;
    //    if eval_str.is_empty() {
    //        bail!("No command generated");
    //    }
    //    if config.read().dry_run {
    //        config.read().print_markdown(&eval_str)?;
    //        return Ok(());
    //    }
    //    if *IS_STDOUT_TERMINAL {
    //        let options = ["execute", "revise", "describe", "copy", "quit"];
    //        let command = color_text(eval_str.trim(), nu_ansi_term::Color::Rgb(255, 165, 0));
    //        let first_letter_color = nu_ansi_term::Color::Cyan;
    //        let prompt_text = options
    //            .iter()
    //            .map(|v| format!("{}{}", color_text(&v[0..1], first_letter_color), &v[1..]))
    //            .collect::<Vec<String>>()
    //            .join(&dimmed_text(" | "));
    //        loop {
    //            println!("{command}");
    //            let answer_char =
    //                read_single_key(&['e', 'r', 'd', 'c', 'q'], 'e', &format!("{prompt_text}: "))?;
    //
    //            match answer_char {
    //                'e' => {
    //                    debug!("{} {:?}", shell.cmd, &[&shell.arg, &eval_str]);
    //                    let code = run_command(&shell.cmd, &[&shell.arg, &eval_str], None)?;
    //                    if code == 0 && config.read().save_shell_history {
    //                        let _ = append_to_shell_history(&shell.name, &eval_str, code);
    //                    }
    //                    process::exit(code);
    //                }
    //                'r' => {
    //                    let revision = Text::new("Enter your revision:").prompt()?;
    //                    let text = format!("{}\n{revision}", input.text());
    //                    input.set_text(text);
    //                    return shell_execute(config, shell, input, abort_signal.clone()).await;
    //                }
    //                'd' => {
    //                    let role = config.read().retrieve_role(EXPLAIN_SHELL_ROLE)?;
    //                    let input = Input::from_str(config, &eval_str, Some(role));
    //                    if input.stream() {
    //                        call_chat_completions_streaming(
    //                            &input,
    //                            client.as_ref(),
    //                            abort_signal.clone(),
    //                        )
    //                        .await?;
    //                    } else {
    //                        call_chat_completions(
    //                            &input,
    //                            true,
    //                            false,
    //                            client.as_ref(),
    //                            abort_signal.clone(),
    //                        )
    //                        .await?;
    //                    }
    //                    println!();
    //                    continue;
    //                }
    //                'c' => {
    //                    set_text(&eval_str)?;
    //                    println!("{}", dimmed_text("✓ Copied the command."));
    //                }
    //                _ => {}
    //            }
    //            break;
    //        }
    //    } else {
    //        println!("{eval_str}");
    //    }
    //    Ok(())
    // }
    private static void shellExecute(Config config, Shell shell, Input input, AbortSignal abortSignal) {

    }
}
