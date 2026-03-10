package com.tsoft.jai.mods.session;

import com.tsoft.jai.mods.cli.struct.Cli;
import com.tsoft.jai.mods.config.struct.Config;
import com.tsoft.jai.mods.session.struct.Session;
import com.tsoft.jai.std.Result;

import static com.tsoft.jai.std.Result.Ok;

public final class SessionMod {

    public static Result<Session> loadOrCreate(Cli cli, Config cfg) {
        return Ok(new Session())
            .then(ses -> ses.setApiBase(cfg.getApiBase()))
            .then(ses -> ses.setModel(cfg.getModel()));
    }

    private SessionMod() { }
}
