package com.tsoft.jai.function;

import com.tsoft.jai.anyhow.Result;
import com.tsoft.jai.config.Config;
import com.tsoft.jai.serde.serdejson.SerdeJson;
import com.tsoft.jai.serde.Value;
import lombok.Data;
import lombok.experimental.Accessors;
import tools.jackson.core.type.TypeReference;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import static com.tsoft.jai.anyhow.Macros.bail;
import static com.tsoft.jai.anyhow.Result.*;
import static com.tsoft.jai.serde.Value.json;
import static com.tsoft.jai.std.Fs.readToString;
import static com.tsoft.jai.utils.base.CollectionsUtils.isEmpty;
import static com.tsoft.jai.utils.base.StringUtils.format;

@Data
@Accessors(chain = true)
public class Functions {

    private List<FunctionDeclaration> declarations;

    // pub fn eval_tool_calls(config: &GlobalConfig, mut calls: Vec<ToolCall>) -> Result<Vec<ToolResult>> {
    //    let mut output = vec![];
    //    if calls.is_empty() {
    //        return Ok(output);
    //    }
    //    calls = ToolCall::dedup(calls);
    //    if calls.is_empty() {
    //        bail!("The request was aborted because an infinite loop of function calls was detected.")
    //    }
    //    let mut is_all_null = true;
    //    for call in calls {
    //        let mut result = call.eval(config)?;
    //        if result.is_null() {
    //            result = json!("DONE");
    //        } else {
    //            is_all_null = false;
    //        }
    //        output.push(ToolResult::new(call, result));
    //    }
    //    if is_all_null {
    //        output = vec![];
    //    }
    //    Ok(output)
    // }
    public static Result<List<ToolResult>> evalToolCalls(Config config, List<ToolCall> calls) {
        List<ToolResult> output = new ArrayList<>();
        if (isEmpty(calls)) {
            return Ok(output);
        }
        calls = ToolCall.dedup(calls);
        if (isEmpty(calls)) {
            return bail("The request was aborted because an infinite loop of function calls was detected.");
        }
        boolean isAllNull = true;
        for (ToolCall call : calls) {
            Value result = call.eval(config);
            if (result == null) {
                result = json("DONE");
            } else {
                isAllNull = false;
            }
            output.add(new ToolResult().setCall(call).setOutput(result));
        }
        if (isAllNull) {
            output = new ArrayList<>();
        }
        return Ok(output);
    }

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
    public static Result<Functions> init(Path declarationsPath) {
        List<FunctionDeclaration> declarations;
        if (Files.exists(declarationsPath)) {
            Supplier<String> ctx = () -> format("Failed to load functions at {}", declarationsPath);
            Result<String> content = readToString(declarationsPath).withContext(ctx);
            if (isErr(content)) {
                return Err(content);
            }
            Result<List<FunctionDeclaration>> res = SerdeJson.fromStr(content.getValue(), new TypeReference<List<FunctionDeclaration>>() { }).withContext(ctx);
            declarations = res.getValue();
        } else {
            declarations = new ArrayList<>();
        }

        return Ok(new Functions().setDeclarations(declarations));
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
