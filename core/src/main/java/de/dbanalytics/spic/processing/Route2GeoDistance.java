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
package de.dbanalytics.spic.processing;

import de.dbanalytics.spic.data.Attributable;
import de.dbanalytics.spic.data.Attributes;
import de.dbanalytics.spic.data.Episode;
import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.analysis.UnivariateRealFunction;

/**
 * @author jillenberger
 */
public class Route2GeoDistance implements EpisodeTask {

    private UnivariateRealFunction function;

    public Route2GeoDistance(UnivariateRealFunction function) {
        this.function = function;
    }

    @Override
    public void apply(Episode episode) {
        for(Attributable leg : episode.getLegs()) {
            String routeDist = leg.getAttribute(Attributes.KEY.TRIP_DISTANCE);
            if(routeDist != null) {
                double rDist = Double.parseDouble(routeDist);

                double gDist = 0;
                try {
                    gDist = function.value(rDist);
                } catch (FunctionEvaluationException e) {
                    e.printStackTrace();
                }

                leg.setAttribute(Attributes.KEY.BEELINE_DISTANCE, String.valueOf(gDist));
            }
        }
    }
}
