package com.tsoft.jai.reqwest;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class StatusCode {

    private Integer value;

    public boolean isSuccess() {
        if (value == null) {
            return false;
        }

        return (value >= 200) && (value < 400);
    }
}
