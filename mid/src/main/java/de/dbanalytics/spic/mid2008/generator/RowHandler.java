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

import de.dbanalytics.spic.util.IOUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author johannes
 *
 */
public abstract class RowHandler {

	private String separator = "\t";

    private int offset = 0;

    protected abstract void handleRow(Map<String, String> attributes);
	
	public void setSeparator(String separator) {
		this.separator = separator;
	}
	
	public void setColumnOffset(int offset) {
		this.offset = offset;
	}
	
	public void read(String file) throws IOException {
		BufferedReader reader = IOUtils.createBufferedReader(file);

		String line = reader.readLine();
		String keys[] = line.split(separator, -1);
		Map<String, String> attributes = new HashMap<String, String>(keys.length);
		
		int lineCount = 1;
		while((line = reader.readLine()) != null) {
			String tokens[] = line.split(separator, -1);

            if (tokens.length - offset > keys.length)
                throw new RuntimeException(String.format("Line %s has more fields (%s) than available keys (%s).", lineCount, tokens.length, keys.length));
			
			for(int i = offset; i < tokens.length; i++) {
				attributes.put(keys[i - offset], tokens[i]);
			}
	
			handleRow(attributes);
		}
		
		reader.close();
	}
}
