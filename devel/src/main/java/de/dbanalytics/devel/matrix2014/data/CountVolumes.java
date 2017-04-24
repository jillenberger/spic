/* *********************************************************************** *
 * project: org.matsim.*
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2017 by the members listed in the COPYING,        *
 *                   LICENSE and WARRANTY file.                            *
 * email           : info at matsim dot org                                *
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *   See also COPYING, LICENSE and WARRANTY file                           *
 *                                                                         *
 * *********************************************************************** */

package de.dbanalytics.devel.matrix2014.data;

import de.dbanalytics.spic.analysis.AnalyzerTask;
import de.dbanalytics.spic.analysis.FileIOContext;
import de.dbanalytics.spic.analysis.Predicate;
import de.dbanalytics.spic.analysis.StatsContainer;
import de.dbanalytics.spic.data.CommonKeys;
import de.dbanalytics.spic.data.Episode;
import de.dbanalytics.spic.data.Person;
import de.dbanalytics.spic.data.Segment;
import gnu.trove.iterator.TObjectDoubleIterator;
import gnu.trove.map.TObjectDoubleMap;
import gnu.trove.map.hash.TObjectDoubleHashMap;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * @author johannes
 */
public class CountVolumes implements AnalyzerTask<Collection<? extends Person>> {

    private Predicate<Episode> episodePredicate;

    private Predicate<Segment> legPredicate;

    private Set<String> edgeIds;

    private final int[] firstNodes;

    private final FileIOContext ioContext;

    public CountVolumes(Set<String> edgeIds, FileIOContext ioContext) {
        this.edgeIds = edgeIds;

        firstNodes = new int[edgeIds.size()];
        int idx = 0;
        for(String edgeId : edgeIds) {
            String tokens[] = edgeId.split("-");
            firstNodes[idx] = Integer.parseInt(tokens[0]);
        }
        Arrays.sort(firstNodes);

        this.ioContext = ioContext;
    }

    public void setEpisodePredicate(Predicate<Episode> episodePredicate) {
        this.episodePredicate = episodePredicate;
    }

    public void setLegPredicate(Predicate<Segment> legPredicate) {
        this.legPredicate = legPredicate;
    }

    @Override
    public void analyze(Collection<? extends Person> persons, List<StatsContainer> containers) {
        TObjectDoubleMap<String> counts = new TObjectDoubleHashMap<>(firstNodes.length);

        for (Person person : persons) {
            for (Episode episode : person.getEpisodes()) {
                if (episodePredicate == null || episodePredicate.test(episode)) {

                    String weightString = episode.getPerson().getAttribute(CommonKeys.PERSON_WEIGHT);
                    if (weightString != null) {
                        double weight = Double.parseDouble(weightString);

                        for (Segment leg : episode.getLegs()) {
                            if (legPredicate == null || legPredicate.test(leg)) {

                                String route = leg.getAttribute(CommonKeys.LEG_ROUTE);
                                if (route != null) {
                                    String[] tokens = route.split("\\s");
                                    int ids[] = toIntIds(tokens);

                                    for(int i = 0; i < ids.length; i++) {
                                        int idx = Arrays.binarySearch(firstNodes, ids[i]);
                                        if (idx >= 0) {
                                            int firstNode = firstNodes[idx];
                                            int secondNode = ids[i+1];
                                            String edgeKey = String.format("%s-%s", firstNode, secondNode);
                                            counts.adjustOrPutValue(edgeKey, weight, weight);
                                        }
                                    }

                                }
                            }
                        }
                    }
                }
            }
        }

        Set<String> remove = new HashSet<>();
        for(String key : counts.keySet()) {
            if(!edgeIds.contains(key)) remove.add(key);
        }
        for(String key : remove) counts.remove(key);

        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(ioContext.getPath() + "counts.txt"));
            writer.write("EdgeKey\tCount");
            writer.newLine();

            TObjectDoubleIterator<String> it = counts.iterator();
            for(int i = 0; i < counts.size(); i++) {
                writer.write(it.key());
                writer.write("\t");
                writer.write(String.valueOf(it.value()));
                writer.newLine();

                it.advance();
            }

            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private int[] toIntIds(String[] tokens) {
        int[] ids = new int[tokens.length];
        for(int i = 0; i < tokens.length; i++) {
            ids[i] = Integer.parseInt(tokens[i]);
        }
        return ids;
    }
}
