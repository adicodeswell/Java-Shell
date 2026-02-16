package com.adi.shell.io;

import java.io.*;

public final class StreamUtils {

    private StreamUtils() {}

    /**
     * Copy all data from in -> out.
     * If closeOutAfter is true, attempt to close the out stream after copying (use for process stdin).
     * If closeOutAfter is false, do not close out (use for System.out).
     */
    public static void streamCopy(InputStream in, OutputStream out, boolean closeOutAfter) {
        try {
            byte[] buf = new byte[8192];
            int len;
            while ((len = in.read(buf)) != -1) {
                out.write(buf, 0, len);
                out.flush();
            }
        } catch (IOException ignored) {
            // ignore per POSIX-like behavior
        } finally {
            if (closeOutAfter) {
                try { out.close(); } catch (IOException ignored) {}
            }
        }
    }

    public static void writeToFile(String file, String content, boolean append) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file, append))) {
            bw.write(content);
            bw.flush();
        } catch (IOException e) {
            // do NOT print errors to stdout, silent fail per POSIX shell
        }
    }
}
