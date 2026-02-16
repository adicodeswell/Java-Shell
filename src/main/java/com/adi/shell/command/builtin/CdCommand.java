package com.adi.shell.command.builtin;

import com.adi.shell.command.Command;
import com.adi.shell.core.ShellContext;

import java.io.*;
import java.util.List;

public class CdCommand implements Command {

    @Override
    public void execute(String[] args, ShellContext context) {
        if (args.length < 2) {
            return;
        }

        String path = args[1];
        File newDir;

        if (path.startsWith("/")) {
            newDir = new File(path);
        } else if (path.startsWith("~")) {
            String home = System.getenv("HOME");
            if (home == null) home = System.getProperty("user.home");
            newDir = new File(home);
        } else {
            newDir = new File(context.getCurrentDir(), path);
        }

        try {
            if (newDir.exists() && newDir.isDirectory()) {
                context.setCurrentDir(newDir.getCanonicalFile());
            } else {
                System.out.println("cd: " + path + ": No such file or directory");
            }
        } catch (IOException e) {
            System.out.println("cd: " + path + ": No such file or directory");
        }
    }

    @Override
    public void executeInPipeline(List<String> args, InputStream in, OutputStream out, ShellContext context) {
        // Do nothing in pipeline context
    }
}
