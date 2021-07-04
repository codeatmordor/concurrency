package com.code.mordor;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/*
SynchronousQueue allows us to exchange information between threads in thread-safe manner.
The SynchronousQueue only has two supported operations: take() and put(), and both of them are blocking.

For example, when we want to add an element to the queue, we need to call the put() method.
That method will block until some other thread calls the take() method, signaling that it is ready to take an element.

Although the SynchronousQueue has an interface of a queue,
we should think about it as an exchange point for a single element between two threads,
in which one thread is handing off an element, and another thread is taking that element.
 */
public class SynchronousQueueDemo {
    /*
    To see why the SynchronousQueue can be so useful, we will implement a logic using a shared variable between two threads and next,
    we will rewrite that logic using SynchronousQueue making our code a lot simpler and more readable.
     */
public static void main(String[] args) {
    ExecutorService executor = Executors.newFixedThreadPool(2);
    AtomicInteger sharedState = new AtomicInteger();
    CountDownLatch countDownLatch = new CountDownLatch(1);

    Runnable producer = () -> {
        Integer producedElement = ThreadLocalRandom.current().nextInt();
        sharedState.set(producedElement);
        countDownLatch.countDown();
    };


    Runnable consumer = () -> {
        try {
            countDownLatch.await();
            Integer consumedElement = sharedState.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    };

    executor.execute(producer);
    executor.execute(consumer);

    try {
        executor.awaitTermination(500, TimeUnit.MILLISECONDS);
        executor.shutdown();
    } catch (InterruptedException e) {
        e.printStackTrace();
    }

    // Lets try SynchronousQueue approach

    ExecutorService executor1 = Executors.newFixedThreadPool(2);
    SynchronousQueue<Integer> synchronousQueue = new SynchronousQueue<>();

    Runnable producer1 = () -> {
        try {Integer producedElement = ThreadLocalRandom
                .current()
                .nextInt();

            synchronousQueue.put(producedElement);
            System.out.println("SynchronousQueue produced " + producedElement);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    };

    Runnable consumer1= () -> {
        try {
            Integer consumedElement = synchronousQueue.take();
            System.out.println("SynchronousQueue Consumed " + consumedElement);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    };

    executor1.execute(producer1);
    executor1.execute(consumer1);

    try {
        executor1.awaitTermination(500, TimeUnit.MILLISECONDS);
        executor1.shutdown();
    } catch (InterruptedException e) {
        e.printStackTrace();
    }
}
}
