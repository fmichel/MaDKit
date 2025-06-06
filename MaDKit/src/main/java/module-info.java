/**
 * 
 * The madkit.base module provides the core functionalities of the MaDKit
 * platform. MaDKit (Multi-Agent Development Kit) is a lightweight framework for
 * building multi-agent systems. It allows developers to create, manage, and
 * simulate agents within an artificial organization.
 *
 * Features provided by MaDKit include:
 * <ul>
 * 
 * <li>Agent Management: Create, launch, and manage the lifecycle of
 * agents.</li>
 * <li>Communication: Facilitate communication between agents using messages and
 * roles.</li>
 * <li>Simulation: Support for simulating agent behaviors and interactions.</li>
 * <li>Logging: Integrated logging system for monitoring and debugging.</li>
 * <li>Configuration: Flexible configuration options for customizing the
 * platform.</li>
 * <li>Extensibility: Easily extendable to add new functionalities and integrate
 * with other systems.</li>
 * </ul>
 *
 * @version 1.0
 * @since 6.0
 */
module madkit.base {
	exports madkit.action;
	exports madkit.agr;
	exports madkit.kernel;
	exports madkit.gui;
	exports madkit.logging;
	exports madkit.messages;
	exports madkit.reflection;
	exports madkit.simulation;
	exports madkit.simulation.environment;
	exports madkit.simulation.scheduler;
	exports madkit.simulation.viewer;
	exports madkit.random;

	requires transitive java.desktop;
	requires transitive java.logging;
	requires transitive javafx.base;
	requires transitive javafx.controls;
	requires transitive javafx.graphics;
	requires transitive org.controlsfx.controls;
	requires transitive org.apache.commons.configuration2;

	requires info.picocli;
	requires java.base;
	requires java.sql;
	requires java.xml;

	requires static java.management;// jconsole
	requires static it.unimi.dsi.fastutil;
	requires static net.jodah.typetools;

	opens madkit.kernel;
	opens madkit.simulation;
}