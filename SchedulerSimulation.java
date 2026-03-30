import java.util.LinkedList;
import java.util.Queue;
import java.util.Map;
import java.util.HashMap;
import java.util.Random;

// ANSI Color Codes for enhanced terminal output
class Colors {
    public static final String RESET = "\u001B[0m";
    public static final String BOLD = "\u001B[1m";
    public static final String CYAN = "\u001B[36m";
    public static final String GREEN = "\u001B[32m";
    public static final String YELLOW = "\u001B[33m";
    public static final String MAGENTA = "\u001B[35m";
    public static final String BLUE = "\u001B[34m";
    public static final String RED = "\u001B[31m";
    public static final String BG_BLUE = "\u001B[44m";
    public static final String BG_GREEN = "\u001B[42m";
    public static final String WHITE = "\u001B[37m";
    public static final String BRIGHT_WHITE = "\u001B[97m";
    public static final String BRIGHT_CYAN = "\u001B[96m";
    public static final String BRIGHT_YELLOW = "\u001B[93m";
    public static final String BRIGHT_GREEN = "\u001B[92m";
}

// Class representing a process that implements Runnable to be run by a thread
class Process implements Runnable {
    private String name;        // Name of the process
    private int burstTime;      // Total time the process requires to complete (in milliseconds)
    private int timeQuantum;    // Time slice (time quantum) allowed per CPU access (in milliseconds)
    private int remainingTime;  // Time left for the process to finish its execution

    // =====================================================================
    // FEATURE 1: Process Priority
    // Added a priority field (integer 1-5, where 5 is the highest priority).
    // Priority is assigned randomly when the process is created.
    // =====================================================================
    private int priority; // Priority level: 1 (lowest) to 5 (highest)

    // =====================================================================
    // FEATURE 3: Waiting Time Tracking
    // Added two fields to track:
    //   - creationTime: the moment the process was first created (ms since epoch)
    //   - totalWaitingTime: total time the process spent waiting in the queue
    // These are updated using System.currentTimeMillis().
    // =====================================================================
    private long creationTime;      // Timestamp when the process was first created
    private long totalWaitingTime;  // Total time spent waiting in the ready queue (ms)
    private long lastEnqueueTime;   // Timestamp when the process most recently entered the queue

    /**
     * Constructor - initializes the process with name, burst time, time quantum, and priority.
     * Also records the creation time for waiting-time calculations (Feature 3).
     *
     * @param name       Unique process name (e.g., "P1")
     * @param burstTime  Total CPU time required (ms)
     * @param timeQuantum Time slice per scheduling round (ms)
     * @param priority   Priority level 1-5 (Feature 1)
     */
    public Process(String name, int burstTime, int timeQuantum, int priority) {
        this.name = name;
        this.burstTime = burstTime;
        this.timeQuantum = timeQuantum;
        this.remainingTime = burstTime; // Initially, remaining time equals burst time

        // Feature 1: Store the randomly assigned priority
        this.priority = priority;

        // Feature 3: Record creation time; waiting time starts from creation
        this.creationTime     = System.currentTimeMillis();
        this.lastEnqueueTime  = this.creationTime; // First enqueue == creation
        this.totalWaitingTime = 0;
    }

