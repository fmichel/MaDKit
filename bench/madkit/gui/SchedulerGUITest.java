package madkit.gui;

import java.time.LocalDateTime;

import madkit.kernel.Scheduler;


public class SchedulerGUITest extends Scheduler {

    public SchedulerGUITest() {
	super(LocalDateTime.of(1, 1, 1, 0, 0));
	getSimulationTime().setSimulationEnd(LocalDateTime.of(1, 1, 1, 0, 0).plusDays(500));
    }
    
    @Override
    public void doSimulationStep() {
	getSimulationTime().setActualDate(getSimulationTime().getActualDate().plusMinutes(4));
    }
    
    

    public static void main(String[] args) {
	executeThisAgent();
    }

}
