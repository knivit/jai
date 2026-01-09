package com.tsoft.jai.client.message;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static com.tsoft.jai.utils.CollectionsUtils.isEmpty;

@Data
@Accessors(chain = true)
public class MessageContent {

    public enum MessageContentEnum {
        Text,
        Array,
        ToolCalls
    }

    private MessageContentEnum type;

    private String text;
    private List<MessageContentPart> array;
    // Note: This type is primarily for convenience and does not exist in OpenAI's API.
    private MessageContentToolCalls toolCalls;

    public static MessageContent Text(String text) {
        return new MessageContent().setType(MessageContentEnum.Text).setText(text);
    }

    public static MessageContent Array(List<MessageContentPart> array) {
        return new MessageContent().setType(MessageContentEnum.Array).setArray(array);
    }

    public static MessageContent ToolCalls(MessageContentToolCalls toolCalls) {
        return new MessageContent().setType(MessageContentEnum.ToolCalls).setToolCalls(toolCalls);
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
}
