package com.adi.shell.command;

import static org.junit.jupiter.api.Assertions.*;

import com.adi.shell.core.ShellContext;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CommandRegistryTest {

    private CommandRegistry registry;

    /** Minimal stub implementation of Command used for testing. */
    private static class StubCommand implements Command {
        @Override
        public void execute(String[] args, ShellContext context) {
            // no-op
        }

        @Override
        public void executeInPipeline(List<String> args, InputStream in, OutputStream out, ShellContext context) throws IOException {
            // no-op
        }
    }

    @BeforeEach
    void setUp() {
        registry = new CommandRegistry();
    }

    @Test
    void registerAndGetReturnsTheCommand() {
        Command cmd = new StubCommand();
        registry.register("echo", cmd);

        assertSame(cmd, registry.get("echo"));
    }

    @Test
    void getReturnsNullForUnknownCommand() {
        assertNull(registry.get("nonexistent"));
    }

    @Test
    void isBuiltinReturnsTrueForRegisteredCommand() {
        registry.register("cd", new StubCommand());

        assertTrue(registry.isBuiltin("cd"));
    }

    @Test
    void isBuiltinReturnsFalseForUnknownCommand() {
        assertFalse(registry.isBuiltin("unknown"));
    }

    @Test
    void getBuiltinNamesReturnsAllRegisteredNames() {
        registry.register("cd", new StubCommand());
        registry.register("exit", new StubCommand());
        registry.register("help", new StubCommand());

        Set<String> names = registry.getBuiltinNames();

        assertEquals(3, names.size());
        assertTrue(names.contains("cd"));
        assertTrue(names.contains("exit"));
        assertTrue(names.contains("help"));
    }

    @Test
    void registeringSameNameOverwritesPreviousCommand() {
        Command first = new StubCommand();
        Command second = new StubCommand();

        registry.register("echo", first);
        registry.register("echo", second);

        assertSame(second, registry.get("echo"));
        assertNotSame(first, registry.get("echo"));
        assertEquals(1, registry.getBuiltinNames().size());
    }
}
