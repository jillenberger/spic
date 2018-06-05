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

import de.dbanalytics.spic.data.*;
import org.matsim.contrib.common.stats.LinearDiscretizer;

/**
 * @author jillenberger
 */
public class TripsPerPersonTask {

    public NumericAnalyzer build(FileIOContext ioContext) {
        ValueProvider<Double, Episode> provider = new TripsCounter(new ModePredicate(Attributes.MODE.CAR));
        EpisodeCollector<Double> collector = new EpisodeCollector<>(provider);

        DiscretizerBuilder builder = new PassThroughDiscretizerBuilder(new LinearDiscretizer(1.0), "linear");
        HistogramWriter writer = new HistogramWriter(ioContext, builder);

        ValueProvider<Double, Person> weightsProvider = new NumericAttributeProvider<>(Attributes.KEY.WEIGHT);
        EpisodePersonCollector<Double> weightsCollector = new EpisodePersonCollector<>(weightsProvider);

        NumericAnalyzer analyzer = new NumericAnalyzer(collector, weightsCollector, "nTrips", writer);

        return analyzer;
    }

}
