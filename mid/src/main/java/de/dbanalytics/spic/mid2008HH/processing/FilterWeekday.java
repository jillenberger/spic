/*
 * (c) Copyright 2017 Johannes Illenberger
 *
 * Project de.dbanalytics.spic.
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

package de.dbanalytics.spic.mid2008HH.processing;

import de.dbanalytics.spic.analysis.AttributePredicate;
import de.dbanalytics.spic.analysis.Predicate;
import de.dbanalytics.spic.analysis.PredicateOrComposite;
import de.dbanalytics.spic.data.CommonKeys;
import de.dbanalytics.spic.data.CommonValues;
import de.dbanalytics.spic.data.Person;
import de.dbanalytics.spic.processing.PersonsTask;
import de.dbanalytics.spic.processing.TaskRunner;
import de.dbanalytics.spic.processing.ValidatePerson;

import java.util.Collection;

/**
 * Created by johannesillenberger on 10.05.17.
 */
public class FilterWeekday implements PersonsTask {

    private Predicate<Person> weekdayPredicate;

    public FilterWeekday() {
        weekdayPredicate = PredicateOrComposite.create(
                new AttributePredicate(CommonKeys.DAY, CommonValues.MONDAY),
                new AttributePredicate(CommonKeys.DAY, CommonValues.TUESDAY),
                new AttributePredicate(CommonKeys.DAY, CommonValues.WEDNESDAY),
                new AttributePredicate(CommonKeys.DAY, CommonValues.THURSDAY),
                new AttributePredicate(CommonKeys.DAY, CommonValues.FRIDAY)
        );
    }

    @Override
    public void apply(Collection<? extends Person> persons) {
        TaskRunner.validatePersons(new ValidatePerson(weekdayPredicate), persons);
    }
}
