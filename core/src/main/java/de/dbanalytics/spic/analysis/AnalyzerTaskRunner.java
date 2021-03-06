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

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

/**
 * @author jillenberger
 */
public class AnalyzerTaskRunner {

    private static final String format = "%.4f";

    private static final String nullString = "NA";

    private static final String TAB = "\t";

    public static <T> void run(T persons, AnalyzerTask<T> task, FileIOContext context) {
        run(persons, task, String.format("%s/stats.txt", context.getPath()));
    }

    public static <T> void run(T persons, AnalyzerTask<T> task, String file) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            writer.write("dimension\tmean\tmin\tmax\tsize\twsum\tmedian\tvariance\tnullValues");
            writer.newLine();

            ArrayList<StatsContainer> containers = new ArrayList<>();
            task.analyze(persons, containers);

            for (StatsContainer container : containers) {
                writer.write(container.getName());
                writer.write(TAB);
                writer.write(doubleToString(container.getMean()));
                writer.write(TAB);
                writer.write(doubleToString(container.getMin()));
                writer.write(TAB);
                writer.write(doubleToString(container.getMax()));
                writer.write(TAB);
                writer.write(intToString(container.getN()));
                writer.write(TAB);
                writer.write(doubleToString(container.getWsum()));
                writer.write(TAB);
                writer.write(doubleToString(container.getMedian()));
                writer.write(TAB);
                writer.write(doubleToString(container.getVariance()));
                writer.write(TAB);
                writer.write(intToString(container.getNullValues()));

                writer.newLine();
            }

            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String doubleToString(Double value) {
        if(value == null) return nullString;
        else return String.format(Locale.US, format, value);
    }

    private static String intToString(Integer value) {
        if(value == null) return nullString;
        else return String.format(Locale.US, "%d", value);
    }
}
