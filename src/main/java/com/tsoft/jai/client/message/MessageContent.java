package com.tsoft.jai.client.message;

import lombok.*;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import static com.tsoft.jai.core.Panic.panic;
import static com.tsoft.jai.utils.base.CollectionsUtils.isEmpty;
import static com.tsoft.jai.utils.base.StringUtils.format;

@Getter
@Setter(AccessLevel.PRIVATE)
@Accessors(chain = true)
@RequiredArgsConstructor
public class MessageContent {

    public enum MessageContentEnum {
        Text,
        Array,
        ToolCalls
    }

    private final MessageContentEnum type;

    private String text;
    private List<MessageContentPart> array;

    // Note: This type is primarily for convenience and does not exist in OpenAI's API.
    private MessageContentToolCalls toolCalls;

    public static MessageContent Text(String text) {
        return new MessageContent(MessageContentEnum.Text).setText(text);
    }

    public MessageContent setText(String text) {
        if (MessageContentEnum.Text.equals(type)) {
            this.text = text;
        } else {
            panic();
        }
        return this;
    }

    public static MessageContent Array(List<MessageContentPart> array) {
        return new MessageContent(MessageContentEnum.Array).setArray(array);
    }

    public static MessageContent Array(MessageContent other) {
        if (other == null) {
            throw new IllegalArgumentException("Argument 'other' can't be null");
        }

        return switch (other.type) {
            case Array -> new MessageContent(MessageContentEnum.Array).setArray(cloneArray(other));
            default -> throw new IllegalStateException("The operation is invalid");
        };
    }

    public void insert(int index, MessageContentPart part) {
        switch (type) {
            case Array -> {
                List<MessageContentPart> clone = cloneArray(this);
                clone.add(index, part);
                this.array = clone;
            }
            default -> throw new IllegalStateException("The operation is invalid");
        }
    }

    public void push(MessageContentPart part) {
        switch (type) {
            case Array -> {
                List<MessageContentPart> clone = cloneArray(this);
                clone.add(part);
                this.array = clone;
            }
            default -> throw new IllegalStateException("The operation is invalid");
        }
    }

    public void append(MessageContent other) {
        if (other == null) {
            throw new IllegalArgumentException("Argument 'other' can't be null");
        }

        if (!Objects.equals(type, other.type)) {
            throw new IllegalArgumentException("Argument 'other' must be the same type as 'this'");
        }

        switch (type) {
            case Array -> {
                List<MessageContentPart> clone = cloneArray(this);
                clone.addAll(other.array);
                this.array = clone;
            }
            default -> throw new IllegalStateException("The operation is invalid");
        }
    }

    private static List<MessageContentPart> cloneArray(MessageContent source) {
        List<MessageContentPart> clone = new ArrayList<>();
        if (!isEmpty(source.array)) {
            clone.addAll(source.array);
        }
        return clone;
    }

    public static MessageContent ToolCalls(MessageContentToolCalls toolCalls) {
        return new MessageContent(MessageContentEnum.ToolCalls).setToolCalls(toolCalls);
    }

    // pub fn merge_prompt(&mut self, replace_fn: impl Fn(&str) -> String) {
    //    match self {
    //        MessageContent::Text(text) => *text = replace_fn(text),
    //        MessageContent::Array(list) => {
    //            if list.is_empty() {
    //                list.push(MessageContentPart::Text {
    //                    text: replace_fn(""),
    //                })
    //            } else if let Some(MessageContentPart::Text { text }) = list.get_mut(0) {
    //                *text = replace_fn(text)
    //            }
    //        }
    //        MessageContent::ToolCalls(_) => {}
    //    }
    // }
    public void mergePrompt(Function<String, String> replaceFn) {
        switch (type) {
            case Text -> text = replaceFn.apply(text);
            case Array -> {
                if (isEmpty(array)) {
                    array = new ArrayList<>();
                    array.add(MessageContentPart.Text(replaceFn.apply("")));
                } else {
                    MessageContentPart part = array.get(0);
                    if (MessageContentPart.MessageContentPartEnum.Text.equals(part.getType())) {
                        part.setText(replaceFn.apply(part.getText()));
                    }
                }
            }
        }
    }

    @Override
    public String toString() {
        return switch (type) {
            case Text -> format("{} (text={})", MessageContentEnum.Text, text);
            case Array -> format("{} (array={})", MessageContentEnum.Array, array);
            case ToolCalls -> format("{} (toolCalls={})", MessageContentEnum.ToolCalls, toolCalls);
        };
    }
}
