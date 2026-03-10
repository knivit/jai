package com.tsoft.jai;

import com.tsoft.jai.config.ConfigService;
import com.tsoft.jai.std.Result;

import static com.tsoft.jai.std.Result.isErr;
import static java.lang.IO.println;

public class Main {

    static void main(String[] args) {
        Result<?> res = ConfigService.loadOrCreate();
        if (isErr(res)) {
            println(res);
        }
    }
}
