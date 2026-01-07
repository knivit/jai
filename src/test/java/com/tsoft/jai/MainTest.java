package com.tsoft.jai;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URISyntaxException;
import java.nio.file.Paths;

import static com.tsoft.jai.Main.main;
import static com.tsoft.jai.inquire.Inquire.JAI_DUMB_TERMINAL_MODE;
import static org.junit.jupiter.api.Assertions.*;

class MainTest {

    @BeforeEach
    void beforeEach() {
        System.setProperty(JAI_DUMB_TERMINAL_MODE, "ON");
    }

    @Test
    void main_list_roles() throws URISyntaxException {
        main(new String[] { "--config-file", Paths.get(getClass().getResource("/configs/config1.yaml").toURI()).toString(), "--list-roles"});
    }

}