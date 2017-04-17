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

package de.dbanalytics.devel.matrix2014.sim;

import com.vividsolutions.jts.geom.Coordinate;
import de.dbanalytics.devel.matrix2014.gis.ZoneSetLAU2Class;
import de.dbanalytics.spic.data.*;
import de.dbanalytics.spic.gis.*;
import de.dbanalytics.spic.processing.PersonTask;
import de.dbanalytics.spic.source.mid2008.MiDKeys;
import org.matsim.api.core.v01.Id;
import org.matsim.facilities.ActivityFacilities;
import org.matsim.facilities.ActivityFacility;

/**
 * @author johannes
 */
public class SetLAU2Attribute implements PersonTask {

    private final ActivityFacilities facilities;

    private final ZoneCollection zones;

    private int errors;

    public SetLAU2Attribute(DataPool dataPool, String layerName) {
        facilities = ((FacilityData)dataPool.get(FacilityDataLoader.KEY)).getAll();
        zones = ((ZoneData)dataPool.get(ZoneDataLoader.KEY)).getLayer(layerName);
        errors = 0;
    }

    public int getErrors() {
        return errors;
    }

    @Override
    public void apply(Person person) {
        ActivityFacility f = null;
        for(Episode episode : person.getEpisodes()) {
            for(Segment act : episode.getActivities()) {
                if(ActivityTypes.HOME.equalsIgnoreCase(act.getAttribute(CommonKeys.ACTIVITY_TYPE))) {
                    String id = act.getAttribute(CommonKeys.ACTIVITY_FACILITY);
                    if(id != null) {
                        f = facilities.getFacilities().get(Id.create(id, ActivityFacility.class));
                        if(f != null) break;

                    }
                }
            }

            if(f != null) break;
        }

        if(f != null) {
            Zone zone = zones.get(new Coordinate(f.getCoord().getX(), f.getCoord().getY()));
            if(zone != null) {
                String val = zone.getAttribute(ZoneData.POPULATION_KEY);
                if(val != null) {
                    double inhabs = Double.parseDouble(val);
//                    int lau2class = PersonMunicipalityClassHandler.getCategory((int)inhabs);
                    String lau2Class = ZoneSetLAU2Class.inhabitants2Class(inhabs);
                    person.setAttribute(MiDKeys.PERSON_LAU2_CLASS, lau2Class);
                }
            } else errors++;
        } else {
            errors++;
        }
    }
}
