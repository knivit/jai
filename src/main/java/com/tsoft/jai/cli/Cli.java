package com.tsoft.jai.cli;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.*;

@Data
@Accessors(chain = true)
public class Cli {

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
    private List<String> agentVariable = new ArrayList<>();

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

    private void doParse(String[] args) {
        if (args == null) {
            return;
        }

        Deque<String> stack = new ArrayDeque<>(Arrays.asList(args).reversed());

        while (!stack.isEmpty()) {
            String arg = stack.pop();
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
                agentVariable.add(vars[0] + "=" + args[1]);
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

    public void help() {
        System.out.println("""
            All-in-one LLM CLI Tool
            
            Usage: jai [OPTIONS] [TEXT]...
            
            Arguments:
              [TEXT]...  Input text
            
            Options:
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

    public void version() {
        System.out.println("""
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
