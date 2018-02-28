package de.dbanalytics.spic.matrix;

import de.dbanalytics.spic.data.Person;
import de.dbanalytics.spic.gis.*;
import de.dbanalytics.spic.job.Job;
import de.dbanalytics.spic.processing.TaskRunner;
import org.apache.commons.configuration2.HierarchicalConfiguration;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.util.Collection;
import java.util.Set;

public class AddODZonesJob implements Job {

    private static final String PLACES_FILE = "placesFile";

    private static final String ZONES_FILE = "zonesFile";

    private static final String SRID = "srid";

    private String placesFile;

    private String zonesFile;

    private PlaceIndex placeIndex;

    private ZoneIndex zoneIndex;

    private int srid;

    @Override
    public void configure(HierarchicalConfiguration config) {
        placesFile = config.getString(PLACES_FILE);
        zonesFile = config.getString(ZONES_FILE);
        srid = config.getInt(SRID, 0);
    }

    @Override
    public Collection<? extends Person> execute(Collection<? extends Person> population) {
        GeoTransformer transformer = null;
        if (srid > 0) {
            transformer = GeoTransformer.WGS84toX(srid);
        }

        if (placeIndex == null) {
            PlacesIO placesIO = new PlacesIO();
            if (transformer != null) placesIO.setGeoTransformer(transformer);
            try {
                Set<Place> places = placesIO.read(placesFile);
                placeIndex = new PlaceIndex(places);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (XMLStreamException e) {
                e.printStackTrace();
            }
        }

        if (zoneIndex == null) {
            FeaturesIO featuresIO = new FeaturesIO();
            if (transformer != null) featuresIO.setTransformer(transformer);
            try {
                Set<Feature> features = featuresIO.read(zonesFile);
                zoneIndex = new ZoneIndex(features);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        TaskRunner.runLegTask(new LegAddODZones(placeIndex, zoneIndex), population);

        return population;
    }
}
