package com.tsoft.jai.mods.session;

import com.tsoft.jai.mods.session.dto.Session;
import com.tsoft.jai.std.Result;

import static com.tsoft.jai.std.Result.Ok;

public final class SessionMod {

    public static Result<Session> loadOrCreate() {
        return Ok();
    }

    private SessionMod() { }
}
