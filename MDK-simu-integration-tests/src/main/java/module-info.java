/**
 * A small named-module simulation consumer used for startup integration tests.
 */
module madkit.simu.integration.tests {
	requires transitive madkit.simu.integration.common;
	requires transitive madkit.base;

	exports madkit.simu.integration;
	exports madkit.simu.integration.viewers;
	exports madkit.simu.integration.environment;
	exports madkit.simu.integration.scheduler;
	exports madkit.simu.integration.model;

	opens madkit.simu.integration to madkit.base;
	opens madkit.simu.integration.viewers to madkit.base;
	opens madkit.simu.integration.environment to madkit.base;
	opens madkit.simu.integration.scheduler to madkit.base;
	opens madkit.simu.integration.model to madkit.base;
}
