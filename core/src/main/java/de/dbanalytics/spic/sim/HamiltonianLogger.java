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

package de.dbanalytics.spic.sim;

import de.dbanalytics.spic.data.Attributable;
import de.dbanalytics.spic.sim.data.CachedPerson;
import org.apache.log4j.Logger;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Collection;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author johannes
 */
public class HamiltonianLogger implements McmcSimulationObserver {

    private static final Logger logger = Logger.getLogger(HamiltonianLogger.class);

    private final Hamiltonian h;

    private final long logInterval;

    private final long startIteration;

    private AtomicLong iter = new AtomicLong();

    private BufferedWriter writer;

    private static final String TAB = "\t";

    private final String outdir;

    private final DecimalFormat format;

    private final String name;

    public HamiltonianLogger(Hamiltonian h, int logInterval, String name) {
        this(h, logInterval, name, null);
    }

    public HamiltonianLogger(Hamiltonian h, long logInterval, String name, String outdir) {
        this(h, logInterval, name, outdir, 0);
    }

    public HamiltonianLogger(Hamiltonian h, long logInterval, String name, String outdir, long startIteration) {
        this.h = h;
        this.logInterval = logInterval;
        this.startIteration = startIteration;
        this.outdir = outdir;

        if(name == null)
            this.name = h.getClass().getSimpleName();
        else
            this.name = name;

        format = new DecimalFormat("0E0", new DecimalFormatSymbols(Locale.US));
        format.setMaximumFractionDigits(340);

        if (outdir != null) {
            try {
                writer = new BufferedWriter(new FileWriter(outdir + "/" + name + ".txt"));
                writer.write("iter\th");
                writer.newLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void afterStep(Collection<CachedPerson> population, Collection<? extends Attributable> mutations, boolean accepted) {
        if(iter.get() % logInterval == 0) {
            long iterNow = iter.get();
            if (iterNow >= startIteration) {
                double hVal = h.evaluate(population);
                logger.info(String.format("%s [%s]: %s", name, format.format(iterNow), hVal));

                if (writer != null) {
                    try {
                        writer.write(String.valueOf(iterNow));
                        writer.write(TAB);
                        writer.write(String.valueOf(hVal));
                        writer.newLine();
                        writer.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                logger.info(String.format("%s [%s]: <<inactive>>", name, format.format(iterNow)));
            }
        }

        iter.incrementAndGet();
    }
}
