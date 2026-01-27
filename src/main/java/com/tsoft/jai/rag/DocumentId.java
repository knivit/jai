package com.tsoft.jai.rag;

import com.tsoft.jai.utils.base.Tuple;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import static com.tsoft.jai.utils.base.StringUtils.*;

@Data
@Accessors(chain = true)
@NoArgsConstructor
public class DocumentId {

    private static final int BITS = 32;

    private int fileIndex;
    private int documentIndex;

    // pub fn new(file_index: usize, document_index: usize) -> Self {
    //    let value = (file_index << (usize::BITS / 2)) | document_index;
    //    Self(value)
    // }
    public DocumentId(int fileIndex, int documentIndex) {
        this.fileIndex = fileIndex;
        this.documentIndex = documentIndex;
    }

    // pub fn split(self) -> (usize, usize) {
    //    let value = self.0;
    //    let low_mask = (1 << (usize::BITS / 2)) - 1;
    //    let low = value & low_mask;
    //    let high = value >> (usize::BITS / 2);
    //    (high, low)
    // }
    public Tuple<Integer, Integer> split() {
        return new Tuple<>(fileIndex, documentIndex);
    }

    public String toStr() {
        return format("{}-{}", fileIndex, documentIndex);
    }

    public static DocumentId fromStr(String str) {
        if (isBlank(str)) {
            return null;
        }
        Tuple<String, String> tuple = splitOnce(str, '-');
        return new DocumentId()
            .setFileIndex(Integer.parseInt(tuple.first()))
            .setDocumentIndex(Integer.parseInt(tuple.second()));
    }
}
