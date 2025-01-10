package madkit.gui;

import static org.testng.Assert.assertTrue;

import java.awt.GraphicsEnvironment;

import org.testng.SkipException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javafx.application.Platform;
import madkit.kernel.JunitMadkit;

public class FXManagerTest extends JunitMadkit {

	
	@BeforeMethod
	protected void checkEnvironment() {
	  if (GraphicsEnvironment.isHeadless()) {
	    throw new SkipException("Skipping tests because the environment is headless");
	  }
	}

	@AfterClass
	public void tearDown() {
		Platform.exit();
	}

	@Test
	public void testStartFX() {
		assertTrue(FXExecutor.isStarted());
	}

}