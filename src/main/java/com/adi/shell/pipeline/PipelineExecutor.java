package com.adi.shell.pipeline;

import com.adi.shell.command.Command;
import com.adi.shell.command.CommandRegistry;
import com.adi.shell.core.ShellContext;
import com.adi.shell.io.StreamUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class PipelineExecutor {

    private final CommandRegistry registry;
    private final ShellContext context;

    public PipelineExecutor(CommandRegistry registry, ShellContext context) {
        this.registry = registry;
        this.context = context;
    }

    public void execute(List<List<String>> cmds) {
        List<Process> processes = new ArrayList<>();
        List<Thread> threads = new ArrayList<>();

        try {
            InputStream prevOut = null;

            for (int i = 0; i < cmds.size(); i++) {
                List<String> cmd = cmds.get(i);
                boolean isLast = (i == cmds.size() - 1);
                boolean isBuiltin = registry.isBuiltin(cmd.get(0));

                if (isBuiltin) {
                    Command command = registry.get(cmd.get(0));
                    if (isLast) {
                        command.executeInPipeline(cmd, prevOut == null ? new ByteArrayInputStream(new byte[0]) : prevOut, System.out, context);
                        prevOut = null;
                        continue;
                    } else {
                        ByteArrayOutputStream builtinOut = new ByteArrayOutputStream();
                        command.executeInPipeline(cmd, prevOut == null ? new ByteArrayInputStream(new byte[0]) : prevOut, builtinOut, context);
                        prevOut = new ByteArrayInputStream(builtinOut.toByteArray());
                        continue;
                    }
                }

                ProcessBuilder pb = new ProcessBuilder(cmd);
                pb.directory(context.getCurrentDir());
                pb.redirectErrorStream(true);
                Process p = pb.start();
                processes.add(p);

                if (prevOut != null) {
                    OutputStream stdin = p.getOutputStream();
                    InputStream finalPrevOut = prevOut;
                    Thread feeder = new Thread(() -> {
                        try {
                            StreamUtils.streamCopy(finalPrevOut, stdin, true);
                        } finally {
                            try { stdin.close(); } catch (IOException ignored) {}
                        }
                    });
                    feeder.start();
                    threads.add(feeder);
                } else {
                    try { p.getOutputStream().close(); } catch (IOException ignored) {}
                }

                prevOut = p.getInputStream();

                if (isLast) {
                    InputStream finalPrevOut = prevOut;
                    Thread outThread = new Thread(() -> {
                        StreamUtils.streamCopy(finalPrevOut, System.out, false);
                    });
                    outThread.start();
                    threads.add(outThread);
                }
            }

            for (Thread t : threads) {
                try { t.join(); } catch (InterruptedException ignored) { Thread.currentThread().interrupt(); }
            }

            for (Process p : processes) {
                try { p.waitFor(); } catch (InterruptedException ignored) { Thread.currentThread().interrupt(); }
            }

        } catch (IOException e) {
            // silent per POSIX behavior
        }
    }
}
