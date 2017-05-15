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
import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.analysis.UnivariateRealFunction;
import org.matsim.contrib.common.stats.Discretizer;
import org.matsim.contrib.common.stats.Histogram;
import org.matsim.contrib.common.stats.LinearDiscretizer;
import org.matsim.contrib.common.util.XORShiftRandom;

import java.util.Arrays;
import java.util.Random;
import java.util.Set;

/**
 * Created by johannesillenberger on 11.05.17.
 */
public class BlurActivityTimes implements EpisodeTask {

    private final Discretizer discretizer;

    private final Random random;

    TDoubleObjectMap<UnivariateRealFunction> funcs;

    public BlurActivityTimes(Set<Person> persons, Random random) {
        this.random = random;
        this.discretizer = new LinearDiscretizer(3600);
        HistogramBuilder builder = new LegAttributeHistogramBuilder(
                CommonKeys.LEG_START_TIME,
                discretizer);
        TDoubleDoubleMap hist = builder.build(persons);
        hist = Histogram.normalize((TDoubleDoubleHashMap) hist);
        //TODO: double hist for periodic boundary

        funcs = new TDoubleObjectHashMap<>();
        double[] keys = hist.keys();
        Arrays.sort(keys);
        for (int i = 0; i < keys.length - 1; i++) {
            double x_left = keys[i];
            double x_right = keys[i + 1];
            double y_left = hist.get(x_left);
            double y_right = hist.get(x_right);

            double b = y_left;
            double a = (y_right - y_left) / (x_right - x_left);

            double x_m = x_left + ((x_right - x_left) / 2.0);
            double y_m = a * (x_m - x_left) + b;

            double offset = 0.5 - y_m;

            y_left += offset;
            y_right += offset;
            b = y_left;
            a = (y_right - y_left) / (x_right - x_left);

            funcs.put(x_left, new ProbabilityFunction(a, b));
        }
    }

    public static void main(String args[]) {
        Set<Person> persons = PopulationIO.loadFromXML("/home/johannesillenberger/gsv/C_Vertrieb/2017_03_21_DRIVE/97_Work/demand/midHH/mid2008HH2.xml", new PlainFactory());

        BlurActivityTimes task = new BlurActivityTimes(persons, new XORShiftRandom());
        TaskRunner.run(task, persons);

//        PopulationIO.writeToXML("", persons);
    }

    @Override
    public void apply(Episode episode) {
        for (Segment act : episode.getActivities()) {
            String startVal = act.getAttribute(CommonKeys.ACTIVITY_START_TIME);
            String endVal = act.getAttribute(CommonKeys.ACTIVITY_END_TIME);

            if (startVal != null && endVal != null) {
                double start = Double.parseDouble(startVal);
                double end = Double.parseDouble(endVal);
                double duration = end - start;

                double bin = discretizer.discretize(start);

                UnivariateRealFunction func = funcs.get(bin);
                try {

                    boolean hit = false;
                    while (!hit) {
                        double newStart = random.nextDouble() * duration;
                        double p = func.value(newStart);
                        if (random.nextDouble() < p) {
                            hit = true;

                            act.setAttribute(CommonKeys.ACTIVITY_START_TIME, String.valueOf(bin + newStart));
                            act.setAttribute(CommonKeys.ACTIVITY_END_TIME, String.valueOf(bin + newStart + duration));
                        }
                    }
                } catch (FunctionEvaluationException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static class ProbabilityFunction implements UnivariateRealFunction {

        private final double a;

        private final double b;

        public ProbabilityFunction(double a, double b) {
            this.a = a;
            this.b = b;
        }

        @Override
        public double value(double v) throws FunctionEvaluationException {
            return a * v + b;
        }
    }
}
