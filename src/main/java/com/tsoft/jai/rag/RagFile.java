package com.tsoft.jai.rag;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class RagFile {

    private String hash;
    private String path;
    private List<RagDocument> documents;
}
