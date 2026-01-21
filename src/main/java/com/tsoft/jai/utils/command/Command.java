package com.tsoft.jai.utils.command;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;

import static java.util.Collections.addAll;

public final class Command {

    public static Shell SHELL = detectShell();

    private static final Duration DEFAULT_EXEC_TIMEOUT = Duration.ofSeconds(30);

    public static ShellCommandResult execute(String ... args) {
        return executeWithTimeout(DEFAULT_EXEC_TIMEOUT, args);
    }

    public static ShellCommandResult executeWithTimeout(Duration timeout, String ... args) {
        try {
            List<String> cmd = new ArrayList<>();
            cmd.add(SHELL.getCmd());
            if (args != null) {
                addAll(cmd, args);
            }

            ProcessBuilder pb = new ProcessBuilder(cmd);
            Process process = pb.start();

            // Read output in separate thread
            Future<String> outputFuture = readStreamAsync(process.getInputStream());
            Future<String> errorFuture = readStreamAsync(process.getErrorStream());

            // Wait for process to complete with timeout
            if (!process.waitFor(timeout)) {
                process.destroyForcibly();
                throw new TimeoutException("Command timed out after " + timeout + " " + timeout);
            }

            int exitCode = process.exitValue();
            String output = outputFuture.get();
            String error = errorFuture.get();

            return new ShellCommandResult(exitCode, output, error);
        } catch (Exception ex) {
            return new ShellCommandResult(null, null, ex.getMessage());
        }
    }

    private static Future<String> readStreamAsync(InputStream stream) {
        try (ExecutorService executor = Executors.newSingleThreadExecutor()) {
            return executor.submit(() -> {
                StringBuilder sb = new StringBuilder();
                try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(stream))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line).append("\n");
                    }
                }
                executor.shutdown();
                return sb.toString();
            });
        }
    }

    // pub fn detect_shell() -> Shell {
    //    let cmd = env::var(get_env_name("shell")).ok().or_else(|| {
    //        if cfg!(windows) {
    //            if let Ok(ps_module_path) = env::var("PSModulePath") {
    //                let ps_module_path = ps_module_path.to_lowercase();
    //                if ps_module_path.starts_with(r"c:\\users") {
    //                    if ps_module_path.contains(r"\powershell\7\") {
    //                        return Some("pwsh.exe".to_string());
    //                    } else {
    //                        return Some("powershell.exe".to_string());
    //                    }
    //                }
    //            }
    //            None
    //        } else {
    //            env::var("SHELL").ok()
    //        }
    //    });
    //    let name = cmd
    //        .as_ref()
    //        .and_then(|v| Path::new(v).file_stem().and_then(|v| v.to_str()))
    //        .map(|v| {
    //            if v == "nu" {
    //                "nushell".into()
    //            } else {
    //                v.to_lowercase()
    //            }
    //        });
    //    let (cmd, name) = match (cmd.as_deref(), name.as_deref()) {
    //        (Some(cmd), Some(name)) => (cmd, name),
    //        _ => {
    //            if cfg!(windows) {
    //                ("cmd.exe", "cmd")
    //            } else {
    //                ("/bin/sh", "sh")
    //            }
    //        }
    //    };
    //    let shell_arg = match name {
    //        "powershel" => "-Command",
    //        "cmd" => "/C",
    //        _ => "-c",
    //    };
    //    Shell::new(name, cmd, shell_arg)
    // }
    public static Shell detectShell() {
        return null;
    }

    // pub fn run_loader_command(path: &str, extension: &str, loader_command: &str) -> Result<String> {
    //    let cmd_args = shell_words::split(loader_command)
    //        .with_context(|| anyhow!("Invalid document loader '{extension}': `{loader_command}`"))?;
    //    let mut use_stdout = true;
    //    let outpath = temp_file("-output-", "").display().to_string();
    //    let cmd_args: Vec<_> = cmd_args
    //        .into_iter()
    //        .map(|mut v| {
    //            if v.contains("$1") {
    //                v = v.replace("$1", path);
    //            }
    //            if v.contains("$2") {
    //                use_stdout = false;
    //                v = v.replace("$2", &outpath);
    //            }
    //            v
    //        })
    //        .collect();
    //    let cmd_eval = shell_words::join(&cmd_args);
    //    debug!("run `{cmd_eval}`");
    //    let (cmd, args) = cmd_args.split_at(1);
    //    let cmd = &cmd[0];
    //    if use_stdout {
    //        let (success, stdout, stderr) =
    //            run_command_with_output(cmd, args, None).with_context(|| {
    //                format!("Unable to run `{cmd_eval}`, Perhaps '{cmd}' is not installed?")
    //            })?;
    //        if !success {
    //            let err = if !stderr.is_empty() {
    //                stderr
    //            } else {
    //                format!("The command `{cmd_eval}` exited with non-zero.")
    //            };
    //            bail!("{err}")
    //        }
    //        Ok(stdout)
    //    } else {
    //        let status = run_command(cmd, args, None).with_context(|| {
    //            format!("Unable to run `{cmd_eval}`, Perhaps '{cmd}' is not installed?")
    //        })?;
    //        if status != 0 {
    //            bail!("The command `{cmd_eval}` exited with non-zero.")
    //        }
    //        let contents = std::fs::read_to_string(&outpath)
    //            .context("Failed to read file generated by the loader")?;
    //        Ok(contents)
    //    }
    // }
    public static String runLoaderCommand(String path, String extension, String loaderCommand) {
        return null;
    }

    // pub fn edit_file(editor: &str, path: &Path) -> Result<()> {
    //    let mut child = Command::new(editor).arg(path).spawn()?;
    //    child.wait()?;
    //    Ok(())
    // }
    public static void editFile(String editor, File roleFile) {

    }

    private Command() { }
}
