package com.tsoft.jai.config;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class AutoName {

    private boolean naming;
    private String chatHistory;
    private String name;
}
