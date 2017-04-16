/*
 * (c) Copyright 2017 Johannes Illenberger
 *
 * Project de.dbanalytics.spic.*
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.dbanalytics.spic.util;

import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author jillenberger
 */
public class Executor {

    private static ThreadPoolExecutor service;

    private static void init() {
        if (service == null) {
            service = (ThreadPoolExecutor) Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
            //TODO: Does not work as expected.
//            Timer timer = new Timer(true);
//            timer.scheduleAtFixedRate(new ShutdownTask(), 0, 60 * 1000);
        }
    }

    public static void shutdown() {
        if(service != null) service.shutdown();
    }

    public static Future<?> submit(Runnable task) {
        init();
        return service.submit(task);
    }

    public static void submitAndWait(List<? extends Runnable> runnables) {
        init();
        List<Future<?>> futures = new ArrayList<>(runnables.size());
        for(Runnable runnable : runnables) {
            futures.add(service.submit(runnable));
        }

        for(Future<?> future : futures) {
            try {
                future.get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
    }

    public static int getFreePoolSize() {
        init();
        return service.getMaximumPoolSize() - service.getActiveCount();
    }

    private static class ShutdownTask extends TimerTask {

        @Override
        public void run() {
            if(service.getActiveCount() == 0) {
                service.shutdown();
            }
        }
    }
}
