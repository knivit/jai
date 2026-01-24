package com.tsoft.jai.function;

import com.tsoft.jai.config.Config;
import com.tsoft.jai.serdejson.Value;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.tsoft.jai.utils.base.StringUtils.isBlank;

@Data
@Accessors(chain = true)
public class ToolCall {

    private String name;
    private Object arguments;
    private String id;

    // pub fn dedup(calls: Vec<Self>) -> Vec<Self> {
    //    let mut new_calls = vec![];
    //    let mut seen_ids = HashSet::new();
    //
    //    for call in calls.into_iter().rev() {
    //        if let Some(id) = &call.id {
    //            if !seen_ids.contains(id) {
    //                seen_ids.insert(id.clone());
    //                new_calls.push(call);
    //            }
    //        } else {
    //            new_calls.push(call);
    //        }
    //    }
    //
    //    new_calls.reverse();
    //    new_calls
    // }
    public static List<ToolCall> dedup(List<ToolCall> calls) {
        List<ToolCall> newCalls = new ArrayList<>();
        Set<String> seenIds = new HashSet<>();

        for (ToolCall call : calls.reversed()) {
            String id = call.id;
            if (!isBlank(id)) {
                if (!seenIds.contains(id)) {
                    seenIds.add(id);
                    newCalls.add(call);
                }
            } else {
                newCalls.add(call);
            }
        }

        return newCalls.reversed();
    }

    // pub fn eval(&self, config: &GlobalConfig) -> Result<Value> {
    //    let (call_name, cmd_name, mut cmd_args, envs) = match &config.read().agent {
    //        Some(agent) => self.extract_call_config_from_agent(config, agent)?,
    //        None => self.extract_call_config_from_config(config)?,
    //    };
    //
    //    let json_data = if self.arguments.is_object() {
    //        self.arguments.clone()
    //    } else if let Some(arguments) = self.arguments.as_str() {
    //        let arguments: Value = serde_json::from_str(arguments).map_err(|_| {
    //            anyhow!("The call '{call_name}' has invalid arguments: {arguments}")
    //        })?;
    //        arguments
    //    } else {
    //        bail!(
    //            "The call '{call_name}' has invalid arguments: {}",
    //            self.arguments
    //        );
    //    };
    //
    //    cmd_args.push(json_data.to_string());
    //
    //    let output = match run_llm_function(cmd_name, cmd_args, envs)? {
    //        Some(contents) => serde_json::from_str(&contents)
    //            .ok()
    //            .unwrap_or_else(|| json!({"output": contents})),
    //        None => Value::Null,
    //    };
    //
    //    Ok(output)
    // }
    public Value eval(Config config) {
        return null;
    }
}
