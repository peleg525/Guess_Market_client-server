package gm.server;

import gm.engine.GmEngine;
import gm.engine.GmEngineImpl;

/**
 * The single, server-wide {@link GmEngine} instance every servlet talks to. A plain static holder
 * is enough here: a WAR deployment gets exactly one classloader/JVM-wide instance of this class
 * for the app's lifetime, and nothing in this exercise needs to survive past that (see the
 * README - state is intentionally lost on server restart).
 */
public final class EngineHolder {

    public static final GmEngine ENGINE = new GmEngineImpl();

    private EngineHolder() {
    }
}
