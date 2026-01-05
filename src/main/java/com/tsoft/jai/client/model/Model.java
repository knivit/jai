package com.tsoft.jai.client.model;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Map;
import java.util.Objects;

@Data
@Accessors(chain = true)
public class Model {

    private String clientName;
    private ModelData data;

    // pub fn real_name(&self) -> &str {
    //     self.data.real_name.as_deref().unwrap_or(&self.data.name)
    // }
    public String getRealName() {
        String result = null;
        if (data != null) {
            result = data.getRealName();
            if (result == null) {
                result = data.getName();
            }
        }
        return result;
    }

    // pub fn patch(&self) -> Option<&Value> {
    //     self.data.patch.as_ref()
    // }
    public Map<String, Object> getPatch() {
        return (data == null) ? null : data.getPatch();
    }

    // pub fn max_tokens_param(&self) -> Option<isize> {
    //    if self.data.require_max_tokens {
    //        self.data.max_output_tokens
    //    } else {
    //        None
    //    }
    // }
    public Integer getMaxTokensParam() {
        if (data != null) {
            if (data.isRequireMaxTokens()) {
                return data.getMaxOutputTokens();
            }
        }
        return null;
    }
}
