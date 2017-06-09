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

package de.dbanalytics.spic.mid2008HH.sim;

import de.dbanalytics.spic.data.Attributable;
import de.dbanalytics.spic.sim.DiscreteDistributionTerm;
import de.dbanalytics.spic.sim.MarkovEngineListener;
import de.dbanalytics.spic.sim.data.CachedPerson;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Collection;
import java.util.Locale;

/**
 * @author johannes
 */
public class DiscretDistributionDebugger implements MarkovEngineListener {

    private final String outputDirectory;

    private final String name;

    private final DiscreteDistributionTerm delegate;

    private final long interval;
    private final DecimalFormat df;
    private long iteration;

    public DiscretDistributionDebugger(DiscreteDistributionTerm delegate, String name, String outDir, long interval) {
        this.delegate = delegate;
        this.name = name;
        this.outputDirectory = outDir;
        this.interval = interval;
        df = new DecimalFormat("0E0", new DecimalFormatSymbols(Locale.US));
        df.setMaximumFractionDigits(340);

    }

    @Override
    public void afterStep(Collection<CachedPerson> population, Collection<? extends Attributable> mutations, boolean accepted) {
        if (iteration % interval == 0) {
            String dir = outputDirectory + "/" + df.format(iteration) + "/";
            new File(dir).mkdirs();
            String filename = dir + name + ".txt";
            try {
                delegate.debugDump(filename);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        iteration++;
    }
}
