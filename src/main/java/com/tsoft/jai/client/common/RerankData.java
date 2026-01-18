package com.tsoft.jai.client.common;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class RerankData {

    private String query;
    private List<String> documents;
    private Integer topN;
}
