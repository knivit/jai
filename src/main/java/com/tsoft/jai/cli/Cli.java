package com.tsoft.jai.cli;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.*;
import java.util.stream.Collectors;

import static com.tsoft.jai.inquire.Inquire.println;
import static com.tsoft.jai.utils.StringUtils.format;
import static com.tsoft.jai.utils.StringUtils.isBlank;

@Data
@Accessors(chain = true)
public class Cli {

    /// Path to a config file
    private String configFile;

    /// Select a LLM model
    //#[clap(short, long)]
    private String model;

    /// Use the system prompt
    //#[clap(long)]
    private String prompt;

    /// Select a role
    //#[clap(short, long)]
    private String role;

    /// Start or join a session
    //#[clap(short = 's', long)]
    private String session;

    /// Ensure the session is empty
    //#[clap(long)]
    private boolean emptySession;

    /// Ensure the new conversation is saved to the session
    //#[clap(long)]
    private boolean saveSession;

    /// Start a agent
    //#[clap(short = 'a', long)]
    private String agent;

    /// Set agent variables
    //#[clap(long, value_names = ["NAME", "VALUE"], num_args = 2)]
    private Map<String, String> agentVariable = new HashMap<>();

    /// Start a RAG
    //#[clap(long)]
    private String rag;

    /// Rebuild the RAG to sync document changes
    //#[clap(long)]
    private boolean rebuildRag;

    /// Execute a macro
    //#[clap(long = "macro", value_name = "MACRO")]
    private String macroName;

    /// Serve the LLM API and WebAPP
    //#[clap(long, value_name = "ADDRESS")]
    private String serve;

    /// Execute commands in natural language
    //#[clap(short = 'e', long)]
    private boolean execute;

    /// Output code only
    //#[clap(short = 'c', long)]
    private boolean code;

    /// Include files, directories, or URLs
    //#[clap(short = 'f', long, value_name = "FILE")]
    private List<String> file = new ArrayList<>();

    /// Turn off stream mode
    //#[clap(short = 'S', long)]
    private boolean noStream;

    /// Display the message without sending it
    //#[clap(long)]
    private boolean dryRun;

    /// Display information
    //#[clap(long)]
    private boolean info;

    /// Sync models updates
    //#[clap(long)]
    private boolean syncModels;

    /// List all available chat models
    //#[clap(long)]
    private boolean listModels;

    /// List all roles
    //#[clap(long)]
    private boolean listRoles;

    /// List all sessions
    //#[clap(long)]
    private boolean listSessions;

    /// List all agents
    //#[clap(long)]
    private boolean listAgents;

    /// List all RAGs
    //#[clap(long)]
    private boolean listRags;

    /// List all macros
    //#[clap(long)]
    private boolean listMacros;

    /// Input text
    //#[clap(trailing_var_arg = true)]
    private List<String> text = new ArrayList<>();

    public static Cli parse(String[] args) {
        Cli cli = new Cli();
        cli.doParse(args);
        return cli;
    }

    // pub fn text(&self) -> Result<Option<String>> {
    //    let mut stdin_text = String::new();
    //    if !stdin().is_terminal() {
    //        let _ = stdin()
    //            .read_to_string(&mut stdin_text)
    //            .context("Invalid stdin pipe")?;
    //    };
    //    match self.text.is_empty() {
    //        true => {
    //            if stdin_text.is_empty() {
    //                Ok(None)
    //            } else {
    //                Ok(Some(stdin_text))
    //            }
    //        }
    //        false => {
    //            if self.macro_name.is_some() {
    //                let text = self
    //                    .text
    //                    .iter()
    //                    .map(|v| shell_words::quote(v))
    //                    .collect::<Vec<_>>()
    //                    .join(" ");
    //                if stdin_text.is_empty() {
    //                    Ok(Some(text))
    //                } else {
    //                    Ok(Some(format!("{text} -- {stdin_text}")))
    //                }
    //            } else {
    //                let text = self.text.join(" ");
    //                if stdin_text.is_empty() {
    //                    Ok(Some(text))
    //                } else {
    //                    Ok(Some(format!("{text}\n{stdin_text}")))
    //                }
    //            }
    //        }
    //    }
    // }
    public String text() {
        String stdinText = null;
        try {
            if (System.in.available() > 0) {
                try (Reader in = new BufferedReader(new InputStreamReader(System.in))) {
                    stdinText = String.join("\n", in.readAllLines());
                }
            }
        } catch (Exception ex) {
            throw new IllegalStateException("Error reading from a pipe", ex);
        }

        if (text == null || text.isEmpty()) {
            return stdinText;
        }

        if (!isBlank(macroName)) {
            String str = text.stream().map(e -> e /* TODO */).collect(Collectors.joining(" "));
            if (isBlank(stdinText)) {
                return str;
            }
            return "%s -- %s".formatted(str, stdinText);
        }

        String str = String.join(" ", text);
        if (isBlank(stdinText)) {
            return str;
        }
        return format("{}\n{}", str, stdinText);
    }

