package com.tsoft.jai;

import com.tsoft.jai.utils.Asset;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.tsoft.jai.Main.main;
import static com.tsoft.jai.inquire.Inquire.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

class MainTest {

    @BeforeEach
    void beforeEach() {
        System.setProperty(JAI_DUMB_TERMINAL_MODE, "ON");
    }

    @Test
    void main_list_roles() {
        main(new String[] { "--config-file", Asset.file("configs/config1/config.yaml").toString(), "--list-roles"});
        
        assertEquals("""
            %code%
            %create-prompt%
            %create-title%
            %explain-shell%
            %functions%
            %shell%
            
            """, dumbOutput.toString());
    }

}