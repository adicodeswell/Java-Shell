package com.adi.shell.command;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class CommandRegistry {

    private final Map<String, Command> commands = new LinkedHashMap<>();

    public void register(String name, Command command) {
        commands.put(name, command);
    }

    public Command get(String name) {
        return commands.get(name);
    }

    public boolean isBuiltin(String name) {
        return commands.containsKey(name);
    }

    public Set<String> getBuiltinNames() {
        return commands.keySet();
    }
}
