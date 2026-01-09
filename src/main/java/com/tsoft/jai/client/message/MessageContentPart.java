package com.tsoft.jai.client.message;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class MessageContentPart {

    public enum MessageContentPartEnum {
        Text,
        ImageUrl
    }

    private MessageContentPartEnum type;

    private String text;
    private ImageUrl imageUrl;

    public static MessageContentPart Text(String text) {
        return new MessageContentPart().setType(MessageContentPartEnum.Text).setText(text);
    }

    public static MessageContentPart ImageUrl(ImageUrl imageUrl) {
        return new MessageContentPart().setType(MessageContentPartEnum.ImageUrl).setImageUrl(imageUrl);
    }
}
