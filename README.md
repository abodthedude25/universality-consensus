
# Universality of Consensus: Lock-Free & Wait-Free Constructions

**A Java implementation of Herlihy & Shavit's Universal Construction.**

This project demonstrates one of the most powerful theoretical results in Distributed Computing: **Universality**. It proves that any hardware primitive with a Consensus Number of $\infty$ (like `Compare-And-Swap`) can be used to create a thread-safe, lock-free implementation of *any* sequential data structure (Queue, Stack, Bank, etc.) without needing custom locking logic.

## 🚀 Overview

In concurrent programming, writing correct lock-free data structures is notoriously difficult. If you want a lock-free Queue, you write one algorithm. If you want a lock-free Tree, you start from scratch.

**Universality** solves this by providing a generic wrapper. You plug in a standard, single-threaded object, and the wrapper automatically makes it concurrent using a shared **Consensus Log**.

### Key Features
* **Lock-Free Construction:** Guarantees system-wide progress. Uses a shared linked list of operations.
* **Wait-Free Construction:** Guarantees individual thread progress. Uses an `announce` array and a "helping" mechanism to prevent starvation.
* **Complex Transactions:** Includes a `SeqBank` object demonstrating atomic multi-step transactions (transfers) without deadlocks.
* **Linearizability Validator:** A built-in audit tool that replays the log to prove the concurrent history is consistent.

## 📚 Theoretical Background

### 1. The Consensus Hierarchy
Not all hardware instructions are created equal. The "power" of an instruction is measured by how many threads it can coordinate to solve the Consensus Problem (agreeing on a single value).

| Consensus Number | Primitive | Power |
| :--- | :--- | :--- |
| **1** | Read/Write Registers | Cannot solve consensus for >1 thread (FLP Theorem). |
| **2** | Test-And-Set, Queue | Can coordinate 2 threads, but fails for 3. |
| **$\infty$** | **Compare-And-Swap (CAS)** | **Universal.** Can coordinate $N$ threads. |

### 2. How This Solution Works
Instead of modifying a shared memory state directly (which requires locks), threads append their method calls to a global **Log of History**.

1.  **Wrap:** A thread packages its method call (e.g., `enqueue(5)`) into a `Node`.
2.  **Consensus:** Threads fight to append their node to the end of the log using a `Consensus` object.
3.  **Replay:** Once a node is added, the thread replays the entire history locally to compute the return value.

```text
[Sentinel] -> [Node 1: enq(A)] -> [Node 2: deq()] -> [Node 3: transfer(X,Y)] -> NULL
   ^                                                                            ^
 Start                                                                         Tail
````

## 📂 Project Structure

```text
src/
├── core/
│   ├── Consensus.java       # Atomic Reference wrapper (The infinite power primitive)
│   ├── Node.java            # Linked List node holding the operation and consensus object
│   └── SeqObject.java       # Interface for any sequential data structure
│
├── constructions/
│   ├── LockFreeUniversal.java # Implementation of Fig 6.4 (Standard Universality)
│   └── WaitFreeUniversal.java # Implementation of Fig 6.6 (With Helping Mechanism)
│
├── objects/
│   ├── SeqQueue.java        # Standard FIFO Queue
│   ├── SeqBank.java         # Bank logic (Deposit/Transfer) to test complex atomicity
│   └── SeqStack.java        # Standard LIFO Stack
│
└── main/
    └── Benchmark.java       # Load tester, throughput metrics, and Log Validator
```

## 🛠️ Installation & Usage

**Prerequisites:** Java Development Kit (JDK) 11 or higher.

1.  **Compile the source code:**

    ```bash
    cd src
    javac main/Benchmark.java core/*.java constructions/*.java objects/*.java
    ```

2.  **Run the Benchmark:**

    ```bash
    java main.Benchmark
    ```

## 📊 Benchmark & Validation

The benchmark spawns **8 concurrent threads** that hammer the data structures with operations.

### Expected Output

You will see lower throughput for the Wait-Free implementation compared to Lock-Free (due to the overhead of the "helping" logic), but the Wait-Free version guarantees zero starvation.

```text
=== Starting Universality Benchmark ===
Lock-Free Queue     : 12504.50 ops/sec  (Total Ops: 25009)
  -> [PASSED] Log Consistency Check. Nodes: 25009
Wait-Free Queue     : 9800.20 ops/sec   (Total Ops: 19600)
  -> [PASSED] Log Consistency Check. Nodes: 19600
Lock-Free Bank      : 11200.10 ops/sec  (Total Ops: 22400)
  -> [PASSED] Log Consistency Check. Nodes: 22400
```

*The **Log Consistency Check** proves Linearizability: The number of nodes successfully appended to the log exactly matches the number of operations the threads reported as "finished."*

## 🧠 Deep Dive: Wait-Free Helping

The **Wait-Free** implementation prevents starvation using a "cyclic priority" system. Before a thread can execute its own task, it calculates a target to help:

```java
// Cyclic Priority Logic
Node help = announce[(before.seq + 1) % n];
if (help.seq == 0) {
    prefer = help; // Forced to help the neighbor!
}
```

This ensures the "priority spotlight" rotates through every thread index ($0 \to 1 \to 2 \dots$). No matter how slow a thread is, the spotlight eventually lands on it, forcing the entire system to stop and process its request.

## 🔗 References

  * **Textbook:** *The Art of Multiprocessor Programming* by Maurice Herlihy & Nir Shavit.
  * **Chapters Covered:**
      * Chapter 6: Universality of Consensus
      * Chapter 5: Relative Power of Synchronization Operations
      * Chapter 3: Linearizability

