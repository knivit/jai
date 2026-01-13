package com.tsoft.jai.utils.command;

public final class Command {

    public static Shell SHELL = detectShell();

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

    private Command() { }
}
