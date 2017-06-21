/*
 * (c) Copyright 2017 Johannes Illenberger
 *
 * Project de.dbanalytics.spic.
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

package de.dbanalytics.spic.mid2008HH.run;

import de.dbanalytics.spic.analysis.LegAttributeHistogramBuilder;
import de.dbanalytics.spic.analysis.LegAttributePredicate;
import de.dbanalytics.spic.analysis.LegHistogramBuilder;
import de.dbanalytics.spic.analysis.Predicate;
import de.dbanalytics.spic.data.*;
import de.dbanalytics.spic.data.io.PopulationIO;
import gnu.trove.list.array.TDoubleArrayList;
import gnu.trove.map.TDoubleDoubleMap;
import gnu.trove.map.hash.TDoubleDoubleHashMap;
import org.apache.log4j.Logger;
import org.matsim.contrib.common.stats.Discretizer;
import org.matsim.contrib.common.stats.FixedBordersDiscretizer;
import org.matsim.contrib.common.stats.StatsWriter;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by johannesillenberger on 14.06.17.
 */
public class RunGenerateDistributions {

    private static final Logger logger = Logger.getLogger(RunGenerateDistributions.class);

    public static void main(String args[]) throws IOException {
        String inFile = args[0];
        String outDir = args[1];

        logger.info("Loading persons...");
        Set<Person> refPersons = PopulationIO.loadFromXML(inFile, new PlainFactory());
        logger.info(String.format("Loaded %s persons.", refPersons.size()));
        /**
         * Generate mode geo distance distributions.
         */
        Set<String> modes = new HashSet<>();
        for (Person person : refPersons) {
            for (Episode episode : person.getEpisodes()) {
                for (Segment leg : episode.getLegs()) {
                    String mode = leg.getAttribute(CommonKeys.LEG_MODE);
                    if (mode != null) modes.add(mode);
                }
            }
        }

        for (String mode : modes) {
            Predicate<Segment> modePredicate = new LegAttributePredicate(CommonKeys.LEG_MODE, mode);

            Discretizer discretizer = createDiscretizer(mode);
            LegHistogramBuilder builder = new LegAttributeHistogramBuilder(CommonKeys.LEG_GEO_DISTANCE, discretizer);
            builder.setPredicate(modePredicate);
            TDoubleDoubleMap hist = builder.build(refPersons);
//            StatsWriter.writeHistogram((TDoubleDoubleHashMap) hist,
//                    "bin",
//                    "height",
//                    String.format("%s/%s.%s.tmp.txt", outDir, CommonKeys.LEG_GEO_DISTANCE, mode));
            StatsWriter.writeHistogram((TDoubleDoubleHashMap) hist,
                    "bin",
                    "height",
                    String.format("%s/%s.%s.txt", outDir, CommonKeys.LEG_GEO_DISTANCE, mode));

//            TDoubleArrayList borders = createSimDiscretizer(mode);
//            TDoubleDoubleMap newHist = HistogramTransformer.transform(borders, hist);
//            StatsWriter.writeHistogram((TDoubleDoubleHashMap) newHist,
//                    "bin",
//                    "height",
//                    String.format("%s/%s.%s.txt", outDir, CommonKeys.LEG_GEO_DISTANCE, mode));
        }
    }

    private static Discretizer createDiscretizer(String mode) {
        TDoubleArrayList borders = new TDoubleArrayList();
        borders.add(-1);
        if (CommonValues.LEG_MODE_CAR.equalsIgnoreCase(mode)) {
            for (int d = 2000; d < 10000; d += 2000) borders.add(d);
            for (int d = 10000; d < 50000; d += 5000) borders.add(d);
        } else if (CommonValues.LEG_MODE_RIDE.equalsIgnoreCase(mode) ||
                CommonValues.LEG_MODE_PT.equalsIgnoreCase(mode)) {
            for (int d = 2000; d < 10000; d += 2000) borders.add(d);
            for (int d = 10000; d < 50000; d += 10000) borders.add(d);
        } else if (CommonValues.LEG_MODE_PED.equalsIgnoreCase(mode) ||
                CommonValues.LEG_MODE_BIKE.equalsIgnoreCase(mode)) {
            for (int d = 500; d < 3000; d += 500) borders.add(d);
            for (int d = 3000; d < 9000; d += 3000) borders.add(d);
        }
        borders.add(Double.MAX_VALUE);
        return new FixedBordersDiscretizer(borders.toArray());
    }

    private static TDoubleArrayList createSimDiscretizer(String mode) {
        TDoubleArrayList borders = new TDoubleArrayList();
//        borders.add(-1);
        if (CommonValues.LEG_MODE_PED.equalsIgnoreCase(mode)) {
            for (int d = 500; d < 3000; d += 500) borders.add(d);
        } else if (CommonValues.LEG_MODE_BIKE.equalsIgnoreCase(mode)) {
            for (int d = 500; d < 5000; d += 500) borders.add(d);
            for (int d = 5000; d < 10000; d += 1000) borders.add(d);
            for (int d = 10000; d < 20000; d += 5000) borders.add(d);
        } else {
            for (int d = 2000; d < 20000; d += 2000) borders.add(d);
            for (int d = 20000; d < 50000; d += 5000) borders.add(d);
        }
        borders.add(Double.MAX_VALUE);
        return borders;
    }
}