    // This method will be called when the thread for this process is started
    @Override
    public void run() {
        // Simulate running for either the time quantum or remaining time, whichever is smaller
        int runTime = Math.min(timeQuantum, remainingTime);

        // Show quantum execution starting
        String quantumBar = createProgressBar(0, 15);
        System.out.println(Colors.BRIGHT_GREEN + "  ▶ " + Colors.BOLD + Colors.CYAN + name +
                          Colors.RESET + Colors.GREEN + " executing quantum" + Colors.RESET +
                          " [" + runTime + "ms] ");

        try {
            // Simulate quantum execution with progress updates
            int steps = 5;
            int stepTime = runTime / steps;

            for (int i = 1; i <= steps; i++) {
                Thread.sleep(stepTime);
                int quantumProgress = (i * 100) / steps;
                quantumBar = createProgressBar(quantumProgress, 15);

                // Clear line and show updated progress
                System.out.print("\r  " + Colors.YELLOW + "⚡" + Colors.RESET +
                                " Quantum progress: " + quantumBar);
            }
            System.out.println(); // New line after quantum completion

        } catch (InterruptedException e) {
            System.out.println(Colors.RED + "\n  ✗ " + name + " was interrupted." + Colors.RESET);
        }

        remainingTime -= runTime; // Deduct the run time from the remaining time
        int overallProgress = (int) (((double)(burstTime - remainingTime) / burstTime) * 100);
        String overallProgressBar = createProgressBar(overallProgress, 20);

        System.out.println(Colors.YELLOW + "  ⏸ " + Colors.CYAN + name + Colors.RESET +
                          " completed quantum " + Colors.BRIGHT_YELLOW + runTime + "ms" + Colors.RESET +
                          " │ Overall progress: " + overallProgressBar);
        System.out.println(Colors.MAGENTA + "     Remaining time: " + remainingTime + "ms" + Colors.RESET);

        if (remainingTime > 0) {
            System.out.println(Colors.BLUE + "  ↻ " + Colors.CYAN + name + Colors.RESET +
                              " yields CPU for context switch" + Colors.RESET);
        } else {
            System.out.println(Colors.BRIGHT_GREEN + "  ✓ " + Colors.BOLD + Colors.CYAN + name +
                              Colors.RESET + Colors.BRIGHT_GREEN + " finished execution!" +
                              Colors.RESET);
        }
        System.out.println();
    }

    // Helper method to create a visual progress bar
    private String createProgressBar(int progress, int width) {
        int filled = (progress * width) / 100;
        StringBuilder bar = new StringBuilder("[");
        for (int i = 0; i < width; i++) {
            if (i < filled) {
                bar.append(Colors.GREEN + "█" + Colors.RESET);
            } else {
                bar.append(Colors.WHITE + "░" + Colors.RESET);
            }
        }
        bar.append("] ").append(progress).append("%");
        return bar.toString();
    }

    // Method to run the last process to completion, ignoring the time quantum
    public void runToCompletion() {
        try {
            System.out.println(Colors.BRIGHT_CYAN + "  ⚡ " + Colors.BOLD + Colors.CYAN + name +
                              Colors.RESET + Colors.BRIGHT_CYAN + " is the last process, running to completion" +
                              Colors.RESET + " [" + remainingTime + "ms]");
            Thread.sleep(remainingTime);
            remainingTime = 0;
            System.out.println(Colors.BRIGHT_GREEN + "  ✓ " + Colors.BOLD + Colors.CYAN + name +
                              Colors.RESET + Colors.BRIGHT_GREEN + " finished execution!" + Colors.RESET);
            System.out.println();
        } catch (InterruptedException e) {
            System.out.println(Colors.RED + "  ✗ " + name + " was interrupted." + Colors.RESET);
        }
    }

    // ─── Getters ───────────────────────────────────────────────────────────────

    public String getName()        { return name; }
    public int    getBurstTime()   { return burstTime; }
    public int    getRemainingTime() { return remainingTime; }

    /** Feature 1: Returns the priority level (1-5) of this process. */
    public int getPriority()       { return priority; }

    // Feature 3: Waiting-time helpers

    /**
     * Called each time the process is (re-)added to the ready queue.
     * Records the current time so we can measure how long it waits.
     */
    public void recordEnqueueTime() {
        this.lastEnqueueTime = System.currentTimeMillis();
    }

    /**
     * Called just before the process starts running.
     * Adds the time spent waiting since the last enqueue to the running total.
     */
    public void recordDequeueTime() {
        long waited = System.currentTimeMillis() - lastEnqueueTime;
        totalWaitingTime += waited;
    }

    /** Returns total time (ms) this process spent waiting in the ready queue. */
    public long getTotalWaitingTime() { return totalWaitingTime; }

    /** Checks whether the process has finished all its execution. */
    public boolean isFinished() { return remainingTime <= 0; }
}


public class SchedulerSimulation {

    // =====================================================================
    // FEATURE 2: Context Switch Counter
    // A static counter that is incremented every time the CPU switches to
    // a new process.  "Static" ensures there is one shared counter for the
    // entire simulation, not one per instance.
    // =====================================================================
    private static int contextSwitchCount = 0;

