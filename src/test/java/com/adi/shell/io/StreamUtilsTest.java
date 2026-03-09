package com.adi.shell.io;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class StreamUtilsTest {

    @Test
    void streamCopyCopiesAllBytesCorrectly() {
        byte[] data = "Hello, StreamUtils!".getBytes(StandardCharsets.UTF_8);
        ByteArrayInputStream in = new ByteArrayInputStream(data);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        StreamUtils.streamCopy(in, out, false);

        assertArrayEquals(data, out.toByteArray());
    }

    @Test
    void streamCopyWithCloseOutAfterTrueClosesOutputStream() {
        ByteArrayInputStream in = new ByteArrayInputStream("data".getBytes(StandardCharsets.UTF_8));
        CloseTrackingOutputStream out = new CloseTrackingOutputStream();

        StreamUtils.streamCopy(in, out, true);

        assertTrue(out.closed, "Output stream should be closed when closeOutAfter is true");
    }

    @Test
    void streamCopyWithCloseOutAfterFalseDoesNotCloseOutputStream() {
        ByteArrayInputStream in = new ByteArrayInputStream("data".getBytes(StandardCharsets.UTF_8));
        CloseTrackingOutputStream out = new CloseTrackingOutputStream();

        StreamUtils.streamCopy(in, out, false);

        assertFalse(out.closed, "Output stream should not be closed when closeOutAfter is false");
    }

    @Test
    void streamCopyHandlesEmptyInputStream() {
        ByteArrayInputStream in = new ByteArrayInputStream(new byte[0]);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        StreamUtils.streamCopy(in, out, false);

        assertEquals(0, out.toByteArray().length);
    }

    @Test
    void writeToFileCreatesNewFileWithContent(@TempDir Path tempDir) throws IOException {
        Path file = tempDir.resolve("newfile.txt");

        StreamUtils.writeToFile(file.toString(), "new content", false);

        assertTrue(Files.exists(file));
        assertEquals("new content", Files.readString(file, StandardCharsets.UTF_8));
    }

    @Test
    void writeToFileWithAppendFalseOverwritesExistingContent(@TempDir Path tempDir) throws IOException {
        Path file = tempDir.resolve("overwrite.txt");
        Files.writeString(file, "original content", StandardCharsets.UTF_8);

        StreamUtils.writeToFile(file.toString(), "replaced", false);

        assertEquals("replaced", Files.readString(file, StandardCharsets.UTF_8));
    }

    @Test
    void writeToFileWithAppendTrueAppendsToExistingContent(@TempDir Path tempDir) throws IOException {
        Path file = tempDir.resolve("append.txt");
        Files.writeString(file, "first", StandardCharsets.UTF_8);

        StreamUtils.writeToFile(file.toString(), " second", true);

        assertEquals("first second", Files.readString(file, StandardCharsets.UTF_8));
    }

    /** Helper output stream that tracks whether {@link #close()} was called. */
    private static class CloseTrackingOutputStream extends OutputStream {
        boolean closed = false;
        private final ByteArrayOutputStream delegate = new ByteArrayOutputStream();

        @Override
        public void write(int b) {
            delegate.write(b);
        }

        @Override
        public void write(byte[] b, int off, int len) {
            delegate.write(b, off, len);
        }

        @Override
        public void flush() {
            // no-op
        }

        @Override
        public void close() {
            closed = true;
        }
    }
}
