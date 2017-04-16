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

package de.dbanalytics.spic.osm;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by NicoKuehnel on 26.09.2016.
 */
public class Mapping {

    static Map<String, String> tag2Type;

    public static void initTag2TypeFromCsv(String file) {

        tag2Type = new HashMap<String, String>();


        String line;
        String cvsSplitBy = ";";

        try (BufferedReader br = new BufferedReader(new FileReader(new File(file)))) {
            br.readLine();
            while ((line = br.readLine()) != null) {
                String[] lineString = line.split(cvsSplitBy);

                String key = lineString[0];
                String tag = lineString[1];
                String type = lineString[2];

                tag2Type.put(key + "_" + tag, type);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
