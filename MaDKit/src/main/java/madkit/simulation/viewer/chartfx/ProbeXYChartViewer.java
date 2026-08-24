package madkit.simulation.viewer.chartfx;

import madkit.kernel.Probe;

/** XY viewer that plots organizational role populations through probes. */
public abstract class ProbeXYChartViewer extends XYChartViewer<Probe> {
	/** Registers a probe and its role-named dataset. */
	protected Probe addProbeDataSet(String group, String role) {
		Probe probe = new Probe(getCommunity(), group, role);
		addProbe(probe);
		addDataSet(probe, role);
		return probe;
	}

	/** Appends current population values before the standard viewer display. */
	@Override
	public void display() {
		double time = ((Number) getSimuTimer().getCurrentTime()).doubleValue();
		getProbes().forEach(probe -> addData(probe, time, probe.size()));
		super.display();
	}

	/** Returns the population axis label. */
	@Override
	protected String getYAxisLabel() {
		return "Population";
	}
}
