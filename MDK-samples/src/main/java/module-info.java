/**
 * MaDKit sample agents demonstrating various features of the MaDKit framework. Each
 * package illustrates a specific topic (lifecycle, messaging, GUI, etc.).
 */
open module madkit.samples {
	requires madkit.base;

	exports madkit.samples;
	exports madkit.sample.launching;
	exports madkit.sample.agent.basic;
	exports madkit.sample.agent.threaded;
	exports madkit.sample.agent.lifecycle;
	exports madkit.sample.agent.management;
	exports madkit.sample.organization;
	exports madkit.sample.organization.secured;
	exports madkit.sample.messaging;
	exports madkit.sample.messaging.enumdispatch;
	exports madkit.sample.randomization;
	exports madkit.sample.logging;
	exports madkit.sample.agent.daemon;
	exports madkit.sample.gui.basic;
	exports madkit.sample.gui.custom;
	exports madkit.sample.gui.properties;
	exports madkit.sample.chartfx;
}
