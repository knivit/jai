package com.tsoft.jai.inquire.prompt;

import com.tsoft.jai.repl.prompt.ReplPrompt;
import lombok.*;
import lombok.experimental.Accessors;
import org.jline.reader.EndOfFileException;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.UserInterruptException;
import org.jline.terminal.Terminal;

@RequiredArgsConstructor
public class Editor {

    @Getter
    @Setter(AccessLevel.PRIVATE)
    @Accessors(chain = true)
    @RequiredArgsConstructor
    public static class Signal {

        public enum SignalEnum {
            Success,
            CtrlC,
            CtrlD
        }

        private final SignalEnum type;
        private String line;

        public static Signal Success(String line) {
            return new Signal(SignalEnum.Success).setLine(line);
        }

        public static Signal CtrlC() {
            return new Signal(SignalEnum.CtrlC);
        }

        public static Signal CtrlD() {
            return new Signal(SignalEnum.CtrlD);
        }
    }

    private final Terminal terminal;

    public Signal readLine(ReplPrompt prompt) {
        LineReader lineReader = LineReaderBuilder.builder()
            .terminal(terminal)
            .variable(LineReader.EDITING_MODE, "emacs")
            .build();

        try {
            String line = lineReader.readLine();
            return Signal.Success(line);
        } catch (UserInterruptException e) {
            return Signal.CtrlC();
        } catch (EndOfFileException e) {
            return Signal.CtrlD();
        }
    }
}
