package com.adi.shell.command.builtin;

import com.adi.shell.core.ShellContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.nio.file.Path;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PwdCommandTest {

    private PwdCommand pwdCommand;
    private ShellContext context;
    private String originalUserDir;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        originalUserDir = System.getProperty("user.dir");
        pwdCommand = new PwdCommand();
        context = new ShellContext();
        context.setCurrentDir(tempDir.toFile());
    }

    @AfterEach
    void tearDown() {
        System.setProperty("user.dir", originalUserDir);
    }

    @Test
    void executeInPipeline_writesCurrentDirectoryPathToOutputStream() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        pwdCommand.executeInPipeline(Collections.emptyList(), new ByteArrayInputStream(new byte[0]), out, context);

        String output = out.toString().trim();
        assertEquals(tempDir.toFile().getAbsolutePath(), output);
    }

    @Test
    void executeInPipeline_writesPathEndingWithNewline() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        pwdCommand.executeInPipeline(Collections.emptyList(), new ByteArrayInputStream(new byte[0]), out, context);

        String output = out.toString();
        assertTrue(output.endsWith(System.lineSeparator()), "Output should end with a newline");
    }

    @Test
    void execute_printsCurrentDirectoryToSystemOut() {
        PrintStream originalOut = System.out;
        try {
            ByteArrayOutputStream capture = new ByteArrayOutputStream();
            System.setOut(new PrintStream(capture));

            pwdCommand.execute(new String[]{}, context);

            String output = capture.toString().trim();
            assertEquals(tempDir.toFile().getAbsolutePath(), output);
        } finally {
            System.setOut(originalOut);
        }
    }

    @Test
    void execute_reflectsNewDirectoryAfterSetCurrentDir(@TempDir Path newTempDir) throws Exception {
        File newDir = newTempDir.toFile();
        context.setCurrentDir(newDir);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        pwdCommand.executeInPipeline(Collections.emptyList(), new ByteArrayInputStream(new byte[0]), out, context);

        String output = out.toString().trim();
        assertEquals(newDir.getAbsolutePath(), output);
    }
}
