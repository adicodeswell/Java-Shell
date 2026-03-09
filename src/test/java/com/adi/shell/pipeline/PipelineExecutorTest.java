package com.adi.shell.pipeline;

import com.adi.shell.command.CommandRegistry;
import com.adi.shell.command.builtin.EchoCommand;
import com.adi.shell.command.builtin.PwdCommand;
import com.adi.shell.core.ShellContext;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PipelineExecutorTest {

    private CommandRegistry registry;
    private ShellContext context;
    private PipelineExecutor pipelineExecutor;
    private PrintStream originalOut;
    private ByteArrayOutputStream outputCapture;
    private String originalUserDir;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        originalUserDir = System.getProperty("user.dir");
        registry = new CommandRegistry();
        registry.register("echo", new EchoCommand());
        registry.register("pwd", new PwdCommand());

        context = new ShellContext();
        context.setCurrentDir(tempDir.toFile());

        pipelineExecutor = new PipelineExecutor(registry, context);

        originalOut = System.out;
        outputCapture = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outputCapture));
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
        if (originalUserDir != null) {
            System.setProperty("user.dir", originalUserDir);
        }
    }

    @Test
    void singleBuiltinStageWritesToStdout() {
        List<List<String>> pipeline = List.of(
                Arrays.asList("echo", "hello", "world")
        );

        pipelineExecutor.execute(pipeline);

        String output = outputCapture.toString();
        assertEquals("hello world\n", output);
    }

    @Test
    void twoBuiltinStagesPipeline() {
        List<List<String>> pipeline = List.of(
                Arrays.asList("echo", "pipeline", "test"),
                Arrays.asList("echo", "final")
        );

        pipelineExecutor.execute(pipeline);

        String output = outputCapture.toString();
        assertEquals("final\n", output);
    }

    @Test
    void pwdInPipelineOutputsCurrentDir() {
        List<List<String>> pipeline = List.of(
                List.of("pwd")
        );

        pipelineExecutor.execute(pipeline);

        String output = outputCapture.toString().trim();
        assertEquals(tempDir.toFile().getAbsolutePath(), output);
    }

    @Test
    void externalCommandInPipeline() {
        List<List<String>> pipeline = List.of(
                Arrays.asList("echo", "hello"),
                Arrays.asList("/bin/cat")
        );

        pipelineExecutor.execute(pipeline);

        String output = outputCapture.toString();
        assertTrue(output.contains("hello"), "External cat should pass through echo output");
    }

    @Test
    void multiStageWithExternalCommands() {
        List<List<String>> pipeline = List.of(
                Arrays.asList("echo", "hello world"),
                Arrays.asList("/usr/bin/tr", "a-z", "A-Z")
        );

        pipelineExecutor.execute(pipeline);

        String output = outputCapture.toString().trim();
        assertEquals("HELLO WORLD", output);
    }

    @Test
    void emptyPipelineDoesNothing() {
        List<List<String>> pipeline = List.of();

        pipelineExecutor.execute(pipeline);

        assertEquals("", outputCapture.toString());
    }
}
