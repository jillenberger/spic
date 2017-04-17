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

package de.dbanalytics.devel.matrix2014.matrix.io;

import de.dbanalytics.spic.matrix.NumericMatrix;

import java.io.*;
import java.util.Set;

/**
 * @author johannes
 *
 */
public class VisumOMatrixReader {

	public static NumericMatrix read(NumericMatrix m, String filename) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(filename));

		String line = reader.readLine();
		int startSection;
		if(line.equals("$O;D3")) startSection = 3;
		else if(line.equals("$OM;D3")) startSection = 4;
		else throw new RuntimeException("Unknown matrix format.");

		boolean comment = false;
		int sectionCount = 0;

		while((line = reader.readLine()) != null) {
			if(line.startsWith("*")) {
				comment = true;
			} else {
				if(comment) sectionCount++;
				comment = false;

				if(sectionCount == startSection) {
					line = line.trim();
					String[] tokens = line.split("\\s+");
					String i = tokens[0];
					String j = tokens[1];
					Double val = new Double(tokens[2]);

					m.set(i, j, val);
				} else if(sectionCount > 3) {
					break;
				}
			}


		}

		reader.close();

		return m;
	}

	public static void write(NumericMatrix m, String file) throws IOException {
		BufferedWriter writer = new BufferedWriter(new FileWriter(file));
		writer.write("$O;D3");
		writer.newLine();
		writer.write("* Von  Bis");
		writer.newLine();
		writer.write("0.00 24.00");
		writer.newLine();
		writer.write("* Faktor");
		writer.newLine();
		writer.write("1.00");
		writer.newLine();
		writer.write("*");
		writer.newLine();

		Set<String> keys = m.keys();
		for(String i : keys) {
			for(String j : keys) {
				Double vol = m.get(i, j);
				if(vol != null) {
					writer.write(i);
					writer.write(" ");
					writer.write(j);
					writer.write(" ");
					writer.write(String.valueOf(vol));
					writer.newLine();
				}
			}
		}

		writer.close();
	}
}
