package com.tsoft.jai.mods.session.dto;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class Message {

    private String role;
    private String content;
}
