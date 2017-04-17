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

import com.vividsolutions.jts.geom.Coordinate;
import de.dbanalytics.devel.matrix2014.data.PersonAttributeUtils;
import de.dbanalytics.spic.analysis.AnalyzerTask;
import de.dbanalytics.spic.analysis.StatsContainer;
import de.dbanalytics.spic.data.Person;
import de.dbanalytics.spic.gis.*;
import de.dbanalytics.spic.source.mid2008.MiDKeys;
import org.apache.log4j.Logger;
import org.matsim.facilities.ActivityFacilities;
import org.matsim.facilities.ActivityFacility;

import java.util.Collection;
import java.util.List;

/**
 * @author jillenberger
 */
public class ValidateLAU2Attribute implements AnalyzerTask<Collection<? extends Person>> {

    private static final Logger logger = Logger.getLogger(ValidateLAU2Attribute.class);

    private final ZoneCollection zones;

    private final ActivityFacilities facilities;

    public ValidateLAU2Attribute(DataPool dataPool) {
        zones = ((ZoneData) dataPool.get(ZoneDataLoader.KEY)).getLayer("lau2");
        facilities = ((FacilityData) dataPool.get(FacilityDataLoader.KEY)).getAll();
    }

    @Override
    public void analyze(Collection<? extends Person> persons, List<StatsContainer> containers) {
        int cnt = 0;
        int fail = 0;

        for(Person p : persons) {
            ActivityFacility f = PersonAttributeUtils.getHomeFacility(p, facilities);
            if(f != null) {
                Zone z = zones.get(new Coordinate(f.getCoord().getX(), f.getCoord().getY()));
                if(z != null) {
                   if(!p.getAttribute(MiDKeys.PERSON_LAU2_CLASS).equalsIgnoreCase(z.getAttribute(MiDKeys.PERSON_LAU2_CLASS))) {
                       cnt++;
                   }
                } else {
                    fail++;
                }
            } else {
                fail++;
            }
        }

        if(cnt > 0) logger.warn(String.format("%s persons located in wrong LAU2 zone", cnt));
        if(fail > 0) logger.warn(String.format("Failed to obtain home zone for %s persons", fail));
    }
}
