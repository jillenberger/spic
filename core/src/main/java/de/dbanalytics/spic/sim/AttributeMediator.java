package de.dbanalytics.spic.sim;

import de.dbanalytics.spic.sim.data.CachedElement;

import java.util.ArrayList;
import java.util.List;

/**
 * @author johannes
 */
public class AttributeMediator {

    private List<AttributeObserver> observers = new ArrayList<>();

    public void attach(AttributeObserver observer) {
        observers.add(observer);
    }

    public void update(CachedElement subject, Object attribute, Object oldValue, Object newValue) {
        for(AttributeObserver observer : observers) {
            observer.update(attribute, oldValue, newValue, subject);
        }
    }
}
