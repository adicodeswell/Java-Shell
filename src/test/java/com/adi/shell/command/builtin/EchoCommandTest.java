package com.adi.shell.command.builtin;

import com.adi.shell.core.ShellContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EchoCommandTest {

    private EchoCommand echoCommand;
    private ShellContext context;

    @BeforeEach
    void setUp() {
        echoCommand = new EchoCommand();
        context = new ShellContext();
    }

    // ── executeInPipeline tests ──────────────────────────────────────

    @Test
    void executeInPipeline_singleArgument() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        List<String> args = Arrays.asList("echo", "hello");

        echoCommand.executeInPipeline(args, new ByteArrayInputStream(new byte[0]), out, context);

        assertEquals("hello\n", out.toString());
    }

    @Test
    void executeInPipeline_multipleArgumentsJoinedWithSpaces() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        List<String> args = Arrays.asList("echo", "hello", "world", "foo");

        echoCommand.executeInPipeline(args, new ByteArrayInputStream(new byte[0]), out, context);

        assertEquals("hello world foo\n", out.toString());
    }

    @Test
    void executeInPipeline_outputEndsWithNewline() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        List<String> args = Arrays.asList("echo", "test");

        echoCommand.executeInPipeline(args, new ByteArrayInputStream(new byte[0]), out, context);

        String output = out.toString();
        assertEquals('\n', output.charAt(output.length() - 1));
    }

    @Test
    void executeInPipeline_noArgumentsOutputsOnlyNewline() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        List<String> args = List.of("echo");

        echoCommand.executeInPipeline(args, new ByteArrayInputStream(new byte[0]), out, context);

        assertEquals("\n", out.toString());
    }

    // ── execute with file redirection tests ──────────────────────────

    @Test
    void execute_redirectWritesToFile(@TempDir Path tempDir) throws IOException {
        Path outFile = tempDir.resolve("output.txt");

        String[] args = {"echo", "hello", "world", "__REDIR__", outFile.toString()};
        echoCommand.execute(args, context);

        String content = Files.readString(outFile);
        assertEquals("hello world\n", content);
    }

    @Test
    void execute_appendAppendsToFile(@TempDir Path tempDir) throws IOException {
        Path outFile = tempDir.resolve("output.txt");
        Files.writeString(outFile, "existing\n");

        String[] args = {"echo", "appended", "__APPEND__", outFile.toString()};
        echoCommand.execute(args, context);

        String content = Files.readString(outFile);
        assertEquals("existing\nappended\n", content);
    }
}
