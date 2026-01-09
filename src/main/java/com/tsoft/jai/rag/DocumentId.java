package com.tsoft.jai.rag;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class DocumentId {

    private int fileIndex;
    private int documentIndex;
}
