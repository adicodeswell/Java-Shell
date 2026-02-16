package com.adi.shell.command;

import com.adi.shell.core.ShellContext;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

public interface Command {

    void execute(String[] args, ShellContext context);

    void executeInPipeline(List<String> args, InputStream in, OutputStream out, ShellContext context) throws IOException;
}
