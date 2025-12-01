package com.github.ktsr42.yajsynclib;

import com.github.perlundq.yajsync.server.module.Module;
import com.github.perlundq.yajsync.server.module.ModuleSecurityException;
import com.github.perlundq.yajsync.server.module.RestrictedModule;
import com.github.perlundq.yajsync.server.module.RsyncAuthContext;

public class ProtectedModule extends RestrictedModule {
    private String password;
    private Module module;

    public ProtectedModule(Module m, String password) {
        this.module = m;
        this.password = password;
    }

    @Override
    public String authenticate(RsyncAuthContext authContext, String userName) throws ModuleSecurityException {
        return authContext.response(password.toCharArray());
    }

    @Override
    public Module toModule() {
        return module;
    }

    @Override
    public String name() {
        return module.name();
    }

    @Override
    public String comment() {
        return module.comment();
    }
}
