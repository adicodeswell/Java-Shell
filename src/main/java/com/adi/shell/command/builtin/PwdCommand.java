package com.adi.shell.command.builtin;

import com.adi.shell.command.Command;
import com.adi.shell.core.ShellContext;

import java.io.*;
import java.util.List;

public class PwdCommand implements Command {

    @Override
    public void execute(String[] args, ShellContext context) {
        System.out.println(context.getCurrentDir().getAbsolutePath());
    }

    @Override
    public void executeInPipeline(List<String> args, InputStream in, OutputStream out, ShellContext context) throws IOException {
        PrintWriter writer = new PrintWriter(new OutputStreamWriter(out));
        writer.println(context.getCurrentDir().getAbsolutePath());
        writer.flush();
    }
}
