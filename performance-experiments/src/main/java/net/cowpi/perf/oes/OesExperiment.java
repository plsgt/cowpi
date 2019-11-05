package net.cowpi.perf.oes;

import net.cowpi.oes.config.OesDslContext;
import net.cowpi.oes.config.OesFlyway;
import net.cowpi.oes.config.OesName;
import net.cowpi.oes.engine.OesEngine;
import net.cowpi.protobuf.MessageProto.CowpiMessage;
import net.cowpi.protobuf.OesProto.OesContainer;
import net.cowpi.protobuf.OesProto.OesPreKey;
import org.flywaydb.core.Flyway;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.*;

@OesScope
public class OesExperiment {
    private static final Logger logger = LoggerFactory.getLogger(OesExperiment.class);

    private int threadCount;

    private final String oesName;

    private final DSLContext oesDslContext;

    private final OesEngine oesEngine;

    private final Flyway flyway;

    private final ExecutorService threadPool;

    @Inject
    public OesExperiment(@OesThreadCount int threadCount, @OesName String oesName,
                         @OesDslContext DSLContext oesDslContext, OesEngine oesEngine, @OesFlyway Flyway flyway) {
        this.threadCount = threadCount;
        this.oesName = oesName;
        this.oesDslContext = oesDslContext;
        this.oesEngine = oesEngine;
        this.flyway = flyway;
        this.threadPool = Executors.newFixedThreadPool(threadCount);
    }

    public List<OesPreKey> generatePreKeys(int count){
        return oesEngine.generatePrekeys(count).getOesPrekeyList();
    }

    public void reset(){
        flyway.clean();
        flyway.migrate();
    }

    public String getName(){
        return oesName;
    }

    public List<CowpiMessage> setupConversations(List<OesContainer> oesContainers) throws InterruptedException, ExecutionException {


        List<List<OesContainer>> queues = new ArrayList<>(threadCount);
        for(int i=0; i<threadCount; i++){
            queues.add(new ArrayList<>());
        }

        for(int i=0; i<oesContainers.size(); i++){
            queues.get(i%threadCount).add(oesContainers.get(i));
        }

        Queue<CowpiMessage> resultMessages = new ConcurrentLinkedQueue<>();

        List<Callable<Void>> tasks = new ArrayList<>(threadCount);
        for(List<OesContainer> queue: queues){
            tasks.add(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    for(OesContainer setup: queue){
                        resultMessages.add(oesEngine.processCowpiMessage(setup.getCowpiMessage()));
                    }
                    return null;
                }
            });
        }

        long start = System.currentTimeMillis();
        List<Future<Void>> futures = threadPool.invokeAll(tasks);
        for(Future<Void> future: futures){
            future.get();
        }
        long end = System.currentTimeMillis();

        Record record = oesDslContext.fetchOne("SELECT pg_total_relation_size('conversation')");
        long tableSize = record.get(0, Long.class);
        record = oesDslContext.fetchOne("SELECT pg_total_relation_size('participant_state')");
        tableSize += record.get(0, Long.class);

        logger.info("OES Setup: {} of size {} in {}, Table Size: {}.", oesContainers.size(), oesContainers.get(0).getCowpiMessage().getParticipantDataCount()+1, end-start, tableSize);

        return new ArrayList<>(resultMessages);
    }
}
