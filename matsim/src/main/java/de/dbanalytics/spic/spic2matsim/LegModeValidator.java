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

package de.dbanalytics.spic.spic2matsim;

import de.dbanalytics.spic.data.CommonKeys;
import de.dbanalytics.spic.data.Episode;
import de.dbanalytics.spic.data.Person;
import de.dbanalytics.spic.data.Segment;
import de.dbanalytics.spic.processing.EpisodeTask;
import de.dbanalytics.spic.processing.PersonTask;
import de.dbanalytics.spic.processing.PersonsTask;
import de.dbanalytics.spic.processing.SegmentTask;

import java.util.Collection;

/**
 * Created by johannesillenberger on 06.04.17.
 */
public class LegModeValidator implements SegmentTask, EpisodeTask, PersonTask, PersonsTask {

    private static final String DEFAULT_LEG_MODE = "undefined";

    @Override
    public void apply(Segment segment) {
        String mode = segment.getAttribute(CommonKeys.MODE);
        if(mode == null) segment.setAttribute(CommonKeys.MODE, DEFAULT_LEG_MODE);
    }

    @Override
    public void apply(Episode episode) {
        for(Segment leg : episode.getLegs()) {
            apply(leg);
        }
    }

    @Override
    public void apply(Person person) {
        for(Episode episode : person.getEpisodes()) {
            apply(episode);
        }
    }

    @Override
    public void apply(Collection<? extends Person> persons) {
        for(Person person : persons) apply(person);
    }
}
