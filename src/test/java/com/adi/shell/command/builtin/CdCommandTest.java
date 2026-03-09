package com.adi.shell.command.builtin;

import com.adi.shell.core.ShellContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class CdCommandTest {

    private CdCommand cdCommand;
    private ShellContext context;
    private PrintStream originalOut;
    private ByteArrayOutputStream outputCapture;
    private String originalUserDir;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        originalUserDir = System.getProperty("user.dir");
        cdCommand = new CdCommand();
        context = new ShellContext();
        context.setCurrentDir(tempDir.toFile());

        originalOut = System.out;
        outputCapture = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outputCapture));
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
        System.setProperty("user.dir", originalUserDir);
    }

    @Test
    void cdWithNoArgsDoesNothing() {
        File before = context.getCurrentDir();
        cdCommand.execute(new String[]{"cd"}, context);
        assertEquals(before, context.getCurrentDir());
    }

    @Test
    void cdToAbsolutePathChangesDirectory() throws IOException {
        Path subDir = Files.createDirectory(tempDir.resolve("absoluteTest"));
        String absolutePath = subDir.toFile().getCanonicalPath();

        cdCommand.execute(new String[]{"cd", absolutePath}, context);

        assertEquals(subDir.toFile().getCanonicalFile(), context.getCurrentDir());
    }

    @Test
    void cdToRelativePathChangesDirectory() throws IOException {
        Path subDir = Files.createDirectory(tempDir.resolve("relativeTest"));

        cdCommand.execute(new String[]{"cd", "relativeTest"}, context);

        assertEquals(subDir.toFile().getCanonicalFile(), context.getCurrentDir());
    }

    @Test
    void cdToTildeChangesToHomeDirectory() throws IOException {
        String home = System.getenv("HOME");
        if (home == null) home = System.getProperty("user.home");

        cdCommand.execute(new String[]{"cd", "~"}, context);

        assertEquals(new File(home).getCanonicalFile(), context.getCurrentDir());
    }

    @Test
    void cdToNonexistentDirectoryPrintsErrorAndDoesNotChangeDir() {
        File before = context.getCurrentDir();

        cdCommand.execute(new String[]{"cd", "nonexistent"}, context);

        assertEquals(before, context.getCurrentDir());
        String output = outputCapture.toString().trim();
        assertTrue(output.contains("cd: nonexistent: No such file or directory"));
    }

    @Test
    void cdToFilePrintsError() throws IOException {
        Path file = Files.createFile(tempDir.resolve("aFile.txt"));
        File before = context.getCurrentDir();

        cdCommand.execute(new String[]{"cd", file.toFile().getCanonicalPath()}, context);

        assertEquals(before, context.getCurrentDir());
        String output = outputCapture.toString().trim();
        assertTrue(output.contains("No such file or directory"));
    }
}
