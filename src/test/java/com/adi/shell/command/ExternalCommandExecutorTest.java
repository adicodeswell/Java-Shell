package com.adi.shell.command;

import com.adi.shell.core.ShellContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class ExternalCommandExecutorTest {

    private ExternalCommandExecutor executor;
    private ShellContext context;

    @BeforeEach
    void setUp() {
        executor = new ExternalCommandExecutor();
        context = new ShellContext();
    }

    @Test
    void execute_withEmptyCommandArrayDoesNothing() {
        String[] commands = {};

        assertDoesNotThrow(() -> {
            executor.execute(commands, context);
        });
    }

    @Test
    void execute_unknownCommandPrintsErrorMessage() {
        java.io.PrintStream originalOut = System.out;
        try {
            ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            System.setOut(new java.io.PrintStream(outStream));
            String[] commands = {"nonexistent_command_xyz"};

            executor.execute(commands, context);

            String output = outStream.toString();
            assertTrue(output.contains("nonexistent_command_xyz") && output.contains("command not found"));
        } finally {
            System.setOut(originalOut);
        }
    }

    @Test
    void execute_validCommandRunsSuccessfully() {
        String[] commands = {"echo", "hello"};

        assertDoesNotThrow(() -> {
            executor.execute(commands, context);
        });
    }

    @Test
    void execute_withRedirectionWritesToFile(@TempDir Path tempDir) throws IOException {
        Path outFile = tempDir.resolve("output.txt");
        String[] commands = {"echo", "test", "__REDIR__", outFile.toString()};

        executor.execute(commands, context);

        String content = Files.readString(outFile);
        assertTrue(content.contains("test"));
    }

    @Test
    void execute_withAppendRedirectionAppendsToFile(@TempDir Path tempDir) throws IOException {
        Path outFile = tempDir.resolve("output.txt");
        Files.writeString(outFile, "existing\n");

        String[] commands = {"echo", "appended", "__APPEND__", outFile.toString()};
        executor.execute(commands, context);

        String content = Files.readString(outFile);
        assertTrue(content.contains("existing") && content.contains("appended"));
    }

    @Test
    void execute_withStderrRedirectionWritesToFile(@TempDir Path tempDir) throws IOException {
        Path errFile = tempDir.resolve("error.txt");
        String[] commands = {"ls", "__REDIR_ERR__", errFile.toString()};

        assertDoesNotThrow(() -> {
            executor.execute(commands, context);
        });

        assertTrue(Files.exists(errFile));
    }
}