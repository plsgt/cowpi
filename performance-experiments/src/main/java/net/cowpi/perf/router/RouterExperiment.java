package net.cowpi.perf.router;

import com.zaxxer.hikari.HikariDataSource;
import net.cowpi.perf.config.RouterThreadCount;
import net.cowpi.protobuf.MessageProto.CowpiMessage;
import net.cowpi.protobuf.OesProto.OesContainer;
import net.cowpi.server.config.OesPublicKeys;
import net.cowpi.server.config.RouterDataSource;
import net.cowpi.server.config.RouterDslContext;
import net.cowpi.server.config.RouterFlyway;
import net.cowpi.server.engine.CowpiEngine;
import net.cowpi.server.engine.UserEngine;
import net.cowpi.server.jooq.tables.OesMessage;
import org.flywaydb.core.Flyway;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.*;
import java.util.concurrent.*;

public class RouterExperiment {
    private static final Logger logger = LoggerFactory.getLogger(RouterExperiment.class);

    private final int threadCount;

    private final Flyway routerFlyway;
    private final HikariDataSource dataSource;
    private final DSLContext routerDslContext;

    private final CowpiEngine cowpiEngine;
    private final UserEngine userEngine;
    private final QueuedOesBroker queuedOesBroker;
    private final QueuedMessageBroker queuedMessageBroker;

    private final Map<String, byte[]> oesPublicKeys;

    private final ExecutorService threadPool;

    @Inject
    public RouterExperiment(@RouterThreadCount int threadCount, @RouterFlyway Flyway routerFlyway,
                            @RouterDataSource HikariDataSource dataSource,
                            @RouterDslContext DSLContext routerDslContext,
                            CowpiEngine cowpiEngine,
                            UserEngine userEngine, QueuedOesBroker queuedOesBroker,
                            QueuedMessageBroker queuedMessageBroker,
                            @OesPublicKeys Map<String, byte[]> oesPublicKeys) {
        this.threadCount = threadCount;
        this.routerFlyway = routerFlyway;
        this.dataSource = dataSource;
        this.routerDslContext = routerDslContext;
        this.cowpiEngine = cowpiEngine;
        this.userEngine = userEngine;
        this.queuedOesBroker = queuedOesBroker;
        this.queuedMessageBroker = queuedMessageBroker;
        this.oesPublicKeys = oesPublicKeys;
        this.threadPool = Executors.newFixedThreadPool(threadCount);
    }

    public Map<String, List<OesContainer>> setupConversations(List<CowpiMessage> setupMessages) throws InterruptedException, ExecutionException {

        queuedOesBroker.getOesQueues().clear();
        for(String oesName: oesPublicKeys.keySet()){
            long oesId = userEngine.getOesRecord(oesName).getId();
            queuedOesBroker.getOesQueues().put(oesId, new ConcurrentLinkedQueue<>());
        }

        List<List<CowpiMessage>> queues = new ArrayList<>(threadCount);
        for(int i=0; i<threadCount; i++){
            queues.add(new ArrayList<>());
        }

        for(int i=0; i<setupMessages.size(); i++){
            queues.get(i%threadCount).add(setupMessages.get(i));
        }

        List<Callable<Void>> tasks = new ArrayList<>(threadCount);
        for(List<CowpiMessage> queue: queues){
            tasks.add(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    for(CowpiMessage setup: queue){
                        cowpiEngine.setupConversation(setup);
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

        logger.info("Router Setup: {} of size {} in {}.", setupMessages.size(), setupMessages.get(0).getParticipantDataCount()+1, end-start);


        Map<String, List<OesContainer>> result = new HashMap<>();

        for(String oesName: oesPublicKeys.keySet()){
            long oesId = userEngine.getOesRecord(oesName).getId();

            List<OesContainer> containers = new ArrayList<>();

            containers.addAll(queuedOesBroker.getOesQueues().get(oesId));

            result.put(oesName, containers);
        }

        return result;

    }

    public Map<String, List<CowpiMessage>> oesSetupConversations(Map<String, List<OesMessage>> oesMessages) {
        return null;
    }

    public void reset() {
        routerFlyway.clean();
        routerFlyway.migrate();
    }

    public void shutdown() {
        dataSource.close();
    }

    public List<RouterMessage> handleOesSetup(String oesName, List<CowpiMessage> messages) throws Exception {

        queuedMessageBroker.getMessageQueue().clear();

        long oesId = userEngine.getOesRecord(oesName).getId();

        List<List<CowpiMessage>> queues = new ArrayList<>(threadCount);
        for(int i=0; i<threadCount; i++){
            queues.add(new ArrayList<>());
        }

        for(int i=0; i<messages.size(); i++){
            queues.get(i%threadCount).add(messages.get(i));
        }

        List<Callable<Void>> tasks = new ArrayList<>(threadCount);
        for(List<CowpiMessage> queue: queues){
            tasks.add(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    for(CowpiMessage setup: queue){
                        cowpiEngine.handleOesMessage(oesId, setup);
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

        Record record = routerDslContext.fetchOne("SELECT pg_total_relation_size('conversation_message')");
        long tableSize = record.get(0, Long.class);

        logger.info("Router  OesSetup: {} of size {} in {}, Table Size: {}.", messages.size(), messages.get(0).getOesDataMap().size(), end-start, tableSize);

        return new ArrayList<>(queuedMessageBroker.getMessageQueue());

    }
}
