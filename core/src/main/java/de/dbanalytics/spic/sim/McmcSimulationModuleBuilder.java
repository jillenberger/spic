package de.dbanalytics.spic.sim;

public interface McmcSimulationModuleBuilder<T> {

    T build(McmcSimulationContext context);

}
