package net.cowpi.android.perf;

import net.cowpi.crypto.CryptoModule;

import javax.inject.Singleton;

import dagger.Component;

@Component(modules = {CryptoModule.class})
@Singleton
public interface PerfComponent {

    PerfExperiment experiment();

}
