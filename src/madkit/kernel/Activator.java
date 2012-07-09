/*
 * Copyright 1997-2011 Fabien Michel, Olivier Gutknecht, Jacques Ferber
 * 
 * This file is part of MadKit.
 * 
 * MadKit is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * MadKit is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with MadKit. If not, see <http://www.gnu.org/licenses/>.
 */
package madkit.kernel;

import java.lang.reflect.Method;

/**
 * This class defines a tool for scheduling mechanism.
 * An activator is configured according to a community, a group and a role.
 * It could be used to activate a group of agents on a particular behavior (a method of the agent's class)
 * Subclasses should override {@link #execute()} for defining how 
 * a sequential execution of the agents take place or {@link #multicoreExecute()} for 
 * defining a concurrent execution process of group of agents. By default these methods
 * do nothing. To set the mode that will be used by the scheduler, 
 * to use multicore execution on activators having internal concurrent mechanism.
 * The multicore is set to <code>false</code> by default.
 * 
 * @author Fabien Michel
 * @author Olivier Gutknecht 
 * @since MadKit 2.0
 * @see Scheduler
 * @see GenericBehaviorActivator 
 * @version 5.0
 * 
 */
public class Activator<A extends AbstractAgent> extends Overlooker<A>{

	private static final String	NOT_IMPLEMENTED	= "Activator not implemented";
	private int nbOfsimultaneousTasks = 1;
	/**
	 * Builds a new Activator on the given CGR location of the
	 * artificial society with multicore mode set to <code>false</code>.
	 * This has the same effect as 
	 * <code>Activator(community, group, role, false)</code>.
	 * @param community
	 * @param group
	 * @param role
	 * @see Scheduler
	 */
	public Activator(String community, String group, String role) {
		super(community, group, role);
	}

	//	/**
	//	 * Builds a new Activator on the given CGR location of the
	//	 * artificial society. Once created, it has to be added by a {@link Scheduler} 
	//	 * agent using the {@link Scheduler#addActivator(Activator)} method.
	//	 * @param community
	//	 * @param group
	//	 * @param role
	//	 * @param multicore if <code>true</code> {@link #multicoreExecute()} is used 
	//	 * when the activator is triggered by a scheduler agent 
	//	 * that uses {@link Scheduler#triggerActivator(Activator)}
	//	 * @see Scheduler
	//	 */
	//	public Activator(String community, String group, String role, boolean multicore) {
	//		super(community, group, role);
	//		this.multicore = multicore;
	//	}

	/**
	 * Subclasses should override this to define how 
	 * the agents which are at the CGR location are executed.
	 * 
	 * By default, this is automatically called by the default scheduler's 
	 * loop when this activator has been added.
	 * 
	 * @throws UnsupportedOperationException if this operation is not supported 
	 * by the activator, i.e. not implemented.
	 * 
	 * @see Scheduler#doSimulationStep()
	 */
	public void execute() {
		throw new UnsupportedOperationException(NOT_IMPLEMENTED+toString());
	}

	/**
	 * Executes the behavior on all the agents in a concurrent way, using several processor cores if available.
	 * This call decomposes the execution of the activator in {@link #nbOfSimultaneousTasks()} tasks so that
	 * there are independently performed by the available core of the host.
	 * <p>
	 * Default implementation 
	 * Beware that using this call will produce different outputs for each run unless a concurrent simulation
	 * model is used. That is to say, a model supporting concurrent phases in the simulation execution such as the
	 * <a href="http://www.aamas-conference.org/Proceedings/aamas07/html/pdf/AAMAS07_0179_07a7765250ef7c3551a9eb0f13b75a58.pdf">IRM4S model<a/>
	 * 
	 * @throws UnsupportedOperationException if this operation is not supported 
	 * by the activator, i.e. not implemented.
	 */
	public void multicoreExecute() {
		throw new UnsupportedOperationException(NOT_IMPLEMENTED+toString());
	}

	@Override
	public String toString() {
		return super.toString()+" multicore mode "+isMulticoreModeOn();
	}

	/**
	 * @return <code>true</code> if the multi core mode is on. I.e. 
	 * {@link #nbOfSimultaneousTasks()} > 1.
	 * This method could be used by the default behavior of scheduler agents as 
	 * they test in which mode each activator has to be used.
	 */
	public boolean isMulticoreModeOn() {
		return nbOfsimultaneousTasks > 1;
	}

	/**
	 * Sets the number of core which will be used. If set to a number greater
	 * than 1, the scheduler will automatically call {@link #multicoreExecute()}
	 * on this activator.
	 * @param nbOfsimultaneousTasks the number of simultaneous tasks
	 * that this activator will use to make a step. Default is 1 upon
	 * creation, so that 
	 * {@link #isMulticoreModeOn()} returns <code>false</code>.
	 */
	public void setMulticore(int nbOfsimultaneousTasks) {
		if (nbOfsimultaneousTasks < 2) {
			this.nbOfsimultaneousTasks = 1;
		}
		else{
			this.nbOfsimultaneousTasks = nbOfsimultaneousTasks;
		}
	}

	/**
	 * Returns the number tasks that will
	 * be created by this activator when {@link #multicoreExecute()}
	 * is used.
	 * @return the number of tasks that will be created.
	 */
	public int nbOfSimultaneousTasks() {
		return nbOfsimultaneousTasks;
	}

	/**
	 * Returns the agent's method named <code>methodName</code>
	 * considering a given agentClass. This also works 
	 * for the private methods of the class, even inherited ones.
	 * 
	 * 
	 * @param agentClass the targeted agent 
	 * @param methodName the name of the method
	 * @return the agent's method named <code>methodName</code>
	 * @throws NoSuchMethodException 
	 */
	//	* This also works on <code>private</code> field.
	public Method findMethodOn(Class<? extends A> agentClass, final String methodName) throws NoSuchMethodException {
		Method m;
		while(true) {
			try {
				m = agentClass.getDeclaredMethod(methodName);
				if(m != null){
					if (! m.isAccessible()) {//TODO seems to be always the case the first time
						m.setAccessible(true);
					}
					return m;
				}
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				agentClass = (Class<? extends A>) agentClass.getSuperclass();//TODO not go further than A
				if (agentClass == AbstractAgent.class) {//TODO bench vs local variable
					throw e;
				}
			}
		} 
	}
}