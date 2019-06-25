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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.dbanalytics.spic.analysis;

import org.matsim.contrib.common.collections.Composite;

import java.util.List;

/**
 * Created by johannesillenberger on 10.05.17.
 */
public class PredicateOrComposite<T> extends Composite<Predicate<T>> implements Predicate<T> {

    public static <T> PredicateOrComposite<T> create(List<Predicate<T>> predicates) {
        PredicateOrComposite<T> composite = new PredicateOrComposite<>();
        for (Predicate<T> pred : predicates) if (pred != null) composite.addComponent(pred);
        return composite;
    }

    public static <T> PredicateOrComposite<T> create(Predicate<T>... predicates) {
        PredicateOrComposite<T> composite = new PredicateOrComposite<>();
        for (Predicate<T> pred : predicates) if (pred != null) composite.addComponent(pred);
        return composite;
    }

    @Override
    public boolean test(T t) {
        for (Predicate<T> predicate : components) {
            if (predicate.test(t)) return true;
        }
        return false;
    }
}
