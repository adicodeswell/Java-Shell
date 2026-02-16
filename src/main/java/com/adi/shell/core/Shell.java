package com.adi.shell.core;

import com.adi.shell.command.Command;
import com.adi.shell.command.CommandRegistry;
import com.adi.shell.command.ExternalCommandExecutor;
import com.adi.shell.command.builtin.*;
import com.adi.shell.parser.CommandParser;
import com.adi.shell.pipeline.PipelineExecutor;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Shell {

    private final ShellContext context;
    private final CommandRegistry registry;
    private final CommandParser parser;
    private final PipelineExecutor pipelineExecutor;
    private final ExternalCommandExecutor externalExecutor;

    public Shell() {
        this.context = new ShellContext();
        this.registry = new CommandRegistry();
        this.parser = new CommandParser();

        registry.register("exit", new ExitCommand());
        registry.register("echo", new EchoCommand());
        registry.register("type", new TypeCommand(registry));
        registry.register("pwd", new PwdCommand());
        registry.register("cd", new CdCommand());

        this.pipelineExecutor = new PipelineExecutor(registry, context);
        this.externalExecutor = new ExternalCommandExecutor();
    }

    public void run() {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.print("$ ");
            if (!scanner.hasNextLine()) break;
            String input = scanner.nextLine();
            List<String> parsed = parser.parse(input);
            if (parsed.isEmpty()) continue;
            String[] commands = parsed.toArray(new String[0]);

            List<List<String>> pipeline = new ArrayList<>();
            List<String> current = new ArrayList<>();
            for (String token : parsed) {
                if (token.equals("|")) {
                    pipeline.add(new ArrayList<>(current));
                    current.clear();
                } else {
                    current.add(token);
                }
            }
            if (!current.isEmpty()) pipeline.add(current);

            if (pipeline.size() > 1) {
                pipelineExecutor.execute(pipeline);
                continue;
            }

            Command builtin = registry.get(commands[0]);
            if (builtin != null) {
                builtin.execute(commands, context);
            } else {
                externalExecutor.execute(commands, context);
            }
        }
    }
}
