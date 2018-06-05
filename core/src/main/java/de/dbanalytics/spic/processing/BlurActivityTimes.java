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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.dbanalytics.spic.processing;

import de.dbanalytics.spic.analysis.LegAttributeHistogramBuilder;
import de.dbanalytics.spic.data.*;
import de.dbanalytics.spic.data.io.PopulationIO;
import de.dbanalytics.spic.sim.HistogramBuilder;
import gnu.trove.map.TDoubleDoubleMap;
import gnu.trove.map.TDoubleObjectMap;
import gnu.trove.map.hash.TDoubleDoubleHashMap;
import gnu.trove.map.hash.TDoubleObjectHashMap;
import org.matsim.contrib.common.stats.Discretizer;
import org.matsim.contrib.common.stats.Histogram;
import org.matsim.contrib.common.stats.LinearDiscretizer;
import org.matsim.contrib.common.util.XORShiftRandom;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Random;
import java.util.Set;

/**
 * Created by johannesillenberger on 11.05.17.
 */
public class BlurActivityTimes implements EpisodeTask {

    private final Discretizer discretizer;

    private final Random random;

    TDoubleObjectMap<InverseDensityFunction> densityFuncs;


    BufferedWriter writer = new BufferedWriter(new FileWriter("/Users/jillenberger/Desktop/times.txt"));

    public BlurActivityTimes(Set<Person> persons, Random random) throws IOException {
        this.random = random;
        this.discretizer = new LinearDiscretizer(3600);
        HistogramBuilder builder = new LegAttributeHistogramBuilder(
                Attributes.KEY.DEPARTURE_TIME,
                discretizer);
        TDoubleDoubleMap hist = builder.build(persons);
        hist = Histogram.normalize((TDoubleDoubleHashMap) hist);
        //TODO: double hist for periodic boundary

        densityFuncs = new TDoubleObjectHashMap<>();
        double[] keys = hist.keys();
        Arrays.sort(keys);
        for (int i = 0; i < keys.length - 1; i++) {
            double x_left = keys[i];
            double x_right = keys[i + 1];
            double y_left = hist.get(x_left);
            double y_right = hist.get(x_right);

            double a = (y_right - y_left) / (x_right - x_left);
            double b = y_left - (a * x_left);

            densityFuncs.put(x_right, new InverseDensityFunction(a, b));
        }
    }

    public static void main(String args[]) throws IOException {
        Set<Person> persons = PopulationIO.loadFromXML("/Users/jillenberger/work/mid2008.midtrips.valid.xml", new PlainFactory());

        BlurActivityTimes task = new BlurActivityTimes(persons, new XORShiftRandom());
        TaskRunner.run(task, persons);
        task.writer.close();
//        PopulationIO.writeToXML("", persons);
    }

    @Override
    public void apply(Episode episode) {
        for (Segment leg : episode.getLegs()) {
            String startVal = leg.getAttribute(Attributes.KEY.DEPARTURE_TIME);

            if (startVal != null) {
                double start = Double.parseDouble(startVal);

                double x_right = discretizer.discretize(start);
                double x_left = x_right - discretizer.binWidth(x_right) + 1;

                InverseDensityFunction func = densityFuncs.get(x_right);

                if (func != null) {
                    double y_left = func.density(x_left);
                    double y_right = func.density(x_right);

                    double y = y_left + (random.nextDouble() * (y_right - y_left));

                    double x = func.inverseDensity(y);

                    if (x > 190000) {
                        System.out.println("Opps");
                    }
                    leg.setAttribute(Attributes.KEY.DEPARTURE_TIME, String.valueOf(x));

                    String endVal = leg.getAttribute(Attributes.KEY.ARRIVAL_TIME);
                    if (endVal != null) {
                        double end = Double.parseDouble(endVal);
                        double newEnd = x + (end - start);

                        leg.setAttribute(Attributes.KEY.ARRIVAL_TIME, String.valueOf(newEnd));
                    }
                    try {
                        writer.write(x_right + "\t" + x);
                        writer.newLine();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private static class InverseDensityFunction {

        private final double a;

        private final double b;

        public InverseDensityFunction(double a, double b) {
            this.a = a;
            this.b = b;
        }

        public double density(double v) {
            return a / 2 * Math.pow(v, 2) + b * v;
        }

        public double inverseDensity(double v) {
            int sign = 1;
            if (a < 0) sign = -1;
            return -(b / a) + sign * Math.sqrt(Math.pow(b / a, 2) + 2 * v / a);
        }
    }
}
