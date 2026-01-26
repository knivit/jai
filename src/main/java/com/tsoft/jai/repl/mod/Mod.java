package com.tsoft.jai.repl.mod;

import com.tsoft.jai.anyhow.Result;
import com.tsoft.jai.client.Client;
import com.tsoft.jai.config.*;
import com.tsoft.jai.config.agent.Agent;
import com.tsoft.jai.core.macros.BuiltIn;
import com.tsoft.jai.function.ToolResult;
import com.tsoft.jai.tokio.Time;
import com.tsoft.jai.utils.AbortSignal;
import com.tsoft.jai.utils.base.Tuple;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.tsoft.jai.anyhow.Macros.bail;
import static com.tsoft.jai.anyhow.Result.*;
import static com.tsoft.jai.client.common.Common.callChatCompletions;
import static com.tsoft.jai.client.common.Common.callChatCompletionsStreaming;
import static com.tsoft.jai.config.StateFlags.AGENT;
import static com.tsoft.jai.core.macros.BuiltIn.cfg;
import static com.tsoft.jai.inquire.Inquire.print;
import static com.tsoft.jai.inquire.Inquire.println;
import static com.tsoft.jai.utils.Mod.dimmedText;
import static com.tsoft.jai.utils.base.CollectionsUtils.isEmpty;
import static com.tsoft.jai.utils.base.StringUtils.*;

public class Mod {

