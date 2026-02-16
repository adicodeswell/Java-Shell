package com.adi.shell.command.builtin;

import com.adi.shell.command.Command;
import com.adi.shell.command.CommandRegistry;
import com.adi.shell.core.ShellContext;

import java.io.*;
import java.util.List;

public class TypeCommand implements Command {

    private final CommandRegistry registry;

    public TypeCommand(CommandRegistry registry) {
        this.registry = registry;
    }

    @Override
    public void execute(String[] args, ShellContext context) {
        if (registry.isBuiltin(args[1])) {
            System.out.println(args[1] + " is a shell builtin");
            return;
        }
        String path = System.getenv("PATH");
        if (path == null) path = "";
        String[] dirs = path.split(":");

        for (String dir : dirs) {
            if (dir.isEmpty()) continue;
            File file = new File(dir, args[1]);
            if (file.exists() && file.canExecute()) {
                System.out.println(args[1] + " is " + file.getAbsolutePath());
                return;
            }
        }
        System.out.println(args[1] + ": not found");
    }

    @Override
    public void executeInPipeline(List<String> args, InputStream in, OutputStream out, ShellContext context) throws IOException {
        PrintWriter writer = new PrintWriter(new OutputStreamWriter(out));
        if (args.size() < 2) return;
        String arg = args.get(1);
        if (registry.isBuiltin(arg)) {
            writer.println(arg + " is a shell builtin");
        } else {
            String path = System.getenv("PATH");
            if (path == null) path = "";
            boolean found = false;
            for (String dir : path.split(":")) {
                File file = new File(dir, arg);
                if (file.exists() && file.canExecute()) {
                    writer.println(arg + " is " + file.getAbsolutePath());
                    found = true;
                    break;
                }
            }
            if (!found) writer.println(arg + ": not found");
        }
        writer.flush();
    }
}
