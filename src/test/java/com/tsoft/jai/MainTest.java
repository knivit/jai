package com.tsoft.jai;

import com.tsoft.jai.utils.Asset;
import com.tsoft.jai.utils.base.ListUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static com.tsoft.jai.Main.main;
import static com.tsoft.jai.inquire.Inquire.*;
import static com.tsoft.jai.testutils.TestStringUtils.*;
import static org.assertj.core.api.Assertions.assertThat;

class MainTest {

    static final String CONFIG_DIR = Asset.file("configs/config1").toString();
    static final String CONFIG_FILE = Asset.file("configs/config1/config.yaml").toString();

    @BeforeEach
    void beforeEach() {
        System.setProperty(JAI_DUMB_TERMINAL_MODE, "ON");
    }

    private String execute(String ... args) {
        List<String> list = ListUtils.of("--config-file", CONFIG_FILE);
        if (args != null) {
            list.addAll(Arrays.asList(args));
        }

        output.reset();
        main(list.toArray(new String[] { }));
        return normalize(output.toString(), CONFIG_DIR, "<dir>");
    }

    @Test
    void main_list_models() {
        assertThat(execute("--list-models")).isEqualTo("""
            ollama:deepseek-v3.2:cloud
            ollama:glm-4.7:cloud
            ollama:kimi-k2:1t-cloud
            ollama:minimax-m2:cloud
            ollama:qwen3-coder:480b-cloud
            """);
    }

    @Test
    void main_list_roles() {
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
        assertThat(execute("--list-sessions")).isEqualTo("\n");

        terminalInputStream.println("Hello !");
        assertThat(execute("--session", "test")).isEqualTo("");

        assertThat(execute("--list-sessions")).isEqualTo("");
    }

    @Test
    void main_info() {
        assertThat(execute("--info")).isEqualTo("""
            model                   ollama:deepseek-v3.2:cloud
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