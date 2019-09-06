package madkit.gui;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import madkit.kernel.Scheduler;


public class SchedulerGUITest extends Scheduler {

    public SchedulerGUITest() {
	super(LocalDateTime.of(1, 1, 1, 0, 0));
    }
    
    @Override
    public void doSimulationStep() {
	getSimulationTime().setCurrentDate(getSimulationTime().getCurrentDate().plusMinutes(4));
	getSimulationTime().incrementCurrentDate(10, ChronoUnit.MINUTES);
    }
    
    

    public static void main(String[] args) {
	executeThisAgent();
    }

}
