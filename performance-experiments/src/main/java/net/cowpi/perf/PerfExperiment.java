package net.cowpi.perf;

import net.cowpi.perf.client.ClientExperiment;
import net.cowpi.perf.config.Oes;
import net.cowpi.perf.config.PerfConfig;
import net.cowpi.perf.oes.OesComponent;
import net.cowpi.perf.oes.OesComponent.Builder;
import net.cowpi.perf.oes.OesExperiment;
import net.cowpi.perf.router.RouterExperiment;
import net.cowpi.perf.router.RouterMessage;
import net.cowpi.protobuf.MessageProto.CowpiMessage;
import net.cowpi.protobuf.OesProto.OesContainer;
import net.cowpi.protobuf.OesProto.OesPreKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Singleton
public class PerfExperiment {
    private static final Logger logger = LoggerFactory.getLogger(PerfExperiment.class);
    private static final int MIN_SIZE = 2;
    private static final int MAX_SIZE = 50;
    private static final int CONVERSATIONS = 1000;

    private final ClientExperiment client;
    private final RouterExperiment router;

    private final Map<String, OesExperiment> oesExperiments = new HashMap<>();
    private final PerfConfig config;
    private final Provider<OesComponent.Builder> oesComponent;

    @Inject
    public PerfExperiment(ClientExperiment client, RouterExperiment router, PerfConfig config, Provider<Builder> oesComponent) {
        this.client = client;
        this.router = router;
        this.config = config;
        this.oesComponent = oesComponent;
    }

    public void run() throws Exception {

        setupOesExperiments();

        warmup();

        for(int i=MIN_SIZE; i<=MAX_SIZE; i++){
            experiment(i);
        }

        router.shutdown();

    }

    private void setupOesExperiments() {

        for(Oes oes: config.getOes()){
            OesComponent component = oesComponent.get()
                    .oes(oes)
                    .build();

            oesExperiments.put(oes.getName(), component.oesExperiment());
        }

    }

    private void warmup() throws Exception {
        logger.info("Warming up...");
        experiment(2);
        experiment(3);
        logger.info("Warming up complete...");
    }

    private void experiment(int size) throws Exception {
        logger.info("resetting...");
        client.reset();
        router.reset();

        Map<String, List<OesPreKey>> oesPrekeys = new HashMap<>();
        for(OesExperiment oes: oesExperiments.values()){
            oes.reset();
            oesPrekeys.put(oes.getName(), oes.generatePreKeys(CONVERSATIONS));
        }

        logger.info("Registering users: {}", size);
        client.registerUsers(size, 3*CONVERSATIONS);

        logger.info("Creating SETUP Messages");
        List<CowpiMessage> setupMessages = client.createSetup(CONVERSATIONS, size, oesPrekeys);

        Map<String, List<OesContainer>> routerToOesSetup = router.setupConversations(setupMessages);

        Map<String, List<CowpiMessage>> oesSetupResult = new HashMap<>();
        for(Map.Entry<String, OesExperiment> entry: oesExperiments.entrySet()){
            oesSetupResult.put(entry.getKey(), entry.getValue().setupConversations(routerToOesSetup.get(entry.getKey())));
        }

        List<RouterMessage> routerSetupMessages = new ArrayList<>();
        for(Map.Entry<String, List<CowpiMessage>> entry: oesSetupResult.entrySet()){
            routerSetupMessages.addAll(router.handleOesSetup(entry.getKey(), entry.getValue()));
        }

        client.handleSetupMessage(routerSetupMessages);

    }

}
