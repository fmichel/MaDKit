/**
 * A small named-module simulation consumer used for startup integration tests.
 */
module madkit.simu.integration.tests {
	requires transitive madkit.simu.integration.common;
	requires transitive madkit.base;

	exports madkit.simu.integration;
	opens madkit.simu.integration to madkit.base;
}
