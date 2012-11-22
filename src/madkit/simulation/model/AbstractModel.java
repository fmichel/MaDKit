package madkit.simulation.model;

import java.lang.reflect.Field;

import javax.swing.BoundedRangeModel;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import madkit.gui.SwingUtil;
import madkit.kernel.AbstractAgent;

/**
 * Prototype class that will be used to encapsulate simulation models
 * in a near future.
 * 
 * @author Fabien Michel
 * @since MadKit 5.0.0.19
 * @version 0.9
 * 
 */
public class AbstractModel extends AbstractAgent {

	/**
	 * 
	 */
	private static final long	serialVersionUID	= 4331001068139223326L;

	/**
	 * Returns a panel containing sliders operating on all the
	 * {@link BoundedRangeModel} fields which are found in this model.
	 * 
	 * @param label the name of the panel
	 * @return a panel containing sliders
	 */
	public JPanel getParametersView(String label) {
		return getParametersView(label, this);
	}

	/**
	 * Returns a panel containing sliders operating on all the
	 * {@link BoundedRangeModel} fields which are found in this model.
	 * 
	 * @param label the name of the panel
	 * @return a panel containing sliders
	 */
	public JPanel getParametersView(String label, AbstractAgent onInstance) {
		final JPanel all = new JPanel();
		all.setBorder(new TitledBorder(label));
		Class<? extends AbstractAgent> agentClass = onInstance.getClass();
		while (true) {
			for (Field f : agentClass.getDeclaredFields()) {
				if (BoundedRangeModel.class.isAssignableFrom(f.getType())) {
					f.setAccessible(true);
					try {
						all.add(SwingUtil.createSliderPanel(
								(BoundedRangeModel) f.get(onInstance), f.getName()));
					} catch (IllegalArgumentException e) {
						e.printStackTrace();
					} catch (IllegalAccessException e) {
						e.printStackTrace();
					}
				}
			}
			agentClass = (Class<? extends AbstractAgent>) agentClass.getSuperclass();
			if(agentClass == AbstractAgent.class)
				break;
		}
		return all;
	}

}
