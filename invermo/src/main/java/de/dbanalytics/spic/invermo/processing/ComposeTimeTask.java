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

package de.dbanalytics.spic.invermo.processing;

import de.dbanalytics.spic.data.Attributable;
import de.dbanalytics.spic.data.Episode;
import de.dbanalytics.spic.processing.EpisodeTask;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author johannes
 * 
 */
public class ComposeTimeTask implements EpisodeTask {

	public static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");

	@Override
	public void apply(Episode plan) {
		for (Attributable leg : plan.getLegs()) {
			setStartTime(leg);
			setEndTime(leg);
		}

	}

	private void setEndTime(Attributable leg) {
		StringBuilder builder = new StringBuilder(100);

		boolean valid = true;

		String value = leg.removeAttribute("endTimeYear");
		if (value == null) {
			valid = false;
		} else {
			if (value.equals("1"))
				value = "2001";
			if (value.equals("0"))
				value = "2000";
			if (value.equals("2"))
				value = "2002";
			if (value.equals("99"))
				value = "1999";
			if (value.equals("3899"))
				value = "1999";
			if (value.equals("0082"))
				value = "1982";
			builder.append(value);
			builder.append("-");
		}

		value = leg.removeAttribute("endTimeMonth");
		if (value == null) {
			valid = false;
		}
		builder.append(makeTwoDigit(value));
		builder.append("-");

		value = leg.removeAttribute("endTimeDay");
		if (value == null) {
			valid = false;
		}
		builder.append(makeTwoDigit(value));
		builder.append(" ");

		value = leg.removeAttribute("endTimeHour");
		if (value == null) {
			valid = false;
		}
		builder.append(makeTwoDigit(value));
		builder.append(":");

		value = leg.removeAttribute("endTimeMin");
		if (value == null) {
			valid = false;
		}
		builder.append(makeTwoDigit(value));

		if (valid) {
			try {
				Date date = dateFormat.parse(builder.toString());
				String out = dateFormat.format(date);
				leg.setAttribute("endTime", out);

			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void setStartTime(Attributable leg) {
		StringBuilder builder = new StringBuilder(100);

		boolean valid = true;

		String value = leg.removeAttribute("startTimeYear");
		if (value == null) {
			valid = false;
		} else {
			if (value.equals("1"))
				value = "2001";
			if (value.equals("0"))
				value = "2000";
			if (value.equals("2"))
				value = "2002";
			if (value.equals("99"))
				value = "1999";
			if (value.equals("3899"))
				value = "1999";
			if (value.equals("82"))
				value = "1982";
			builder.append(value);
			builder.append("-");
		}

		value = leg.removeAttribute("startTimeMonth");
		if (value == null) {
			valid = false;
		}
		builder.append(makeTwoDigit(value));
		builder.append("-");

		value = leg.removeAttribute("startTimeDay");
		if (value == null) {
			valid = false;
		}
		builder.append(makeTwoDigit(value));
		builder.append(" ");

		value = leg.removeAttribute("startTimeHour");
		if (value == null) {
			valid = false;
		}
		builder.append(makeTwoDigit(value));
		builder.append(":");

		value = leg.removeAttribute("startTimeMin");
		if (value == null) {
			valid = false;
		}
		builder.append(makeTwoDigit(value));

		if (valid) {
			try {
				Date date = dateFormat.parse(builder.toString());
				String out = dateFormat.format(date);
				leg.setAttribute("startTime", out);

			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
	}
	
	private String makeTwoDigit(String number) {
		if (number == null)
			return "";

		int num = Integer.parseInt(number);
		return String.format("%02d", num);
	}
}