    public static void main(String[] args) {
        // ⚠️ IMPORTANT: Student ID is used as the random seed so every student
        //               gets unique simulation parameters.
        int studentID = 445052261; // ← Student ID

        Random random = new Random(studentID);

        // Time quantum: random value in {2000, 3000, 4000, 5000} ms
        int timeQuantum = 2000 + random.nextInt(4) * 1000;

        // Number of processes: random value between 10 and 20
        int numProcesses = 10 + random.nextInt(11);

        // Queue to manage processes in FIFO order
        Queue<Thread> processQueue = new LinkedList<>();

        // Map to associate each thread with its respective Process object
        Map<Thread, Process> processMap = new HashMap<>();

        // ── Simulation header ──────────────────────────────────────────────
        System.out.println("\n" + Colors.BOLD + Colors.BRIGHT_CYAN +
                          "╔═══════════════════════════════════════════════════════════════════════════════════════╗" +
                          Colors.RESET);
        System.out.println(Colors.BOLD + Colors.BRIGHT_CYAN + "║" + Colors.RESET +
                          Colors.BG_BLUE + Colors.BRIGHT_WHITE + Colors.BOLD +
                          "                          CPU SCHEDULER SIMULATION                                " +
                          Colors.RESET + Colors.BOLD + Colors.BRIGHT_CYAN + "║" + Colors.RESET);
        System.out.println(Colors.BOLD + Colors.BRIGHT_CYAN +
                          "╠═══════════════════════════════════════════════════════════════════════════════════════╣" +
                          Colors.RESET);
        System.out.println(Colors.BOLD + Colors.BRIGHT_CYAN + "║" + Colors.RESET +
                          Colors.YELLOW + "  ⚙ Processes:     " + Colors.RESET + Colors.BRIGHT_YELLOW +
                          String.format("%-65s", numProcesses) +
                          Colors.BOLD + Colors.BRIGHT_CYAN + "║" + Colors.RESET);
        System.out.println(Colors.BOLD + Colors.BRIGHT_CYAN + "║" + Colors.RESET +
                          Colors.YELLOW + "  ⏱ Time Quantum:  " + Colors.RESET + Colors.BRIGHT_YELLOW +
                          String.format("%-65s", timeQuantum + "ms") +
                          Colors.BOLD + Colors.BRIGHT_CYAN + "║" + Colors.RESET);
        System.out.println(Colors.BOLD + Colors.BRIGHT_CYAN + "║" + Colors.RESET +
                          Colors.YELLOW + "  🔑 Student ID:    " + Colors.RESET + Colors.BRIGHT_YELLOW +
                          String.format("%-65s", studentID) +
                          Colors.BOLD + Colors.BRIGHT_CYAN + "║" + Colors.RESET);
        System.out.println(Colors.BOLD + Colors.BRIGHT_CYAN +
                          "╚═══════════════════════════════════════════════════════════════════════════════════════╝" +
                          Colors.RESET + "\n");

        // ── Create processes ───────────────────────────────────────────────
        for (int i = 1; i <= numProcesses; i++) {
            // Burst time: random between timeQuantum/2 and 3*timeQuantum
            int burstTime = timeQuantum / 2 + random.nextInt(2 * timeQuantum + 1);

            // Feature 1: Assign a random priority between 1 and 5
            int priority = 1 + random.nextInt(5); // nextInt(5) gives 0-4, so +1 → 1-5

            Process process = new Process("P" + i, burstTime, timeQuantum, priority);
            addProcessToQueue(process, processQueue, processMap);
        }

        // ── Scheduler start banner ─────────────────────────────────────────
        System.out.println(Colors.BOLD + Colors.GREEN +
                          "╔════════════════════════════════════════════════════════════════════════════════╗" +
                          Colors.RESET);
        System.out.println(Colors.BOLD + Colors.GREEN + "║" + Colors.RESET +
                          Colors.BG_GREEN + Colors.WHITE + Colors.BOLD +
                          "                        ▶  SCHEDULER STARTING  ◀                               " +
                          Colors.RESET + Colors.BOLD + Colors.GREEN + "║" + Colors.RESET);
        System.out.println(Colors.BOLD + Colors.GREEN +
                          "╚════════════════════════════════════════════════════════════════════════════════╝" +
                          Colors.RESET + "\n");

        // ── Scheduling loop ────────────────────────────────────────────────
        while (!processQueue.isEmpty()) {
            Thread currentThread = processQueue.poll();
            Process process = processMap.get(currentThread);

            // Feature 3: Record the moment this process leaves the queue so we
            //            can calculate how long it waited.
            process.recordDequeueTime();

            // Feature 2: Every time the CPU picks a new process, it is a context switch.
            contextSwitchCount++;

            // ── Print ready queue state ────────────────────────────────────
            System.out.println(Colors.BOLD + Colors.MAGENTA + "┌─ Ready Queue " + "─".repeat(65) + Colors.RESET);
            System.out.print(Colors.MAGENTA + "│ " + Colors.RESET + Colors.BRIGHT_WHITE + "[" + Colors.RESET);
            int queueCount = 0;
            for (Thread t : processQueue) {
                Process p = processMap.get(t);
                if (queueCount > 0) System.out.print(Colors.WHITE + " → " + Colors.RESET);
                System.out.print(Colors.BRIGHT_CYAN + p.getName() + Colors.RESET);
                queueCount++;
            }
            if (queueCount == 0) {
                System.out.print(Colors.YELLOW + "empty" + Colors.RESET);
            }
            System.out.println(Colors.BRIGHT_WHITE + "]" + Colors.RESET);
            System.out.println(Colors.BOLD + Colors.MAGENTA + "└" + "─".repeat(79) + Colors.RESET + "\n");

            // ── Feature 2: Show context-switch number as the process starts ─
            System.out.println(Colors.BRIGHT_YELLOW + "  [Context Switch #" + contextSwitchCount + "]" +
                               Colors.RESET);

            // Start the thread → runs process.run() for one quantum
            currentThread.start();

            try {
                currentThread.join(); // Wait for this quantum to finish
            } catch (InterruptedException e) {
                System.out.println("Main thread interrupted.");
            }

            // Re-enqueue if not finished
            if (!process.isFinished()) {
                if (!processQueue.isEmpty()) {
                    addProcessToQueue(process, processQueue, processMap);
                } else {
                    // Last process remaining: run it all the way through
                    System.out.println(Colors.BRIGHT_YELLOW + "  ⚠ " + Colors.CYAN + process.getName() +
                                      Colors.RESET + Colors.YELLOW + " is the last process → running to completion" +
                                      Colors.RESET);
                    process.runToCompletion();
                }
            }
        }

        // ── Completion banner ──────────────────────────────────────────────
        System.out.println(Colors.BOLD + Colors.BRIGHT_GREEN +
                          "╔════════════════════════════════════════════════════════════════════════════════╗" +
                          Colors.RESET);
        System.out.println(Colors.BOLD + Colors.BRIGHT_GREEN + "║" + Colors.RESET +
                          Colors.BG_GREEN + Colors.WHITE + Colors.BOLD +
                          "                     ✓  ALL PROCESSES COMPLETED  ✓                            " +
                          Colors.RESET + Colors.BOLD + Colors.BRIGHT_GREEN + "║" + Colors.RESET);
        System.out.println(Colors.BOLD + Colors.BRIGHT_GREEN +
                          "╚════════════════════════════════════════════════════════════════════════════════╝" +
                          Colors.RESET + "\n");

        // =====================================================================
        // FEATURE 2: Print total context switches at the end of simulation
        // =====================================================================
        System.out.println(Colors.BOLD + Colors.BRIGHT_CYAN +
                           "  📊 Total context switches: " + contextSwitchCount + Colors.RESET + "\n");

        // =====================================================================
        // FEATURE 3: Print waiting-time summary table
        // The processMap still holds every process created during the simulation.
        // We iterate over it to build the final table.
        // =====================================================================
        System.out.println(Colors.BOLD + Colors.BRIGHT_CYAN +
                           "╔══════════════════════════════════════════════════════════╗" + Colors.RESET);
        System.out.println(Colors.BOLD + Colors.BRIGHT_CYAN + "║" + Colors.RESET +
                           Colors.BOLD + Colors.BRIGHT_WHITE +
                           "              PROCESS WAITING TIME SUMMARY                " +
                           Colors.RESET + Colors.BOLD + Colors.BRIGHT_CYAN + "║" + Colors.RESET);
        System.out.println(Colors.BOLD + Colors.BRIGHT_CYAN +
                           "╠══════════════╦══════════════════╦══════════════════════╣" + Colors.RESET);
        System.out.println(Colors.BOLD + Colors.BRIGHT_CYAN + "║" + Colors.RESET +
                           Colors.BOLD + Colors.YELLOW +
                           String.format("  %-12s", "Process") +
                           Colors.BRIGHT_CYAN + "║" + Colors.RESET +
                           Colors.BOLD + Colors.YELLOW +
                           String.format("  %-16s", "Burst Time (ms)") +
                           Colors.BRIGHT_CYAN + "║" + Colors.RESET +
                           Colors.BOLD + Colors.YELLOW +
                           String.format("  %-20s", "Waiting Time (ms)") +
                           Colors.RESET + Colors.BOLD + Colors.BRIGHT_CYAN + "║" + Colors.RESET);
        System.out.println(Colors.BOLD + Colors.BRIGHT_CYAN +
                           "╠══════════════╬══════════════════╬══════════════════════╣" + Colors.RESET);

        // Print one row per unique process
        // (processMap may contain duplicate entries for the same process because
        //  every call to addProcessToQueue creates a new Thread key, so we use
        //  a set to avoid printing the same process twice.)
        java.util.Set<String> printed = new java.util.HashSet<>();
        for (Process p : processMap.values()) {
            if (!printed.contains(p.getName())) {
                printed.add(p.getName());
                System.out.println(Colors.BOLD + Colors.BRIGHT_CYAN + "║" + Colors.RESET +
                                   Colors.BRIGHT_WHITE +
                                   String.format("  %-12s", p.getName()) +
                                   Colors.BRIGHT_CYAN + "║" + Colors.RESET +
                                   Colors.BRIGHT_WHITE +
                                   String.format("  %-16s", p.getBurstTime()) +
                                   Colors.BRIGHT_CYAN + "║" + Colors.RESET +
                                   Colors.BRIGHT_YELLOW +
                                   String.format("  %-20s", p.getTotalWaitingTime()) +
                                   Colors.RESET + Colors.BOLD + Colors.BRIGHT_CYAN + "║" + Colors.RESET);
            }
        }

        System.out.println(Colors.BOLD + Colors.BRIGHT_CYAN +
                           "╚══════════════╩══════════════════╩══════════════════════╝" + Colors.RESET + "\n");
    }

