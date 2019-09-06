package madkit.gui;

import java.time.LocalDateTime;
import java.util.List;

import madkit.kernel.AbstractAgent;
import madkit.simulation.activator.DateBasedDiscreteEventActivator;
import madkit.simulation.scheduler.DateBasedDiscreteEventScheduler;


public class DateBasedDiscreteEventSchedulerGUITest extends DateBasedDiscreteEventScheduler {

    DateBasedDiscreteEventActivator<AbstractAgent> activator1;
    DateBasedDiscreteEventActivator<AbstractAgent> activator2;
    
    public DateBasedDiscreteEventSchedulerGUITest() {
	super(LocalDateTime.of(1, 1, 1, 0, 0));
	getSimulationTime().setEndDate(LocalDateTime.of(1, 1, 1, 0, 0).plusDays(500));
	activator1 = new DateBasedDiscreteEventActivator<>("test", "test", "test", "test");
	activator1.setNextActivationDate(getSimulationTime().getCurrentDate());
	activator2 = new SpecificActivator("test", "test", "test", "test");
	activator2.setNextActivationDate(getSimulationTime().getCurrentDate());
//	activator2.setPriority(10);
    }
    
    @Override
    protected void activate() {
        super.activate();
        addActivator(activator1);
        System.err.println(getActivationList());
        addActivator(activator2);
        System.err.println(getActivationList());
   }
    
    public static void main(String[] args) {
	executeThisAgent();
    }

}

class SpecificActivator extends DateBasedDiscreteEventActivator<AbstractAgent>{
    
    public SpecificActivator(String community, String group, String role, String theBehaviorToActivate) {
	super(community, group, role, theBehaviorToActivate);
    }
    
    @Override
    public void execute(List<AbstractAgent> agents, Object... args) {
	super.execute(agents, args);
	setNextActivationDate(getSimulationTime().getCurrentDate().plusHours(9));
    }
}