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

package de.dbanalytics.spic.matrix;

import org.matsim.core.utils.io.MatsimXmlParser;
import org.xml.sax.Attributes;

import java.util.Stack;

/**
 * @author johannes
 *
 */
public class NumericMatrixXMLReader extends MatsimXmlParser {

	private NumericMatrix m;
	
	public NumericMatrix getMatrix() {
		return m;
	}
	
	@Override
	public void startTag(String name, Attributes atts, Stack<String> context) {
		if(name.equalsIgnoreCase(NumericMatrixXMLWriter.MATRIX_TAG)) {
			m = new NumericMatrix();
		} else if(name.equalsIgnoreCase(NumericMatrixXMLWriter.CELL_TAG)) {
			String row = atts.getValue(NumericMatrixXMLWriter.ROW_KEY);
			String col = atts.getValue(NumericMatrixXMLWriter.COL_KEY);
			String val = atts.getValue(NumericMatrixXMLWriter.VALUE_KEY);
			
			m.set(row, col, new Double(val));
		}

	}

	@Override
	public void endTag(String name, String content, Stack<String> context) {
	}
}
