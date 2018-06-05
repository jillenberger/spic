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

package de.dbanalytics.devel.matrix2014.source.mid2008;

import de.dbanalytics.spic.data.*;
import de.dbanalytics.spic.processing.PersonsTask;
import gnu.trove.function.TDoubleFunction;
import gnu.trove.iterator.TDoubleDoubleIterator;
import gnu.trove.iterator.TDoubleObjectIterator;
import gnu.trove.map.hash.TDoubleDoubleHashMap;
import gnu.trove.map.hash.TDoubleObjectHashMap;
import org.apache.log4j.Logger;
import org.matsim.contrib.common.stats.Discretizer;
import org.matsim.contrib.common.stats.Histogram;
import org.matsim.contrib.common.stats.LinearDiscretizer;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * @author johannes
 */
public class ReweightJourneys implements PersonsTask {

    private final static Logger logger = Logger.getLogger(ReweightJourneys.class);

    private Discretizer discretizer;

    private TDoubleDoubleHashMap referenceHist;

    private double threshold;

    public ReweightJourneys() {
        discretizer = new LinearDiscretizer(50000);
        threshold = 300000;

        referenceHist = new TDoubleDoubleHashMap();
        try {
            BufferedReader reader = new BufferedReader(new FileReader
                    ("/Users/johannes/gsv/matrix2014/popgen/mid-fusion/tomtom.dist.txt"));
            String line = reader.readLine();
            while ((line = reader.readLine()) != null) {
                String[] tokens = line.split("\t");
                double key = Double.parseDouble(tokens[0]);
                double value = Double.parseDouble(tokens[1]);
                referenceHist.put(key, value);
            }

            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Histogram.normalize(referenceHist);
    }

    @Override
    public void apply(Collection<? extends Person> persons) {
        TDoubleDoubleHashMap hist = new TDoubleDoubleHashMap();
        TDoubleObjectHashMap<Set<Person>> personsMap = new TDoubleObjectHashMap<>();

        for (Person person : persons) {
            if (person.getEpisodes().size() > 1) {
                logger.warn("Runs only with one leg per person");
            } else {
                double w = Double.parseDouble(person.getAttribute(Attributes.KEY.WEIGHT));

                Episode episode = person.getEpisodes().get(0);
                if (episode.getLegs().size() > 1) {
                    logger.warn("Runs only with one leg per person");
                } else {
                    Segment leg = episode.getLegs().get(0);
                    String value = leg.getAttribute(Attributes.KEY.BEELINE_DISTANCE);
                    if (value != null) {
                        if(Attributes.MODE.CAR.equalsIgnoreCase(leg.getAttribute(Attributes.KEY.MODE))) {
                            double d = Double.parseDouble(value);
                            d = discretizer.discretize(d);

                            hist.adjustOrPutValue(d, w, w);

                            Set<Person> pset = personsMap.get(d);
                            if (pset == null) {
                                pset = new HashSet<>();
                                personsMap.put(d, pset);
                            }
                            pset.add(person);
                        }
                    }
                }
            }
        }

        double sumRef = 0;
        TDoubleDoubleIterator it = referenceHist.iterator();
        for (int i = 0; i < referenceHist.size(); i++) {
            it.advance();
            if (it.key() >= threshold) {
                sumRef += it.value();
            }
        }

        Histogram.normalize(hist);
        double sumMid = 0;
        TDoubleDoubleIterator it2 = hist.iterator();
        for (int i = 0; i < hist.size(); i++) {
            it2.advance();
            if (it2.key() >= threshold) {
                sumMid += it2.value();
            }
        }

        final double factor = sumRef / sumMid;
        hist.transformValues(new TDoubleFunction() {
            @Override
            public double execute(double v) {
                return v * factor;
            }
        });

        TDoubleDoubleHashMap weigths = new TDoubleDoubleHashMap();
//        it2 = hist.iterator();
        double[] keys = hist.keys();
        Arrays.sort(keys);
        for (int i = 0; i < hist.size(); i++) {
//            it2.advance();
//            if (it2.key() >= threshold) {
            double d = keys[i];
            double vol = hist.get(d);
//                double f = referenceHist.get(it2.key()) / it2.value();
            double f = referenceHist.get(d) / vol;
                weigths.put(d, f);
                logger.info(String.format("Weight for distance %s: %s.", d, f));
//            }
        }

        TDoubleObjectIterator<Set<Person>> it3 = personsMap.iterator();
        for (int i = 0; i < personsMap.size(); i++) {
            it3.advance();
            if (it3.key() >= threshold) {
                double f = weigths.get(it3.key());
                for (Person person : it3.value()) {
                    double w = Double.parseDouble(person.getAttribute(Attributes.KEY.WEIGHT));
                    w = w * f;
                    person.setAttribute(Attributes.KEY.WEIGHT, String.valueOf(w));
                }
            } else {
                for (Person person : it3.value()) {
                    double w = Double.parseDouble(person.getAttribute(Attributes.KEY.WEIGHT));
                    w = w * 0.5;
                    person.setAttribute(Attributes.KEY.WEIGHT, String.valueOf(w));
                }
            }
        }
    }
}
