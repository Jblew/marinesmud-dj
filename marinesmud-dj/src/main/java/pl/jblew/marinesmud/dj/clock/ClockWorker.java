/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.jblew.marinesmud.dj.clock;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import pl.jblew.marinesmud.dj.config.StaticConfig;

/**
 *
 * @author teofil
 */
public class ClockWorker {
    private final AtomicInteger percentBusy = new AtomicInteger(0);
    public static final int TASK_DISPATCH_AUDIO = 0;
    public static final int TASK_PROCESS_AUDIO = 1;
    public static final int TASK_PROCESS_EFFECTS = 2;
    public static final int TASK_PROCESS_VISUALISATIONS = 3;
    public static final int TASK_DISPLAY_STATS = 4;

    private final ExecutorService executor = Executors.newFixedThreadPool(2);
    private final RunnableRef [] tasks = new RunnableRef [5];
    private final AtomicReference<Runnable> dmxTaskRef = new AtomicReference<>(null);
    private final Lock lock = new ReentrantLock();
    private final Condition tasksProcessingFinished = lock.newCondition();
    private final Condition dmxProcessingFinished = lock.newCondition();
    private final AtomicBoolean dmxProcessingInProgress = new AtomicBoolean(false);

    public ClockWorker() {
        for(int i = 0;i < tasks.length;i++) tasks[i] = new RunnableRef(null);
    }
    
    public void start() {
        long tickInterval_ms = 1000 / StaticConfig.CLOCK_FREQUENCY_HZ;
        execute(() -> {//tasks thread
            Thread.currentThread().setName("ClockWorker-tasks");

            while (true) {
                //System.out.println("\n\n>");
                long sTime = System.currentTimeMillis();

                for (RunnableRef taskRef : tasks) {
                    Runnable task = (Runnable) taskRef.get();
                    if (task != null) {
                        task.run();
                    }
                }

                //System.out.println("Awaiting last DMX");
                if (dmxProcessingInProgress.get()) {
                    lock.lock();
                    try {
                        dmxProcessingFinished.await();
                    } catch (InterruptedException ex) {
                        Logger.getLogger(ClockWorker.class.getName()).log(Level.SEVERE, null, ex);
                    } finally {
                        lock.unlock();
                    }
                }
                
                //System.out.println("Tasks finished");
                
                lock.lock();
                try {
                    tasksProcessingFinished.signalAll();
                } finally {
                    lock.unlock();
                }

                long eTime = System.currentTimeMillis();
                long currentInterval_ms = eTime - sTime;
                long timeLeft = tickInterval_ms - currentInterval_ms;
                percentBusy.set((int) ((float) currentInterval_ms / (float) tickInterval_ms * 100f));

                //System.out.println("Busy: "+percentBusy.get()+"%");
                
                if (timeLeft > 0) {
                    try {
                        TimeUnit.MILLISECONDS.sleep(timeLeft);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(ClockWorker.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        });
        execute(() -> {//dmx thread
            Thread.currentThread().setName("ClockWorker-dmx");

            while (true) {
                lock.lock();
                try {
                    tasksProcessingFinished.await();
                } catch (InterruptedException ex) {
                    Logger.getLogger(ClockWorker.class.getName()).log(Level.SEVERE, null, ex);
                } finally {
                    lock.unlock();
                }
                //System.out.println("Tick DMX");

                dmxProcessingInProgress.set(true);
                Runnable dmxTask = dmxTaskRef.get();
                if (dmxTask != null) {
                    dmxTask.run();
                }
                dmxProcessingInProgress.set(false);

                lock.lock();
                try {
                    dmxProcessingFinished.signalAll();
                } finally {
                    lock.unlock();
                }

            }
        });
    }

    private void execute(Runnable r) {
        executor.submit(r);
    }

    public void setTask(int task_flag, Runnable task) {
        tasks[task_flag].set(task);
    }

    public void setDMXTask(Runnable task) {
        dmxTaskRef.set(task);
    }
    public int getPercentBusy() {
        return percentBusy.get();
    }
    
    public static boolean isInClockTaskThread() {
        return Thread.currentThread().getName().equals("ClockWorker-tasks");
    }
    
    private static class RunnableRef extends AtomicReference<Runnable> {
        public RunnableRef(Runnable initValue) {
            super(initValue);
        }
    }
}
