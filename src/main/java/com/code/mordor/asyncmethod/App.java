package com.code.mordor.asyncmethod;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Callable;

@Slf4j
public class App {

    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(App.class);
    public static void main(String[] args) throws Exception {
        // construct a new executor that will run async tasks
        var executor = new ThreadAsyncExecutor();

        // start few async tasks with varying processing times, two last with callback handlers
        final var asyncResult1 = executor.startProcess(lazyval(10, 500));
        final var asyncResult2 = executor.startProcess(lazyval("test", 300));
        final var asyncResult3 = executor.startProcess(lazyval(50L, 700));
        final var asyncResult4 = executor.startProcess(lazyval(20, 400),
                callback("Deploying lunar rover"));
        final var asyncResult5 =
                executor.startProcess(lazyval("callback", 600), callback("Deploying lunar rover"));

        // emulate processing in the current thread while async tasks are running in their own threads
        Thread.sleep(350); // Oh boy, we are working hard here
        log("Mission command is sipping coffee");

        // wait for completion of the tasks
        final var result1 = executor.endProcess(asyncResult1);
        final var result2 = executor.endProcess(asyncResult2);
        final var result3 = executor.endProcess(asyncResult3);
        asyncResult4.await();
        asyncResult5.await();

        // log the results of the tasks, callbacks log immediately when complete
        log("Space rocket <" + result1 + "> launch complete");
        log("Space rocket <" + result2 + "> launch complete");
        log("Space rocket <" + result3 + "> launch complete");
    }

    /**
     * Creates a callable that lazily evaluates to given value with artificial delay.
     *
     * @param value       value to evaluate
     * @param delayMillis artificial delay in milliseconds
     * @return new callable for lazy evaluation
     */
    private static <T> Callable<T> lazyval(T value, long delayMillis) {
        return () -> {
            Thread.sleep(delayMillis);
            log("Space rocket <" + value + "> launched successfully");
            return value;
        };
    }

    /**
     * Creates a simple callback that logs the complete status of the async result.
     *
     * @param name callback name
     * @return new async callback
     */
    private static <T> AsyncCallback<T> callback(String name) {
        return (value, ex) -> {
            if (ex.isPresent()) {
                log(name + " failed: " + ex.map(Exception::getMessage).orElse(""));
            } else {
                log(name + " <" + value + ">");
            }
        };
    }

    private static void log(String msg) {
       LOGGER.info(msg);
    }
}

