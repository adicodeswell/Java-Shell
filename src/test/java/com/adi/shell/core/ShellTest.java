package com.adi.shell.core;

import com.adi.shell.command.CommandRegistry;
import com.adi.shell.command.ExternalCommandExecutor;
import com.adi.shell.parser.CommandParser;
import com.adi.shell.pipeline.PipelineExecutor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ShellTest {

    private Shell shell;

    @BeforeEach
    void setUp() {
        shell = new Shell();
    }

    @Test
    void constructor_initializesAllComponents() {
        assertNotNull(shell);
    }

    @Test
    void shell_hasRegisteredBuiltinCommands() throws Exception {
        java.lang.reflect.Field registryField = Shell.class.getDeclaredField("registry");
        registryField.setAccessible(true);
        CommandRegistry registry = (CommandRegistry) registryField.get(shell);

        assertNotNull(registry.get("exit"));
        assertNotNull(registry.get("echo"));
        assertNotNull(registry.get("type"));
        assertNotNull(registry.get("pwd"));
        assertNotNull(registry.get("cd"));
    }

    @Test
    void shell_hasParserInitialized() throws Exception {
        java.lang.reflect.Field parserField = Shell.class.getDeclaredField("parser");
        parserField.setAccessible(true);
        CommandParser parser = (CommandParser) parserField.get(shell);

        assertNotNull(parser);
    }

    @Test
    void shell_hasPipelineExecutorInitialized() throws Exception {
        java.lang.reflect.Field pipelineField = Shell.class.getDeclaredField("pipelineExecutor");
        pipelineField.setAccessible(true);
        PipelineExecutor pipelineExecutor = (PipelineExecutor) pipelineField.get(shell);

        assertNotNull(pipelineExecutor);
    }

    @Test
    void shell_hasExternalExecutorInitialized() throws Exception {
        java.lang.reflect.Field executorField = Shell.class.getDeclaredField("externalExecutor");
        executorField.setAccessible(true);
        ExternalCommandExecutor executor = (ExternalCommandExecutor) executorField.get(shell);

        assertNotNull(executor);
    }
}