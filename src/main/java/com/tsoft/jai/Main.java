package com.tsoft.jai;

import com.tsoft.jai.config.ConfigService;

public class Main {

    static void main(String[] args) {
        ConfigService.loadOrCreate();
    }
}