    // static REPL_COMMANDS: LazyLock<[ReplCommand; 36]> = LazyLock::new(|| {
    //    [
    //        ReplCommand::new(".help", "Show this help guide", AssertState::pass()),
    //        ReplCommand::new(".info", "Show system info", AssertState::pass()),
    //        ReplCommand::new(
    //            ".edit config",
    //            "Modify configuration file",
    //            AssertState::False(StateFlags::AGENT),
    //        ),
    //        ReplCommand::new(".model", "Switch LLM model", AssertState::pass()),
    //        ReplCommand::new(
    //            ".prompt",
    //            "Set a temporary role using a prompt",
    //            AssertState::False(StateFlags::SESSION | StateFlags::AGENT),
    //        ),
    //        ReplCommand::new(
    //            ".role",
    //            "Create or switch to a role",
    //            AssertState::False(StateFlags::SESSION | StateFlags::AGENT),
    //        ),
    //        ReplCommand::new(
    //            ".info role",
    //            "Show role info",
    //            AssertState::True(StateFlags::ROLE),
    //        ),
    //        ReplCommand::new(
    //            ".edit role",
    //            "Modify current role",
    //            AssertState::TrueFalse(StateFlags::ROLE, StateFlags::SESSION),
    //        ),
    //        ReplCommand::new(
    //            ".save role",
    //            "Save current role to file",
    //            AssertState::TrueFalse(
    //                StateFlags::ROLE,
    //                StateFlags::SESSION_EMPTY | StateFlags::SESSION,
    //            ),
    //        ),
    //        ReplCommand::new(
    //            ".exit role",
    //            "Exit active role",
    //            AssertState::TrueFalse(StateFlags::ROLE, StateFlags::SESSION),
    //        ),
    //        ReplCommand::new(
    //            ".session",
    //            "Start or switch to a session",
    //            AssertState::False(StateFlags::SESSION_EMPTY | StateFlags::SESSION),
    //        ),
    //        ReplCommand::new(
    //            ".empty session",
    //            "Clear session messages",
    //            AssertState::True(StateFlags::SESSION),
    //        ),
    //        ReplCommand::new(
    //            ".compress session",
    //            "Compress session messages",
    //            AssertState::True(StateFlags::SESSION),
    //        ),
    //        ReplCommand::new(
    //            ".info session",
    //            "Show session info",
    //            AssertState::True(StateFlags::SESSION_EMPTY | StateFlags::SESSION),
    //        ),
    //        ReplCommand::new(
    //            ".edit session",
    //            "Modify current session",
    //            AssertState::True(StateFlags::SESSION_EMPTY | StateFlags::SESSION),
    //        ),
    //        ReplCommand::new(
    //            ".save session",
    //            "Save current session to file",
    //            AssertState::True(StateFlags::SESSION_EMPTY | StateFlags::SESSION),
    //        ),
    //        ReplCommand::new(
    //            ".exit session",
    //            "Exit active session",
    //            AssertState::True(StateFlags::SESSION_EMPTY | StateFlags::SESSION),
    //        ),
    //        ReplCommand::new(".agent", "Use an agent", AssertState::bare()),
    //        ReplCommand::new(
    //            ".starter",
    //            "Use a conversation starter",
    //            AssertState::True(StateFlags::AGENT),
    //        ),
    //        ReplCommand::new(
    //            ".edit agent-config",
    //            "Modify agent configuration file",
    //            AssertState::True(StateFlags::AGENT),
    //        ),
    //        ReplCommand::new(
    //            ".info agent",
    //            "Show agent info",
    //            AssertState::True(StateFlags::AGENT),
    //        ),
    //        ReplCommand::new(
    //            ".exit agent",
    //            "Leave agent",
    //            AssertState::True(StateFlags::AGENT),
    //        ),
    //        ReplCommand::new(
    //            ".rag",
    //            "Initialize or access RAG",
    //            AssertState::False(StateFlags::AGENT),
    //        ),
    //        ReplCommand::new(
    //            ".edit rag-docs",
    //            "Add or remove documents from an existing RAG",
    //            AssertState::TrueFalse(StateFlags::RAG, StateFlags::AGENT),
    //        ),
    //        ReplCommand::new(
    //            ".rebuild rag",
    //            "Rebuild RAG for document changes",
    //            AssertState::True(StateFlags::RAG),
    //        ),
    //        ReplCommand::new(
    //            ".sources rag",
    //            "Show citation sources used in last query",
    //            AssertState::True(StateFlags::RAG),
    //        ),
    //        ReplCommand::new(
    //            ".info rag",
    //            "Show RAG info",
    //            AssertState::True(StateFlags::RAG),
    //        ),
    //        ReplCommand::new(
    //            ".exit rag",
    //            "Leave RAG",
    //            AssertState::TrueFalse(StateFlags::RAG, StateFlags::AGENT),
    //        ),
    //        ReplCommand::new(".macro", "Execute a macro", AssertState::pass()),
    //        ReplCommand::new(
    //            ".file",
    //            "Include files, directories, URLs or commands",
    //            AssertState::pass(),
    //        ),
    //        ReplCommand::new(
    //            ".continue",
    //            "Continue previous response",
    //            AssertState::pass(),
    //        ),
    //        ReplCommand::new(
    //            ".regenerate",
    //            "Regenerate last response",
    //            AssertState::pass(),
    //        ),
    //        ReplCommand::new(".copy", "Copy last response", AssertState::pass()),
    //        ReplCommand::new(".set", "Modify runtime settings", AssertState::pass()),
    //        ReplCommand::new(
    //            ".delete",
    //            "Delete roles, sessions, RAGs, or agents",
    //            AssertState::pass(),
    //        ),
    //        ReplCommand::new(".exit", "Exit REPL", AssertState::pass()),
    //    ]
    // });
    private static final List<ReplCommand> REPL_COMMANDS = Arrays.asList(
        new ReplCommand(".help", "Show this help guide", AssertState.pass()),
        new ReplCommand(".info", "Show system info", AssertState.pass()),
        new ReplCommand(".edit config", "Modify configuration file", AssertState.False(AGENT)),
        new ReplCommand(".model", "Switch LLM model", AssertState.pass()),
        new ReplCommand(".prompt", "Set a temporary role using a prompt", AssertState.False(StateFlags.SESSION | AGENT)),
        new ReplCommand(".role", "Create or switch to a role", AssertState.False(StateFlags.SESSION | StateFlags.AGENT)),
        new ReplCommand(".info role", "Show role info", AssertState.True(StateFlags.ROLE)),
        new ReplCommand(".edit role", "Modify current role", AssertState.TrueFalse(StateFlags.ROLE, StateFlags.SESSION)),
        new ReplCommand(".save role", "Save current role to file", AssertState.TrueFalse(StateFlags.ROLE, StateFlags.SESSION_EMPTY | StateFlags.SESSION)),
        new ReplCommand(".exit role", "Exit active role", AssertState.TrueFalse(StateFlags.ROLE, StateFlags.SESSION)),
        new ReplCommand(".session", "Start or switch to a session", AssertState.False(StateFlags.SESSION_EMPTY | StateFlags.SESSION)),
        new ReplCommand(".empty session", "Clear session messages", AssertState.True(StateFlags.SESSION)),
        new ReplCommand(".compress session", "Compress session messages", AssertState.True(StateFlags.SESSION)),
        new ReplCommand(".info session", "Show session info", AssertState.True(StateFlags.SESSION_EMPTY | StateFlags.SESSION)),
        new ReplCommand(".edit session", "Modify current session", AssertState.True(StateFlags.SESSION_EMPTY | StateFlags.SESSION)),
        new ReplCommand(".save session", "Save current session to file", AssertState.True(StateFlags.SESSION_EMPTY | StateFlags.SESSION)),
        new ReplCommand(".exit session", "Exit active session", AssertState.True(StateFlags.SESSION_EMPTY | StateFlags.SESSION)),
        new ReplCommand(".agent", "Use an agent", AssertState.bare()),
        new ReplCommand(".starter", "Use a conversation starter", AssertState.True(StateFlags.AGENT)),
        new ReplCommand(".edit agent-config", "Modify agent configuration file", AssertState.True(StateFlags.AGENT)),
        new ReplCommand(".info agent", "Show agent info", AssertState.True(StateFlags.AGENT)),
        new ReplCommand(".exit agent", "Leave agent", AssertState.True(StateFlags.AGENT)),
        new ReplCommand(".rag", "Initialize or access RAG", AssertState.False(StateFlags.AGENT)),
        new ReplCommand(".edit rag-docs", "Add or remove documents from an existing RAG", AssertState.TrueFalse(StateFlags.RAG, StateFlags.AGENT)),
        new ReplCommand(".rebuild rag", "Rebuild RAG for document changes", AssertState.True(StateFlags.RAG)),
        new ReplCommand(".sources rag", "Show citation sources used in last query", AssertState.True(StateFlags.RAG)),
        new ReplCommand(".info rag", "Show RAG info", AssertState.True(StateFlags.RAG)),
        new ReplCommand(".exit rag", "Leave RAG", AssertState.TrueFalse(StateFlags.RAG, StateFlags.AGENT)),
        new ReplCommand(".macro", "Execute a macro", AssertState.pass()),
        new ReplCommand(".file", "Include files, directories, URLs or commands", AssertState.pass()),
        new ReplCommand(".continue", "Continue previous response", AssertState.pass()),
        new ReplCommand(".regenerate", "Regenerate last response", AssertState.pass()),
        new ReplCommand(".copy", "Copy last response", AssertState.pass()),
        new ReplCommand(".set", "Modify runtime settings", AssertState.pass()),
        new ReplCommand(".delete", "Delete roles, sessions, RAGs, or agents", AssertState.pass()),
        new ReplCommand(".exit", "Exit REPL", AssertState.pass())
    );

