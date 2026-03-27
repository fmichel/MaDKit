/*******************************************************************************
 * MaDKit - Multi-agent systems Development Kit 
 * 
 * Copyright (c) 1998-2025 Fabien Michel, Olivier Gutknecht, Jacques Ferber...
 * 
 * This software is a computer program whose purpose is to
 * provide a lightweight Java API for developing and simulating 
 * Multi-Agent Systems (MAS) using an organizational perspective.
 *
 * This software is governed by the CeCILL-C license under French law and
 * abiding by the rules of distribution of free software.You can use,
 * modify and/ or redistribute the software under the terms of the CeCILL-C
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty and the software's author, the holder of the
 * economic rights, and the successive licensors have only limited
 * liability.
 *
 * In this respect, the user's attention is drawn to the risks associated
 * with loading, using, modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean that it is complicated to manipulate, and that also
 * therefore means that it is reserved for developers and experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or
 * data to be ensured and, more generally, to use and operate it in the
 * same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-C license and that you accept its terms.
 *******************************************************************************/
package madkit.sample.chartfx;

import madkit.chartfx.XYChartViewer;
import madkit.simulation.PropertyProbe;

/**
 * A chart-fx viewer that plots average agent energy metrics over simulation
 * time, demonstrating multi-series live plotting with
 * {@link XYChartViewer}.
 *
 * <p>This viewer uses {@link PropertyProbe} instances to read the
 * {@code energy} field from {@link MetricAgent} instances. It computes
 * the average energy for each role ("producer" and "consumer") at every
 * simulation step and plots them as separate series on the same chart.
 *
 * <p><b>Thread safety:</b> All data updates go through the non-blocking
 * {@link XYChartViewer#addData(Object, double, double)} path. No
 * {@link madkit.gui.FXExecutor#runAndWait(Runnable)} call is involved.
 *
 * @see MetricAgent
 * @see MetricChartLauncher
 * @see XYChartViewer
 */
public class MetricChartViewer extends XYChartViewer<String> {

	private PropertyProbe<Double> producerProbe;
	private PropertyProbe<Double> consumerProbe;

	/**
	 * Activates this viewer and registers property probes for the
	 * "producer" and "consumer" roles.
	 *
	 * <p>Two datasets are created for the chart: one for the average
	 * producer energy and one for the average consumer energy. Two
	 * {@link PropertyProbe} instances are registered to read the
	 * {@code energy} field from agents playing each role.
	 */
	@Override
	protected void onActivation() {
		super.onActivation();
		addDataSet(MetricAgent.PRODUCER, "Avg Producer Energy");
		addDataSet(MetricAgent.CONSUMER, "Avg Consumer Energy");

		producerProbe = new PropertyProbe<>(getModelGroup(), MetricAgent.PRODUCER, "energy");
		addProbe(producerProbe);

		consumerProbe = new PropertyProbe<>(getModelGroup(), MetricAgent.CONSUMER, "energy");
		addProbe(consumerProbe);
	}

	/**
	 * Updates the chart with the current average energy for each role.
	 *
	 * <p>At each simulation step, computes the average {@code energy}
	 * value across all agents in each role using
	 * {@link PropertyProbe#getAverage()} and appends the data points
	 * to the chart using the non-blocking
	 * {@link XYChartViewer#addData(Object, double, double)} method.
	 */
	@Override
	public void display() {
		double time = ((Number) getSimuTimer().getCurrentTime()).doubleValue();
		if (producerProbe.size() > 0) {
			addData(MetricAgent.PRODUCER, time, producerProbe.getAverage());
		}
		if (consumerProbe.size() > 0) {
			addData(MetricAgent.CONSUMER, time, consumerProbe.getAverage());
		}
		super.display();
	}

	/**
	 * Returns the chart title.
	 *
	 * @return {@code "Agent Energy Metrics (chart-fx)"}
	 */
	@Override
	protected String getChartTitle() {
		return "Agent Energy Metrics (chart-fx)";
	}

	/**
	 * Returns the Y-axis label.
	 *
	 * @return {@code "Average Energy"}
	 */
	@Override
	protected String getYAxisLabel() {
		return "Average Energy";
	}
}
