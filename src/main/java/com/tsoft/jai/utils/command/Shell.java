package com.tsoft.jai.utils.command;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.Duration;
import java.util.concurrent.*;

@Data
@Accessors(chain = true)
public class Shell {

    private String name;
    private String cmd;
    private String arg;
}
