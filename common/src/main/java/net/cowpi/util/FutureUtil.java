package net.cowpi.util;

import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.FutureListener;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.function.BiConsumer;

public class FutureUtil {

    public static <T> CompletionStage<Void> toVoidCompletionStage(Future<T> future){

        CompletableFuture<Void> result = new CompletableFuture<>();

        future.addListener(new FutureListener<T>() {
            @Override
            public void operationComplete(Future<T> future) throws Exception {
                if(future.isDone() && future.isSuccess()){
                    result.complete(null);
                }
                else if(future.isDone()){
                    result.completeExceptionally(future.cause());
                }
            }
        });

        return result;

    }

    public static <T> CompletionStage<T> toCompletionStage(Future<T> future){

        CompletableFuture<T> result = new CompletableFuture<>();

        future.addListener(new FutureListener<T>() {
            @Override
            public void operationComplete(Future<T> future) throws Exception {
                if(future.isDone() && future.isSuccess()){
                    result.complete(future.get());
                }
                else if(future.isDone()){
                    result.completeExceptionally(future.cause());
                }
            }
        });

        return result;

    }

    public static CompletionStage<Void> allCompleted(List<CompletionStage<?>> stages){
        CompletableFuture<?>[] futures = new CompletableFuture[stages.size()];
        int i=0;
        for(CompletionStage<?> stage: stages){
            futures[i] = stage.toCompletableFuture();
            i++;
        }

        return CompletableFuture.allOf(futures);
    }
}
