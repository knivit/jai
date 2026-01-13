package com.tsoft.jai.config;

import com.tsoft.jai.client.message.ImageUrl;
import com.tsoft.jai.client.message.MessageContent;
import com.tsoft.jai.client.message.MessageContentPart;
import com.tsoft.jai.client.message.MessageContentToolCalls;
import com.tsoft.jai.rag.Rag;
import com.tsoft.jai.utils.AbortSignal;
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

    // pub async fn use_embeddings(&mut self, abort_signal: AbortSignal) -> Result<()> {
    //    if self.text.is_empty() {
    //        return Ok(());
    //    }
    //    let rag = self.config.read().rag.clone();
    //    if let Some(rag) = rag {
    //        let result = Config::search_rag(&self.config, &rag, &self.text, abort_signal).await?;
    //        self.patched_text = Some(result);
    //        self.rag_name = Some(rag.name().to_string());
    //    }
    //    Ok(())
    // }
    public void useEmbeddings(AbortSignal abortSignal) {
        if (isBlank(text)) {
            return;
        }
        Rag rag = config.getRag();
        if (rag != null) {
            patchedText = Config.searchRag(config, rag, text, abortSignal);
            ragName = rag.getName();
        }
    }
}
