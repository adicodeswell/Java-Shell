package com.adi.shell.core;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class ShellContextTest {

    private ShellContext context;
    private String originalUserDir;

    @BeforeEach
    void setUp() {
        originalUserDir = System.getProperty("user.dir");
        context = new ShellContext();
    }

    @AfterEach
    void tearDown() {
        System.setProperty("user.dir", originalUserDir);
    }

    @Test
    void defaultConstructorUsesUserDir() {
        File expected = new File(System.getProperty("user.dir"));
        assertEquals(expected, context.getCurrentDir());
    }

    @Test
    void getCurrentDirReturnsCorrectValue() {
        File currentDir = context.getCurrentDir();
        assertNotNull(currentDir);
        assertTrue(currentDir.isAbsolute());
    }

    @Test
    void setCurrentDirUpdatesDir(@TempDir Path tempDir) {
        File newDir = tempDir.toFile();
        context.setCurrentDir(newDir);
        assertEquals(newDir, context.getCurrentDir());
    }

    @Test
    void setCurrentDirUpdatesSystemProperty(@TempDir Path tempDir) {
        File newDir = tempDir.toFile();
        context.setCurrentDir(newDir);
        assertEquals(newDir.getAbsolutePath(), System.getProperty("user.dir"));
    }

    @Test
    void setCurrentDirMultipleTimesRetainsLatest(@TempDir Path tempDir) {
        File firstDir = tempDir.resolve("first").toFile();
        firstDir.mkdir();
        File secondDir = tempDir.resolve("second").toFile();
        secondDir.mkdir();

        context.setCurrentDir(firstDir);
        assertEquals(firstDir, context.getCurrentDir());

        context.setCurrentDir(secondDir);
        assertEquals(secondDir, context.getCurrentDir());
        assertEquals(secondDir.getAbsolutePath(), System.getProperty("user.dir"));
    }
}
