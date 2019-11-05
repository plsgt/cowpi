package net.cowpi.oes.engine;

import net.cowpi.ManagedService;
import net.cowpi.oes.config.MessageQueueThreads;
import net.cowpi.oes.config.MessageQueueSize;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

@Singleton
public class MessageProcessingQueue implements ManagedService {
    private static final Logger logger = LoggerFactory.getLogger(MessageProcessingQueue.class);

    private final int messageQueueSize;
    private final int messageQueueThreads;
    private final ThreadFactory threadFactory;

    private final List<BlockingQueue<Runnable>> queues = Collections.synchronizedList(new ArrayList<>());
    private final List<Thread> threads = Collections.synchronizedList(new ArrayList<>());
    private final AtomicBoolean running = new AtomicBoolean(false);

    @Inject
    public MessageProcessingQueue(@MessageQueueSize int messageQueueSize, @MessageQueueThreads int messageQueueThreads, ThreadFactory threadFactory) {
        this.messageQueueSize = messageQueueSize;
        this.messageQueueThreads = messageQueueThreads;
        this.threadFactory = threadFactory;
    }

    @Override
    public synchronized CompletionStage<Void> start() {

        for(int i=0; i<messageQueueThreads; i++) {
            BlockingQueue<Runnable> queue = new LinkedBlockingQueue<>(messageQueueSize);
            Thread thread = threadFactory.newThread(new WorkerRunnable(queue));
            queues.add(queue);
            threads.add(thread);
            thread.start();
        }

        return CompletableFuture.completedFuture(null);
    }

    @Override
    public synchronized CompletionStage<Void> shutdown() {
        running.set(true);
        for(Thread thread: threads){
            thread.interrupt();
        }

        queues.clear();;
        threads.clear();

        return CompletableFuture.completedFuture(null);
    }

    private class WorkerRunnable implements Runnable{

        private final BlockingQueue<Runnable> tasks;

        private WorkerRunnable(BlockingQueue<Runnable> tasks) {
            this.tasks = tasks;
        }

        @Override
        public void run() {
            while(running.get()){
                try {
                    tasks.take().run();
                } catch (InterruptedException e) {
                    logger.error("Worker Task interupted.", e);
                }
            }
        }
    }
}
