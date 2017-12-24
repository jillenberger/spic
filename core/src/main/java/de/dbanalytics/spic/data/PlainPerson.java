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

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

/**
 * @author johannes
 *
 */
public class PlainPerson extends PlainElement implements Person {

	private String id;
	
//	private PlainEpisode plan;

	private List<Episode> plans = new ArrayList<>(1);
	
	public PlainPerson(String id) {
		this.id = id;		
	}
	
	public String getId() {
		return id;
	}
	
	public void setPlan(Episode plan) {
//		this.plan = plan;
		if(plans.isEmpty())
			addEpisode(plan);
		else {
			plans.set(0, plan);
			((PlainEpisode)plan).setPerson(this);
		}
	}
	
	public void addEpisode(Episode plan) {
		plans.add(plan);
		((PlainEpisode)plan).setPerson(this);
	}

	public void removeEpisode(Episode episode) {
		plans.remove(episode);
		((PlainEpisode)episode).setPerson(null);
	}

	public Episode getPlan() {
//		return plan;
		return plans.get(0);
	}
	
	public List<? extends Episode> getEpisodes() {
		return plans;
	}
	
	public PlainPerson clone() {
		return cloneWithNewId(id);
	}
	
	public PlainPerson cloneWithNewId(String newId) {
		PlainPerson clone = new PlainPerson(newId);
		
		for(Entry<String, String> entry : getAttributes().entrySet()) {
			clone.setAttribute(entry.getKey(), entry.getValue());
		}
		
//		clone.setPlan(plan.clone());
		for(Episode plan : plans)
			clone.addEpisode(((PlainEpisode) plan).clone());
		
		return clone;
	}
}
