package madkit.gui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.math.BigDecimal;
import java.math.BigInteger;

import org.junit.Test;

import madkit.kernel.Scheduler;


public class TickBasedSchedulerGUITest extends Scheduler {

    public TickBasedSchedulerGUITest() {
	super(30);
    }
    
    @Override
    @Test
    public void doSimulationStep() {
	getSimulationTime().setCurrentTick(BigDecimal.TEN);
	
	getSimulationTime().incrementCurrentTick(10);
	
	assertEquals(new BigDecimal(BigInteger.valueOf(20)).doubleValue(), getSimulationTime().getCurrentTick().doubleValue(),0);

	getSimulationTime().setEndTick(BigDecimal.valueOf(20));

	assertFalse(getSimulationTime().hasReachedEndTime());

    }
    
    public static void main(String[] args) {
	executeThisAgent();
    }

}
