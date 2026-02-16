package com.adi.shell.parser;

import java.util.ArrayList;
import java.util.List;

public class CommandParser {

    public List<String> parse(String input) {
        List<String> tokens = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        boolean inSingle = false, inDouble = false;

        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);

            if (c == '\'' && !inDouble) {
                inSingle = !inSingle;
                if (!inSingle && cur.length() == 0) tokens.add("");
                continue;
            }

            if (c == '"' && !inSingle) {
                inDouble = !inDouble;
                if (!inDouble && cur.length() == 0) tokens.add("");
                continue;
            }

            if (c == '\\' && !inSingle) {
                if (inDouble) {
                    if (i + 1 < input.length()) {
                        char next = input.charAt(i + 1);
                        if (next == '"' || next == '\\' || next == '$' || next == '`') {
                            cur.append(next);
                            i++;
                            continue;
                        }
                    }
                    cur.append('\\');
                    continue;
                }

                if (i + 1 < input.length()) {
                    char next = input.charAt(i + 1);
                    if (next == ' ') {
                        cur.append(' ');
                        i++;
                        continue;
                    }
                    cur.append(next);
                    i++;
                    continue;
                }

                cur.append('\\');
                continue;
            }

            if (c == ' ' && !inSingle && !inDouble) {
                if (cur.length() > 0) {
                    tokens.add(cur.toString());
                    cur.setLength(0);
                }
                continue;
            }

            cur.append(c);
        }

        if (cur.length() > 0) tokens.add(cur.toString());

        List<String> cleaned = new ArrayList<>();
        String out = null, err = null;
        boolean appendOut = false, appendErr = false;

        for (int i = 0; i < tokens.size(); i++) {
            String t = tokens.get(i);

            if (t.equals(">>") || t.equals("1>>")) {
                appendOut = true;
                out = tokens.get(++i);
                cleaned.add("__APPEND__");
                cleaned.add(out);
                continue;
            }
            if (t.startsWith(">>")) {
                appendOut = true;
                out = t.substring(2);
                cleaned.add("__APPEND__");
                cleaned.add(out);
                continue;
            }
            if (t.startsWith("1>>")) {
                appendOut = true;
                out = t.substring(3);
                cleaned.add("__APPEND__");
                cleaned.add(out);
                continue;
            }

            if (t.equals("2>>")) {
                appendErr = true;
                err = tokens.get(++i);
                cleaned.add("__APPEND_ERR__");
                cleaned.add(err);
                continue;
            }
            if (t.startsWith("2>>")) {
                appendErr = true;
                err = t.substring(3);
                cleaned.add("__APPEND_ERR__");
                cleaned.add(err);
                continue;
            }

            if (t.equals(">") || t.equals("1>")) {
                out = tokens.get(++i);
                continue;
            }
            if (t.startsWith(">")) {
                out = t.substring(1);
                continue;
            }
            if (t.startsWith("1>")) {
                out = t.substring(2);
                continue;
            }

            if (t.equals("2>")) {
                err = tokens.get(++i);
                continue;
            }
            if (t.startsWith("2>")) {
                err = t.substring(2);
                continue;
            }

            cleaned.add(t);
        }

        if (out != null && !appendOut) {
            cleaned.add("__REDIR__");
            cleaned.add(out);
        }

        boolean hasErrRedir = cleaned.contains("__APPEND_ERR__") || cleaned.contains("__REDIR_ERR__");
        if (err != null && !hasErrRedir) {
            if (appendErr) {
                cleaned.add("__APPEND_ERR__");
                cleaned.add(err);
            } else {
                cleaned.add("__REDIR_ERR__");
                cleaned.add(err);
            }
        }

        return cleaned;
    }
}
