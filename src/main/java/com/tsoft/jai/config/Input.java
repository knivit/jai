package com.tsoft.jai.config;

import com.tsoft.jai.client.message.MessageContentToolCalls;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.Map;

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
}
