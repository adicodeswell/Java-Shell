package com.adi.shell.command.builtin;

import com.adi.shell.command.Command;
import com.adi.shell.core.ShellContext;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

public class ExitCommand implements Command {

    @Override
    public void execute(String[] args, ShellContext context) {
        if (args.length > 1) System.exit(Integer.parseInt(args[1]));
        else System.exit(0);
    }

    @Override
    public void executeInPipeline(List<String> args, InputStream in, OutputStream out, ShellContext context) {
        // No-op in pipelines
    }
}