    private static final Pattern COMMAND_RE = Pattern.compile("^\\s*(\\.\\S*)\\s*");
    private static final Pattern MULTILINE_RE = Pattern.compile("(?s)^\\s*:::\\s*(.*)\\s*:::\\s*$");

    // pub async fn run_repl_command(
    //    config: &GlobalConfig,
    //    abort_signal: AbortSignal,
    //    mut line: &str,
    //) -> Result<bool> {
    //    if let Ok(Some(captures)) = MULTILINE_RE.captures(line) {
    //        if let Some(text_match) = captures.get(1) {
    //            line = text_match.as_str();
    //        }
    //    }
    //    match parse_command(line) {
    //        Some((cmd, args)) => match cmd {
    //            ".help" => {
    //                dump_repl_help();
    //            }
    //            ".info" => match args {
    //                Some("role") => {
    //                    let info = config.read().role_info()?;
    //                    print!("{info}");
    //                }
    //                Some("session") => {
    //                    let info = config.read().session_info()?;
    //                    print!("{info}");
    //                }
    //                Some("rag") => {
    //                    let info = config.read().rag_info()?;
    //                    print!("{info}");
    //                }
    //                Some("agent") => {
    //                    let info = config.read().agent_info()?;
    //                    print!("{info}");
    //                }
    //                Some(_) => unknown_command()?,
    //                None => {
    //                    let output = config.read().sysinfo()?;
    //                    print!("{output}");
    //                }
    //            },
    //            ".model" => match args {
    //                Some(name) => {
    //                    config.write().set_model(name)?;
    //                }
    //                None => println!("Usage: .model <name>"),
    //            },
    //            ".prompt" => match args {
    //                Some(text) => {
    //                    config.write().use_prompt(text)?;
    //                }
    //                None => println!("Usage: .prompt <text>..."),
    //            },
    //            ".role" => match args {
    //                Some(args) => match args.split_once(['\n', ' ']) {
    //                    Some((name, text)) => {
    //                        let role = config.read().retrieve_role(name.trim())?;
    //                        let input = Input::from_str(config, text, Some(role));
    //                        ask(config, abort_signal.clone(), input, false).await?;
    //                    }
    //                    None => {
    //                        let name = args;
    //                        if !Config::has_role(name) {
    //                            config.write().new_role(name)?;
    //                        }
    //                        config.write().use_role(name)?;
    //                    }
    //                },
    //                None => println!(
    //                    r#"Usage:
    //    .role <name>                    # If the role exists, switch to it; otherwise, create a new role
    //    .role <name> [text]...          # Temporarily switch to the role, send the text, and switch back"#
    //                ),
    //            },
    //            ".session" => {
    //                config.write().use_session(args)?;
    //                Config::maybe_autoname_session(config.clone());
    //            }
    //            ".rag" => {
    //                Config::use_rag(config, args, abort_signal.clone()).await?;
    //            }
    //            ".agent" => match split_first_arg(args) {
    //                Some((agent_name, args)) => {
    //                    let (new_args, _) = split_args_text(args.unwrap_or_default(), cfg!(windows));
    //                    let (session_name, variable_pairs) = match new_args.first() {
    //                        Some(name) if name.contains('=') => (None, new_args.as_slice()),
    //                        Some(name) => (Some(name.as_str()), &new_args[1..]),
    //                        None => (None, &[] as &[String]),
    //                    };
    //                    let variables: AgentVariables = variable_pairs
    //                        .iter()
    //                        .filter_map(|v| v.split_once('='))
    //                        .map(|(key, value)| (key.to_string(), value.to_string()))
    //                        .collect();
    //                    if variables.len() != variable_pairs.len() {
    //                        bail!("Some variable values are not key=value pairs");
    //                    }
    //                    if !variables.is_empty() {
    //                        config.write().agent_variables = Some(variables);
    //                    }
    //                    let ret =
    //                        Config::use_agent(config, agent_name, session_name, abort_signal.clone())
    //                            .await;
    //                    config.write().agent_variables = None;
    //                    ret?;
    //                }
    //                None => {
    //                    println!(r#"Usage: .agent <agent-name> [session-name] [key=value]..."#)
    //                }
    //            },
    //            ".starter" => match args {
    //                Some(id) => {
    //                    let mut text = None;
    //                    if let Some(agent) = config.read().agent.as_ref() {
    //                        for (i, value) in agent.conversation_staters().iter().enumerate() {
    //                            if (i + 1).to_string() == id {
    //                                text = Some(value.clone());
    //                            }
    //                        }
    //                    }
    //                    match text {
    //                        Some(text) => {
    //                            println!("{}", dimmed_text(&format!(">> {text}")));
    //                            let input = Input::from_str(config, &text, None);
    //                            ask(config, abort_signal.clone(), input, true).await?;
    //                        }
    //                        None => {
    //                            bail!("Invalid starter value");
    //                        }
    //                    }
    //                }
    //                None => {
    //                    let banner = config.read().agent_banner()?;
    //                    config.read().print_markdown(&banner)?;
    //                }
    //            },
    //            ".save" => match split_first_arg(args) {
    //                Some(("role", name)) => {
    //                    config.write().save_role(name)?;
    //                }
    //                Some(("session", name)) => {
    //                    config.write().save_session(name)?;
    //                }
    //                _ => {
    //                    println!(r#"Usage: .save <role|session> [name]"#)
    //                }
    //            },
    //            ".edit" => {
    //                if config.read().macro_flag {
    //                    bail!("Cannot perform this operation because you are in a macro")
    //                }
    //                match args {
    //                    Some("config") => {
    //                        config.read().edit_config()?;
    //                    }
    //                    Some("role") => {
    //                        config.write().edit_role()?;
    //                    }
    //                    Some("session") => {
    //                        config.write().edit_session()?;
    //                    }
    //                    Some("rag-docs") => {
    //                        Config::edit_rag_docs(config, abort_signal.clone()).await?;
    //                    }
    //                    Some("agent-config") => {
    //                        config.write().edit_agent_config()?;
    //                    }
    //                    _ => {
    //                        println!(r#"Usage: .edit <config|role|session|rag-docs|agent-config>"#)
    //                    }
    //                }
    //            }
    //            ".compress" => match args {
    //                Some("session") => {
    //                    abortable_run_with_spinner(
    //                        Config::compress_session(config),
    //                        "Compressing",
    //                        abort_signal.clone(),
    //                    )
    //                    .await?;
    //                    println!("✓ Successfully compressed the session.");
    //                }
    //                _ => {
    //                    println!(r#"Usage: .compress session"#)
    //                }
    //            },
    //            ".empty" => match args {
    //                Some("session") => {
    //                    config.write().empty_session()?;
    //                }
    //                _ => {
    //                    println!(r#"Usage: .empty session"#)
    //                }
    //            },
    //            ".rebuild" => match args {
    //                Some("rag") => {
    //                    Config::rebuild_rag(config, abort_signal.clone()).await?;
    //                }
    //                _ => {
    //                    println!(r#"Usage: .rebuild rag"#)
    //                }
    //            },
    //            ".sources" => match args {
    //                Some("rag") => {
    //                    let output = Config::rag_sources(config)?;
    //                    println!("{output}");
    //                }
    //                _ => {
    //                    println!(r#"Usage: .sources rag"#)
    //                }
    //            },
    //            ".macro" => match split_first_arg(args) {
    //                Some((name, extra)) => {
    //                    if !Config::has_macro(name) && extra.is_none() {
    //                        config.write().new_macro(name)?;
    //                    } else {
    //                        macro_execute(config, name, extra, abort_signal.clone()).await?;
    //                    }
    //                }
    //                None => println!("Usage: .macro <name> <text>..."),
    //            },
    //            ".file" => match args {
    //                Some(args) => {
    //                    let (files, text) = split_args_text(args, cfg!(windows));
    //                    let input = Input::from_files_with_spinner(
    //                        config,
    //                        text,
    //                        files,
    //                        None,
    //                        abort_signal.clone(),
    //                    )
    //                    .await?;
    //                    ask(config, abort_signal.clone(), input, true).await?;
    //                }
    //                None => println!(
    //                    r#"Usage: .file <file|dir|url|cmd|loader:resource|%%>... [-- <text>...]
    //
    //.file /tmp/file.txt
    //.file src/ Cargo.toml -- analyze
    //.file https://example.com/file.txt -- summarize
    //.file https://example.com/image.png -- recognize text
    //.file `git diff` -- Generate git commit message
    //.file jina:https://example.com
    //.file %% -- translate last reply to english"#
    //                ),
    //            },
    //            ".continue" => {
    //                let LastMessage {
    //                    mut input, output, ..
    //                } = match config
    //                    .read()
    //                    .last_message
    //                    .as_ref()
    //                    .filter(|v| v.continuous && !v.output.is_empty())
    //                    .cloned()
    //                {
    //                    Some(v) => v,
    //                    None => bail!("Unable to continue the response"),
    //                };
    //                input.set_continue_output(&output);
    //                ask(config, abort_signal.clone(), input, true).await?;
    //            }
    //            ".regenerate" => {
    //                let LastMessage { mut input, .. } = match config
    //                    .read()
    //                    .last_message
    //                    .as_ref()
    //                    .filter(|v| v.continuous)
    //                    .cloned()
    //                {
    //                    Some(v) => v,
    //                    None => bail!("Unable to regenerate the response"),
    //                };
    //                input.set_regenerate();
    //                ask(config, abort_signal.clone(), input, true).await?;
    //            }
    //            ".set" => match args {
    //                Some(args) => {
    //                    Config::update(config, args)?;
    //                }
    //                _ => {
    //                    println!("Usage: .set <key> <value>...")
    //                }
    //            },
    //            ".delete" => match args {
    //                Some(args) => {
    //                    Config::delete(config, args)?;
    //                }
    //                _ => {
    //                    println!("Usage: .delete <role|session|rag|macro|agent-data>")
    //                }
    //            },
    //            ".copy" => {
    //                let output = match config
    //                    .read()
    //                    .last_message
    //                    .as_ref()
    //                    .filter(|v| !v.output.is_empty())
    //                    .map(|v| v.output.clone())
    //                {
    //                    Some(v) => v,
    //                    None => bail!("No chat response to copy"),
    //                };
    //                set_text(&output).context("Failed to copy the last chat response")?;
    //            }
    //            ".exit" => match args {
    //                Some("role") => {
    //                    config.write().exit_role()?;
    //                }
    //                Some("session") => {
    //                    if config.read().agent.is_some() {
    //                        config.write().exit_agent_session()?;
    //                    } else {
    //                        config.write().exit_session()?;
    //                    }
    //                }
    //                Some("rag") => {
    //                    config.write().exit_rag()?;
    //                }
    //                Some("agent") => {
    //                    config.write().exit_agent()?;
    //                }
    //                Some(_) => unknown_command()?,
    //                None => {
    //                    return Ok(true);
    //                }
    //            },
    //            ".clear" => match args {
    //                Some("messages") => {
    //                    bail!("Use '.empty session' instead");
    //                }
    //                _ => unknown_command()?,
    //            },
    //            _ => unknown_command()?,
    //        },
    //        None => {
    //            let input = Input::from_str(config, line, None);
    //            ask(config, abort_signal.clone(), input, true).await?;
    //        }
    //    }
    //
    //    if !config.read().macro_flag {
    //        println!();
    //    }
    //
    //    Ok(false)
    // }
    public static Result<Boolean> runReplCommand(Config config, AbortSignal abortSignal, String line) {
        Matcher matcher = MULTILINE_RE.matcher(line);
        if (matcher.find()) {
            line = line.substring(matcher.end()).trim();
        }

        Tuple<String, String> tuple = parseCommand(line);
        if (tuple != null) {
            String cmd = tuple.first();
            String args = tuple.second();
            if (".help".equals(cmd)) {
                dumpReplHelp();
            } else if (".info".equals(cmd)) {
                if ("role".equals(args)) {
                    String info = config.roleInfo();
                    print("{}", info);
                } else if ("session".equals(args)) {
                    String info = config.sessionInfo();
                    print("{}", info);
                } else if ("rag".equals(args)) {
                    String info = config.ragInfo();
                    print("{}", info);
                } else if ("agent".equals(args)) {
                    String info = config.agentInfo();
                    print("{}", info);
                } else if (!isBlank(args)) {
                    unknownCommand();
                } else {
                    String output = config.sysinfo();
                    print("{}", output);
                }
            } else if (".model".equals(cmd)) {
                if (!isBlank(args)) {
                    String name = args;
                    config.setModel(name);
                } else {
                    println("Usage: .model <name>");
                }
            } else if (".prompt".equals(cmd)) {
                if (!isBlank(args)) {
                    String text = args;
                    config.usePrompt(text);
                } else {
                    println("Usage: .prompt <text>...");
                }
            } else if (".role".equals(cmd)) {
                if (!isBlank(args)) {
                    tuple = splitOnce(args, '\n', ' ');
                    String name = tuple.first();
                    String text = tuple.second();
                    if (!isBlank(name) && !isBlank(text)) {
                        Result<Role> res = config.retrieveRole(name.trim());
                        if (isErr(res)) {
                            return Err();
                        }
                        Role role = res.getValue();
                        Input input = Input.fromStr(config, text, role);
                        ask(config, abortSignal, input, false);
                    } else {
                        name = args;
                        if (!config.hasRole(name)) {
                            config.newRole(name);
                        }
                        config.useRole(name);
                    }
                } else {
                    println("""
                        Usage:
                        .role <name>                    # If the role exists, switch to it; otherwise, create a new role
                        .role <name> [text]...          # Temporarily switch to the role, send the text, and switch back
                        """);
                }
            } else if (".session".equals(cmd)) {
                config.useSession(args);
                Config.maybeAutonameSession(config);
            } else if (".rag".equals(cmd)) {
                config.useRag(args, abortSignal);
            } else if (".agent".equals(cmd)) {
                tuple = splitFirstArg(args);
                String agentName = tuple.first();
                args = tuple.second();
                if (!isBlank(agentName) && !isBlank(args)) {
                    Tuple<List<String>, String> tuple2 = splitArgsText(args, cfg(BuiltIn.Platform.WINDOWS));
                    List<String> newArgs = tuple2.first();
                    String sessionName = null;
                    List<String> variablePairs = Collections.emptyList();
                    String name = isEmpty(newArgs) ? null : newArgs.getFirst();
                    if (name != null && name.contains("=")) {
                        variablePairs = newArgs;
                    } else if (name != null) {
                        sessionName = name;
                        variablePairs = newArgs.subList(1, newArgs.size());
                    }
                    Map<String, String> variables = new HashMap<>();
                    for (String pair : variablePairs) {
                        tuple = splitOnce(pair, '=');
                        if (!isBlank(tuple.second())) {
                            variables.put(tuple.first(), tuple.second());
                        }
                    }
                    if (variables.size() != variablePairs.size()) {
                        return bail("Some variable values are not key=value pairs");
                    }
                    if (!isEmpty(variables)) {
                        config.setAgentVariables(variables);
                    }
                    config.useAgent(agentName, sessionName, abortSignal);
                    config.setAgentVariables(null);
                    return Ok(true);
                }
            } else if (".starter".equals(cmd)) {
                String id = args;
                if (!isBlank(id)) {
                    String text = null;
                    Agent agent = config.getAgent();
                    if (agent != null) {
                        List<String> conversationStarters = agent.conversationStarters();
                        if (!isEmpty(conversationStarters)) {
                            for (int i = 0; i < conversationStarters.size(); i++) {
                                if (Objects.equals(Integer.toString(i + 1), id)) {
                                    text = conversationStarters.get(i);
                                }
                            }
                        }
                    }
                    if (!isBlank(text)) {
                        println("{}", dimmedText(format(">> {}", text)));
                        Input input = Input.fromStr(config, text, null);
                        ask(config, abortSignal, input, true);
                    } else {
                        return bail("Invalid starter value");
                    }
                } else {
                    Result<String> banner = config.agentBanner();
                    if (isErr(banner)) {
                        return Err(banner);
                    }
                    config.printMarkdown(banner.getValue());
                }
            } else if (".save".equals(cmd)) {
                tuple = splitFirstArg(args);
                String name = tuple.second();
                if ("role".equals(tuple.first()) && !isBlank(name)) {
                    config.saveRole(name);
                } else if ("session".equals(tuple.first()) && !isBlank(name)) {
                    config.saveSession(name);
                } else {
                    println("Usage: .save <role|session> [name]");
                }
            }
        } else {
            Input input = Input.fromStr(config, line, null);
            Result<?> res = ask(config, abortSignal, input, true);
            if (isErr(res)) {
                return Err(res);
            }
        }

        if (!config.isMacroFlag()) {
            println();
        }

        return Ok(false);
    }

