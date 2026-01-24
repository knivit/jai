package com.tsoft.jai.config.agent;

import com.tsoft.jai.function.FunctionDeclaration;
import com.tsoft.jai.function.Functions;
import com.tsoft.jai.serdejson.SerDe;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.File;
import java.util.List;

import static com.tsoft.jai.utils.base.CollectionsUtils.isEmpty;
import static com.tsoft.jai.utils.base.StringUtils.isBlank;

@Data
@Accessors(chain = true)
public class AgentDefinition {

    private String name;
    //#[serde(default)]
    private String description;
    //#[serde(default)]
    private String version;
    //#[serde(default)]
    private String instructions;
    //#[serde(default)]
    private boolean dynamicInstructions;
    //#[serde(default)]
    private List<AgentVariable> variables;
    //#[serde(default)]
    private List<String> conversationStarters;
    //#[serde(default)]
    private List<String> documents;

    public static AgentDefinition load(File configFile) {
        return SerDe.readFromYamlFile(configFile, AgentDefinition.class);
    }

    // fn replace_tools_placeholder(&mut self, functions: &Functions) {
    //    let tools_placeholder: &str = "{{__tools__}}";
    //    if self.instructions.contains(tools_placeholder) {
    //        let tools = functions
    //            .declarations()
    //            .iter()
    //            .enumerate()
    //            .map(|(i, v)| {
    //                let description = match v.description.split_once('\n') {
    //                    Some((v, _)) => v,
    //                    None => &v.description,
    //                };
    //                format!("{}. {}: {description}", i + 1, v.name)
    //            })
    //            .collect::<Vec<String>>()
    //            .join("\n");
    //        self.instructions = self.instructions.replace(tools_placeholder, &tools);
    //    }
    // }
    public void replaceToolsPlaceholder(Functions functions) {
        String toolsPlaceholder = "{{__tools__}}";
        if (instructions != null && instructions.contains(toolsPlaceholder)) {
            List<FunctionDeclaration> declarations = (functions == null) ? null : functions.getDeclarations();

            StringBuilder tools = new StringBuilder();
            if (!isEmpty(declarations)) {
                for (int i = 0; i < declarations.size(); i ++) {
                    FunctionDeclaration declaration = declarations.get(i);
                    String description = declaration.getDescription();
                    if (!isBlank(description)) {
                        int n = description.indexOf('\n');
                        description = (n <= 0) ? description : description.substring(0, n);
                        tools.append(i + 1).append(". ").append(declaration.getName()).append(": ").append(description);
                    } else {
                        tools.append(i + 1).append(". ").append(declaration.getName());
                    }
                }
            }

            instructions = instructions.replace(toolsPlaceholder, tools.toString());
        }
    }
}
