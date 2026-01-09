package com.tsoft.jai.client.message;

public enum MessageRole {

    System,
    Assistant,
    User,
    Tool;

    public static boolean isSystem(MessageRole role) {
        return MessageRole.System.equals(role);
    }

    public static boolean isUser(MessageRole role) {
        return MessageRole.User.equals(role);
    }

    public static boolean isAssistant(MessageRole role) {
        return MessageRole.Assistant.equals(role);
    }
}
