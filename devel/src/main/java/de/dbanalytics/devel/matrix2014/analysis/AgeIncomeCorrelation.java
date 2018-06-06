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

import de.dbanalytics.spic.analysis.AnalyzerTask;
import de.dbanalytics.spic.analysis.FileIOContext;
import de.dbanalytics.spic.analysis.StatsContainer;
import de.dbanalytics.spic.data.Person;
import de.dbanalytics.spic.mid2008.MidAttributes;
import gnu.trove.list.array.TDoubleArrayList;
import gnu.trove.map.hash.TDoubleDoubleHashMap;
import org.matsim.contrib.common.stats.Correlations;
import org.matsim.contrib.common.stats.StatsWriter;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

/**
 * @author johannes
 *
 */
public class AgeIncomeCorrelation implements AnalyzerTask<Collection<? extends Person>> {

	private final FileIOContext ioContext;

	public AgeIncomeCorrelation(FileIOContext ioContext) {
		this.ioContext = ioContext;
	}

	@Override
	public void analyze(Collection<? extends Person> persons, List<StatsContainer> containers) {
		TDoubleArrayList ages = new TDoubleArrayList();
		TDoubleArrayList incomes = new TDoubleArrayList();

		for(Person person : persons) {
			String aStr = person.getAttribute(MidAttributes.KEY.AGE);
			String iStr = person.getAttribute(MidAttributes.KEY.HH_INCOME);
			if(aStr != null && iStr != null) {
				double age = Double.parseDouble(aStr);
				double income = Double.parseDouble(iStr);

				ages.add(age);
				incomes.add(income);
			}
		}

		TDoubleDoubleHashMap correl = Correlations.mean(ages.toArray(), incomes.toArray());
		try {
			StatsWriter.writeHistogram(correl, "age", "income", String.format("%s/age-income.txt", ioContext.getPath()));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
