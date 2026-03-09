package com.adi.shell.command.builtin;

import com.adi.shell.command.Command;
import com.adi.shell.command.CommandRegistry;
import com.adi.shell.core.ShellContext;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TypeCommandTest {

    private CommandRegistry registry;
    private TypeCommand typeCommand;
    private ShellContext context;
    private PrintStream originalOut;
    private ByteArrayOutputStream outputCapture;

    private static class StubCommand implements Command {
        @Override
        public void execute(String[] args, ShellContext context) {}
        @Override
        public void executeInPipeline(List<String> args, InputStream in, OutputStream out, ShellContext context) throws IOException {}
    }

    @BeforeEach
    void setUp() {
        registry = new CommandRegistry();
        registry.register("echo", new StubCommand());
        registry.register("cd", new StubCommand());
        typeCommand = new TypeCommand(registry);
        context = new ShellContext();

        originalOut = System.out;
        outputCapture = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outputCapture));
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
    }

    @Test
    void execute_identifiesBuiltinCommand() {
        typeCommand.execute(new String[]{"type", "echo"}, context);

        String output = outputCapture.toString().trim();
        assertEquals("echo is a shell builtin", output);
    }

    @Test
    void execute_identifiesAnotherBuiltin() {
        typeCommand.execute(new String[]{"type", "cd"}, context);

        String output = outputCapture.toString().trim();
        assertEquals("cd is a shell builtin", output);
    }

    @Test
    void execute_notFoundForUnknownCommand() {
        typeCommand.execute(new String[]{"type", "nonexistent_cmd_xyz"}, context);

        String output = outputCapture.toString().trim();
        assertEquals("nonexistent_cmd_xyz: not found", output);
    }

    @Test
    void executeInPipeline_identifiesBuiltin() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        List<String> args = Arrays.asList("type", "echo");

        typeCommand.executeInPipeline(args, new ByteArrayInputStream(new byte[0]), out, context);

        String output = out.toString().trim();
        assertEquals("echo is a shell builtin", output);
    }

    @Test
    void executeInPipeline_notFoundForUnknown() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        List<String> args = Arrays.asList("type", "nonexistent_cmd_xyz");

        typeCommand.executeInPipeline(args, new ByteArrayInputStream(new byte[0]), out, context);

        String output = out.toString().trim();
        assertEquals("nonexistent_cmd_xyz: not found", output);
    }

    @Test
    void executeInPipeline_noArgsProducesNoOutput() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        List<String> args = List.of("type");

        typeCommand.executeInPipeline(args, new ByteArrayInputStream(new byte[0]), out, context);

        assertEquals("", out.toString());
    }
}
