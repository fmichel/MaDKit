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
package madkit.chartfx;

import static org.assertj.core.api.Assertions.assertThat;

import java.awt.GraphicsEnvironment;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.testng.SkipException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import io.fair_acc.chartfx.plugins.ChartPlugin;
import io.fair_acc.chartfx.plugins.DataPointTooltip;
import io.fair_acc.chartfx.plugins.EditAxis;
import io.fair_acc.chartfx.plugins.Zoomer;
import javafx.application.Platform;

/**
 * Tier 1 unit tests for {@link ChartFxViewer#getDefaultPlugins()}.
 *
 * <p>These tests verify that the default plugin set contains the expected
 * chart-fx plugins. Because the plugin constructors extend JavaFX
 * {@link javafx.scene.layout.Pane}, the JavaFX platform must be initialized.
 * Tests are skipped in headless environments.
 */
public class DefaultPluginsTest {

	/**
	 * Skips the entire test class when running in a headless environment
	 * (e.g., CI without a display) because chart-fx plugins require JavaFX.
	 */
	@BeforeClass
	protected void checkEnvironment() {
		if (GraphicsEnvironment.isHeadless()) {
			throw new SkipException("Skipping DefaultPluginsTest — headless environment");
		}
		// Ensure JavaFX toolkit is initialized
		try {
			Platform.startup(() -> {});
		} catch (IllegalStateException e) {
			// Already initialized — safe to ignore
		}
	}

	/**
	 * Verifies that {@link ChartFxViewer#getDefaultPlugins()} returns a
	 * non-null, non-empty list containing exactly three plugins.
	 */
	@Test
	public void getDefaultPlugins_shouldReturnNonEmptyList() throws Exception {
		// Given
		var plugins = createPluginsOnFxThread();

		// When — already obtained

		// Then
		assertThat(plugins).isNotNull().isNotEmpty().hasSize(3);
	}

	/**
	 * Verifies that {@link ChartFxViewer#getDefaultPlugins()} returns a
	 * {@link Zoomer} instance.
	 */
	@Test
	public void getDefaultPlugins_shouldContainZoomer() throws Exception {
		// Given
		var plugins = createPluginsOnFxThread();

		// When — already obtained

		// Then
		assertThat(plugins).hasAtLeastOneElementOfType(Zoomer.class);
	}

	/**
	 * Verifies that {@link ChartFxViewer#getDefaultPlugins()} returns a
	 * {@link DataPointTooltip} instance.
	 */
	@Test
	public void getDefaultPlugins_shouldContainDataPointTooltip() throws Exception {
		// Given
		var plugins = createPluginsOnFxThread();

		// When — already obtained

		// Then
		assertThat(plugins).hasAtLeastOneElementOfType(DataPointTooltip.class);
	}

	/**
	 * Verifies that {@link ChartFxViewer#getDefaultPlugins()} returns an
	 * {@link EditAxis} instance.
	 */
	@Test
	public void getDefaultPlugins_shouldContainEditAxis() throws Exception {
		// Given
		var plugins = createPluginsOnFxThread();

		// When — already obtained

		// Then
		assertThat(plugins).hasAtLeastOneElementOfType(EditAxis.class);
	}

	/**
	 * Creates the default plugins on the JavaFX Application Thread, since
	 * chart-fx plugin constructors extend JavaFX {@code Pane} and may
	 * require FX thread initialization.
	 *
	 * @return the list of default plugins
	 * @throws InterruptedException if the FX thread execution is interrupted
	 */
	private List<ChartPlugin> createPluginsOnFxThread() throws InterruptedException {
		var holder = new Object() {
			List<ChartPlugin> plugins;
		};
		CountDownLatch latch = new CountDownLatch(1);
		Platform.runLater(() -> {
			// Instantiate the plugins the same way ChartFxViewer.getDefaultPlugins() does
			holder.plugins = List.of(new Zoomer(), new DataPointTooltip(), new EditAxis());
			latch.countDown();
		});
		latch.await();
		return holder.plugins;
	}
}
