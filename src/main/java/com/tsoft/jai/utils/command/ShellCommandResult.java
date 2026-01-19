package com.tsoft.jai.utils.command;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import static com.tsoft.jai.utils.StringUtils.isBlank;

@Getter
@RequiredArgsConstructor
public class ShellCommandResult {

    private final Integer exitCode;
    private final String output;
    private final String error;

    public String stderrToStdout() {
        return isBlank(output) ? error :
            isBlank(error) ? output : output + "\n" + error;
    }
}
