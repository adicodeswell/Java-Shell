package com.adi.shell.command.builtin;

import com.adi.shell.command.Command;
import com.adi.shell.core.ShellContext;
import com.adi.shell.io.StreamUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EchoCommand implements Command {

    @Override
    public void execute(String[] args, ShellContext context) {
        List<String> echoList = new ArrayList<>(Arrays.asList(args));

        boolean outAppend = false;
        String outFileTmp = null;

        boolean errAppend = false;
        String errFileTmp = null;

        boolean changed = true;
        while (changed && echoList.size() >= 2) {
            changed = false;
            String marker = echoList.get(echoList.size() - 2);
            String fname = echoList.get(echoList.size() - 1);
            if (marker.equals("__APPEND__")) {
                outAppend = true;
                outFileTmp = fname;
                echoList = echoList.subList(0, echoList.size() - 2);
                changed = true;
            } else if (marker.equals("__REDIR__")) {
                outFileTmp = fname;
                echoList = echoList.subList(0, echoList.size() - 2);
                changed = true;
            } else if (marker.equals("__APPEND_ERR__")) {
                errAppend = true;
                errFileTmp = fname;
                echoList = echoList.subList(0, echoList.size() - 2);
                changed = true;
            } else if (marker.equals("__REDIR_ERR__")) {
                errAppend = false;
                errFileTmp = fname;
                echoList = echoList.subList(0, echoList.size() - 2);
                changed = true;
            }
        }

        String[] commands = echoList.toArray(new String[0]);

        StringBuilder echoOut = new StringBuilder();
        for (int i = 1; i < commands.length; i++) {
            if (i > 1) echoOut.append(" ");
            echoOut.append(commands[i]);
        }
        echoOut.append("\n");

        if (errFileTmp != null) {
            File target = new File(errFileTmp);
            File parent = target.getParentFile();
            if (parent == null || parent.exists()) {
                try {
                    if (!target.exists()) {
                        target.createNewFile();
                    }
                } catch (IOException ignored) {}
            }
        }

        if (outFileTmp == null) {
            System.out.print(echoOut.toString());
        }

        if (outFileTmp != null) {
            File target = new File(outFileTmp);
            File parent = target.getParentFile();

            if (parent != null && !parent.exists()) {
                String nullDevice = System.getProperty("os.name").toLowerCase().contains("win") ? "NUL" : "/dev/null";
                try (FileWriter fw = new FileWriter(nullDevice, true)) {
                    fw.write(echoOut.toString());
                } catch (IOException ignored) {}
            } else {
                StreamUtils.writeToFile(outFileTmp, echoOut.toString(), outAppend);
            }
        }
    }

    @Override
    public void executeInPipeline(List<String> args, InputStream in, OutputStream out, ShellContext context) throws IOException {
        PrintWriter writer = new PrintWriter(new OutputStreamWriter(out));
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i < args.size(); i++) {
            if (i > 1) sb.append(" ");
            sb.append(args.get(i));
        }
        sb.append("\n");
        writer.print(sb.toString());
        writer.flush();
    }
}