    // #[async_recursion::async_recursion]
    // async fn ask(
    //    config: &GlobalConfig,
    //    abort_signal: AbortSignal,
    //    mut input: Input,
    //    with_embeddings: bool,
    // ) -> Result<()> {
    //    if input.is_empty() {
    //        return Ok(());
    //    }
    //    if with_embeddings {
    //        input.use_embeddings(abort_signal.clone()).await?;
    //    }
    //    while config.read().is_compressing_session() {
    //        tokio::time::sleep(std::time::Duration::from_millis(100)).await;
    //    }
    //
    //    let client = input.create_client()?;
    //    config.write().before_chat_completion(&input)?;
    //    let (output, tool_results) = if input.stream() {
    //        call_chat_completions_streaming(&input, client.as_ref(), abort_signal.clone()).await?
    //    } else {
    //        call_chat_completions(&input, true, false, client.as_ref(), abort_signal.clone()).await?
    //    };
    //    config
    //        .write()
    //        .after_chat_completion(&input, &output, &tool_results)?;
    //    if !tool_results.is_empty() {
    //        ask(
    //            config,
    //            abort_signal,
    //            input.merge_tool_results(output, tool_results),
    //            false,
    //        )
    //        .await
    //    } else {
    //        Config::maybe_autoname_session(config.clone());
    //        Config::maybe_compress_session(config.clone());
    //        Ok(())
    //    }
    // }
    public static Result<?> ask(Config config, AbortSignal abortSignal, Input input, boolean withEmbeddings) {
        if (input == null || input.isEmpty()) {
            return Ok();
        }
        if (withEmbeddings) {
            input.useEmbeddings(abortSignal);
        }
        while (config.isCompressingSession()) {
            Time.sleep(100);
        }

        Client client = input.createClient();
        config.beforeChatCompletion(input);
        Result<Tuple<String, List<ToolResult>>> res;
        if (input.stream()) {
            res = callChatCompletionsStreaming(input, client, abortSignal);
        } else {
            res = callChatCompletions(input, true, false, client, abortSignal);
        }
        if (isErr(res)) {
            return Err(res);
        }
        Tuple<String, List<ToolResult>> tuple = res.getValue();

        String output = tuple.first();
        List<ToolResult> toolResults = tuple.second();
        config.afterChatCompletion(input, output, toolResults);

        if (!isEmpty(toolResults)) {
            return ask(config, abortSignal, input.mergeToolResults(output, toolResults), false);
        } else {
            Config.maybeAutonameSession(config);
            Config.maybeCompressSession(config);
            return Ok();
        }
    }