    /**
     * Adds a process to the ready queue and creates its associated thread.
     * Prints an entry message showing the process name, priority (Feature 1),
     * burst time, and remaining time.
     *
     * @param process      The process to enqueue
     * @param processQueue The FIFO thread queue
     * @param processMap   Thread-to-Process mapping
     */
    public static void addProcessToQueue(Process process,
                                         Queue<Thread> processQueue,
                                         Map<Thread, Process> processMap) {
        // Create a new thread for this process
        Thread thread = new Thread(process);

        // Add to the queue and register in the map
        processQueue.add(thread);
        processMap.put(thread, process);

        // Feature 3: Record the time this process enters the queue
        process.recordEnqueueTime();

        // Feature 1: Include priority in the ready-queue message
        // Priority is displayed as "Priority: X" right after the process name
        System.out.println(Colors.BLUE + "  ➕ " + Colors.BOLD + Colors.CYAN + process.getName() +
                          Colors.RESET +
                          Colors.MAGENTA + " (Priority: " + process.getPriority() + ")" + Colors.RESET +
                          Colors.BLUE + " added to ready queue" + Colors.RESET +
                          " │ Burst time: " + Colors.YELLOW + process.getBurstTime() + "ms" + Colors.RESET +
                          " │ Remaining: " + Colors.YELLOW + process.getRemainingTime() + "ms" + Colors.RESET);
    }
}
