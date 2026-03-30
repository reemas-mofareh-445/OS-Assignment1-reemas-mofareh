# Development Log

---

## Entry 1 — March 25, 2026, 10:15 AM

**What I did**: Repository setup and initial code exploration

**Details**:
- Created GitHub account using university email (445052261@std.psau.edu.sa)
- Forked the starter repository from `https://github.com/makopt/OS-Assignment1-Starter`
- Renamed the repository to `OS-Assignment1-reemas-mofareh`
- Verified the repository is set to **Public** in Settings → Danger Zone
- Cloned the repository locally with VS Code: `git clone ...`
- Opened `SchedulerSimulation.java` and read through the entire file to understand the `Process` class and the scheduling loop

**Challenges**:
- Initially confused about why `addProcessToQueue` creates a **new** `Thread` object every time a process is re-enqueued. I expected one thread per process for its entire lifetime.

**Solution**:
- Re-read the Java documentation for `Thread`: once a thread terminates (its `run()` returns), it **cannot** be restarted — `start()` would throw `IllegalThreadStateException`. So a fresh `Thread` wrapper must be created for each quantum.

**Time spent**: 45 minutes

---

## Entry 2 — March 26, 2026, 2:30 PM

**What I did**: Changed student ID and verified unique output; started Feature 1 (Priority)

**Details**:
- Found line 150 in `SchedulerSimulation.java` where `studentID` is declared
- Changed the placeholder `123456789` to my actual student ID `445052261`
- Compiled with `javac SchedulerSimulation.java` and ran with `java SchedulerSimulation` to confirm the simulation generates a unique time quantum and process count based on my ID
- Committed: `"Set my student ID: 445052261"`
- Began implementing **Feature 1**: added `private int priority;` field to the `Process` class and updated the constructor signature to accept a `priority` parameter

**Challenges**:
- The existing constructor `Process(String name, int burstTime, int timeQuantum)` is called in `main()`. Adding a fourth parameter meant I had to update every call site.

**Solution**:
- Updated the constructor and all call sites in one edit, then tested that the code still compiled cleanly before committing.

**Time spent**: 1 hour

---

## Entry 3 — March 27, 2026, 11:00 AM

**What I did**: Completed Feature 1 and implemented Feature 2 (Context Switch Counter)

**Details**:
- Finished Feature 1:
  - Added `int priority = 1 + random.nextInt(5);` in the process-creation loop so each process gets a random priority 1–5
  - Updated `addProcessToQueue()` to print `(Priority: X)` next to the process name
  - Tested output — confirmed that each process shows its priority when entering the ready queue
  - Committed: `"Feature 1: Added priority field to Process class"`
- Implemented Feature 2:
  - Added `private static int contextSwitchCount = 0;` at the top of `SchedulerSimulation`
  - Incremented `contextSwitchCount++` right before `currentThread.start()` in the scheduling loop — this is the exact moment the CPU is handed to a new process
  - Added a print statement at the end of `main()`: `"📊 Total context switches: " + contextSwitchCount`
  - Also added `"[Context Switch #" + contextSwitchCount + "]"` inline just before each process starts so the operator can trace every switch in real time
  - Committed: `"Feature 2: Implemented context switch counter"`

**Challenges**:
- Debated where exactly to increment the counter. Should it be before `start()`, or after `join()`?

**Solution**:
- A context switch happens *before* the new process starts running (the CPU state is saved and the new process's state is loaded). So incrementing just before `start()` is semantically correct.

**Time spent**: 1.5 hours

---

## Entry 4 — March 28, 2026, 9:00 AM

**What I did**: Implemented Feature 3 (Waiting Time Tracking)

**Details**:
- Added three fields to `Process`:
  - `creationTime` — set in the constructor with `System.currentTimeMillis()`
  - `lastEnqueueTime` — updated every time the process enters the queue
  - `totalWaitingTime` — accumulates the difference `(dequeueTime - lastEnqueueTime)` each time the process starts running
- Added two helper methods:
  - `recordEnqueueTime()` — called at the end of `addProcessToQueue()`, captures the current timestamp
  - `recordDequeueTime()` — called in the scheduling loop *before* `currentThread.start()`, adds elapsed waiting time to `totalWaitingTime`
- At the end of `main()`, printed a formatted table showing Process Name, Burst Time, and Waiting Time for every process
- Committed: `"Feature 3: Added waiting time tracking and summary table"`

**Challenges**:
- The `processMap` contains **multiple keys** for the same process (because a new `Thread` is created every time the process is re-enqueued). Iterating over `processMap.values()` would print the same process multiple times in the summary table.

**Solution**:
- Used a `HashSet<String>` called `printed` to track which process names had already been printed. Before adding a row, I check `!printed.contains(p.getName())` and skip duplicates.

**Time spent**: 2 hours

---

## Entry 5 — March 29, 2026, 3:00 PM

**What I did**: Full end-to-end testing and documentation

**Details**:
- Ran the simulation several times and verified:
  - ✅ Every process shows `(Priority: X)` when entering the ready queue
  - ✅ Context switch counter increments by 1 for every process start
  - ✅ The total at the end matches the number of `[Context Switch #N]` lines in the output
  - ✅ The waiting time table appears at the end with one row per process
  - ✅ Waiting times are non-negative and plausible (roughly proportional to position in queue and number of re-queues)
- Started filling in `ANSWERS.md`, `DEVELOPMENT_LOG.md`, and `REFLECTION.md`
- Pushed all changes: `git push origin main`

**Challenges**:
- The waiting times printed `0ms` for the very first processes because they entered and immediately started running (no wait). This is correct behavior but initially looked like a bug.

**Solution**:
- Traced through the logic: P1 enters the queue, `recordEnqueueTime()` is called, then almost immediately `recordDequeueTime()` is called → elapsed ≈ 0 ms. That is accurate — P1 didn't wait at all in the first round.

**Time spent**: 1.5 hours

---

## Entry 6 — March 30, 2026, 10:00 AM

**What I did**: Final review, markdown files, and submission preparation

**Details**:
- Completed all three markdown documentation files (`ANSWERS.md`, `DEVELOPMENT_LOG.md`, `REFLECTION.md`)
- Did a final read-through of `SchedulerSimulation.java` to make sure all comments are clear and accurate
- Verified commit history: 5 meaningful commits spread over 6 days
- Confirmed repository is still **Public**
- Prepared the submission text file for Blackboard

**Challenges**:
- Making sure the ANSWERS.md answers are in my own words and reference actual output from the simulation.

**Solution**:
- Ran the simulation one more time, copied real output snippets into `ANSWERS.md`, and rewrote any sections that felt too generic.

**Time spent**: 1 hour

---

## Summary

**Total time spent on assignment**: ~8 hours

**Most challenging part**: Feature 3 (waiting time) — specifically handling the duplicate entries in `processMap` and ensuring `recordDequeueTime()` is called at exactly the right moment in the scheduling loop.

**Most interesting learning**: Discovering that Java threads cannot be restarted after termination. This seemingly minor detail explains why `addProcessToQueue()` must always create a *new* `Thread` object, which in turn explains why `processMap` ends up with multiple keys for the same process.

**What I would do differently next time**: Write a small unit test for the waiting-time calculation before integrating it into the full simulation, to catch the "duplicate map keys" issue earlier.
