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

    // pub fn edit_file(editor: &str, path: &Path) -> Result<()> {
    //    let mut child = Command::new(editor).arg(path).spawn()?;
    //    child.wait()?;
    //    Ok(())
    // }
    public static void editFile(String editor, File roleFile) {

    }

    private Command() { }
}
