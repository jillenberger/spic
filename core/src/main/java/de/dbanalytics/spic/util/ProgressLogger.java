/*
 * (c) Copyright 2017 Johannes Illenberger
 *
 *  Project de.dbanalytics.spic.*
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.dbanalytics.spic.util;

import org.apache.log4j.Logger;
import org.matsim.contrib.common.util.LoggerUtils;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author johannes
 */
public class ProgressLogger {

    private final Logger delegate;

    private String message;

    private AtomicInteger percentage;

    private long max;

    private int step;

    private AtomicLong count;

    private boolean relativeMode = true;

    public ProgressLogger(Logger delegate) {
        this.delegate = delegate;
    }

    public void start(String message, long max) {
        this.message = message;
        this.max = max;
        this.step = 0;
        this.percentage = new AtomicInteger(0);
        this.count = new AtomicLong(0);
        relativeMode = true;

        LoggerUtils.disableNewLine();
        printRel();
    }

    public void start(String message, int step) {
        this.message = message;
        this.max = 0;
        this.step = step;
        this.percentage = null;
        this.count = new AtomicLong(0);
        relativeMode = false;

        LoggerUtils.disableNewLine();
        printAbs();
    }

    public void step() {
        count.incrementAndGet();
        if (relativeMode) {
            int tmp = (int) Math.floor(count.get() / (double) max * 100);
            if (tmp != percentage.get()) {
                percentage.set(tmp);
                printRel();
            }
        } else {
            if (count.get() % step == 0) {
                printAbs();
            }
        }
    }

    public void stop() {
        if (relativeMode) printRel();
        else printAbs();
        System.out.println();
        LoggerUtils.enableNewLine();
    }

    public void stop(String endMessage) {
        System.out.print("\r");
        if (relativeMode) delegate.info(message + " " + percentage + " % (" + endMessage + ")");
        else delegate.info(message + " " + count + " (" + endMessage + ")");
        System.out.println();
        LoggerUtils.enableNewLine();
    }

    private synchronized void printRel() {
        System.out.print("\r");
        delegate.info(message + " " + percentage + " %");
    }

    private synchronized void printAbs() {
        System.out.print("\r");
        delegate.info(message + " " + count);
    }
}
