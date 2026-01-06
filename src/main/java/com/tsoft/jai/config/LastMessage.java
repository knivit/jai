package com.tsoft.jai.config;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class LastMessage {

    private Input input;
    private String output;
    private boolean continuous = true;
}
