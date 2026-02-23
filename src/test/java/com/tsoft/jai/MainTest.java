package com.tsoft.jai;

import com.tsoft.jai.anyhow.Result;
import com.tsoft.jai.inquire.TestTerminal;
import com.tsoft.jai.reqwest.TestHttpClient;
import com.tsoft.jai.utils.Asset;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;

import static com.tsoft.jai.Main.main;
import static com.tsoft.jai.reqwest.TestHttpClient.getCapturedHttpRequests;
import static com.tsoft.jai.testutils.TestStringUtils.*;
import static org.assertj.core.api.Assertions.assertThat;

class MainTest {

    static final Result<File> CONFIG_DIR = Asset.file("configs/config1");
    static final Result<File> CONFIG_FILE = Asset.file("configs/config1/config.yaml");

    @BeforeAll
    static void beforeAll() {
        TestTerminal.init();
    }

    private String execute(String ... args) {
        // Add a config file reference
        System.setProperty("CONFIG_FILE_JAI", CONFIG_FILE.getValue().toString());

        // Start
        main(args);

        // Return the gathered output
        return normalize(TestTerminal.getOutput(), CONFIG_DIR.getValue().toString(), "<dir>");
    }

    @Test
    void main_list_models() {
        TestTerminal.newSession();

        assertThat(execute("--list-models")).isEqualTo("""
            ollama:lfm2.5-thinking
            ollama:deepseek-v3.2:cloud
            ollama:glm-4.7:cloud
            ollama:kimi-k2:1t-cloud
            ollama:minimax-m2:cloud
            ollama:qwen3-coder:480b-cloud
            """);
    }

    @Test
    void main_list_roles() {
        TestTerminal.newSession();

        assertThat(execute("--list-roles")).isEqualTo("""
            %code%
            %create-prompt%
            %create-title%
            %explain-shell%
            %functions%
            %shell%
            """);
    }

    @Test
    void main_start_and_list_sessions() {
        // Step 1
        TestTerminal.newSession();

        assertThat(execute("--list-sessions")).isEqualTo("\n");

        // Step 2
        TestTerminal.newSession();

        TestTerminal.prepareInput("Hello !");

        TestHttpClient.newSession();

        TestHttpClient.prepareResponses(
            """
            {
              "id": "chatcmpl-582",
              "object": "chat.completion.chunk",
              "created": 1769971085,
              "model": "lfm2.5-thinking",
              "system_fingerprint": "fp_ollama",
              "choices": [
                {
                  "index": 0,
                  "delta": {
                    "role": "assistant",
                    "content": "",
                    "reasoning": "Okay"
                  },
                  "finish_reason": null
                }
              ]
            }
            """,
            """
            {
              "id": "chatcmpl-582",
              "object": "chat.completion.chunk",
              "created": 1769971090,
              "model": "lfm2.5-thinking",
              "system_fingerprint": "fp_ollama",
              "choices": [
                {
                  "index": 0,
                  "delta": {
                    "role": "assistant",
                    "content": "I don't know"
                  },
                  "finish_reason": null
                }
              ]
            }
            """
        );

        assertThat(execute("--session", "test")).isEqualTo("\n\n");

        assertThat(getCapturedHttpRequests()).containsExactly(
            """
            {"model":"lfm2.5-thinking","messages":[{"role":"user","content":"Hello !"}],"stream":true}"""
        );

        assertThat(execute("--list-sessions")).isEqualTo("");
    }

    @Test
    void main_info() {
        TestTerminal.newSession();

        assertThat(execute("--info")).isEqualTo("""
            model                   ollama:lfm2.5-thinking
            temperature             null
            top_p                   null
            use_tools               null
            max_output_tokens       null
            save_session            false
            compress_threshold      4000
            rag_reranker_model      null
            rag_top_k               5
            dry_run                 false
            function_calling        true
            stream                  true
            save                    false
            keybindings             emacs
            wrap                    no
            wrap_code               false
            highlight               true
            theme                   null
            config_file             <dir>/config.yaml
            roles_dir               <dir>/roles
            sessions_dir            <dir>/sessions
            rags_dir                <dir>/rags
            macros_dir              <dir>/macros
            functions_dir           <dir>/functions
            messages_file           <dir>/messages.md
            """);
    }

    private String normalize(String value, String ... repls) {
        value = normalizePathSeparators(value);
        value = normalizeLineSeparators(value);
        if (repls == null || repls.length == 0){
            return value;
        }
        for (int i = 0; i < repls.length; i += 2) {
            value = value.replace(normalizePathSeparators(repls[i]), repls[i + 1]);
        }
        return value;
    }
}