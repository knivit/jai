package com.tsoft.jai.config;

public final class Mod {

    public static final String SUMMARIZE_PROMPT =
        "Summarize the discussion briefly in 200 words or less to use as a prompt for future context.";
    public static final String SUMMARY_PROMPT = "This is a summary of the chat history as a recap: ";

    public static final String RAG_TEMPLATE = """
        Answer the query based on the context while respecting the rules. (user query, some textual context and rules, all inside xml tags)

        <context>
        __CONTEXT__
        </context>

        <rules>
        - If you don't know, just say so.
        - If you are not sure, ask for clarification.
        - Answer in the same language as the user query.
        - If the context appears unreadable or of poor quality, tell the user then answer as best as you can.
        - If the answer is not in the context but you think you know the answer, explain that to the user then answer with your own knowledge.
        - Answer directly and without using xml tags.
        </rules>

        <user_query>
        __INPUT__
        </user_query>
        """;

    private Mod() { }
}
