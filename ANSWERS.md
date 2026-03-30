# Assignment Questions – Answers

---

## Question 1: Thread vs Process

**Question**: Explain the difference between a **thread** and a **process**. Why did we use threads in this assignment instead of creating separate processes?

**Answer:**

A **process** is a fully independent program in execution, with its own private memory space, file handles, and system resources managed by the operating system. A **thread**, on the other hand, is a lightweight unit of execution that lives *inside* a process and shares its memory space with every other thread in the same process. Creating a new process is expensive because the OS must duplicate the parent's memory image (via `fork()` on Unix or `CreateProcess()` on Windows), whereas spawning a new thread requires only a new stack and register set — the same heap and code segment are reused.

In this assignment, we used Java threads (`new Thread(process)`) rather than separate processes for two key reasons. First, **shared memory**: the `SchedulerSimulation` class needs to inspect every process's `remainingTime` after each quantum, which is trivial when threads share the same heap but would require inter-process communication (pipes, shared memory, sockets) with separate processes. Second, **low creation overhead**: with up to 20 processes each potentially re-queued many times, spawning a full OS process for each scheduling event would be prohibitively slow. Thread creation in Java takes microseconds; process creation takes milliseconds or more. The `processMap` in `SchedulerSimulation.java` illustrates this sharing — all threads read/write the same `Process` objects without any synchronization overhead.

---

## Question 2: Ready Queue Behavior

**Question**: In Round-Robin scheduling, what happens when a process doesn't finish within its time quantum? Explain using an example from your program output.

**Answer:**

When a process exhausts its time quantum without finishing, it is **preempted** — removed from the CPU — and placed back at the **tail** of the ready queue so that other processes get their fair share of CPU time. Only after all other processes have had a turn does this process get to run again. This cycling behavior is the core fairness guarantee of Round-Robin scheduling.

Example from program output:
```
  ➕ P3 (Priority: 2) added to ready queue │ Burst time: 7500ms │ Remaining: 7500ms
  ...
  [Context Switch #3]
  ▶ P3 executing quantum [3000ms]
  ⏸ P3 completed quantum 3000ms │ Overall progress: [████████░░░░░░░░░░░░] 40%
     Remaining time: 4500ms
  ↻ P3 yields CPU for context switch
  ➕ P3 (Priority: 2) added to ready queue │ Burst time: 7500ms │ Remaining: 4500ms
```

In this example, **P3** needs 7500 ms but the time quantum is only 3000 ms. After its first quantum the remaining time drops to 4500 ms and `addProcessToQueue()` is called again, placing P3 at the back of the queue. This re-queuing is essential for fairness: without it, a long-running process like P3 could monopolize the CPU and starve shorter processes of execution time.

---

## Question 3: Thread States

**Question**: Walk through the thread lifecycle states (New → Runnable → Running → Waiting → Terminated) for process P1.

**Answer:**

1. **New** – P1 enters the *New* state the instant `new Thread(process)` is called inside `addProcessToQueue()`. At this point a `Thread` object exists in the JVM but has not yet been scheduled by the OS. No resources beyond the object itself have been allocated.

2. **Runnable** – P1 moves to *Runnable* when `currentThread.start()` is called in the scheduler loop. The JVM registers P1's thread with the OS thread scheduler, which places it in the OS run queue. The thread is *eligible* to run but may not be on the CPU yet.

3. **Running** – P1 enters *Running* the moment the OS assigns it a CPU core and begins executing `Process.run()`. The method's `Thread.sleep(stepTime)` calls repeatedly pause and resume P1 as the quantum progress bar updates, but between sleeps the thread is actively on the CPU.

4. **Waiting** – P1 enters the *Waiting* (or *Timed Waiting*) state each time `Thread.sleep(stepTime)` is called inside `run()`. The thread relinquishes the CPU for `stepTime` milliseconds and is automatically moved back to *Runnable* when the timer expires. Additionally, the *main* thread enters *Waiting* when it calls `currentThread.join()`, blocking until P1's quantum finishes.

5. **Terminated** – P1's thread reaches *Terminated* when `run()` returns — either because `remainingTime` dropped to zero (process finished) or the process was re-queued and a new `Thread` will be created for its next quantum. Once terminated, the thread object cannot be restarted; `addProcessToQueue()` always creates a fresh `Thread` instance for re-queued processes.

---

## Question 4: Real-World Applications

**Question**: Describe **two** real-world scenarios where Round-Robin scheduling with threads would be useful.

---

### Example 1: Multi-Player Online Game Server

**Description**: A game server handles dozens or hundreds of players simultaneously. Each connected player corresponds to a session that needs periodic CPU time to process inputs (key presses, mouse clicks), update game state, and send updates back to the client. The server uses a pool of threads, one (or a small set) per player session.

**Why Round-Robin works well here**: Fairness is critical — no single player should experience lag because another player's input processing is hogging the CPU. Round-Robin's fixed time quantum guarantees that every session gets a predictable slice of CPU time before yielding, keeping input latency consistent for all players regardless of how complex their session's logic is. The re-queuing behavior mirrors exactly how the simulation re-enqueues P3 when it hasn't finished: a player's session thread runs for one quantum, yields, and rejoins the ready queue so the next player's thread can run.

---

### Example 2: Web Server Handling HTTP Requests

**Description**: A web server (e.g., Apache, Nginx with worker threads) receives thousands of HTTP requests per second. Each incoming request is assigned to a worker thread that reads the request, fetches data (from a database or file system), and sends a response. When many requests arrive simultaneously, threads wait in a queue and are dispatched to available CPU cores.

**Why Round-Robin works well here**: Different requests have very different processing times — a simple static-file request completes in milliseconds, while a database-heavy API call may take seconds. Round-Robin prevents any single long-running request from blocking short ones, keeping average response time low and giving every client a fair share of server resources. This is directly analogous to how our simulation prevents P1 (which might have a short burst time) from being starved by P3 (which has a long burst time): Round-Robin's preemption ensures responsiveness for all clients.

---

## Summary

**Key concepts understood through these questions:**
1. Threads share memory within a process, making them far cheaper and easier to coordinate than separate OS processes — crucial for a scheduler that must inspect process state after every quantum.
2. Round-Robin's re-queuing mechanism is the fundamental mechanism that provides fairness: no process can run for more than one time quantum before every other ready process gets a turn.
3. A thread's lifecycle (New → Runnable → Running → Timed Waiting → Terminated) maps directly onto specific Java method calls: `new Thread()`, `start()`, executing `run()`, `Thread.sleep()`, and `run()` returning.

**Concepts to study more:**
1. Thread synchronization (mutexes, semaphores) — needed when multiple threads write shared data concurrently.
2. Priority-based and Multilevel Feedback Queue scheduling — extensions to Round-Robin that take process priority into account.
