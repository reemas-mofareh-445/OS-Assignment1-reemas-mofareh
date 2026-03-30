# Reflection Questions

---

## Question 1: What did you learn about multithreading?

Before this assignment, I had a vague idea that threads allow programs to "do multiple things at once," but I had never traced through what that actually means at the code level. Working with `Process implements Runnable` made it concrete: a thread is just an independent call stack that executes a single method (`run()`) and then stops. What surprised me most was the thread lifecycle — specifically that a terminated thread **cannot be restarted**. This forced the scheduler to create a brand-new `Thread` object every time a process was re-enqueued, which initially looked like a design flaw but is actually a fundamental Java constraint. I also learned to appreciate `Thread.sleep()` as more than just a "pause" — it actively relinquishes the CPU, allowing other threads to run, and causes the calling thread to enter a timed-waiting state. Seeing `Thread.join()` in the main loop showed me how one thread can block on another, which is the foundation of synchronization. Finally, the color-coded output made thread interleaving *visible*: I could watch P3 yield the CPU and P4 immediately take over, which made the abstract concept of Round-Robin scheduling feel real.

---

## Question 2: What was the most challenging part of this assignment?

The most challenging part was implementing **Feature 3 (waiting time tracking)** correctly. The core difficulty was that `processMap` maps *threads* (not processes) to `Process` objects, and a new `Thread` is created every time a process is re-queued. This means the same `Process` object can appear as a value under *multiple* keys in the map, and a naive iteration over `processMap.values()` would print the same process several times in the summary table. I also had to decide exactly *when* to call `recordEnqueueTime()` and `recordDequeueTime()` — off-by-a-few-milliseconds errors would silently produce wrong waiting times that still looked plausible. Getting the timing right required carefully reading the scheduling loop and understanding the sequence: enqueue → wait in queue → dequeue → start running. Understanding which Java call corresponds to each transition (the `add()` call in `addProcessToQueue`, versus the `start()` call in the main loop) required tracing the code more carefully than I had for the earlier features.

---

## Question 3: How did you overcome the challenges you faced?

My primary strategy was to **add print statements at key moments** and compare the output against my mental model. For example, when I suspected `recordDequeueTime()` was being called too late, I printed the current time and `lastEnqueueTime` side by side before and after the call to confirm the difference was being captured correctly. For the duplicate-map-key problem, I first printed all `processMap.values()` to the console to see exactly how many times each `Process` appeared, which immediately revealed the root cause. Once I understood the issue, the fix was straightforward: a `HashSet<String>` of already-printed process names. I also consulted the official Java documentation for `Thread` to understand why `start()` throws `IllegalThreadStateException` on an already-terminated thread — reading the specification directly, rather than guessing, saved me from chasing a phantom bug. Finally, I committed working code after each feature before moving to the next one, so I always had a known-good checkpoint to roll back to if something broke.

---

## Question 4: How can you apply multithreading concepts in real-world applications?

Multithreading is everywhere in the software I use every day, and this assignment gave me the vocabulary to recognize it. A **web browser** is the clearest example: each open tab runs in its own thread (or process), so a JavaScript-heavy page in one tab cannot freeze the tab I'm reading in another. The browser's scheduler is analogous to our `SchedulerSimulation` — it gives each tab a quantum of rendering time and then moves on. A **mobile app** like a music player uses a dedicated audio thread that runs continuously at high priority to prevent dropouts, while a lower-priority UI thread handles button presses. Without thread separation, a slow network call to fetch album art could stutter the audio — exactly the kind of starvation our Round-Robin scheduler prevents. **Database servers** use thread pools where each incoming query is assigned to a worker thread, and the OS scheduler ensures long-running analytical queries don't block fast transactional ones. Even **video games** apply multithreading: physics calculations run on one thread, rendering on another, and AI on a third, with the main loop coordinating them much like our `main()` coordinates the process threads via `join()`. Understanding `Thread.start()`, `Thread.join()`, and `Thread.sleep()` gives me a concrete foundation to reason about all of these systems.

---

## Additional Reflections

### What would you like to learn more about?

I would like to learn about **thread synchronization** — specifically mutexes, semaphores, and Java's `synchronized` keyword. In this assignment every process ran independently with no shared mutable state (other than the static counter, which was only written from the main thread). In real applications, multiple threads often need to read and write the same data, and getting that right without race conditions or deadlocks is a much harder problem. I am also curious about **lock-free data structures** and how they avoid the overhead of locks entirely.

---

### How confident do you feel about multithreading concepts now?

**Intermediate.**

I am confident about: thread creation (`new Thread(runnable)`), the thread lifecycle, `Thread.start()` / `join()` / `sleep()`, and why Round-Robin scheduling provides fairness. I feel comfortable reading and modifying multithreaded Java code that follows the pattern shown in this assignment.

I still need practice with: concurrent data structures, the Java `ExecutorService` framework, and debugging race conditions — areas that require deeper experience than one assignment can provide.

---

### Feedback on the assignment

The assignment was well-designed because it gave me working, visually rich code to start from rather than a blank file. Being able to *run* the starter code immediately and *see* the color-coded output made the abstract concept of Round-Robin scheduling tangible from day one. The requirement to make one commit per feature was an excellent discipline — it forced me to understand each feature fully before moving on, rather than writing everything at once and committing a mess. If I could suggest one improvement, it would be to add a short paragraph in the README explaining *why* a new `Thread` must be created for each re-enqueue (the "terminated thread cannot restart" rule), since discovering this on my own cost me about 20 minutes of confusion.
