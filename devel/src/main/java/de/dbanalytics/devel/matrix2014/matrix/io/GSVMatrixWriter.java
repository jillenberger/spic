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

package de.dbanalytics.devel.matrix2014.matrix.io;

import de.dbanalytics.spic.matrix.NumericMatrix;
import org.apache.commons.lang3.tuple.Pair;
import org.matsim.core.utils.io.IOUtils;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * @author johannes
 */
public class GSVMatrixWriter {

    public static final String MODE_KEY = "mode";

    public static final String PURPOSE_KEY = "purpose";

    public static final String DIRECTION_KEY = "direction";

    private static final String FROM_KEY = "from";

    private static final String TO_KEY = "to";

    private static final String VOLUME_KEY = "volume";

    public static final String SEPARATOR = ";";

    public static void write(Collection<Pair<NumericMatrix, Map<String, String>>> matrices, String file) throws IOException {
        BufferedWriter writer = IOUtils.getBufferedWriter(file);

        writer.write(FROM_KEY);
        writer.write(SEPARATOR);
        writer.write(TO_KEY);
        writer.write(SEPARATOR);
        writer.write(VOLUME_KEY);
        writer.write(SEPARATOR);
        writer.write(MODE_KEY);
        writer.write(SEPARATOR);
        writer.write(PURPOSE_KEY);
        writer.write(SEPARATOR);
        writer.write(DIRECTION_KEY);
        writer.newLine();

        for(Pair<NumericMatrix, Map<String, String>> pair : matrices) {
            NumericMatrix m = pair.getLeft();
            Map<String, String> dimensions = pair.getRight();

            String dimString = buildDimensionString(dimensions);

            Set<String> ids = m.keys();
            for(String i : ids) {
                for(String j : ids) {
                    Double vol = m.get(i, j);
                    if(vol != null) {
                        writer.write(i);
                        writer.write(SEPARATOR);
                        writer.write(j);
                        writer.write(SEPARATOR);
                        writer.write(String.valueOf(vol));
                        writer.write(SEPARATOR);
                        writer.write(dimString);
                        writer.newLine();
                    }
                }
            }
        }

        writer.close();
    }

    private static String buildDimensionString(Map<String, String> dimension) {
        StringBuilder builder = new StringBuilder();

        String value = dimension.get(MODE_KEY);
        if(value != null) builder.append(value);

        builder.append(SEPARATOR);
        value = dimension.get(PURPOSE_KEY);
        if(value != null) builder.append(value);

        builder.append(SEPARATOR);
        value = dimension.get(DIRECTION_KEY);
        if(value != null) builder.append(value);

        return builder.toString();
    }
}
