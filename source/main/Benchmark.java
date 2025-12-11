package main;

import constructions.*;
import core.*;
import objects.*;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicBoolean;

public class Benchmark {
    static final int NUM_THREADS = 8;
    static final int DURATION_MS = 2000;
    static final AtomicLong totalOps = new AtomicLong(0);

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Starting Universality Benchmark ===");
        
        // ---------------------------------------------------------------
        // 1. Benchmark Lock-Free Queue
        // ---------------------------------------------------------------
        Universal lfQueue = new LockFreeUniversal(NUM_THREADS) {
            @Override protected SeqObject getFreshObject() { return new SeqQueue(); }
        };
        runTest("Lock-Free Queue", lfQueue);
        validateLog(lfQueue); // Verify the history


        // ---------------------------------------------------------------
        // 2. Benchmark Wait-Free Queue
        // ---------------------------------------------------------------
        Universal wfQueue = new WaitFreeUniversal(NUM_THREADS) {
            @Override protected SeqObject getFreshObject() { return new SeqQueue(); }
        };
        runTest("Wait-Free Queue", wfQueue);
        validateLog(wfQueue);


        // ---------------------------------------------------------------
        // 3. Benchmark Lock-Free Bank (Complex Transactions)
        // ---------------------------------------------------------------
        Universal lfBank = new LockFreeUniversal(NUM_THREADS) {
            @Override protected SeqObject getFreshObject() { return new SeqBank(); }
        };
        runBankTest("Lock-Free Bank", lfBank);
        validateLog(lfBank);
    }

    // Standard Randomized Load Test
    public static void runTest(String name, Universal obj) throws InterruptedException {
        totalOps.set(0);
        Thread[] threads = new Thread[NUM_THREADS];
        AtomicBoolean running = new AtomicBoolean(true); 

        for (int i = 0; i < NUM_THREADS; i++) {
            threads[i] = new Thread(() -> {
                while (running.get()) { 
                    if (Math.random() > 0.5) {
                        obj.apply(new Invocation("enq", new Object[]{1}));
                    } else {
                        obj.apply(new Invocation("deq", null));
                    }
                    totalOps.incrementAndGet();
                }
            });
        }
        runBenchmarkLogic(name, threads, running);
    }

    // Specialized Bank Test
    public static void runBankTest(String name, Universal obj) throws InterruptedException {
        totalOps.set(0);
        Thread[] threads = new Thread[NUM_THREADS];
        AtomicBoolean running = new AtomicBoolean(true); 

        for (int i = 0; i < NUM_THREADS; i++) {
            threads[i] = new Thread(() -> {
                while (running.get()) { 
                    double chance = Math.random();
                    if (chance < 0.1) {
                        obj.apply(new Invocation("balance", new Object[]{"User1"}));
                    } else if (chance < 0.6) {
                        obj.apply(new Invocation("deposit", new Object[]{"User1", 10}));
                    } else {
                        obj.apply(new Invocation("transfer", new Object[]{"User1", "User2", 5}));
                    }
                    totalOps.incrementAndGet();
                }
            });
        }
        runBenchmarkLogic(name, threads, running);
    }

    // Core Timing Logic
    private static void runBenchmarkLogic(String name, Thread[] threads, AtomicBoolean running) throws InterruptedException {
        long start = System.currentTimeMillis();
        for (Thread t : threads) t.start();
        
        Thread.sleep(DURATION_MS);
        running.set(false); 
        
        for (Thread t : threads) t.join();
        long end = System.currentTimeMillis();

        double throughput = totalOps.get() / ((end - start) / 1000.0);
        System.out.printf("%-25s: %.2f ops/sec  (Total Ops: %d)%n", name, throughput, totalOps.get());
    }

    // --------------------------------------------------------------------
    // VALIDATION LOGIC
    // --------------------------------------------------------------------
    public static void validateLog(Universal obj) {
        int actualNodes = 0;
        
        // Reflection-style check to access the log length
        if (obj instanceof LockFreeUniversal) {
            actualNodes = ((LockFreeUniversal) obj).getLogLength();
        } else if (obj instanceof WaitFreeUniversal) {
            actualNodes = ((WaitFreeUniversal) obj).getLogLength();
        } else {
            System.out.println("  -> Skipping validation (Unknown implementation)");
            return;
        }
        
        // Verification: The number of nodes in the linked list MUST match 
        // the number of operations our threads successfully finished.
        if (actualNodes == totalOps.get()) {
            System.out.println("  -> [PASSED] Log Consistency Check. Nodes: " + actualNodes);
        } else {
            System.err.println("  -> [FAILED] Consistency Error! Log Nodes: " + actualNodes + " vs Ops: " + totalOps.get());
        }
    }
}