    // fn parse_command(line: &str) -> Option<(&str, Option<&str>)> {
    //    match COMMAND_RE.captures(line) {
    //        Ok(Some(captures)) => {
    //            let cmd = captures.get(1)?.as_str();
    //            let args = line[captures[0].len()..].trim();
    //            let args = if args.is_empty() { None } else { Some(args) };
    //            Some((cmd, args))
    //        }
    //        _ => None,
    //    }
    // }
    public static Tuple<String, String> parseCommand(String line) {
        Matcher matcher = COMMAND_RE.matcher(line);
        if (matcher.find()) {
            String cmd = line.substring(matcher.start(), matcher.end()).trim();
            String args = line.substring(matcher.end()).trim();
            if (isBlank(args)) {
                args = null;
            }
            return new Tuple<>(cmd, args);
        }
        return null;
    }

    // fn unknown_command() -> Result<()> {
    //    bail!(r#"Unknown command. Type ".help" for additional help."#);
    // }
    private static void unknownCommand() {
        bail("Unknown command. Type \".help\" for additional help.");
    }

    // fn dump_repl_help() {
    //    let head = REPL_COMMANDS
    //        .iter()
    //        .map(|cmd| format!("{:<24} {}", cmd.name, cmd.description))
    //        .collect::<Vec<String>>()
    //        .join("\n");
    //    println!(
    //        r###"{head}
    //
    //Type ::: to start multi-line editing, type ::: to finish it.
    //Press Ctrl+O to open an editor for editing the input buffer.
    //Press Ctrl+C to cancel the response, Ctrl+D to exit the REPL."###,
    //    );
    // }
    private static void dumpReplHelp() {
        String head = REPL_COMMANDS.stream()
            .map(cmd -> format("{} {}", padRight(cmd.getName(), 24), cmd.getDescription()))
            .collect(Collectors.joining("\n"));

        println("""
            {}
            
            Type ::: to start multi-line editing, type ::: to finish it.
            Press Ctrl+O to open an editor for editing the input buffer.
            Press Ctrl+C to cancel the response, Ctrl+D to exit the REPL.
            """, head);
    }

