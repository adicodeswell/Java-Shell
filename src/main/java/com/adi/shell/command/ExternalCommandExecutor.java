package com.adi.shell.command;

import com.adi.shell.core.ShellContext;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ExternalCommandExecutor {

    public void execute(String[] commands, ShellContext context) {
        if (commands.length == 0) return;

        String cmd = commands[0];
        String path = System.getenv("PATH");
        if (path == null) path = "";
        String[] dirs = path.split(":");

        boolean redirectOut = false;
        boolean appendOut = false;
        String outFileName = null;

        boolean redirectErr = false;
        boolean appendErr = false;
        String errFileName = null;

        List<String> cmdList = new ArrayList<>();

        for (int i = 0; i < commands.length; ) {
            String token = commands[i];

            if (token.equals("__APPEND__") && i + 1 < commands.length) {
                appendOut = true;
                redirectOut = true;
                outFileName = commands[i + 1];
                i += 2;
                continue;
            }

            if (token.equals("__REDIR__") && i + 1 < commands.length) {
                appendOut = false;
                redirectOut = true;
                outFileName = commands[i + 1];
                i += 2;
                continue;
            }

            if (token.equals("__APPEND_ERR__") && i + 1 < commands.length) {
                appendErr = true;
                redirectErr = true;
                errFileName = commands[i + 1];
                i += 2;
                continue;
            }

            if (token.equals("__REDIR_ERR__") && i + 1 < commands.length) {
                appendErr = false;
                redirectErr = true;
                errFileName = commands[i + 1];
                i += 2;
                continue;
            }

            cmdList.add(token);
            i++;
        }

        File outTarget = (outFileName != null) ? new File(outFileName) : null;
        File errTarget = (errFileName != null) ? new File(errFileName) : null;

        try {
            ProcessBuilder pb = new ProcessBuilder(cmdList);
            pb.directory(context.getCurrentDir());

            configureStdoutRedirection(pb, redirectOut, appendOut, outTarget);
            configureStderrRedirection(pb, redirectErr, appendErr, errTarget);

            Process p = pb.start();
            p.waitFor();
            return;

        } catch (IOException e) {
            for (String dir : dirs) {
                if (dir.isEmpty()) continue;
                File file = new File(dir, cmd);
                if (file.exists() && file.canExecute()) {
                    try {
                        List<String> newCmd = new ArrayList<>(cmdList);
                        newCmd.set(0, file.getAbsolutePath());

                        ProcessBuilder pb2 = new ProcessBuilder(newCmd);
                        pb2.directory(context.getCurrentDir());

                        configureStdoutRedirection(pb2, redirectOut, appendOut, outTarget);
                        configureStderrRedirection(pb2, redirectErr, appendErr, errTarget);

                        Process p2 = pb2.start();
                        p2.waitFor();
                        return;
                    } catch (Exception ignored) {}
                }
            }
            System.out.println(cmd + ": command not found");
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(ex);
        }
    }

    private void configureStdoutRedirection(ProcessBuilder pb, boolean redirectOut, boolean appendOut, File outTarget) {
        if (redirectOut && outTarget != null) {
            File parent = outTarget.getParentFile();
            if (parent != null && !parent.exists()) {
                pb.redirectOutput(ProcessBuilder.Redirect.to(new File("/dev/null")));
            } else {
                if (appendOut) {
                    pb.redirectOutput(ProcessBuilder.Redirect.appendTo(outTarget));
                } else {
                    pb.redirectOutput(ProcessBuilder.Redirect.to(outTarget));
                }
            }
        } else {
            pb.redirectOutput(ProcessBuilder.Redirect.INHERIT);
        }
    }

    private void configureStderrRedirection(ProcessBuilder pb, boolean redirectErr, boolean appendErr, File errTarget) {
        if (redirectErr && errTarget != null) {
            File parent = errTarget.getParentFile();
            if (parent != null && !parent.exists()) {
                pb.redirectError(ProcessBuilder.Redirect.to(new File("/dev/null")));
            } else {
                try {
                    if (appendErr && !errTarget.exists()) {
                        errTarget.createNewFile();
                    }
                } catch (IOException ignored) {}

                if (appendErr) {
                    pb.redirectError(ProcessBuilder.Redirect.appendTo(errTarget));
                } else {
                    pb.redirectError(ProcessBuilder.Redirect.to(errTarget));
                }
            }
        } else {
            pb.redirectError(ProcessBuilder.Redirect.INHERIT);
        }
    }
}
