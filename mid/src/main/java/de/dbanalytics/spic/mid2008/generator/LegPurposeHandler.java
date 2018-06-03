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

package de.dbanalytics.spic.mid2008.generator;

import de.dbanalytics.spic.data.ActivityTypes;
import de.dbanalytics.spic.data.CommonKeys;
import de.dbanalytics.spic.data.Segment;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author johannes
 */
public class LegPurposeHandler implements LegAttributeHandler {

    private Map<String, String> mapping;

    public LegPurposeHandler(Map<String, String> mapping) {
        this.mapping = mapping;
    }

    public static Map<String, String> loadMappingFromFile(String filename) throws IOException {
        Map<String, String> mapping = new HashMap<>();

        BufferedReader reader = new BufferedReader(new FileReader(filename));
        String line = reader.readLine();

        while ((line = reader.readLine()) != null) {
            String tokens[] = line.split("\\s");
            mapping.put(tokens[0], tokens[1]);
        }

        return mapping;
    }

    @Override
    public void handle(Segment leg, Map<String, String> attributes) {
        /*
        First, assign the main purpose label. Fallback to "misc" if no mapping is available.
         */
        String mainTypeCode = attributes.get(VariableNames.LEG_MAIN_TYPE);
        String mainTypeLabel = mapping.get(mainTypeCode);

        if(mainTypeLabel == null) mainTypeLabel = ActivityTypes.MISC;
        leg.setAttribute(CommonKeys.TRAVEL_PURPOSE, mainTypeLabel);
        /*
        Second, override the main purpose if a sub purpose label is available.
         */
        String subTypeCode = attributes.get(VariableNames.LEG_SUB_TYPE);
        String subTypeLabel = mapping.get(subTypeCode);

        if(subTypeLabel != null) leg.setAttribute(CommonKeys.TRAVEL_PURPOSE, subTypeLabel);
    }
}
