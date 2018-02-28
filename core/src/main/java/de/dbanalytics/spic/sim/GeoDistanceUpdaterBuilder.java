package de.dbanalytics.spic.sim;

public class GeoDistanceUpdaterBuilder implements McmcSimulationModuleBuilder<AttributeObserver> {

    @Override
    public AttributeObserver build(McmcSimulationContext context) {
        return new GeoDistanceUpdater(context.getAttributeMediator());
    }
}
