/*
 * (c) Copyright 2017 Johannes Illenberger
 *
 *  Project de.dbanalytics.spic.*
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.dbanalytics.spic.sim;

/**
 * @author johannes
 */
public class RelativeErrorFunction implements ErrorFunction {

    private final double exponent;

    private final double infValue;

    public RelativeErrorFunction() {
        exponent = 1;
        infValue = Double.MAX_VALUE;
    }

    public RelativeErrorFunction(double exponent, double infValue) {
        this.exponent = exponent;
        this.infValue = infValue;
    }

    @Override
    public double evaluate(double simValue, double refValue) {
        if (refValue > 0) {
            return Math.pow(Math.abs(simValue - refValue) / refValue, exponent);
        } else {
            if (simValue == 0) return 0;
            else return infValue;
        }
    }
}
