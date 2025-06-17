package com.github.perlundq.yajsync.server.module;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class RsyncSecrets {
    // no support for groups
    private static String filename;
    private HashMap<String, String> passwords;

    public RsyncSecrets(String filename) {
        this.filename = filename;
        this.passwords = new HashMap<>();
    }

    public String getFilename() { return filename; }

    public void parseFile() throws ModuleException {
        try {
            BufferedReader reader = Files.newBufferedReader(Paths.get(filename), Charset.defaultCharset());

            boolean atEof = false;
            while (!atEof) {
                String rawLine = reader.readLine();
                if (Objects.isNull(rawLine)) {
                    atEof = true;
                    continue;
                }

                String line = rawLine.trim();
                if (line.isEmpty()) continue;   // ignore empty lines
                char firstChar = line.charAt(0);
                if (firstChar == '#') continue; // skip comments
                if (firstChar == '@') {
                    throw new ModuleException("Authentication groups are not supported - secret: " + line);
                }
                String[] parts = line.split(":", 2);
                if (parts.length != 2) {
                    throw new ModuleException("Malformed secrets line (no colon): " + line);
                }
                if (parts[0].length() == 0 || parts[1].length() == 0) {
                    throw new ModuleException("Malformed secrets line (empty username or password:" + line);
                }
                passwords.put(parts[0], parts[1]);
            }
        } catch(IOException iox) {
            throw new ModuleException(iox);
        }
    }

    public void debugOutput() {
        for(Map.Entry e : passwords.entrySet()) {
            System.out.println(String.format("User: '%s', password '%s'", e.getKey(), e.getValue()));
        }
    }

    public String getPassword(String username) {
        return passwords.getOrDefault(username,null);
    }
}
