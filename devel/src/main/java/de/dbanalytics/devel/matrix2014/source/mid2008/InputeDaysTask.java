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
import de.dbanalytics.spic.mid2008.MiDValues;
import de.dbanalytics.spic.processing.PersonTask;
import gnu.trove.iterator.TObjectIntIterator;
import gnu.trove.map.hash.TObjectIntHashMap;
import org.matsim.contrib.common.collections.ChoiceSet;
import org.matsim.contrib.common.util.XORShiftRandom;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author johannes
 */
public class InputeDaysTask implements PersonTask {

    private Map<String, ChoiceSet<String>> map;


    public InputeDaysTask(Collection<? extends Person> persons) {
        Map<String, TObjectIntHashMap<String>> matrix = new HashMap<>();

        for(Person person : persons) {
            String day = person.getAttribute(CommonKeys.DAY);
            if(day != null) {
                for(Episode episode : person.getEpisodes()) {
                    for(Segment leg : episode.getLegs()) {
                        String mode = leg.getAttribute(CommonKeys.LEG_MODE);
//                        if(CommonValues.LEG_MODE_CAR.equalsIgnoreCase(mode)) {
                            String purpose = leg.getAttribute(CommonKeys.LEG_PURPOSE);
                            if (purpose != null) {
                                TObjectIntHashMap<String> days = matrix.get(purpose);
                                if (days == null) {
                                    days = new TObjectIntHashMap<>();
                                    matrix.put(purpose, days);
                                }
                                days.adjustOrPutValue(day, 1, 1);
//                            }
                        }
                    }
                }
            }
        }

        matrix.put(ActivityTypes.VACATIONS_LONG, matrix.get(ActivityTypes.VACATIONS_SHORT));

        map = new HashMap<>();

        for(Map.Entry<String, TObjectIntHashMap<String>> entry : matrix.entrySet()) {
            System.out.print(entry.getKey());
            System.out.print(": ");

            ChoiceSet<String> choiceSet = new ChoiceSet<>(new XORShiftRandom());

            TObjectIntHashMap<String> days = entry.getValue();
            TObjectIntIterator<String> it = days.iterator();
            for(int i = 0; i < days.size(); i++) {
                it.advance();
                choiceSet.addOption(it.key(), it.value());

                System.out.print(it.key());
                System.out.print("=");
                System.out.print(String.valueOf(it.value()));
                System.out.print(" ");
            }
            System.out.println();

            map.put(entry.getKey(), choiceSet);
        }


    }

    @Override
    public void apply(Person person) {
        String day = person.getAttribute(CommonKeys.DAY);
        if(day == null) {
            Episode episode = person.getEpisodes().get(0);
            if(MiDValues.MID_JOUNREYS.equalsIgnoreCase(episode.getAttribute(CommonKeys.DATA_SOURCE))) {
                Segment leg = episode.getLegs().get(0);
                String purpose = leg.getAttribute(CommonKeys.LEG_PURPOSE);

                day = map.get(purpose).randomWeightedChoice();

                person.setAttribute(CommonKeys.DAY, day);
            }
        }
    }
}
