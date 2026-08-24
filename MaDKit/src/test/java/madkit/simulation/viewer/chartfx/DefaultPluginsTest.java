package madkit.simulation.viewer.chartfx;

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

/** Verifies the standard chart-fx interactive plugin set. */
public class DefaultPluginsTest {
	@BeforeClass
	protected void checkEnvironment() {
		if (GraphicsEnvironment.isHeadless()) throw new SkipException("JavaFX requires a display");
		try { Platform.startup(() -> { }); } catch (IllegalStateException ignored) { }
	}

	@Test
	public void getDefaultPlugins_shouldContainStandardPlugins() throws InterruptedException {
		// Given
		List<ChartPlugin> plugins = createPluginsOnFxThread();
		// When
		List<Class<?>> pluginTypes = new java.util.ArrayList<>();
		plugins.forEach(plugin -> pluginTypes.add(plugin.getClass()));
		// Then
		assertThat(pluginTypes).contains(Zoomer.class, DataPointTooltip.class, EditAxis.class);
	}

	private List<ChartPlugin> createPluginsOnFxThread() throws InterruptedException {
		var holder = new Object() { List<ChartPlugin> plugins; };
		CountDownLatch complete = new CountDownLatch(1);
		Platform.runLater(() -> { holder.plugins = List.of(new Zoomer(), new DataPointTooltip(), new EditAxis()); complete.countDown(); });
		complete.await();
		return holder.plugins;
	}
}
