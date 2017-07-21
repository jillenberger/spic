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

    private AtomicLong count;

    public ProgressLogger(Logger delegate) {
        this.delegate = delegate;
    }

    public void start(String message, long max) {
        this.message = message;
        this.max = max;
        this.percentage = new AtomicInteger(0);
        this.count = new AtomicLong(0);

        LoggerUtils.disableNewLine();
        print();
    }

    public void step() {
        count.incrementAndGet();
        int tmp = (int) Math.floor(count.get() / (double) max * 100);
        if (tmp != percentage.get()) {
            percentage.set(tmp);
            print();
        }
    }

    public void stop() {
        print();
        System.out.println();
        LoggerUtils.enableNewLine();
    }

    private synchronized void print() {
        System.out.print("\r");
        delegate.info(message + " " + percentage + " %");
    }
}
