package com.tsoft.jai.rag;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.Map;

@Data
@Accessors(chain = true)
public class RagData {

    private String embeddingModel;
    private Integer chunkSize;
    private Integer chunkOverlap;
    private String rerankerModel;
    private Integer topK;
    private Integer batchSize;
    private Integer nextFileId;
    private List<String> documentPaths;
    private Map<Integer, RagFile> files;
    //#[serde(with = "serde_vectors")]
    @JsonProperty("serde_vectors")
    private Map<DocumentId, List<Float>> vectors;
}
