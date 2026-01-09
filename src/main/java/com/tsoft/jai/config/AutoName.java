package com.tsoft.jai.config;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class AutoName {

    private boolean naming;
    private String chatHistory;
    private String name;

    // pub fn new_from_chat_history(chat_history: String) -> Self {
    //    Self {
    //        chat_history: Some(chat_history),
    //        ..Default::default()
    //    }
    // }
    public static AutoName newFromChatHistory(String chatHistory) {
        return new AutoName()
            .setChatHistory(chatHistory);
    }
}
