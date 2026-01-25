package com.tsoft.jai.inquire.prompt;

import lombok.RequiredArgsConstructor;
import org.jline.terminal.Terminal;

import java.util.function.Supplier;

@RequiredArgsConstructor
public class Spinner {

    private final Terminal terminal;
    private volatile boolean active = false;
    private Thread spinnerThread;

    /**
     * // Usage example
     * Spinner spinner = new Spinner();
     * spinner.runWithSpinner("Processing files", () -> {
     *     // Your long-running task here
     *     processFiles();
     * });
     * spinner.close();
     */

    /**
     * Run a task with a spinner
     * @param message The message to display with spinner
     * @param task The task to execute
     * @param <T> Task result type
     * @return Task result
     */
    public <T> T runWithSpinner(String message, Supplier<T> task) {
        start(message);
        try {
            return task.get();
        } finally {
            stop();
        }
    }

    /**
     * Run a task with a spinner (void version)
     * @param message The message to display with spinner
     * @param task The task to execute
     */
    public void runWithSpinner(String message, Runnable task) {
        start(message);
        try {
            task.run();
        } finally {
            stop();
        }
    }

    private void start(String message) {
        if (active) {
            return;
        }

        active = true;
        spinnerThread = new Thread(() -> {
            String[] frames = {"⠋", "⠙", "⠹", "⠸", "⠼", "⠴", "⠦", "⠧", "⠇", "⠏"};

            try {
                terminal.writer().write("\033[?25l"); // Hide cursor
                terminal.flush();

                int frameIndex = 0;
                while (active) {
                    String frame = frames[frameIndex % frames.length];

                    // Move cursor to beginning of line and clear
                    terminal.writer().write("\r\033[K");
                    terminal.writer().write(frame + " " + message);
                    terminal.flush();

                    frameIndex++;
                    Thread.sleep(80);
                }

                // Clear line
                terminal.writer().write("\r\033[K");
                terminal.flush();

            } catch (Exception e) {
                // Ignore on shutdown
            }
        });

        spinnerThread.start();
    }

    private void stop() {
        active = false;
        try {
            if (spinnerThread != null) {
                spinnerThread.join(100);
            }

            terminal.writer().write("\033[?25h"); // Show cursor
            terminal.flush();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void close() throws Exception {
        stop();
        terminal.close();
    }
}
