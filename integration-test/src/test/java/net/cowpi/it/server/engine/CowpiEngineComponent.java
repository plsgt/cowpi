package net.cowpi.it.server.engine;

import dagger.Component;
import net.cowpi.server.db.Database;
import net.cowpi.server.engine.CowpiEngine;

import javax.inject.Singleton;

@Singleton
@Component(modules = {CowpiEngineModule.class})
public interface CowpiEngineComponent {
    Database database();
    CowpiEngine cowpiEngine();
}
