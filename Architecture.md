# Architecture — Java Mini Shell

## 1. Project Overview

**Java Mini Shell** (`mini-shell` v1.0.3) is a POSIX-compliant shell written in Java. It implements a Read-Eval-Print Loop (REPL) capable of executing built-in commands, launching external programs, handling I/O redirection, and chaining commands through pipelines.

The project originated from the [CodeCrafters "Build Your Own Shell"](https://codecrafters.io) challenge.

---

## 2. Tech Stack

| Component        | Technology              | Notes                                            |
| ---------------- | ----------------------- | ------------------------------------------------ |
| Language          | Java 25                 | Preview features enabled (`--enable-preview`)    |
| Build System      | Apache Maven            | Assembly plugin produces a fat JAR               |
| Terminal Library  | JLine 3.26.1            | Declared in `pom.xml` but **not yet integrated** |

---

## 3. Directory Structure

```
My-Shell-beta/
├── src/main/java/
│   ├── Main.java                                   # Thin entry point (no package)
│   └── com/adi/shell/
│       ├── core/
│       │   ├── Shell.java                          # REPL loop, wires all dependencies
│       │   └── ShellContext.java                    # Mutable state (current directory)
│       ├── parser/
│       │   └── CommandParser.java                  # Tokenizer & redirection marker parser
│       ├── command/
│       │   ├── Command.java                        # Interface for all commands
│       │   ├── CommandRegistry.java                # Map-based builtin registry
│       │   ├── ExternalCommandExecutor.java        # External process execution + PATH lookup
│       │   └── builtin/
│       │       ├── EchoCommand.java                # echo with inline redirection handling
│       │       ├── ExitCommand.java                # exit [code]
│       │       ├── TypeCommand.java                # type — identifies command origin
│       │       ├── PwdCommand.java                 # pwd — prints working directory
│       │       └── CdCommand.java                  # cd — changes directory
│       ├── pipeline/
│       │   └── PipelineExecutor.java               # Multi-stage pipeline engine
│       └── io/
│           └── StreamUtils.java                    # streamCopy + writeToFile utilities
├── bin/
│   └── Shell.bat                                   # Windows launcher
├── Shell.sh                                        # Unix launcher
├── your_program.sh                                 # CodeCrafters dev script
├── pom.xml                                         # Maven config
├── Architecture.md                                 # This file
├── TODO.md                                         # Improvement backlog
└── README.md                                       # User-facing docs
```

---

## 4. High-Level Architecture

The shell follows a **modular, package-based design** with clean separation of concerns. `Main.java` is a thin entry point that delegates to `Shell`, which wires all components together.

```
┌─────────┐
│  Main   │  (no package — thin entry point)
│ main()  │
└────┬────┘
     ▼
┌─────────────────────────────────────────────────────────┐
│                   Shell  (core)                          │
│  Wires: ShellContext, CommandRegistry, CommandParser,    │
│         PipelineExecutor, ExternalCommandExecutor        │
│                                                          │
│  REPL Loop:                                              │
│    input → parse → detect pipeline → dispatch            │
└────┬──────────┬──────────────┬──────────────────────────┘
     │          │              │
     ▼          ▼              ▼
┌─────────┐ ┌──────────┐ ┌─────────────────────┐
│ Command │ │ Pipeline │ │ External Command    │
│Registry │ │ Executor │ │ Executor            │
│         │ │          │ │                     │
│ echo    │ │ stage 1  │ │ ProcessBuilder      │
│ exit    │ │ stage 2  │ │ + PATH fallback     │
│ type    │ │  ...     │ │ + I/O redirection   │
│ pwd     │ │ stage N  │ │                     │
│ cd      │ │          │ │                     │
└─────────┘ └──────────┘ └─────────────────────┘
     │
     ▼
┌──────────────┐   ┌──────────────┐   ┌──────────────┐
│ ShellContext  │   │ CommandParser│   │ StreamUtils  │
│ (cwd state)  │   │ (tokenizer)  │   │ (I/O helpers)│
└──────────────┘   └──────────────┘   └──────────────┘
```

### Design Principles

- **Single Responsibility:** Each class has one clear purpose.
- **Open/Closed:** New builtins are added by implementing `Command` and registering in `Shell` — no existing code changes needed.
- **Dependency Inversion:** `TypeCommand` receives `CommandRegistry` via constructor injection rather than hardcoding builtin names.
- **Encapsulated State:** `ShellContext` holds mutable state (cwd) instead of static globals. Passed explicitly through method parameters.

---

## 5. Package Breakdown

### 5.1 `Main.java` (no package)

Thin entry point — creates a `Shell` instance and calls `run()`. Kept packageless to match `pom.xml`'s `<mainClass>Main</mainClass>`.

### 5.2 `com.adi.shell.core`

| Class          | Responsibility                                                          |
| -------------- | ----------------------------------------------------------------------- |
| `Shell`        | REPL loop, dependency wiring, command routing (builtin vs pipeline vs external) |
| `ShellContext`  | Holds mutable state: current working directory. Passed to all commands. |

**Shell constructor wiring:**
```
ShellContext → CommandRegistry → (registers all builtins)
                               → PipelineExecutor
                               → ExternalCommandExecutor
                               → CommandParser
```

### 5.3 `com.adi.shell.parser`

| Class           | Responsibility                                                              |
| --------------- | --------------------------------------------------------------------------- |
| `CommandParser` | Hand-written POSIX-like tokenizer. Handles quoting (single, double, backslash) and converts redirection operators into internal markers. |

**Redirection markers:**

| Operator     | Marker Token      |
| ------------ | ----------------- |
| `>` / `1>`   | `__REDIR__`       |
| `>>` / `1>>` | `__APPEND__`      |
| `2>`         | `__REDIR_ERR__`   |
| `2>>`        | `__APPEND_ERR__`  |

### 5.4 `com.adi.shell.command`

| Class                      | Responsibility                                                     |
| -------------------------- | ------------------------------------------------------------------ |
| `Command` (interface)       | Contract: `execute(args, context)` + `executeInPipeline(args, in, out, context)` |
| `CommandRegistry`           | `Map<String, Command>` — registers builtins, provides `isBuiltin()` and `get()` |
| `ExternalCommandExecutor`   | Runs external processes via `ProcessBuilder` with I/O redirection and PATH fallback |

### 5.5 `com.adi.shell.command.builtin`

Each builtin implements the `Command` interface:

| Class          | Command       | Notes                                                |
| -------------- | ------------- | ---------------------------------------------------- |
| `EchoCommand`  | `echo [args]` | Handles inline redirection markers for stdout/stderr |
| `ExitCommand`  | `exit [code]` | Calls `System.exit()`. No-op in pipelines.           |
| `TypeCommand`  | `type [cmd]`  | Receives `CommandRegistry` via constructor injection |
| `PwdCommand`   | `pwd`         | Reads `context.getCurrentDir()`                      |
| `CdCommand`    | `cd [path]`   | Modifies `context` — supports absolute, relative, `~` |

### 5.6 `com.adi.shell.pipeline`

| Class              | Responsibility                                                          |
| ------------------ | ----------------------------------------------------------------------- |
| `PipelineExecutor` | Executes multi-stage pipelines. Mixes builtins (in-process `ByteArrayOutputStream`) and external commands (`ProcessBuilder` with feeder threads). |

### 5.7 `com.adi.shell.io`

| Class         | Responsibility                                             |
| ------------- | ---------------------------------------------------------- |
| `StreamUtils` | Static utilities: `streamCopy()` (8 KB buffered) and `writeToFile()` (BufferedWriter) |

---

## 6. Data Flow

```
User Input (stdin)
       │
       ▼
 ┌──────────────┐
 │ Scanner.      │     Shell.run()
 │ nextLine()    │
 └──────┬───────┘
        ▼
 ┌──────────────┐     Tokens + Markers
 │CommandParser │────▶ ["ls", "__REDIR__", "out.txt"]
 │  .parse()    │      or ["echo", "hi", "|", "grep", "h"]
 └──────────────┘
        │
        ▼
 ┌──────────────────┐
 │ Pipeline Split   │  Split tokens on "|"
 │ (in Shell.run()) │
 └────────┬─────────┘
          │
    ┌─────┴─────┐
    ▼           ▼
 Single      Multi-stage
 Command     Pipeline
    │           │
    ▼           ▼
 Registry    PipelineExecutor.execute()
 .get()         │
    │           ├─ Builtin stage  → command.executeInPipeline()
    │           │    (ByteArray I/O for intermediate stages)
    │           │
    │           └─ External stage → ProcessBuilder
    │                (Thread-based stream piping)
    │
    ├─ builtin found → command.execute(args, context)
    │
    └─ null → ExternalCommandExecutor.execute(args, context)
                 (ProcessBuilder + PATH fallback)
        │
        ▼
 Output → stdout / stderr / file
```

---

## 7. Build & Run

### Build

```bash
mvn -B package
```

Produces: `target/mini-shell.jar` (fat JAR with dependencies via `maven-assembly-plugin`).

### Run

```bash
java --enable-preview -jar target/mini-shell.jar
```

### Launcher Scripts

| Script             | Platform | Description                                           |
| ------------------ | -------- | ----------------------------------------------------- |
| `Shell.sh`         | Unix     | Runs the JAR from the project directory                |
| `bin/Shell.bat`    | Windows  | Opens a new `cmd` window with color and title          |
| `your_program.sh`  | Unix     | CodeCrafters script: builds to `/tmp/` then runs       |

---

## 8. Remaining Limitations

- **No tests:** Zero unit or integration tests.
- **JLine unused:** The JLine dependency is declared but never imported — input is read via `java.util.Scanner`.
- **Duplicated redirection logic:** `EchoCommand` handles redirection markers independently from `ExternalCommandExecutor`. A shared redirection module would reduce duplication.
- **Unix-only assumptions:** Hardcoded `/dev/null` and `:` as PATH separator won't work on Windows.

See [TODO.md](TODO.md) for the full improvement backlog.
