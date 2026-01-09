package com.tsoft.jai.function;

import com.tsoft.jai.serdejson.SerDe;
import lombok.Data;
import lombok.experimental.Accessors;
import tools.jackson.core.type.TypeReference;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
@Accessors(chain = true)
public class Functions {

    private List<FunctionDeclaration> declarations;

    // pub fn init(declarations_path: &Path) -> Result<Self> {
    //    let declarations: Vec<FunctionDeclaration> = if declarations_path.exists() {
    //        let ctx = || {
    //            format!(
    //                "Failed to load functions at {}",
    //                declarations_path.display()
    //            )
    //        };
    //        let content = fs::read_to_string(declarations_path).with_context(ctx)?;
    //        serde_json::from_str(&content).with_context(ctx)?
    //    } else {
    //        vec![]
    //    };
    //
    //    Ok(Self { declarations })
    // }
    public static Functions init(File functionsFile) {
        List<FunctionDeclaration> declarations;
        if (functionsFile.exists()) {
            declarations = SerDe.readFromYamlFile(functionsFile, new TypeReference<>() { });
        } else {
            declarations = new ArrayList<>();
        }

        return new Functions().setDeclarations(declarations);
    }

    // pub fn run_llm_function(
    //    cmd_name: String,
    //    cmd_args: Vec<String>,
    //    mut envs: HashMap<String, String>,
    // ) -> Result<Option<String>> {
    //    let prompt = format!("Call {cmd_name} {}", cmd_args.join(" "));
    //
    //    let mut bin_dirs: Vec<PathBuf> = vec![];
    //    if cmd_args.len() > 1 {
    //        let dir = Config::agent_functions_dir(&cmd_name).join("bin");
    //        if dir.exists() {
    //            bin_dirs.push(dir);
    //        }
    //    }
    //    bin_dirs.push(Config::functions_bin_dir());
    //    let current_path = std::env::var("PATH").context("No PATH environment variable")?;
    //    let prepend_path = bin_dirs
    //        .iter()
    //        .map(|v| format!("{}{PATH_SEP}", v.display()))
    //        .collect::<Vec<_>>()
    //        .join("");
    //    envs.insert("PATH".into(), format!("{prepend_path}{current_path}"));
    //
    //    let temp_file = temp_file("-eval-", "");
    //    envs.insert("LLM_OUTPUT".into(), temp_file.display().to_string());
    //
    //    #[cfg(windows)]
    //    let cmd_name = polyfill_cmd_name(&cmd_name, &bin_dirs);
    //    if *IS_STDOUT_TERMINAL {
    //        println!("{}", dimmed_text(&prompt));
    //    }
    //    let exit_code = run_command(&cmd_name, &cmd_args, Some(envs))
    //        .map_err(|err| anyhow!("Unable to run {cmd_name}, {err}"))?;
    //    if exit_code != 0 {
    //        bail!("Tool call exit with {exit_code}");
    //    }
    //    let mut output = None;
    //    if temp_file.exists() {
    //        let contents =
    //            fs::read_to_string(temp_file).context("Failed to retrieve tool call output")?;
    //        if !contents.is_empty() {
    //            output = Some(contents);
    //        }
    //    };
    //    Ok(output)
    // }
    public static String runLlmFunction(String cmdName, List<String> cmdArgs, Map<String, String> envs) {
        return null;
    }
}