    private void doParse(String[] args) {
        if (args == null) {
            return;
        }

        Deque<String> stack = new ArrayDeque<>(Arrays.asList(args));

        while (!stack.isEmpty()) {
            String arg = stack.pop();

            if ("--config-file".equals(arg)) {
                configFile = getArg(stack, "error: a value is required for '--config-file <FILE>' but none was supplied");
                continue;
            }

            if ("--model".equals(arg) || "-m".equals(arg)) {
                model = getArg(stack, "error: a value is required for '--model <MODEL>' but none was supplied");
                continue;
            }

            if ("--prompt".equals(arg)) {
                prompt = getArg(stack, "error: a value is required for '--prompt <PROMPT>' but none was supplied");
                continue;
            }

            if ("--role".equals(arg) || "-r".equals(arg)) {
                role = getArg(stack, "error: a value is required for '--role <ROLE>' but none was supplied");
                continue;
            }

            if ("--session".equals(arg) || "-s".equals(arg)) {
                session = getArg(stack, "error: a value is required for '--session <SESSION>' but none was supplied");
                continue;
            }

            if ("--empty-session".equals(arg)) {
                emptySession = true;
                continue;
            }

            if ("--save-session".equals(arg)) {
                saveSession = true;
                continue;
            }

            if ("--agent".equals(arg) || "-a".equals(arg)) {
                agent = getArg(stack, "error: a value is required for '--agent <AGENT>' but none was supplied");
                continue;
            }

            if ("--agent-variable".equals(arg)) {
                String[] vars = getArg(stack, "error: a value is required for '--agent-variable <NAME> <VALUE>' but none was supplied",
                    "error: 2 values required for '--agent-variable <NAME> <VALUE>' but 1 was provided");
                agentVariable.put(vars[0], vars[1]);
                continue;
            }

            if ("--rag".equals(arg)) {
                rag = getArg(stack, "error: a value is required for '--rag <RAG>' but none was supplied");
                continue;
            }

            if ("--rebuild-rag".equals(arg)) {
                rebuildRag = true;
                continue;
            }

            if ("--macro".equals(arg)) {
                macroName = getArg(stack, "error: a value is required for '--macro <MACRO>' but none was supplied");
                continue;
            }

            if ("--serve".equals(arg)) {
                serve = getArg(stack, "error: a value is required for '--serve <ADDRESS>' but none was supplied");
                continue;
            }

            if ("--execute".equals(arg) || "-e".equals(arg)) {
                execute = true;
                continue;
            }

            if ("--code".equals(arg) || "-c".equals(arg)) {
                code = true;
                continue;
            }

            if ("--file".equals(arg) || "-f".equals(arg)) {
                String var = getArg(stack, "error: a value is required for '--file <FILE>' but none was supplied");
                file.add(var);
                continue;
            }

            if ("--no-stream".equals(arg) || "-S".equals(arg)) {
                noStream = true;
                continue;
            }

            if ("--dry-run".equals(arg)) {
                dryRun = true;
                continue;
            }

            if ("--info".equals(arg)) {
                info = true;
                continue;
            }

            if ("--sync-models".equals(arg)) {
                syncModels = true;
                continue;
            }

            if ("--list-models".equals(arg)) {
                listModels = true;
                continue;
            }

            if ("--list-roles".equals(arg)) {
                listRoles = true;
                continue;
            }

            if ("--list-sessions".equals(arg)) {
                listSessions = true;
                continue;
            }

            if ("--list-agents".equals(arg)) {
                listAgents = true;
                continue;
            }

            if ("--list-rags".equals(arg)) {
                listRags = true;
                continue;
            }

            if ("--list-macros".equals(arg)) {
                listMacros = true;
                continue;
            }

            if ("--help".equals(arg) || "-h".equals(arg)) {
                help();
                continue;
            }

            if ("--version".equals(arg) || "-V".equals(arg)) {
                version();
                continue;
            }

            text.add(arg);
        }
    }

    private void help() {
        println("""
            All-in-one LLM CLI Tool
            
            Usage: jai [OPTIONS] [TEXT]...
            
            Arguments:
              [TEXT]...  Input text
            
            Options:
                  --config-file <FILE>             Path to a config file
              -m, --model <MODEL>                  Select a LLM model
                  --prompt <PROMPT>                Use the system prompt
              -r, --role <ROLE>                    Select a role
              -s, --session [<SESSION>]            Start or join a session
                  --empty-session                  Ensure the session is empty
                  --save-session                   Ensure the new conversation is saved to the session
              -a, --agent <AGENT>                  Start a agent
                  --agent-variable <NAME> <VALUE>  Set agent variables
                  --rag <RAG>                      Start a RAG
                  --rebuild-rag                    Rebuild the RAG to sync document changes
                  --macro <MACRO>                  Execute a macro
                  --serve [<ADDRESS>]              Serve the LLM API and WebAPP
              -e, --execute                        Execute commands in natural language
              -c, --code                           Output code only
              -f, --file <FILE>                    Include files, directories, or URLs
              -S, --no-stream                      Turn off stream mode
                  --dry-run                        Display the message without sending it
                  --info                           Display information
                  --sync-models                    Sync models updates
                  --list-models                    List all available chat models
                  --list-roles                     List all roles
                  --list-sessions                  List all sessions
                  --list-agents                    List all agents
                  --list-rags                      List all RAGs
                  --list-macros                    List all macros
              -h, --help                           Print help
              -V, --version                        Print version
            """);

        System.exit(0);
    }

    private void version() {
        println("""
            jai 0.30.0
            """);

        System.exit(0);
    }

    private String getArg(Deque<String> stack, String err) {
        if (!stack.isEmpty()) {
            return stack.pop();
        }

        System.out.printf(err);
        System.exit(1);

        throw new IllegalStateException(err);
    }

    private String[] getArg(Deque<String> stack, String ... errs) {
        String[] args = new String[errs.length];

        int i = 0;
        for (String err : errs) {
            if (!stack.isEmpty()) {
                args[i] = stack.pop();
                i ++;
                continue;
            }

            System.out.printf(err);
            System.exit(1);

            throw new IllegalStateException(err);
        }
        return args;
    }
}
