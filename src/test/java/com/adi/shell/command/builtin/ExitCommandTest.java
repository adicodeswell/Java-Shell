package com.adi.shell.command.builtin;

import com.adi.shell.core.ShellContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ExitCommandTest {

    private ExitCommand exitCommand;
    private ShellContext context;

    @BeforeEach
    void setUp() {
        exitCommand = new ExitCommand();
        context = new ShellContext();
    }

    @org.junit.jupiter.api.Disabled("System.exit() kills the JVM")
    @Test
    void execute_withNoArgumentsExitsWithCodeZero() {
        String[] args = {"exit"};

        assertThrows(SecurityException.class, () -> {
            exitCommand.execute(args, context);
        });
    }

    @org.junit.jupiter.api.Disabled("System.exit() kills the JVM")
    @Test
    void execute_withExitCodeArgumentExitsWithThatCode() {
        String[] args = {"exit", "42"};

        assertThrows(SecurityException.class, () -> {
            exitCommand.execute(args, context);
        });
    }

    @Test
    void execute_withInvalidExitCodeThrowsNumberFormatException() {
        String[] args = {"exit", "not_a_number"};

        assertThrows(NumberFormatException.class, () -> {
            exitCommand.execute(args, context);
        });
    }

    @Test
    void executeInPipeline_isNoOp() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        List<String> args = List.of("exit", "0");

        assertDoesNotThrow(() -> {
            exitCommand.executeInPipeline(args, new ByteArrayInputStream(new byte[0]), out, context);
        });

        assertEquals("", out.toString());
    }
}