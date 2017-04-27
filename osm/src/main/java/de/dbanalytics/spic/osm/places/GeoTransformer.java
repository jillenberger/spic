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

package de.dbanalytics.spic.osm.places;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import org.geotools.referencing.CRS;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CRSAuthorityFactory;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.NoninvertibleTransformException;
import org.opengis.referencing.operation.TransformException;

/**
 * Created by johannesillenberger on 26.04.17.
 */
public class GeoTransformer {

    private MathTransform forwardTransform;
    private MathTransform backwardTransform;

    public GeoTransformer(int source, int target) {
        CRSAuthorityFactory factory = CRS.getAuthorityFactory(true);
        try {
            CoordinateReferenceSystem sourceCrs = factory.createCoordinateReferenceSystem(
                    String.format("EPSG:%s", source));
            CoordinateReferenceSystem targetCrs = factory.createCoordinateReferenceSystem(
                    String.format("EPSG:%s", target));

            forwardTransform = CRS.findMathTransform(sourceCrs, targetCrs);
            backwardTransform = forwardTransform.inverse();
        } catch (FactoryException e) {
            e.printStackTrace();
        } catch (NoninvertibleTransformException e) {
            e.printStackTrace();
        }
    }

    public GeoTransformer(CoordinateReferenceSystem source, CoordinateReferenceSystem target) {
        try {
            forwardTransform = CRS.findMathTransform(source, target);
            backwardTransform = forwardTransform.inverse();
        } catch (FactoryException e) {
            e.printStackTrace();
        } catch (NoninvertibleTransformException e) {
            e.printStackTrace();
        }
    }

    public static GeoTransformer WGS84toWebMercartor() {
        return new GeoTransformer(4326, 3857);
    }

    public static GeoTransformer WGS84toX(int code) {
        return new GeoTransformer(4326, code);
    }

    public void forward(double[] coordinate) {
        try {
            forwardTransform.transform(coordinate, 0, coordinate, 0, 1);
        } catch (TransformException e) {
            e.printStackTrace();
        }
    }

    public void backward(double[] coordinate) {
        try {
            backwardTransform.transform(coordinate, 0, coordinate, 0, 1);
        } catch (TransformException e) {
            e.printStackTrace();
        }
    }

    public void forward(Geometry geometry) {
        for (Coordinate coordinate : geometry.getCoordinates()) {
            forward(coordinate);
        }
    }

    public void forward(Coordinate coordinate) {
        double[] xy = new double[]{coordinate.x, coordinate.y, coordinate.z};
        forward(xy);
        coordinate.x = xy[0];
        coordinate.y = xy[1];
        coordinate.z = xy[2];
    }

    public void backward(Coordinate coordinate) {
        double[] xy = new double[]{coordinate.x, coordinate.y, coordinate.z};
        backward(xy);
        coordinate.x = xy[0];
        coordinate.y = xy[1];
        coordinate.z = xy[2];
    }

    public MathTransform getForwardTransform() {
        return forwardTransform;
    }

    public MathTransform getBackwardTransform() {
        return backwardTransform;
    }
}
