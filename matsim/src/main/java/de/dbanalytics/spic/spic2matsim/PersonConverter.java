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
import de.dbanalytics.spic.data.Segment;
import de.dbanalytics.spic.gis.Place;
import de.dbanalytics.spic.gis.PlaceIndex;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.population.*;
import org.matsim.facilities.ActivityFacility;
import org.matsim.utils.objectattributes.ObjectAttributes;

import java.util.Collection;
import java.util.Set;

/**
 * Created by johannesillenberger on 06.04.17.
 */
public class PersonConverter {

    private final Population population;

    private final PlaceIndex placeIndex;

    public PersonConverter(Population population, PlaceIndex placeIndex) {
        this.population = population;
        this.placeIndex = placeIndex;
    }

    public Population convert(Set<? extends de.dbanalytics.spic.data.Person> persons) {
        for(de.dbanalytics.spic.data.Person person : persons) {
            convert(person);
        }
        return population;
    }

    public Person convert(de.dbanalytics.spic.data.Person person) {
        PopulationFactory factory = population.getFactory();
        ObjectAttributes attributes = population.getPersonAttributes();
        /*
        Create person and add to population.
         */
        Person matsimPerson = factory.createPerson(Id.create(person.getId(), Person.class));
        population.addPerson(matsimPerson);
        /*
        Transfer attributes.
         */
        for(String key : person.keys()) {
            attributes.putAttribute(person.getId(), key, person.getAttribute(key));
        }
        /*
        Convert episodes.
         */
        Collection<? extends Episode> episodes = person.getEpisodes();
        for (Episode episode : episodes) {
            /*
            Create and add plan.
             */
            Plan matsimPlan = factory.createPlan();
            matsimPerson.addPlan(matsimPlan);
            /*
            Insert activities and legs.
             */
            for (int i = 0; i < episode.getActivities().size(); i++) {
                Segment actSegment = episode.getActivities().get(i);
                /*
                Create and add activity.
                 */
                String type = actSegment.getAttribute(CommonKeys.ACTIVITY_TYPE);
                Place place = placeIndex.get(actSegment.getAttribute(CommonKeys.ACTIVITY_FACILITY));
                Activity matsimAct = factory.createActivityFromCoord(
                        type,
                        new Coord(place.getGeometry().getCoordinate().x, place.getGeometry().getCoordinate().y));
                matsimAct.setFacilityId(Id.create(place.getId(), ActivityFacility.class));
                matsimPlan.addActivity(matsimAct);
                /*
                Transfer attributes.
                 */
                String startTime = actSegment.getAttribute(CommonKeys.ACTIVITY_START_TIME);
                matsimAct.setStartTime((int) Double.parseDouble(startTime));

                String endTime = actSegment.getAttribute(CommonKeys.ACTIVITY_END_TIME);
                matsimAct.setEndTime((int) Double.parseDouble(endTime));
                /*
                Create and add leg.
                 */
                if (i < episode.getLegs().size()) {
                    Segment legSegment = episode.getLegs().get(i);
                    String mode = legSegment.getAttribute(CommonKeys.LEG_MODE);
                    Leg matsimLeg = factory.createLeg(mode);
                    matsimPlan.addLeg(matsimLeg);
                    /*
                    Transfer attributes
                     */
                    matsimLeg.setDepartureTime(Double.parseDouble(legSegment.getAttribute(CommonKeys.LEG_START_TIME)));
                }
            }
        }

        return matsimPerson;
    }
}
