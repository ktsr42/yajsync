package com.github.ktsr42.yajsynclib;

import com.github.perlundq.yajsync.server.module.Module;
import com.github.perlundq.yajsync.server.module.ModuleException;
import com.github.perlundq.yajsync.server.module.Modules;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class MultiModules implements Modules {
    private Map<String, Module> _modules = new HashMap<>();

    public MultiModules addModule(String name, String path) {
        _modules.put(name, new SimpleModule(name, path));
        return this;
    }

    @Override
    public Module get(String moduleName) throws ModuleException {
        Module m = _modules.getOrDefault(moduleName, null);
        if(Objects.isNull(m))
            throw new ModuleException("Module not found: " + moduleName);
        return m;
    }

    @Override
    public Iterable<Module> all() {
        return _modules.values();
    }
}
