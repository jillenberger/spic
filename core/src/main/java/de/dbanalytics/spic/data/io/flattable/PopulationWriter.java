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

package de.dbanalytics.spic.data.io.flattable;

import de.dbanalytics.spic.data.Episode;
import de.dbanalytics.spic.data.Person;
import de.dbanalytics.spic.data.Segment;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by johannesillenberger on 05.04.17.
 */
public class PopulationWriter {

    public static void write(Set<? extends Person> persons, String basename) throws IOException {
        TableWriter writer = new TableWriter();
        /*
        Write persons.
         */
        addId(persons);
        writer.write(persons, String.format("%spersons.txt", basename));
        removeId(persons);
        /*
        Write episodes.
         */
        Set<Episode> episodes = new HashSet<>(persons.size());
        for(Person p : persons) {
            episodes.addAll(p.getEpisodes());
        }
        writer.write(episodes, String.format("%sepisodes.txt", basename));
        /*
        Write activities.
         */
        Set<Segment> activities = new HashSet<>(episodes.size() * 5);
        for(Episode episode : episodes) {
            activities.addAll(episode.getActivities());
        }
        addPersonId(activities);
        writer.write(activities, String.format("%sactivities.txt", basename));
        removePersonId(activities);
        /*
        Write legs.
         */
        Set<Segment> legs = new HashSet<>(activities.size());
        for(Episode episode : episodes) {
            legs.addAll(episode.getLegs());
        }
        addPersonId(legs);
        writer.write(legs, String.format("%slegs.txt", basename));
        removePersonId(legs);
    }

    static private void addId(Set<? extends Person> persons) {
        for(Person p : persons) {
            p.setAttribute("personId", p.getId());
        }
    }

    static private void removeId(Set<? extends Person> persons) {
        for(Person p : persons) {
            p.removeAttribute("personId");
        }
    }

    static private void addPersonId(Set<? extends Segment> segments) {
        for(Segment s : segments) {
            s.setAttribute("personId", s.getEpisode().getPerson().getId());
        }
    }

    static private void removePersonId(Set<? extends Segment> segments) {
        for(Segment s : segments) {
            s.removeAttribute("personId");
        }
    }
}
