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
import org.matsim.facilities.ActivityFacilities;

/**
 * @author johannes
 */
public class ActDistanceBuilder {

    private Predicate<Segment> predicate;

    private String predicateName;

    private boolean useWeights = false;

    private HistogramWriter writer;

    public ActDistanceBuilder setPredicate(Predicate<Segment> predicate, String predicateName) {
        this.predicate = predicate;
        this.predicateName = predicateName;
        return this;
    }

    public ActDistanceBuilder setUseWeights(boolean useWeights) {
        this.useWeights = useWeights;
        return this;
    }

    public ActDistanceBuilder setHistogramWriter(HistogramWriter writer) {
        this.writer = writer;
        return this;
    }

     public NumericAnalyzer build(ActivityFacilities facilities) {
        String dimension = "facDistance";

        ValueProvider<Double, Segment> provider = new FacilityDistanceProvider(facilities);

        LegCollector<Double> collector = new LegCollector<>(provider);
        if (predicate != null) {
            collector.setPredicate(predicate);
            dimension = String.format("%s.%s", dimension, predicateName);
        }

        LegPersonCollector<Double> weightCollector = null;
        if (useWeights) {
            ValueProvider<Double, Person> weightProvider = new NumericAttributeProvider<>(Attributes.KEY.WEIGHT);
            weightCollector = new LegPersonCollector<>(weightProvider);
            if (predicate != null) weightCollector.setPredicate(predicate);
        }

        return new NumericAnalyzer(collector, weightCollector, dimension, writer);
    }
}
