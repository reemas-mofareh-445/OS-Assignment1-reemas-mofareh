 CS3701 Operating Systems - Assignment 1
 
Student: Reemas Mofareh
Student ID: 445052261
Course: CS-3701 Operating Systems - Semester 2, AY 2025/2026
 
---
 
## Video Demo
 
[Watch the video](https://drive.google.com/file/d/1U9T8OdEYPnCT9COjdHVa4mpEWzz50piZ/view?usp=drivesdk)
 
---
 
## About the Assignment
 
This assignment is about implementing a Round-Robin CPU Scheduler using Java threads. The simulation creates multiple processes and schedules them using a FIFO queue with a fixed time quantum. I added three new features to the starter code.
 
---
 
## What I Added
 
**Feature 1 - Process Priority**
 
I added a priority field to the Process class with values from 1 to 5 where 5 is the highest. The priority is randomly assigned when each process is created and shows up when the process enters the ready queue.
 
**Feature 2 - Context Switch Counter**
 
I added a static variable called contextSwitchCount that increases by 1 every time the CPU moves to a new process. The number of each context switch shows up before the process runs and the total appears at the end.
 
**Feature 3 - Waiting Time Tracking**
 
I added fields to track how long each process waits in the ready queue using System.currentTimeMillis(). At the end of the simulation a table shows the process name, burst time, and total waiting time for each process.
 
---
 
## Files in this Repository
 
- SchedulerSimulation.java — the main code with my three additions
- ANSWERS.md — answers to the four technical questions
- DEVELOPMENT_LOG.md — my development log with six entries
- REFLECTION.md — my reflections on what I learned
- README.md — this file
 
---
 
## How to Run
 
You need Java JDK 17 or higher.
 
Open a terminal in the folder and run:
 
    javac SchedulerSimulation.java
    java SchedulerSimulation
 
Or open the project in VS Code and press the run button.
 
---
 
## Simulation Details
 
The simulation uses my student ID 445052261 as the random seed so all the values below are unique to me:
 
- Time quantum is randomly picked between 2000 and 5000 ms
- Number of processes is randomly picked between 10 and 20
- Burst time for each process is random between timeQuantum/2 and 3 times timeQuantum
- Priority for each process is random between 1 and 5
 
---
 
## Commits
 
1. Set student ID and added priority, context switch counter, and waiting time tracking
2. Added documentation files
3. Added video link to README
 
