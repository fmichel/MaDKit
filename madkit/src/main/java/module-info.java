module madkit.base {
	exports madkit.action;
	exports madkit.agr;
	exports madkit.gui;
	exports madkit.gui.fx;
	exports madkit.kernel;
	exports madkit.logging;
	exports madkit.messages;
	exports madkit.reflection;
	exports madkit.simulation;
	exports madkit.simulation.activator;
	exports madkit.simulation.environment;
	exports madkit.simulation.probe;
	exports madkit.simulation.scheduler;
	exports madkit.simulation.viewer;

	requires transitive java.logging;
	requires transitive javafx.base;
	requires transitive javafx.controls;
	requires transitive javafx.graphics;
	requires transitive org.controlsfx.controls;
	requires transitive org.apache.commons.configuration2;

	requires java.base;
	requires java.sql;
	requires java.desktop;
	requires java.xml;
	requires info.picocli;

	requires static java.management;// jconsole
	requires static it.unimi.dsi.fastutil;
	requires static net.jodah.typetools;

	opens madkit.kernel;
	opens madkit.simulation;
	
}