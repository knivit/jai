package com.tsoft.jai.client.common;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class EmbeddingsData {

    private List<String> texts;
    private boolean query;
}
