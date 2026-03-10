package com.tsoft.jai;

import com.tsoft.jai.mods.cli.CliMod;
import com.tsoft.jai.mods.cli.struct.Cli;
import com.tsoft.jai.mods.config.ConfigMod;
import com.tsoft.jai.mods.config.struct.Config;
import com.tsoft.jai.mods.repl.ReplMod;
import com.tsoft.jai.mods.session.SessionMod;
import com.tsoft.jai.mods.session.struct.Session;
import com.tsoft.jai.std.ValueRef;

import static com.tsoft.jai.std.Result.Ok;
import static com.tsoft.jai.user.terminal.TerminalUtils.println;

public class Main {

    static void main(String[] args) {
        ValueRef<Cli> cliRef = new ValueRef<>(new Cli());
        ValueRef<Config> configRef = new ValueRef<>(new Config());
        ValueRef<Session> sessionRef = new ValueRef<>(new Session());

        Ok()
            .then(_ -> CliMod.parse(args))
            .then(cliRef::set)
            .then(_ -> ConfigMod.loadOrCreate(cliRef.get()))
            .then(configRef::set)
            .then(_ -> SessionMod.loadOrCreate(cliRef.get(), configRef.get()))
            .then(sessionRef::set)
            .then(_ -> ReplMod.start(cliRef.get(), configRef.get(), sessionRef.get()))
            .thenOrElse(
                _ -> println("Bye."),
                err -> println(err.toString()));
    }
}
