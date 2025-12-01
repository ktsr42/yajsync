package com.github.perlundq.yajsync.server.module;

import java.util.Objects;

public class ServerRestrictedModule extends RestrictedModule {
    private final Module module;
    private RsyncSecrets secrets;

    public ServerRestrictedModule(Module module, RsyncSecrets secrets) {
        this.module = module;
        this.secrets = secrets;
    }
    @Override
    public String authenticate(RsyncAuthContext authContext, String userName) throws ModuleSecurityException {
        if(Objects.isNull(secrets))  return null;

        String password = secrets.getPassword(userName);
        if(Objects.isNull(password)) return null;
        else                         return authContext.response(password.toCharArray());
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
        return module.name();
    }
}
