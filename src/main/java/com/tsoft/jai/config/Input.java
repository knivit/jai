package com.tsoft.jai.config;

import com.tsoft.jai.client.Client;
import com.tsoft.jai.client.message.ImageUrl;
import com.tsoft.jai.client.message.MessageContent;
import com.tsoft.jai.client.message.MessageContentPart;
import com.tsoft.jai.client.message.MessageContentToolCalls;
import com.tsoft.jai.function.ToolResult;
import com.tsoft.jai.rag.Rag;
import com.tsoft.jai.utils.AbortSignal;
import com.tsoft.jai.utils.CollectionsUtils;
import com.tsoft.jai.utils.Triple;
import com.tsoft.jai.utils.Tuple;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.tsoft.jai.client.macros.Macros.initClient;
import static com.tsoft.jai.utils.StringUtils.isBlank;

@Data
@Accessors(chain = true)
public class Input implements Cloneable {

    private Config config;
    private String text;
    private Tuple<String, List<String>> raw;
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

    // pub fn from_str(config: &GlobalConfig, text: &str, role: Option<Role>) -> Self {
    //    let (role, with_session, with_agent) = resolve_role(&config.read(), role);
    //    Self {
    //        config: config.clone(),
    //        text: text.to_string(),
    //        raw: (text.to_string(), vec![]),
    //        patched_text: None,
    //        last_reply: None,
    //        continue_output: None,
    //        regenerate: false,
    //        medias: Default::default(),
    //        data_urls: Default::default(),
    //        tool_calls: None,
    //        role,
    //        rag_name: None,
    //        with_session,
    //        with_agent,
    //    }
    // }
    public static Input fromStr(Config config, String text, Role role) {
        Triple<Role, Boolean, Boolean> triple = resolveRole(config, role);
        role = triple.first();
        boolean withSession = triple.second();
        boolean withAgent = triple.third();
        return new Input()
            .setConfig(config)
            .setText(text)
            .setRaw(new Tuple<>(text, new ArrayList<>()))
            .setRole(role)
            .setWithSession(withSession)
            .setWithAgent(withAgent);
    }

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
        if (CollectionsUtils.isEmpty(medias)) {
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

    // pub fn merge_tool_results(mut self, output: String, tool_results: Vec<ToolResult>) -> Self {
    //    match self.tool_calls.as_mut() {
    //        Some(exist_tool_results) => {
    //            exist_tool_results.merge(tool_results, output);
    //        }
    //        None => self.tool_calls = Some(MessageContentToolCalls::new(tool_results, output)),
    //    }
    //    self
    // }
    public Input mergeToolResults(String output, List<ToolResult> toolResults) {
        if (toolCalls != null) {
            toolCalls.merge(toolResults, output);
        } else {
            toolCalls = new MessageContentToolCalls().setToolResults(toolResults).setText(output);
        }
        return this;
    }

    // pub fn create_client(&self) -> Result<Box<dyn Client>> {
    //    init_client(&self.config, Some(self.role().model().clone()))
    // }
    public Client createClient() {
        return initClient(config, role.getModel());
    }

    // pub fn is_empty(&self) -> bool {
    //    self.text.is_empty() && self.medias.is_empty()
    // }
    public boolean isEmpty() {
        return isBlank(text) && CollectionsUtils.isEmpty(medias);
    }

    // pub fn stream(&self) -> bool {
    //    self.config.read().stream && !self.role().model().no_stream()
    // }
    public boolean stream() {
        return config.isStream() && (role != null && role.getModel() != null && !role.getModel().noStream());
    }

    // fn resolve_role(config: &Config, role: Option<Role>) -> (Role, bool, bool) {
    //    match role {
    //        Some(v) => (v, false, false),
    //        None => (
    //            config.extract_role(),
    //            config.session.is_some(),
    //            config.agent.is_some(),
    //        ),
    //    }
    // }
    private static Triple<Role, Boolean, Boolean> resolveRole(Config config, Role role) {
        if (role != null) {
            return new Triple<>(role, false, false);
        } else {
            return new Triple<>(config.extractRole(), config.getSession() != null, config.getAgent() != null);
        }
    }

    @Override
    public Input clone() {
        try {
            Input clone = (Input) super.clone();
            // TODO: copy mutable state here, so the clone can't change the internals of the original
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
