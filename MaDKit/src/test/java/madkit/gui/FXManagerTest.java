package madkit.gui;

import static org.testng.Assert.assertTrue;

import java.awt.GraphicsEnvironment;

import org.testng.annotations.*;
import org.testng.*;
import org.testng.annotations.Test;

import javafx.application.Platform;

public class FXManagerTest {

	
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
		// When the FXManager is started
		FXManager.startFX();

		// Then the FXManager should be started
		assertTrue(FXManager.isStarted());
	}

}