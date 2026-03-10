package com.tsoft.jai;

import com.tsoft.jai.mods.config.ConfigMod;
import com.tsoft.jai.mods.repl.ReplMod;
import com.tsoft.jai.mods.session.SessionMod;
import com.tsoft.jai.std.Value;

import static com.tsoft.jai.std.Result.Ok;
import static com.tsoft.jai.user.terminal.TerminalUtils.println;

public class Main {

    static void main(String[] args) {
        Value ctx = new Value();

        Ok()
            .then(_ -> ConfigMod.loadOrCreate())
            .then(cfg -> ctx.set("config", cfg))
            .then(_ -> SessionMod.loadOrCreate())
            .then(ses -> ctx.set("session", ses))
            .then(_ -> ReplMod.start())
            .thenOrElse(
                ok -> println("Bye."),
                err -> println(err.toString()));
    }
}
