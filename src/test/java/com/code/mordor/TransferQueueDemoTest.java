package com.code.mordor;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;

import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;
import com.code.mordor.TransferQueueDemo;
import org.junit.jupiter.api.TestMethodOrder;


@TestMethodOrder(MethodOrderer.MethodName.class)
public class TransferQueueDemoTest {
    @Test
    public void whenUseOneProducerAndNoConsumers_thenShouldFailWithTimeout()
            throws InterruptedException {
        // given
        TransferQueue<String> transferQueue;
        transferQueue = new LinkedTransferQueue<>();
        ExecutorService exService = Executors.newFixedThreadPool(2);
        TransferQueueDemo.Producer producer = new TransferQueueDemo.Producer(transferQueue, "1", 3);

        // when
        exService.execute(producer);

        // then
        exService.awaitTermination(5000, TimeUnit.MILLISECONDS);
        exService.shutdown();

        assertEquals(producer.numberOfProducedMessages.intValue(), 0);
    }

    @Test
    public void whenUseOneConsumerAndOneProducer_thenShouldProcessAllMessages()
            throws InterruptedException {
        // given
        TransferQueue<String> transferQueue = new LinkedTransferQueue<>();
        ExecutorService exService = Executors.newFixedThreadPool(2);
        TransferQueueDemo.Producer producer = new TransferQueueDemo.Producer(transferQueue, "1", 3);
        TransferQueueDemo.Consumer consumer = new TransferQueueDemo.Consumer(transferQueue, "1", 3);

        // when
        exService.execute(producer);
        exService.execute(consumer);

        // then
        exService.awaitTermination(5000, TimeUnit.MILLISECONDS);
        exService.shutdown();

        assertEquals(producer.numberOfProducedMessages.intValue(), 3);
        assertEquals(consumer.numberOfConsumedMessages.intValue(), 3);
    }

    @Test
    public void whenMultipleConsumersAndProducers_thenProcessAllMessages()
            throws InterruptedException {
        // given
        TransferQueue<String> transferQueue = new LinkedTransferQueue<>();
        ExecutorService exService = Executors.newFixedThreadPool(3);
        TransferQueueDemo.Producer producer1 = new TransferQueueDemo.Producer(transferQueue, "1", 3);
        TransferQueueDemo.Producer producer2 = new TransferQueueDemo.Producer(transferQueue, "2", 3);
        TransferQueueDemo.Consumer consumer1 = new TransferQueueDemo.Consumer(transferQueue, "1", 3);
        TransferQueueDemo.Consumer consumer2 = new TransferQueueDemo.Consumer(transferQueue, "2", 3);

        // when
        exService.execute(producer1);
        exService.execute(producer2);
        exService.execute(consumer1);
        exService.execute(consumer2);

        // then
        exService.awaitTermination(10_000, TimeUnit.MILLISECONDS);
        exService.shutdown();

        assertEquals(producer1.numberOfProducedMessages.intValue(), 3);
        assertEquals(producer2.numberOfProducedMessages.intValue(), 3);
    }

}