package com.tsoft.jai.client.message;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class MessageContentPart {

    private String text;
    private ImageUrl imageUrl;
}