    // fn split_first_arg(args: Option<&str>) -> Option<(&str, Option<&str>)> {
    //    args.map(|v| match v.split_once(' ') {
    //        Some((subcmd, args)) => (subcmd, Some(args.trim())),
    //        None => (v, None),
    //    })
    // }
    private static Tuple<String, String> splitFirstArg(String args) {
        Tuple<String, String> tuple = splitOnce(args, ' ');
        String subcmd = tuple.first();
        String subargs = tuple.second();
        if (!isBlank(subcmd) && !isBlank(subargs)) {
            return new Tuple<>(subcmd, subargs);
        }
        return new Tuple<>(args, null);
    }

    // pub fn split_args_text(line: &str, is_win: bool) -> (Vec<String>, &str) {
    //    let mut words = Vec::new();
    //    let mut word = String::new();
    //    let mut unbalance: Option<char> = None;
    //    let mut prev_char: Option<char> = None;
    //    let mut text_starts_at = None;
    //    let unquote_word = |word: &str| {
    //        if ((word.starts_with('"') && word.ends_with('"'))
    //            || (word.starts_with('\'') && word.ends_with('\'')))
    //            && word.len() >= 2
    //        {
    //            word[1..word.len() - 1].to_string()
    //        } else {
    //            word.to_string()
    //        }
    //    };
    //    let chars: Vec<char> = line.chars().collect();
    //
    //    for (i, char) in chars.iter().cloned().enumerate() {
    //        match unbalance {
    //            Some(ub_char) if ub_char == char => {
    //                word.push(char);
    //                unbalance = None;
    //            }
    //            Some(_) => {
    //                word.push(char);
    //            }
    //            None => match char {
    //                ' ' | '\t' | '\r' | '\n' => {
    //                    if char == '\r' && chars.get(i + 1) == Some(&'\n') {
    //                        continue;
    //                    }
    //                    if let Some('\\') = prev_char.filter(|_| !is_win) {
    //                        word.push(char);
    //                    } else if !word.is_empty() {
    //                        if word == "--" {
    //                            word.clear();
    //                            text_starts_at = Some(i + 1);
    //                            break;
    //                        }
    //                        words.push(unquote_word(&word));
    //                        word.clear();
    //                    }
    //                }
    //                '\'' | '"' | '`' => {
    //                    word.push(char);
    //                    unbalance = Some(char);
    //                }
    //                '\\' => {
    //                    if is_win || prev_char.map(|c| c == '\\').unwrap_or_default() {
    //                        word.push(char);
    //                    }
    //                }
    //                _ => {
    //                    word.push(char);
    //                }
    //            },
    //        }
    //        prev_char = Some(char);
    //    }
    //
    //    if !word.is_empty() && word != "--" {
    //        words.push(unquote_word(&word));
    //    }
    //    let text = match text_starts_at {
    //        Some(start) => &line[start..],
    //        None => "",
    //    };
    //
    //    (words, text)
    // }
    private static Tuple<List<String>, String> splitArgsText(String line, boolean isWin) {
        return null;
    }
}
