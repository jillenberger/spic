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

package de.dbanalytics.devel.matrix2014.analysis;

import de.dbanalytics.spic.analysis.*;
import de.dbanalytics.spic.data.Attributes;
import de.dbanalytics.spic.data.Person;
import de.dbanalytics.spic.data.Segment;
import de.dbanalytics.spic.mid2008.MiDKeys;
import de.dbanalytics.spic.mid2008.MiDValues;
import gnu.trove.iterator.TObjectDoubleIterator;
import gnu.trove.map.hash.TObjectDoubleHashMap;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author johannes
 */
public class DaySeasonTask implements de.dbanalytics.spic.analysis.AnalyzerTask<Collection<? extends Person>> {

    @Override
    public void analyze(Collection<? extends Person> persons, List<StatsContainer> containers) {
        Collector<String> dayCollector = new LegPersonCollector<>(new AttributeProvider<Person>(Attributes.KEY.WEEKDAY));
        Collector<String> seasonCollector = new LegPersonCollector<>(new AttributeProvider<Person>(MiDKeys
                .PERSON_MONTH));
        Collector<String> purposeCollector = new LegCollector<>(new AttributeProvider<Segment>(Attributes.KEY.TRAVEL_PURPOSE));

        List<String> days = dayCollector.collect(persons);
        List<String> months = seasonCollector.collect(persons);
        List<String> purposes =purposeCollector.collect(persons);

        Map<String, TObjectDoubleHashMap<String>> map = new HashMap<>();

        for(int i = 0; i < days.size(); i++) {
            String day = days.get(i);
            String month = months.get(i);
            String purpose = purposes.get(i);

            if(day != null && month != null) {
                String season = "S";
                if(month.equalsIgnoreCase(MiDValues.NOVEMBER) ||
                        month.equalsIgnoreCase(MiDValues.DECEMBER) ||
                        month.equalsIgnoreCase(MiDValues.JANUARY) ||
                        month.equalsIgnoreCase(MiDValues.FEBRUARY) ||
                        month.equalsIgnoreCase(MiDValues.MARCH)) {
                    season = "W";
                }

                String key = String.format("%s.%s", purpose, season);
                TObjectDoubleHashMap<String> hist = map.get(key);
                if(hist == null) {
                    hist = new TObjectDoubleHashMap<>();
                    map.put(key, hist);
                }

                hist.adjustOrPutValue(day, 1, 1);
            }
        }

        for(Map.Entry<String, TObjectDoubleHashMap<String>> entry : map.entrySet()) {
            System.out.print(entry.getKey());
            System.out.print(":");

            TObjectDoubleHashMap<String> hist = entry.getValue();
            Histogram.normalize(hist);

            TObjectDoubleIterator<String> it = hist.iterator();
            for(int i = 0; i < hist.size(); i++) {
                it.advance();
                System.out.print(" ");
                System.out.print(it.key());
                System.out.print("=");
                System.out.print(String.format("%.2f", it.value()));
                System.out.print(",");
            }
            System.out.println();
        }

    }
}
