package com.adi.shell.parser;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CommandParserTest {

    private CommandParser parser;

    @BeforeEach
    void setUp() {
        parser = new CommandParser();
    }

    @Test
    void parseSimpleCommand() {
        List<String> tokens = parser.parse("echo hello");
        assertEquals(List.of("echo", "hello"), tokens);
    }

    @Test
    void parseCommandWithMultipleArgs() {
        List<String> tokens = parser.parse("echo hello world foo");
        assertEquals(List.of("echo", "hello", "world", "foo"), tokens);
    }

    @Test
    void parseEmptyInput() {
        List<String> tokens = parser.parse("");
        assertTrue(tokens.isEmpty());
    }

    @Test
    void parseSingleQuotedString() {
        List<String> tokens = parser.parse("echo 'hello world'");
        assertEquals(List.of("echo", "hello world"), tokens);
    }

    @Test
    void parseDoubleQuotedString() {
        List<String> tokens = parser.parse("echo \"hello world\"");
        assertEquals(List.of("echo", "hello world"), tokens);
    }

    @Test
    void parseEmptySingleQuotes() {
        List<String> tokens = parser.parse("echo ''");
        assertEquals(List.of("echo", ""), tokens);
    }

    @Test
    void parseEmptyDoubleQuotes() {
        List<String> tokens = parser.parse("echo \"\"");
        assertEquals(List.of("echo", ""), tokens);
    }

    @Test
    void parseBackslashEscapeInDoubleQuotes() {
        List<String> tokens = parser.parse("echo \"hello\\\"world\"");
        assertEquals(List.of("echo", "hello\"world"), tokens);
    }

    @Test
    void parseBackslashEscapeOutsideQuotes() {
        List<String> tokens = parser.parse("echo hello\\ world");
        assertEquals(List.of("echo", "hello world"), tokens);
    }

    @Test
    void parseBackslashInDoubleQuotesNonSpecialChar() {
        List<String> tokens = parser.parse("echo \"hello\\nworld\"");
        assertEquals(List.of("echo", "hello\\nworld"), tokens);
    }

    @Test
    void parseSingleQuotesPreserveBackslash() {
        List<String> tokens = parser.parse("echo 'hello\\world'");
        assertEquals(List.of("echo", "hello\\world"), tokens);
    }

    @Test
    void parseStdoutRedirect() {
        List<String> tokens = parser.parse("echo hello > out.txt");
        assertTrue(tokens.contains("__REDIR__"));
        assertTrue(tokens.contains("out.txt"));
        assertTrue(tokens.contains("echo"));
        assertTrue(tokens.contains("hello"));
    }

    @Test
    void parseStdoutAppendRedirect() {
        List<String> tokens = parser.parse("echo hello >> out.txt");
        assertTrue(tokens.contains("__APPEND__"));
        assertTrue(tokens.contains("out.txt"));
    }

    @Test
    void parseStderrRedirect() {
        List<String> tokens = parser.parse("echo hello 2> err.txt");
        assertTrue(tokens.contains("__REDIR_ERR__"));
        assertTrue(tokens.contains("err.txt"));
    }

    @Test
    void parseStderrAppendRedirect() {
        List<String> tokens = parser.parse("echo hello 2>> err.txt");
        assertTrue(tokens.contains("__APPEND_ERR__"));
        assertTrue(tokens.contains("err.txt"));
    }

    @Test
    void parseExplicitStdoutRedirect1() {
        List<String> tokens = parser.parse("echo hello 1> out.txt");
        assertTrue(tokens.contains("__REDIR__"));
        assertTrue(tokens.contains("out.txt"));
    }

    @Test
    void parseExplicitStdoutAppend1() {
        List<String> tokens = parser.parse("echo hello 1>> out.txt");
        assertTrue(tokens.contains("__APPEND__"));
        assertTrue(tokens.contains("out.txt"));
    }

    @Test
    void parsePipeSymbolPreserved() {
        List<String> tokens = parser.parse("echo hello | cat");
        assertTrue(tokens.contains("|"));
    }

    @Test
    void parseMultipleSpacesBetweenArgs() {
        List<String> tokens = parser.parse("echo   hello    world");
        assertEquals(List.of("echo", "hello", "world"), tokens);
    }

    @Test
    void parseBackslashEscapedBackslashInDoubleQuotes() {
        List<String> tokens = parser.parse("echo \"hello\\\\world\"");
        assertEquals(List.of("echo", "hello\\world"), tokens);
    }

    @Test
    void parseRedirectAttachedToOperator() {
        List<String> tokens = parser.parse("echo hello >out.txt");
        assertTrue(tokens.contains("__REDIR__"));
        assertTrue(tokens.contains("out.txt"));
    }

    @Test
    void parseBothStdoutAndStderrRedirect() {
        List<String> tokens = parser.parse("cmd > out.txt 2> err.txt");
        assertTrue(tokens.contains("__REDIR__"));
        assertTrue(tokens.contains("out.txt"));
        assertTrue(tokens.contains("__REDIR_ERR__"));
        assertTrue(tokens.contains("err.txt"));
    }
}
