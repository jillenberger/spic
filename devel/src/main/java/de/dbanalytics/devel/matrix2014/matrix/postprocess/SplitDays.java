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

package de.dbanalytics.devel.matrix2014.matrix.postprocess;

import de.dbanalytics.spic.data.CommonKeys;
import de.dbanalytics.spic.gis.ZoneIndex;
import gnu.trove.list.array.TDoubleArrayList;
import gnu.trove.map.TObjectDoubleMap;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;
import org.matsim.contrib.common.gis.CartesianDistanceCalculator;
import org.matsim.contrib.common.gis.DistanceCalculator;
import org.matsim.contrib.common.stats.Discretizer;
import org.matsim.contrib.common.stats.FixedBordersDiscretizer;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @author johannes
 */
public class SplitDays {

    private static final Logger logger = Logger.getLogger(SplitSeason.class);

    public static void main(String args[]) throws IOException {
        String sourceFile = args[0];// "/Users/johannes/gsv/matrix2014/sim/output/1E7/gsv-matrix-season.txt";
        String targetFile = args[3];//"/Users/johannes/gsv/matrix2014/sim/output/1E7/gsv-matrix-day.txt";
        String shareFile = args[1];//"/Users/johannes/gsv/matrix2014/sim/output/day-share.txt";
        String zoneFile = args[2];//"/Users/johannes/gsv/gis/zones/geojson/tomtom.de.gk3.geojson";
        /*
        Load share table.
         */
        logger.info("Loading share table...");
        List<Pair<Map<String, String>, TObjectDoubleMap<String>>> shareTable = SplitSeason.loadShareTable(shareFile, 4);
        /*
        Create distance discretizer.
         */
        // TODO: Consolidate with SeasonTask
        TDoubleArrayList borders = new TDoubleArrayList();
        borders.add(-1);
        borders.add(50000);
        borders.add(100000);
        borders.add(1000000);
        borders.add(Double.MAX_VALUE);
        Discretizer discretizer = new FixedBordersDiscretizer(borders.toArray());
        /*
        Load zones...
         */
        logger.info("Loading zones...");
//        ZoneCollection zones = ZoneGeoJsonIO.readFromGeoJSON(zoneFile, "NO", "modena");
        ZoneIndex zones = null;
        /*
        Create dimension calculator.
         */
//        DistanceCalculator distanceCalculator = new WGS84DistanceCalculator();
        DistanceCalculator distanceCalculator = CartesianDistanceCalculator.getInstance();
        DistanceDimensionCalculator calculator = new DistanceDimensionCalculator(zones, distanceCalculator, discretizer);
        /*
        Run matrix splitter...
         */
        MatrixSplitter splitter = new MatrixSplitter(CommonKeys.DAY, shareTable);
        splitter.addDimensionCalculator(CommonKeys.BEELINE_DISTANCE, calculator);

        logger.info("Processing matrix...");
        splitter.process(sourceFile, targetFile);
        logger.info("Done.");
    }
}
