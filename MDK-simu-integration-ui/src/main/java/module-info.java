/**
 * JavaFX-enabled integration application built on the shared simulation fixtures.
 */
module madkit.simu.integration.ui {
	requires transitive madkit.simu.integration.common;
	requires javafx.graphics;

	exports madkit.simu.integration.ui;
	opens madkit.simu.integration.ui to madkit.base;
}
