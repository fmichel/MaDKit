package madkit.gui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import org.junit.Test;

import madkit.kernel.Scheduler;


public class DateBasedSchedulerGUITest extends Scheduler {

    public DateBasedSchedulerGUITest() {
	super(LocalDateTime.of(1, 1, 1, 0, 0));
    }
    
    @Override
    @Test
    public void doSimulationStep() {
	getSimulationTime().setCurrentDate(getSimulationTime().getCurrentDate().plusMinutes(4));
	getSimulationTime().incrementCurrentDate(10, ChronoUnit.MINUTES);
	assertEquals(LocalDateTime.of(1, 1, 1,0,14),getSimulationTime().getCurrentDate());
	getSimulationTime().setEndDate(LocalDateTime.of(10, 1, 1, 0, 0));
	assertFalse(getSimulationTime().hasReachedEndTime());
    }
    
    

    public static void main(String[] args) {
	executeThisAgent();
    }

}
