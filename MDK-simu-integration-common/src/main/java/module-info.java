/**
 * Reusable simulation fixtures shared by headless and UI integration applications.
 */
module madkit.simu.integration.common {
	requires transitive madkit.base;

	exports madkit.simu.integration.common;
	opens madkit.simu.integration.common to madkit.base;
}
