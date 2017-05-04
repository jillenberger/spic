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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.dbanalytics.spic.source.mid2008HH;

import de.dbanalytics.spic.data.*;
import de.dbanalytics.spic.data.io.PopulationIO;
import de.dbanalytics.spic.gis.*;
import org.matsim.contrib.common.util.XORShiftRandom;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.util.*;

/**
 * Created by johannesillenberger on 04.05.17.
 */
public class HomeLocator {

    private final Random random;

    public HomeLocator() {
        this(new XORShiftRandom());
    }

    public HomeLocator(Random random) {
        this.random = random;
    }

    public static void main(String args[]) throws IOException, XMLStreamException {
        String refPersonsFile = "";
        String zoneFile = "";
        String placesFile = "";
        String outFile = "";

        Set<Person> refPersons = PopulationIO.loadFromXML(refPersonsFile, new PlainFactory());
        ZoneCollection zones = ZoneGeoJsonIO.readFromGeoJSON(zoneFile, "PLZ8", null);
        PlacesIO placesIO = new PlacesIO();
        placesIO.setGeoTransformer(GeoTransformer.WGS84toX(31467));
        Set<Place> places = placesIO.read(placesFile);

        HomeLocator locator = new HomeLocator();
        Set<Person> clones = locator.run(refPersons, new PlaceIndex(places), zones, "SV_HVV", "A_GESAMT");

        PopulationIO.writeToXML(outFile, clones);
    }

    public Set<Person> run(Set<Person> refPersons, PlaceIndex placeIndex, ZoneCollection zones, String key, String inhabKey) {
        AttributableIndex<Person> personIndex = new AttributableIndex<>(refPersons);
        Set<Person> targetPersons = new HashSet<>();

        for (Zone zone : zones.getZones()) {
            String value = zone.getAttribute(key);
            Set<Person> templates = personIndex.get(key, value);

            String inhabValue = zone.getAttribute(inhabKey);
            int inhabitants = 0;
            if (inhabValue != null) inhabitants = Integer.parseInt(inhabValue);

            Set<Person> clones = (Set<Person>) PersonUtils.weightedCopy(templates, new PlainFactory(), inhabitants, random);

            List<Place> homePlaces = new ArrayList<>(placeIndex.getForActivity(zone.getGeometry(), "home"));

            clones.parallelStream().forEach(clone -> {
                Place home = homePlaces.get(random.nextInt(homePlaces.size()));
                clone.getEpisodes().stream().forEach(episode -> episode.getActivities().
                        stream().
                        filter(activity -> "home".equalsIgnoreCase(activity.getAttribute(CommonKeys.ACTIVITY_TYPE))).
                        forEach(activity -> activity.setAttribute(CommonKeys.ACTIVITY_FACILITY, home.getId()))
                );
            });


            targetPersons.addAll(clones);
        }

        return targetPersons;
    }
}
