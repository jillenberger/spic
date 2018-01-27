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

package de.dbanalytics.spic.matrix;

import com.vividsolutions.jts.geom.Point;
import de.dbanalytics.spic.analysis.*;
import de.dbanalytics.spic.gis.Feature;
import de.dbanalytics.spic.gis.ZoneIndex;
import gnu.trove.list.array.TDoubleArrayList;
import gnu.trove.map.TDoubleDoubleMap;
import gnu.trove.map.hash.TDoubleDoubleHashMap;
import org.apache.log4j.Logger;
import org.matsim.contrib.common.gis.DistanceCalculator;
import org.matsim.contrib.common.gis.OrthodromicDistanceCalculator;
import org.matsim.contrib.common.stats.Discretizer;
import org.matsim.contrib.common.stats.Histogram;
import org.matsim.contrib.common.stats.StatsWriter;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author johannes
 */
public class GeoDistanceTask implements AnalyzerTask<NumericMatrix> {

    private static final Logger logger = Logger.getLogger(GeoDistanceTask.class);

    private final ZoneIndex zones;

    private final DistanceCalculator distanceCalculator;

    private final DiscretizerBuilder discretizerBuilder;

    private final FileIOContext ioContext;

    public GeoDistanceTask(ZoneIndex zones, FileIOContext ioContext) {
        this(zones, ioContext, new StratifiedDiscretizerBuilder(50, 50), new OrthodromicDistanceCalculator());
    }

    public GeoDistanceTask(ZoneIndex zones, FileIOContext ioContext, DiscretizerBuilder discretizerBuilder, DistanceCalculator calculator) {
        this.zones = zones;
        this.ioContext = ioContext;
        this.discretizerBuilder = discretizerBuilder;
        this.distanceCalculator = calculator;
    }

    @Override
    public void analyze(NumericMatrix m, List<StatsContainer> containers) {
        TDoubleArrayList values = new TDoubleArrayList();
        TDoubleArrayList weights = new TDoubleArrayList();

        NumericMatrix distanceMatrix = new NumericMatrix();

        Set<String> notfound = new HashSet<>();
        Set<String> keys = m.keys();
        for(String i : keys) {
            for(String j : keys) {
                Double vol = m.get(i, j);
                if(vol != null && vol > 0) {
                    double d = getDistance(i, j, distanceMatrix);
                    if(!Double.isInfinite(d)) {
                        values.add(d);
                        weights.add(vol);
                    } else {
                        if(zones.get(i) == null) notfound.add(i);
                        if(zones.get(j) == null) notfound.add(j);
                    }
                }
            }
        }

        if(!notfound.isEmpty()) logger.warn(String.format("Zone %s not found.", notfound.toString()));



        try {
            Discretizer discretizer = discretizerBuilder.build(values.toArray());
            TDoubleDoubleMap hist = Histogram.createHistogram(values.toArray(), weights.toArray(), discretizer, true);
            StatsWriter.writeHistogram((TDoubleDoubleHashMap) hist, "Distance", "Probability", ioContext.getPath() + "/geoDistance.txt");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private double getDistance(String i, String j, NumericMatrix distanceMatrix) {
        Double d = distanceMatrix.get(i, j);

        if(d == null) {
            Feature z_i = zones.get(i);
            Feature z_j = zones.get(j);

            if(z_i != null && z_j != null) {
                Point p_i = z_i.getGeometry().getCentroid();
                Point p_j = z_j.getGeometry().getCentroid();

                d = distanceCalculator.distance(p_i, p_j);
                distanceMatrix.set(i, j, d);
            } else {
                d = Double.POSITIVE_INFINITY;
            }
        }

        return d;
    }
}
