package com.code.mordor;


import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;

/*
TransferQueue allows us to create programs according to the producer-consumer pattern, and coordinate messages passing from producers to consumers.

The implementation is actually similar to the BlockingQueue – but gives us the new ability to implement a form of backpressure.
This means that, when the producer sends a message to the consumer using the transfer() method,
the producer will stay blocked until the message is consumed.
 */
public class TransferQueueDemo {
    public static class Producer implements Runnable {
        private TransferQueue<String> transferQueue;
        private String name;
        private Integer numberOfMessagesToProduce;
        public AtomicInteger numberOfProducedMessages = new AtomicInteger();


        @Override
        public void run() {

            for(int i=0; i<numberOfMessagesToProduce; i++){
                try {
                    boolean added = transferQueue.tryTransfer("A" + i, 4000, TimeUnit.MILLISECONDS);
                    if(added){
                        numberOfProducedMessages.incrementAndGet();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        public Producer(TransferQueue<String> transferQueue, String name, Integer numberOfMessagesToProduce) {
            this.transferQueue = transferQueue;
            this.name = name;
            this.numberOfMessagesToProduce = numberOfMessagesToProduce;
        }
    }

    public static class Consumer implements Runnable {
        private TransferQueue<String> transferQueue;

        private String name;

        private int numberOfMessagesToConsume;

        public AtomicInteger numberOfConsumedMessages
                = new AtomicInteger();

        public Consumer(TransferQueue<String> transferQueue, String name, int numberOfMessagesToConsume) {
            this.transferQueue = transferQueue;
            this.name = name;
            this.numberOfMessagesToConsume = numberOfMessagesToConsume;
        }

        @Override
        public void run() {
            for (int i = 0; i < numberOfMessagesToConsume; i++) {
                try {
                    String element = transferQueue.take();
                    longProcessing(element);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        private void longProcessing(String element)
                throws InterruptedException {
            numberOfConsumedMessages.incrementAndGet();
            Thread.sleep(500);
        }
    }

    public static void main(String[] args) {
        TransferQueue<String> transferQueue = new LinkedTransferQueue<>();
        ExecutorService exService = Executors.newFixedThreadPool(2);
        Producer producer = new Producer(transferQueue, "1", 3);

        // when
        exService.execute(producer);

        // then
        try {
            exService.awaitTermination(5000, TimeUnit.MILLISECONDS);
            exService.shutdown();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        assertEquals(producer.numberOfProducedMessages.intValue(), 0);
    }
}
