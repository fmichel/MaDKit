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

import static madkit.kernel.AbstractAgent.State.INITIALIZING;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.LogManager;

import madkit.kernel.Madkit.Roles;

/**
 * @author Oliver Gutknecht
 * @author Fabien Michel since v.3
 * @version 5.0
 * @since MadKit 1.0
 *
 */
final class KernelAgent extends Agent {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 4909420700830858864L;
	final private MadkitKernel operatingKernel;
	final static Map<String, Class<?>> primitiveTypes = new HashMap<String, Class<?>>();
	static{
		primitiveTypes.put("java.lang.Integer",int.class);
		primitiveTypes.put("java.lang.Boolean",boolean.class);
		primitiveTypes.put("java.lang.Byte",byte.class);
		primitiveTypes.put("java.lang.Character",char.class);
		primitiveTypes.put("java.lang.Float",float.class);
		primitiveTypes.put("java.lang.Void",void.class);
		primitiveTypes.put("java.lang.Short",short.class);
		primitiveTypes.put("java.lang.Double",double.class);
		primitiveTypes.put("java.lang.Long",long.class);
	}

	/**
	 * 
	 */
	KernelAgent(MadkitKernel k) {
		super(Executors.newSingleThreadScheduledExecutor(daemonAgentThreadFactory));
		setKernel(k);
		operatingKernel = k;
		k.setKernelAgent(this);
		setName(getClass().getSimpleName()+k.getKernelAddress().hashCode());
		state.set(INITIALIZING);
		getAlive().set(true);
		setLogLevel(Level.INFO,Level.INFO);
	}
	
	void start(){
		try {
			operatingKernel.startAgentLifeCycle(this,getAgentExecutor()).get();
		} catch (InterruptedException e) {
			Utils.logSevereException(logger, e, "*********please bug report !!");
		} catch (ExecutionException e) {
			Utils.logSevereException(logger, e, "*********please bug report !!");
		}
	}
	
	protected void activate() {
		createGroup(Roles.LOCAL_COMMUNITY, Roles.SYSTEM_GROUP, true);
		requestRole(Roles.LOCAL_COMMUNITY, Roles.SYSTEM_GROUP, Roles.KERNEL_ROLE, null);
		launchBooterAgent();
		launchNetworkAgent();
//		logCurrentOrganization(logger,Level.FINEST);
	}

	/**
	 * @see madkit.kernel.Agent#live()
	 */
	@Override
	protected void live() {
		while (true) {
			Message m = null;
			KernelMessage msg = null;
			try{
				m = waitNextMessage();
				msg = (KernelMessage) m;
			}
			catch (ClassCastException e) {
				if(logger != null)
					logger.warning("I received a message that I do not understang. Discarding "+m);
				continue;
			}
			Method operation = null;
			final Object[] arguments = msg.getContent();
			switch (msg.getCode()) {
				case LAUNCH_AGENT:
					operation = launchAgent(arguments);
					break;
				case SHUTDOWN_NOW:
					return;
				default:
					break;
				}
			doOperation(operation,arguments);
			}
		}
	/**
	 * @param operation
	 * @param arguments
	 */
	private void doOperation(Method operation, Object[] arguments) {
		try {//TODO log failures
			operation.invoke(this, arguments);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @param content
	 * @return 
	 */
	private Method launchAgent(Object[] content) {
		return checkValidity("launchAgent", content);
	}
	
	private Method checkValidity(String method, Object[] content){
		Class<?>[] parameters = new Class<?>[content.length];
		for(int i = 0;i < content.length;i++){
			parameters[i] = content[i].getClass();
			final Class<?> primitive = primitiveTypes.get(parameters[i].getName());
			if(primitive != null)
				parameters[i] = primitive;
		}
		try {//TODO log failures
			return getClass().getMethod(method, parameters);
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * @see madkit.kernel.AbstractAgent#end()
	 */
	@Override
	synchronized protected void end() {//TODO this should be the last call
		super.end();
//		shutdownNow();
		if(logger != null)
			logger.info("Madkit is ending");
		LogManager.getLogManager().reset();
	}

	
	
	private void launchBooterAgent(){ //TODO put a no booter option
		AbstractAgent booter=null;
		if(! getMadkitProperty(Madkit.booterAgentKey).toLowerCase().equals("null"))
			booter=launchPlatformAgent(Madkit.booterAgentKey, "Boot Agent");
		else if(logger != null){
			logger.fine("** Booter agent off: --booterAgent property is null**\n");
			return;
		}
//		if(booter != null && booter instanceof MadKitGUIsManager){
//			if(logger != null)
//				logger.fine("** Setting "+booter.getName()+" as AgentsGUIManager **\n");
//			operatingKernel.setGuiManager((MadKitGUIsManager) booter);
//		}
	}

	private AbstractAgent launchPlatformAgent(String mkProperty,String userMessage){
		final String agentClassName=getMadkitProperty(mkProperty);
		if(logger != null){
			logger.fine("** Launching "+userMessage+": "+agentClassName+" **");
		}
		AbstractAgent targetAgent = launchAgent(agentClassName);
		if(targetAgent == null){
			if(logger != null){
				logger.warning("Problem building "+userMessage+" "+agentClassName+" -> Using MK default "+userMessage+" : "+Madkit.defaultConfig.get(mkProperty));
			}
			targetAgent = launchAgent(Madkit.defaultConfig.getProperty(mkProperty));
		}
		return targetAgent;
	}

	private void launchNetworkAgent() {
		if(! Boolean.parseBoolean(getMadkitProperty(Madkit.network))){
			if(logger != null){
				logger.fine("** Networking is off: No Net Agent **\n");
			}
		}
		else{
			final AbstractAgent a = launchPlatformAgent("networkAgent",  "Net Agent");
			if(a != null){
				operatingKernel.setNetAgent(a);
			}
		}
	}

}
