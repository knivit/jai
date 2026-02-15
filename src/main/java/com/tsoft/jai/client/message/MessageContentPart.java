package com.tsoft.jai.client.message;

import lombok.*;
import lombok.experimental.Accessors;

import static com.tsoft.jai.core.Panic.panic;
import static com.tsoft.jai.utils.base.StringUtils.format;

@Getter
@Setter(AccessLevel.PRIVATE)
@Accessors(chain = true)
@RequiredArgsConstructor
public class MessageContentPart {

    public enum MessageContentPartEnum {
        Text,
        ImageUrl
    }

    private final MessageContentPartEnum type;

    private String text;
    private ImageUrl imageUrl;

    public static MessageContentPart Text(String text) {
        return new MessageContentPart(MessageContentPartEnum.Text).setText(text);
    }

    public static MessageContentPart ImageUrl(ImageUrl imageUrl) {
        return new MessageContentPart(MessageContentPartEnum.ImageUrl).setImageUrl(imageUrl);
    }

    public MessageContentPart setText(String text) {
        if (MessageContentPartEnum.Text.equals(type)) {
            this.text = text;
        } else {
            panic();
        }
        return this;
    }

    @Override
    public String toString() {
        return switch (type) {
            case Text -> format("{} (text={})", MessageContentPartEnum.Text, text);
            case ImageUrl -> format("{} (imageUrl={})", MessageContentPartEnum.ImageUrl, imageUrl);
        };
    }
}
