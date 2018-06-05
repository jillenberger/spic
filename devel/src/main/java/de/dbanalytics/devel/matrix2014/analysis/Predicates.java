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

package de.dbanalytics.devel.matrix2014.analysis;

import de.dbanalytics.spic.analysis.LegNextPredicate;
import de.dbanalytics.spic.analysis.ModePredicate;
import de.dbanalytics.spic.analysis.Predicate;
import de.dbanalytics.spic.analysis.PredicateAndComposite;
import de.dbanalytics.spic.data.*;

import java.util.*;

/**
 * @author johannes
 */
public class Predicates {

    public static Map<String, Predicate<Segment>> actTypePredicates(Collection<? extends Person> persons) {
        return actTypePredicates(persons, false);
    }

    public static Map<String, Predicate<Segment>> actTypePredicates(Collection<? extends Person> persons, boolean
            ignoreHome) {
        Set<String> acttypes = new HashSet<>();

        for(Person person : persons) {
            for(Episode episode : person.getEpisodes()) {
                for(Segment act : episode.getActivities()) {
                    String type = act.getAttribute(Attributes.KEY.TYPE);
                    if(type != null ) {
                        if(ignoreHome) {
                            if(!ActivityTypes.HOME.equalsIgnoreCase(type))
                                acttypes.add(type);
                        } else
                            acttypes.add(type);
                    }
                }
            }
        }

        Map<String, Predicate<Segment>> predicates = new HashMap<>();
        for(String type : acttypes) {
            predicates.put(type, new ActTypePredicate(type));
        }

        return predicates;
    }

    public static Map<String, Predicate<Segment>> legPurposePredicates(Collection<? extends Person> persons) {
        Map<String, Predicate<Segment>> actPredicates = actTypePredicates(persons);
        Map<String, Predicate<Segment>> legPredicates = new HashMap<>();

        for(Map.Entry<String, Predicate<Segment>> entry : actPredicates.entrySet()) {
            legPredicates.put(entry.getKey(), new LegNextPredicate(entry.getValue()));
        }

        return legPredicates;
    }

    public static Map<String, Predicate<Segment>> legModePredicates(Collection<? extends Person> persons) {
        Set<String> modes = new HashSet<>();

        for(Person person : persons) {
            for(Episode episode : person.getEpisodes()) {
                for(Segment leg : episode.getLegs()) {
                    String mode = leg.getAttribute(Attributes.KEY.MODE);
                    if(mode != null)
                        modes.add(mode);
                }
            }
        }

        Map<String, Predicate<Segment>> predicates = new HashMap<>();
        for(String mode : modes) {
            predicates.put(mode, new ModePredicate(mode));
        }

        return predicates;
    }

    public static Map<String, Predicate<Segment>> legPredicates(Collection<? extends Person> persons) {
        Map<String, Predicate<Segment>> actTypePredicates = legPurposePredicates(persons);
        Map<String, Predicate<Segment>> legModePredicates = legModePredicates(persons);

        actTypePredicates.put("all", TrueLegPredicate.getInstance());
        legModePredicates.put("all", TrueLegPredicate.getInstance());

        Map<String, Predicate<Segment>> predicates = new HashMap<>();

        for(Map.Entry<String, Predicate<Segment>> modeEntry : legModePredicates.entrySet()) {
            for(Map.Entry<String, Predicate<Segment>> actEntry : actTypePredicates.entrySet()) {
                LegNextPredicate purposePredicate = new LegNextPredicate(actEntry.getValue());

                PredicateAndComposite<Segment> composite = new PredicateAndComposite<>();
                composite.addComponent(modeEntry.getValue());
                composite.addComponent(purposePredicate);

                predicates.put(String.format("%s.%s", modeEntry.getKey(), actEntry.getKey()), composite);
            }

        }

        return predicates;
    }

    public static class TrueLegPredicate implements Predicate<Segment> {

        private static TrueLegPredicate instance;

        public static TrueLegPredicate getInstance() {
            if(instance == null) instance = new TrueLegPredicate();
            return instance;
        }

        @Override
        public boolean test(Segment segment) {
            return true;
        }
    }
}
