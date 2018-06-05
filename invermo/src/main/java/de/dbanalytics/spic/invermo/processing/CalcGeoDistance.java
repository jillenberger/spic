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

package de.dbanalytics.spic.invermo.processing;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import de.dbanalytics.spic.data.Attributable;
import de.dbanalytics.spic.data.Attributes;
import de.dbanalytics.spic.data.Episode;
import de.dbanalytics.spic.invermo.InvermoKeys;
import de.dbanalytics.spic.processing.EpisodeTask;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.matsim.contrib.common.gis.DistanceCalculator;
import org.matsim.contrib.common.gis.OrthodromicDistanceCalculator;

/**
 * @author johannes
 *
 */
public class CalcGeoDistance implements EpisodeTask {

	private final GeometryFactory factory = JTSFactoryFinder.getGeometryFactory(null);
	
	private final DistanceCalculator dCalc = OrthodromicDistanceCalculator.getInstance();
	

	@Override
	public void apply(Episode plan) {
		for(int i = 0; i < plan.getLegs().size(); i++) {
			Attributable prev = plan.getActivities().get(i);
			Attributable leg = plan.getLegs().get(i);
			Attributable next = plan.getActivities().get(i + 1);
			
			String sourceStr = prev.getAttribute(InvermoKeys.COORD);
			String destStr = next.getAttribute(InvermoKeys.COORD);
			
			if(sourceStr != null && destStr != null) {
				Point source = string2Coord(sourceStr);
				Point dest = string2Coord(destStr);
				
				double d = dCalc.distance(source, dest);

				leg.setAttribute(Attributes.KEY.BEELINE_DISTANCE, String.valueOf(d));
			}
		}

	}
	
	private Point string2Coord(String str) {
		String tokens[] = str.split(",");
		double x = Double.parseDouble(tokens[0]);
		double y = Double.parseDouble(tokens[1]);

		Point p = factory.createPoint(new Coordinate(x, y));
		p.setSRID(4326);
		return p;
	}
}
