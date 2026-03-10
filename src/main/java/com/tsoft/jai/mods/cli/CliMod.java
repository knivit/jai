package com.tsoft.jai.mods.cli;

import com.tsoft.jai.mods.cli.struct.Cli;
import com.tsoft.jai.std.Result;

import static com.tsoft.jai.std.Result.Ok;

public final class CliMod {

    public static Result<Cli> parse(String[] args) {
        return Ok()
            .then(_ -> Ok(new Cli()));
    }

    private CliMod() { }
}
