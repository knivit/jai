package com.tsoft.jai.config;

import com.tsoft.jai.client.message.ImageUrl;
import com.tsoft.jai.client.message.MessageContent;
import com.tsoft.jai.client.message.MessageContentPart;
import com.tsoft.jai.client.message.MessageContentToolCalls;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.tsoft.jai.utils.CollectionsUtils.isEmpty;
import static com.tsoft.jai.utils.StringUtils.isBlank;

@Data
@Accessors(chain = true)
public class Input {

    private Config config;
    private String text;
    private List<String> raw;
    private String patchedText;
    private String lastReply;
    private String continueOutput;
    private boolean regenerate;
    private List<String> medias;
    private Map<String, String> dataUrls;
    private MessageContentToolCalls toolCalls;
    private Role role;
    private String ragName;
    private boolean withSession;
    private boolean withAgent;

    // pub fn message_content(&self) -> MessageContent {
    //    if self.medias.is_empty() {
    //        MessageContent::Text(self.text())
    //    } else {
    //        let mut list: Vec<MessageContentPart> = self
    //            .medias
    //            .iter()
    //            .cloned()
    //            .map(|url| MessageContentPart::ImageUrl {
    //                image_url: ImageUrl { url },
    //            })
    //            .collect();
    //        if !self.text.is_empty() {
    //            list.insert(0, MessageContentPart::Text { text: self.text() });
    //        }
    //        MessageContent::Array(list)
    //    }
    // }
    public MessageContent messageContent() {
        if (isEmpty(medias)) {
            return MessageContent.Text(text);
        } else {
            List<MessageContentPart> list = new ArrayList<>();
            for (String url : medias) {
                list.add(MessageContentPart.ImageUrl(new ImageUrl().setUrl(url)));
            }
            if (!isBlank(text)) {
                list.addFirst(MessageContentPart.Text(text));
            }
            return MessageContent.Array(list);
        }
    }
}
