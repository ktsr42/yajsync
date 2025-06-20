package com.github.perlundq.yajsync.server.module;

import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class RsyncSecretsTest {
    @Test
    public void parseSecretsTest() throws IOException, ModuleException {
        RsyncSecrets secrets = new RsyncSecrets(null);
        secrets.parseFile(new BufferedReader(new StringReader("joe:foo\n\n# lolo\njack:boo\n")));
        assertEquals("foo", secrets.getPassword("joe"));
        assertEquals("boo",  secrets.getPassword("jack"));
        assertNull(secrets.getPassword("jill"));
    }

    @Test(expected=ModuleException.class)
    public void parseError_nousername() throws IOException, ModuleException {
        RsyncSecrets secrets = new RsyncSecrets(null);
        secrets.parseFile(new BufferedReader(new StringReader("joe:foo\n\n# lolo\n:boo\njill:hello\n")));
    }

    @Test(expected=ModuleException.class)
    public void parseErrors_nopassword() throws IOException, ModuleException {
        RsyncSecrets secrets = new RsyncSecrets(null);
        secrets.parseFile(new BufferedReader(new StringReader("joe:foo\n\n# lolo\nboo:\njill:hello\n")));
    }

    @Test(expected=ModuleException.class)
    public void parseErrors_nocolon() throws IOException, ModuleException {
        RsyncSecrets secrets = new RsyncSecrets(null);
        secrets.parseFile(new BufferedReader(new StringReader("joe:foo\n\n# lolo\nboo\njill:hello\n")));
    }

    @Test(expected=ModuleException.class)
    public void parseErrors_nogroups() throws IOException, ModuleException {
        RsyncSecrets secrets = new RsyncSecrets(null);
        secrets.parseFile(new BufferedReader(new StringReader("joe:foo\n\n# lolo\n@boo:foo\njill:hello\n")));
    }

}
