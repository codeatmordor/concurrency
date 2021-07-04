package com.code.mordor;


import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.UUID;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

import static org.junit.Assert.*;

/*
This construct allows us to create thread-safe logic in a lock-free way.
It's ideal for problems when we want to make an immutable snapshot of the data while other threads are still inserting data into the map.
We will be solving a problem of sorting a stream of events and getting a snapshot of the events that arrived in the last 60 seconds using that construct.
 */
public class ConcurrentSkipListMapDemo {

    public static class Event {
        private ZonedDateTime time;
        private String content;

        public Event(ZonedDateTime time, String content) {
            this.time = time;
            this.content = content;
        }

        public ZonedDateTime getTime() {
            return time;
        }

        public void setTime(ZonedDateTime time) {
            this.time = time;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }
    }


    public static class EventWindowSort {
        private final ConcurrentSkipListMap<ZonedDateTime, String> events
                = new ConcurrentSkipListMap<>(Comparator.comparingLong(value -> value.toInstant().toEpochMilli()));

        void acceptEvent(Event event) {
            events.put(event.getTime(), event.getContent());
        }

        /*
    The most notable pros of the ConcurrentSkipListMap are the methods that we can make an immutable snapshot of its data in a lock-free way.
    To get all events that arrived within the past minute, we can use the tailMap() method and pass the time from which we want to get elements:
     */
        ConcurrentNavigableMap<ZonedDateTime, String> getEventsFromLastMinute() {
            return events.tailMap(ZonedDateTime
                    .now()
                    .minusMinutes(1));
        }

        ConcurrentNavigableMap<ZonedDateTime, String> getEventsOlderThatOneMinute() {
            return events.headMap(ZonedDateTime
                    .now()
                    .minusMinutes(1));
        }

    }


    public static void main(String[] args) {
        ExecutorService executorService = Executors.newFixedThreadPool(3);
        EventWindowSort eventWindowSort = new EventWindowSort();
        int numberOfThreads = 2;

        Runnable producer = () -> IntStream
                .rangeClosed(0, 100)
                .forEach(index -> eventWindowSort.acceptEvent(
                        new Event(ZonedDateTime.now().minusSeconds(index), UUID.randomUUID().toString()))
                );

        for (int i = 0; i < numberOfThreads; i++) {
            executorService.execute(producer);
        }

        ConcurrentNavigableMap<ZonedDateTime, String> eventsFromLastMinute
                = eventWindowSort.getEventsFromLastMinute();

        long eventsOlderThanOneMinute = eventsFromLastMinute
                .entrySet()
                .stream()
                .filter(e -> e.getKey().isBefore(ZonedDateTime.now().minusMinutes(1)))
                .count();

        assertEquals(eventsOlderThanOneMinute, 0);

        long eventYoungerThanOneMinute = eventsFromLastMinute
                .entrySet()
                .stream()
                .filter(e -> e.getKey().isAfter(ZonedDateTime.now().minusMinutes(1)))
                .count();
        assertTrue(eventYoungerThanOneMinute > 0);
    }
}
