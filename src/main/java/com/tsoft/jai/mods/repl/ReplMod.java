package com.tsoft.jai.mods.repl;

import com.tsoft.jai.mods.cli.struct.Cli;
import com.tsoft.jai.mods.config.struct.Config;
import com.tsoft.jai.mods.provider.ProviderMod;
import com.tsoft.jai.mods.session.struct.Session;
import com.tsoft.jai.std.Result;
import com.tsoft.jai.std.ValueRef;
import com.tsoft.jai.user.UserInput;

import static com.tsoft.jai.std.Result.Ok;
import static com.tsoft.jai.std.Result.isErr;
import static com.tsoft.jai.user.terminal.TerminalUtils.readLine;

public final class ReplMod {

    public static Result<?> start(Cli cli, Config cfg, Session ses) {
        Result<?> res = Ok();
        ValueRef<Boolean> exit = new ValueRef<>(false);
        while (!exit.get() && !isErr(res)) {
            res = Ok()
                .then(_ -> readLine("> "))
                .then(UserInput::getMessage)
                .then(msg -> (Result<?>)switch (msg.trim().toLowerCase()) {
                    case "" -> Ok();
                    case ".exit", ".quit" -> exit.set(true);
                    default -> ProviderMod.chat(ses, msg);
                });
        }

        return res;
    }

    private ReplMod() { }
}
