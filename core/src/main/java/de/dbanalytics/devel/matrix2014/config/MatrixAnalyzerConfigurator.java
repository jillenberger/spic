/* *********************************************************************** *
 * project: org.matsim.*
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2015 by the members listed in the COPYING,        *
 *                   LICENSE and WARRANTY file.                            *
 * email           : info at matsim dot org                                *
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *   See also COPYING, LICENSE and WARRANTY file                           *
 *                                                                         *
 * *********************************************************************** */

package de.dbanalytics.devel.matrix2014.config;

import de.dbanalytics.devel.matrix2014.analysis.*;
import de.dbanalytics.devel.matrix2014.gis.ActivityLocationLayer;
import de.dbanalytics.devel.matrix2014.gis.ActivityLocationLayerLoader;
import de.dbanalytics.spic.analysis.AnalyzerTaskComposite;
import de.dbanalytics.spic.analysis.FileIOContext;
import de.dbanalytics.spic.analysis.HistogramWriter;
import de.dbanalytics.spic.analysis.PassThroughDiscretizerBuilder;
import de.dbanalytics.spic.gis.*;
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
