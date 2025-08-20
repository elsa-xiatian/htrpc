package netty;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class MyCompletebalFuture {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        /**
         * 可以获取子线程中的返回，过程中的结果，并可以在主线程中阻塞等待其完成
         */
        CompletableFuture<Integer> completableFuture = new CompletableFuture<>();
        new Thread(() ->{
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            int i = 8;
            completableFuture.complete(i);
        }).start();
        Integer integer = completableFuture.get();
    }
}
