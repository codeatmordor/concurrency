package com.code.mordor;

import com.google.common.primitives.Ints;

import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/*
DelayQueue construct from java.util.concurrent package, has a very useful characteristic.
when the consumer wants to take an element from the queue,
they can take it only when the delay for that particular element has expired.
 */
public class DelayedQueueDemo {


    public static void main(String[] args) {
        ExecutorService executor = Executors.newFixedThreadPool(2);

        BlockingQueue<DelayObject> queue = new DelayQueue<>();
        int numberOfElementsToProduce = 2;
        int delayOfEachProducedMessageMilliseconds = 500;
        DelayQueueConsumer consumer = new DelayQueueConsumer(
                queue, numberOfElementsToProduce);
        DelayQueueProducer producer = new DelayQueueProducer(
                queue, numberOfElementsToProduce, delayOfEachProducedMessageMilliseconds);

        // when
        executor.submit(producer);
        executor.submit(consumer);

        // then
        try {
            executor.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        executor.shutdown();
    }
    private static class DelayObject implements Delayed {
        private String data;
        private long startTime;

        public DelayObject(String data, long delayInMilliseconds) {
            this.data = data;
            this.startTime = System.currentTimeMillis() + delayInMilliseconds;
        }

        /*
        When the consumer tries to take an element from the queue,
        the DelayQueue will execute getDelay() to find out if that element is allowed to be returned from the queue.
        If the getDelay() method will return zero or a negative number, it means that it could be retrieved from the queue.
         */
        @Override
        public long getDelay(TimeUnit unit) {
            long diff = startTime - System.currentTimeMillis();
            return unit.convert(diff, TimeUnit.MILLISECONDS);
        }

        /*
        We also need to implement the compareTo() method, because the elements in the DelayQueue will be sorted according to the expiration time.
        The item that will expire first is kept at the head of the queue and the element with the highest expiration time is kept at the tail of the queue
         */
        @Override
        public int compareTo(Delayed o) {
            return Ints.saturatedCast(
                    this.startTime - ((DelayObject) o).startTime);
        }
    }

    private static class DelayQueueProducer implements Runnable {

        private BlockingQueue<DelayObject> queue;
        private Integer numberOfElementsToProduce;
        private Integer delayOfEachProducedMessageMilliseconds;

        public DelayQueueProducer(BlockingQueue<DelayObject> queue, Integer numberOfElementsToProduce, Integer delayOfEachProducedMessageMilliseconds) {
            this.queue = queue;
            this.numberOfElementsToProduce = numberOfElementsToProduce;
            this.delayOfEachProducedMessageMilliseconds = delayOfEachProducedMessageMilliseconds;
        }

        @Override
        public void run() {
            for (int i = 0; i < numberOfElementsToProduce; i++) {
                DelayObject object
                        = new DelayObject(
                        UUID.randomUUID().toString(), delayOfEachProducedMessageMilliseconds);
                System.out.println("Put object: " + object);
                try {
                    queue.put(object);
                    Thread.sleep(500);
                } catch (InterruptedException ie) {
                    ie.printStackTrace();
                }
            }
        }
    }

    public static class DelayQueueConsumer implements Runnable {
        private BlockingQueue<DelayObject> queue;
        private Integer numberOfElementsToTake;
        public AtomicInteger numberOfConsumedElements = new AtomicInteger();

        public DelayQueueConsumer(BlockingQueue<DelayObject> queue, Integer numberOfElementsToTake) {
            this.queue = queue;
            this.numberOfElementsToTake = numberOfElementsToTake;
        }

        @Override
        public void run() {
            for (int i = 0; i < numberOfElementsToTake; i++) {
                try {
                    DelayObject object = queue.take();
                    numberOfConsumedElements.incrementAndGet();
                    System.out.println("Consumer take: " + object);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
