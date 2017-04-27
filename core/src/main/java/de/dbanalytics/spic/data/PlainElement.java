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

package de.dbanalytics.spic.data;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * @author johannes
 *
 */
public abstract class PlainElement implements Attributable {

	private Map<String, String> attributes;
	
	private Map<String, String> unmodAttribs;
	
	public Map<String, String> getAttributes() {
		initAttriutes();
		return unmodAttribs;
	}
	
	public String getAttribute(String key) {
		initAttriutes();
		return attributes.get(key);
	}
	
	public String setAttribute(String key, String value) {
		initAttriutes();
		return attributes.put(key, value);
	}
	
	public String removeAttribute(String key) {
		if(attributes != null) {
			return attributes.remove(key);
		} else {
			return null;
		}
	}

	@Override
	public Collection<String> keys() {
		initAttriutes();
		return attributes.keySet();
	}

	public PlainElement clone() {
		PlainElement clone = new PlainSegment();
		
		for(Entry<String, String> entry : attributes.entrySet()) {
			clone.setAttribute(entry.getKey(), entry.getValue());
		}
		
		return clone;
	}
	
	private void initAttriutes() {
		if(attributes == null) {
			attributes = new HashMap<String, String>(5);
			unmodAttribs = Collections.unmodifiableMap(attributes);
		}
	}
}
