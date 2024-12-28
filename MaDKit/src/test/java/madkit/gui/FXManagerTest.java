package madkit.gui;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

import javafx.application.Platform;

public class FXManagerTest {

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