package madkit.simulation.scheduler;

import java.time.LocalDateTime;
import java.util.PriorityQueue;

import madkit.kernel.AbstractAgent;
import madkit.kernel.Activator;
import madkit.kernel.Scheduler;
import madkit.simulation.activator.DateBasedDiscreteEventActivator;


public class DateBasedDiscreteEventScheduler extends Scheduler {
    
    private PriorityQueue<DateBasedDiscreteEventActivator<? extends AbstractAgent>> activationList = new PriorityQueue<>();

    public DateBasedDiscreteEventScheduler() {
    }

    public DateBasedDiscreteEventScheduler(LocalDateTime initialDate) {
	super(initialDate);
    }
    
    @Override
    public void doSimulationStep() {
	logActivationStep();
	DateBasedDiscreteEventActivator<? extends AbstractAgent> nextActivator = getActivationList().poll();
	getSimulationTime().setCurrentDate(nextActivator.getNextActivationDate());
	nextActivator.execute();
	getActivationList().add(nextActivator);
    }
    
    @Override
    public void addActivator(Activator<? extends AbstractAgent> activator) {
        super.addActivator(activator);
        if (activator instanceof DateBasedDiscreteEventActivator<?>) {
	    getActivationList().add((DateBasedDiscreteEventActivator<? extends AbstractAgent>) activator);
	}
    }
    
    @Override
    public void logActivationStep() {
        super.logActivationStep();
	getLogger().finest(() -> "Activation list -> " + getActivationList());
    }

    public PriorityQueue<DateBasedDiscreteEventActivator<? extends AbstractAgent>> getActivationList() {
	return activationList;
    }

}
