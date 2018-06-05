package de.dbanalytics.spic.data.io;

import de.dbanalytics.spic.data.Person;
import de.dbanalytics.spic.data.PlainFactory;

import java.util.Set;

public class RunConverter {

    public static void main(String args[]) {
        if (args.length < 2) {
            System.out.println("Too few arguments.");
            System.exit(0);
        }

        String inFile = args[0];
        String outFile = args[1];

        Set<Person> population = PopulationIO.loadFromXML(inFile, new PlainFactory());
        PopulationIO.writeToXML(outFile, population);
    }

}
