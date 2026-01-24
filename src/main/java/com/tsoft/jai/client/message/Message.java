package com.tsoft.jai.client.message;

import com.tsoft.jai.client.model.Model;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Arrays;
import java.util.List;

import static com.tsoft.jai.client.message.MessageContent.MessageContentEnum.Array;
import static com.tsoft.jai.client.message.MessageContent.MessageContentEnum.Text;
import static com.tsoft.jai.utils.base.CollectionsUtils.isEmpty;
import static com.tsoft.jai.utils.base.StringUtils.isBlank;

@Data
@Accessors(chain = true)
public class Message {

    private MessageRole role;
    private MessageContent content;

    // pub fn patch_messages(messages: &mut Vec<Message>, model: &Model) {
    //    if messages.is_empty() {
    //        return;
    //    }
    //    if let Some(prefix) = model.system_prompt_prefix() {
    //        if messages[0].role.is_system() {
    //            messages[0].merge_system(MessageContent::Text(prefix.to_string()));
    //        } else {
    //            messages.insert(
    //                0,
    //                Message {
    //                    role: MessageRole::System,
    //                    content: MessageContent::Text(prefix.to_string()),
    //                },
    //            );
    //        }
    //    }
    //    if model.no_system_message() && messages[0].role.is_system() {
    //        let system_message = messages.remove(0);
    //        if let (Some(message), system) = (messages.get_mut(0), system_message.content) {
    //            message.merge_system(system);
    //        }
    //    }
    // }
    public static void patchMessages(List<Message> messages, Model model) {
        if (isEmpty(messages)) {
            return;
        }

        String prefix = model.systemPromptPrefix();
        if (!isBlank(prefix)) {
            if (MessageRole.isSystem(messages.get(0).getRole())) {
                messages.get(0).mergeSystem(MessageContent.Text(prefix));
            } else {
                messages.addFirst(new Message().setRole(MessageRole.System).setContent(MessageContent.Text(prefix)));
            }
        }
        if (model.noSystemMessage() && MessageRole.isSystem(messages.get(0).getRole())) {
            Message systemMessage = messages.removeFirst();
            Message message = messages.get(0);
            MessageContent system = systemMessage.getContent();
            if (message != null && system != null) {
                message.mergeSystem(system);
            }
        }
    }

    // pub fn merge_system(&mut self, system: MessageContent) {
    //    match (&mut self.content, system) {
    //        (MessageContent::Text(text), MessageContent::Text(system_text)) => {
    //            self.content = MessageContent::Array(vec![
    //                MessageContentPart::Text { text: system_text },
    //                MessageContentPart::Text {
    //                    text: text.to_string(),
    //                },
    //            ])
    //        }
    //        (MessageContent::Array(list), MessageContent::Text(system_text)) => {
    //            list.insert(0, MessageContentPart::Text { text: system_text })
    //        }
    //        (MessageContent::Text(text), MessageContent::Array(mut system_list)) => {
    //            system_list.push(MessageContentPart::Text {
    //                text: text.to_string(),
    //            });
    //            self.content = MessageContent::Array(system_list);
    //        }
    //        (MessageContent::Array(list), MessageContent::Array(mut system_list)) => {
    //            system_list.append(list);
    //            self.content = MessageContent::Array(system_list);
    //        }
    //        _ => {}
    //    }
    // }
    public void mergeSystem(MessageContent system) {
        if (content == null || system == null) {
            return;
        }

        if (Text.equals(content.getType()) && Text.equals(system.getType())) {
            content = MessageContent.Array(Arrays.asList(
                MessageContentPart.Text(system.getText()),
                MessageContentPart.Text(content.getText())
            ));
        } else if (Array.equals(content.getType()) && Text.equals(system.getType())) {
            content.insert(0, MessageContentPart.Text(content.getText()));
        } else if (Text.equals(content.getType()) && Array.equals(system.getType())) {
            system.push(MessageContentPart.Text(content.getText()));
            content = MessageContent.Array(system);
        } else if (Array.equals(content.getType()) && Array.equals(system.getType())) {
            system.append(content);
            this.content = MessageContent.Array(system);
        }
    }
}
