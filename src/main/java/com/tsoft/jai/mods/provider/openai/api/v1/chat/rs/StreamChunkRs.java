package com.tsoft.jai.mods.provider.openai.api.v1.chat.rs;

import lombok.Data;

import java.util.List;

@Data
public class StreamChunkRs {

    private String id;
    private String object;
    private Long created;
    private String model;
    private List<StreamChoiceRs> choices;
}
