package com.tsoft.jai.inquire.spinner;

import com.tsoft.jai.anyhow.Result;
import lombok.Data;

import static com.tsoft.jai.anyhow.Result.Ok;
import static com.tsoft.jai.inquire.Inquire.IS_STDOUT_TERMINAL;
import static com.tsoft.jai.inquire.Inquire.terminal;
import static com.tsoft.jai.utils.base.StringUtils.*;

@Data
public class SpinnerInner {

    // const DATA: [&'static str; 10] = ["⠋", "⠙", "⠹", "⠸", "⠼", "⠴", "⠦", "⠧", "⠇", "⠏"];
    private static final String[] DATA = {"⠋", "⠙", "⠹", "⠸", "⠼", "⠴", "⠦", "⠧", "⠇", "⠏"};

    private int index;
    private String message;

    public static SpinnerInner getDefault() {
        return new SpinnerInner();
    }

    // fn step(&mut self) -> Result<()> {
    //    if !*IS_STDOUT_TERMINAL || self.message.is_empty() {
    //        return Ok(());
    //    }
    //    let mut writer = stdout();
    //    let frame = Self::DATA[self.index % Self::DATA.len()];
    //    let dots = ".".repeat((self.index / 5) % 4);
    //    let line = format!("{frame}{}{:<3}", self.message, dots);
    //    queue!(writer, cursor::MoveToColumn(0), style::Print(line),)?;
    //    if self.index == 0 {
    //        queue!(writer, cursor::Hide)?;
    //    }
    //    writer.flush()?;
    //    self.index += 1;
    //    Ok(())
    // }
    public Result<?> step() {
        if (!IS_STDOUT_TERMINAL || isBlank(message)) {
            return Ok();
        }
        String frame = DATA[index % DATA.length];
        String dots = repeat(".", (index / 5) % 4);
        String line = format("{}{}{}", frame, message, dots.substring(0, Math.min(dots.length(), 3)));

        // Move cursor to beginning of line and clear
        terminal.writer().write("\r\033[K");
        terminal.writer().write(line);
        terminal.flush();

        if (index == 0) {
            hideCursor();
        }
        terminal.flush();
        index += 1;
        return Ok();
    }

    public void showCursor() {
        terminal.writer().write("\033[?25h");
    }

    public void hideCursor() {
        terminal.writer().write("\033[?25l");
    }

    // fn clear_message(&mut self) -> Result<()> {
    //    if !*IS_STDOUT_TERMINAL || self.message.is_empty() {
    //        return Ok(());
    //    }
    //    self.message.clear();
    //    let mut writer = stdout();
    //    queue!(
    //        writer,
    //        cursor::MoveToColumn(0),
    //        terminal::Clear(terminal::ClearType::FromCursorDown),
    //        cursor::Show
    //    )?;
    //    writer.flush()?;
    //    Ok(())
    // }
    public Result<?> clearMessage() {
        if (!IS_STDOUT_TERMINAL || isBlank(message)) {
            return Ok();
        }
        message = "";

        // Move cursor to beginning of line and clear
        terminal.writer().write("\r\033[K");
        terminal.flush();
        return Ok();
    }
}
