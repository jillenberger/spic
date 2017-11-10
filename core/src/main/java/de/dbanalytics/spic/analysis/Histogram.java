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

package de.dbanalytics.spic.analysis;

import gnu.trove.function.TDoubleFunction;
import gnu.trove.map.TDoubleDoubleMap;
import gnu.trove.map.TObjectDoubleMap;
import gnu.trove.map.hash.TDoubleDoubleHashMap;
import gnu.trove.map.hash.TObjectDoubleHashMap;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * @author johannes
 */
public class Histogram {

    public static TObjectDoubleMap<?> normalize(TObjectDoubleMap<?> histogram) {
        double sum = 0;
        double[] values = histogram.values();

        for (int i = 0; i < values.length; i++) {
            sum += values[i];
        }

        return normalize(histogram, sum);
    }

    //FIXME: This function standalone does not make much sense.
    public static TObjectDoubleMap<?> normalize(TObjectDoubleMap<?> histogram, double sum) {
        final double norm = 1 / sum;

        TDoubleFunction fct = new TDoubleFunction() {
            public double execute(double value) {
                return value * norm;
            }

        };

        histogram.transformValues(fct);

        return histogram;
    }

    public static TDoubleDoubleMap readHistogram(String filename) throws IOException {
        TDoubleDoubleMap hist = new TDoubleDoubleHashMap();
        BufferedReader reader = new BufferedReader(new FileReader(filename));
        String line = reader.readLine();
        while ((line = reader.readLine()) != null) {
            String tokens[] = line.split("\t");
            double bin = Double.parseDouble(tokens[0]);
            double height = Double.parseDouble(tokens[1]);
            hist.put(bin, height);
        }
        reader.close();

        return hist;
    }

    public static TObjectDoubleMap<String> readLabeledHistogram(String filename) throws IOException {
        TObjectDoubleMap<String> hist = new TObjectDoubleHashMap<>();
        BufferedReader reader = new BufferedReader(new FileReader(filename));
        String line = reader.readLine();
        while ((line = reader.readLine()) != null) {
            String tokens[] = line.split("\t");
            String label = tokens[0];
            double height = Double.parseDouble(tokens[1]);
            hist.put(label, height);
        }
        reader.close();

        return hist;
    }
}
