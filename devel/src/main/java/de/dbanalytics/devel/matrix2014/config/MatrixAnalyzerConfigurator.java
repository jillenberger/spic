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

package de.dbanalytics.devel.matrix2014.config;

import de.dbanalytics.devel.matrix2014.analysis.MatrixComparator;
import de.dbanalytics.devel.matrix2014.analysis.MatrixDistanceCompare;
import de.dbanalytics.devel.matrix2014.analysis.MatrixMarginalsCompare;
import de.dbanalytics.devel.matrix2014.analysis.MatrixVolumeCompare;
import de.dbanalytics.spic.analysis.AnalyzerTaskComposite;
import de.dbanalytics.spic.analysis.FileIOContext;
import de.dbanalytics.spic.analysis.HistogramWriter;
import de.dbanalytics.spic.analysis.PassThroughDiscretizerBuilder;
import de.dbanalytics.spic.gis.*;
import de.dbanalytics.spic.matrix.MatrixBuilderFactory;
import de.dbanalytics.spic.matrix.NumericMatrix;
import de.dbanalytics.spic.matrix.NumericMatrixIO;
import org.apache.commons.lang3.tuple.Pair;
import org.matsim.contrib.common.stats.LinearDiscretizer;
import org.matsim.core.config.ConfigGroup;

/**
 * @author johannes
 */
public class MatrixAnalyzerConfigurator implements DataLoader {

    public static final String ZONE_LAYER_NAME = "zoneLayer";

    //public static final String PARAM_SET_KEY = "referenceMatrix";

    public static final String MATRIX_NAME = "name";

    public static final String MATRIX_FILE = "file";

    public static final String THRESHOLD = "volumeThreshold";

    private final ConfigGroup config;

    private final DataPool dataPool;

    private final FileIOContext ioContext;

    private final MatrixBuilderFactory factory;

    public MatrixAnalyzerConfigurator(ConfigGroup config, DataPool dataPool, MatrixBuilderFactory factory, FileIOContext ioContext) {
        this.config = config;
        this.dataPool = dataPool;
        this.factory = factory;
        this.ioContext = ioContext;
    }

    @Override
    public Object load() {
        String zoneLayerName = config.getValue(ZONE_LAYER_NAME);

        ActivityLocationLayer locationLayer = (ActivityLocationLayer) dataPool.get(ActivityLocationLayerLoader.KEY);
        ZoneData zoneData = (ZoneData) dataPool.get(ZoneDataLoader.KEY);
        ZoneCollection zones = zoneData.getLayer(zoneLayerName);

        String name = config.getValue(MATRIX_NAME);
        String path = config.getValue(MATRIX_FILE);
        String strThreshold = config.getValue(THRESHOLD);
        double threshold = 0;
        if (strThreshold != null)
            threshold = Double.parseDouble(strThreshold);

        NumericMatrix m = NumericMatrixIO.read(path);

        AnalyzerTaskComposite<Pair<NumericMatrix, NumericMatrix>> composite = new AnalyzerTaskComposite<>();

        HistogramWriter writer = new HistogramWriter(ioContext, new PassThroughDiscretizerBuilder(new
                LinearDiscretizer(0.05), "linear"));

        MatrixVolumeCompare volTask = new MatrixVolumeCompare(String.format("matrix.%s.vol", name));
        volTask.setIoContext(ioContext);
        volTask.setHistogramWriter(writer);

        MatrixDistanceCompare distTask = new MatrixDistanceCompare(String.format("matrix.%s.dist", name), zones);
        distTask.setFileIoContext(ioContext);

        MatrixMarginalsCompare marTask = new MatrixMarginalsCompare(String.format("matrix.%s", name));
        marTask.setHistogramWriter(writer);

        composite.addComponent(volTask);
        composite.addComponent(distTask);
        composite.addComponent(marTask);

        MatrixComparator analyzer = new MatrixComparator(m, factory.create(locationLayer, zones), composite);
        analyzer.setVolumeThreshold(threshold);

        return analyzer;
    }
}
