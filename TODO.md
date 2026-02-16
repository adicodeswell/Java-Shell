# TODO — Java Mini Shell

## 🔴 Critical

- [ ] **Integrate JLine library** — JLine 3.26.1 is declared in `pom.xml` but completely unused; replace `Scanner` with JLine `LineReader` for proper terminal handling, line editing, and signal support.
- [ ] **Add unit tests** — Zero test coverage currently; add JUnit 5 and write tests for the parser (`parseCommand`), built-in commands, redirection logic, and pipeline execution.
- [ ] **Remove dead code** — `printCNF()` method is declared but never called anywhere.

## 🟠 High Priority — Architecture

- [ ] **Break up monolithic `Main.java`** — Extract into a proper package structure:
  - `com.adi.shell.core` — Shell REPL loop and main entry point
  - `com.adi.shell.parser` — Command tokenizer and redirection parser
  - `com.adi.shell.command` — Command interface + individual builtin implementations
  - `com.adi.shell.pipeline` — Pipeline engine
  - `com.adi.shell.io` — I/O redirection and stream management
- [ ] **Create a `Command` interface** — Define `execute(args, stdin, stdout, stderr)` so builtins are pluggable, testable, and follow a uniform contract.
- [ ] **Eliminate global mutable state** — Replace `static File currentDir` with a `ShellContext` object (holding cwd, environment variables, exit status) passed through method parameters.
- [ ] **Unify redirection handling** — The `echo` builtin has its own inline redirection parsing that duplicates logic in `runExternalCommand()`; centralize into a single I/O redirection module.
- [ ] **Decouple builtin list** — The builtins are hardcoded in both `isBuiltin()` and a `String[]` in `type()`; use a single registry (e.g., `Map<String, Command>`).

## 🟡 Medium Priority — Features

- [ ] **Tab completion** — Leverage JLine's `Completer` API for command names and file path completion.
- [ ] **Command history** — Use JLine `History` with persistence to `~/.myshell_history`.
- [ ] **Environment variable expansion** — Support `$VAR`, `${VAR}`, and `$?` (last exit code).
- [ ] **Logical operators** — Implement `&&`, `||`, and `;` for command chaining.
- [ ] **Glob / wildcard expansion** — Support `*`, `?`, and `[...]` patterns before command execution.
- [ ] **Signal handling** — Handle Ctrl+C (SIGINT), Ctrl+D (EOF gracefully), and Ctrl+Z (SIGTSTP).
- [ ] **Additional builtins** — `export`, `unset`, `set`, `alias`, `unalias`, `source`, `history`.
- [ ] **Command substitution** — Support `$(command)` and backtick syntax.
- [ ] **Here documents** — Support `<<EOF ... EOF` input redirection.
- [ ] **Job control** — Background execution with `&`, plus `jobs`, `fg`, `bg` builtins.
- [ ] **Prompt customization** — Support `PS1`-style configurable prompts (currently hardcoded as `$ `).

## 🟢 Low Priority — Quality & DevOps

- [ ] **Add logging** — Integrate SLF4J / Logback for debug and trace logging (especially around process execution and I/O).
- [ ] **Javadoc** — Document all public and package-private methods with proper Javadoc comments.
- [ ] **CI/CD pipeline** — Set up GitHub Actions to build, test, and lint on every push/PR.
- [ ] **Static analysis** — Add SpotBugs, Checkstyle, or PMD to enforce code quality standards.
- [ ] **Code coverage** — Integrate JaCoCo and set a minimum coverage threshold.
- [ ] **Improve error handling** — Replace broad `catch (Exception)` / `catch (IOException ignored)` with specific exception types; create custom shell exceptions.
- [ ] **Cross-platform fixes** — Replace hardcoded `/dev/null` with platform-aware null device (`NUL` on Windows); handle PATH separator (`:` vs `;`).
- [ ] **Security hardening** — Add input length limits; validate file paths to prevent path traversal exploits.
- [ ] **Contributing guide** — Create `CONTRIBUTING.md` with setup instructions, coding standards, and PR process.
- [ ] **Changelog** — Create `CHANGELOG.md` to track version history and notable changes.
- [ ] **Release automation** — Configure Maven Release plugin or GitHub Releases for versioned artifacts.
