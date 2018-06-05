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

package de.dbanalytics.spic.data;

import gnu.trove.map.hash.TObjectDoubleHashMap;
import org.matsim.contrib.common.util.ProgressLogger;

import java.util.*;

/**
 * @author johannes
 */
public class PersonUtils {

    public static  Set<? extends Person> weightedCopy(Collection<? extends Person> persons, Factory factory, int N,
                                                      Random random) {
        if(persons.size() > N) {
            throw new IllegalArgumentException("Cannot shrink population.");
        }

        List<Person> templates = new ArrayList<>(persons);
		/*
		 * get max weight
		 */
        TObjectDoubleHashMap<Person> weights = new TObjectDoubleHashMap<>(persons.size());
        double maxW = 0;
        for(Person person : persons) {
            String wStr = person.getAttribute(Attributes.KEY.WEIGHT);
            double w = 0;
            if(wStr != null) {
                w = Double.parseDouble(wStr);
            }
            weights.put(person, w);
            maxW = Math.max(w, maxW);
        }
		/*
		 * adjust weight so that max weight equals probability 1
		 */
        ProgressLogger.init(N, 2, 10);
        Set<Person> clones = new LinkedHashSet<>();
        while(clones.size() < N) {
            Person template = templates.get(random.nextInt(templates.size()));
            double w = weights.get(template);
            double p = w/maxW;
            if(p > random.nextDouble()) {
                StringBuilder builder = new StringBuilder();
                builder.append(template.getId());
                builder.append("clone");
                builder.append(clones.size());

                Person clone = PersonUtils.deepCopy(template, builder.toString(), factory);
                clone.setAttribute(Attributes.KEY.WEIGHT, "1.0");
                clones.add(clone);
                ProgressLogger.step();
            }
        }

        return clones;
    }

    public static Person shallowCopy(Person person, Factory factory) {
        return shallowCopy(person, person.getId(), factory);
    }

    public static Person shallowCopy(Person person, String id, Factory factory) {
        Person clone = factory.newPerson(id);
        for(String key : person.keys()) {
            clone.setAttribute(key, person.getAttribute(key));
        }

        return clone;
    }

    public static Person deepCopy(Person person, String id, Factory factory) {
        Person clone = shallowCopy(person, id, factory);
        for(Episode e : person.getEpisodes()) {
            clone.addEpisode(deepCopy(e, factory));
        }
        return clone;
    }

    public static Episode shallowCopy(Episode episode, Factory factory) {
        Episode clone = factory.newEpisode();
        for(String key : episode.keys()) {
            clone.setAttribute(key, episode.getAttribute(key));
        }

        return clone;
    }

    public static Episode deepCopy(Episode episode, Factory factory) {
        Episode clone = shallowCopy(episode, factory);

        for(Segment act : episode.getActivities()) {
            Segment actClone = shallowCopy(act, factory);
            clone.addActivity(actClone);
        }

        for(Segment leg : episode.getLegs()) {
            Segment legClone = shallowCopy(leg, factory);
            clone.addLeg(legClone);
        }

        return clone;
    }

    public static Segment shallowCopy(Segment segment, Factory factory) {
        Segment clone = factory.newSegment();
        for(String key : segment.keys()) {
            clone.setAttribute(key, segment.getAttribute(key));
        }

        return clone;
    }
}
