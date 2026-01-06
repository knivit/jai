package com.tsoft.jai.rag;

import com.tsoft.jai.client.model.Model;
import com.tsoft.jai.config.Config;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class Rag {

    private Config config;
    private String name;
    private String path;
    private Model embeddingModel;
    // ? hnsw: Hnsw<'static, f32, DistCosine>,
    // ? bm25: SearchEngine<DocumentId>,
    // ? data: RagData,
    private String lastSources;
}
