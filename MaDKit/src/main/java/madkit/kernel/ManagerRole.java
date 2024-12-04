package madkit.kernel;

import static madkit.kernel.Agent.ReturnCode.ACCESS_DENIED;
import static madkit.kernel.Agent.ReturnCode.ROLE_ALREADY_HANDLED;

import java.util.HashSet;

import madkit.agr.DefaultMaDKitRoles;
import madkit.kernel.Agent.ReturnCode;

/**
 * @author Fabien Michel
 * @version 0.91
 * @since MaDKit 5.0.0.2
 *
 */
final class ManagerRole extends Role {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1919401829672949296L;

	ManagerRole(final Group groupObject, Agent requester, boolean securedGroup) {
		super(groupObject, DefaultMaDKitRoles.GROUP_MANAGER_ROLE);
		synchronized (players) {
			players.add(requester);
			agentAddresses = new HashSet<>(1, 1);
			agentAddresses.add(new GroupManagerAddress(requester, this, getKernelAddress(), securedGroup));
			modified = true;
		}
	}

	ManagerRole(final Group groupObject, AgentAddress creator) {
		super(groupObject, DefaultMaDKitRoles.GROUP_MANAGER_ROLE);
		synchronized (players) {
			agentAddresses = new HashSet<>(1, 1);
			agentAddresses.add(creator);
			creator.setRoleObject(this);// required for equals to work
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see madkit.kernel.Role#addMember(madkit.kernel.Agent)
	 */
	@Override
	ReturnCode addMember(Agent requester) {// manager is never changed from outside
		if (players.contains(requester))
			return ROLE_ALREADY_HANDLED;
		return ACCESS_DENIED;
	}

//	/* (non-Javadoc)
//	 * @see madkit.kernel.Role#removeMember(madkit.kernel.Agent)
//	 */
//	@Override
//	ReturnCode removeMember(final Agent requester) {
//		if(super.removeMember(requester) == SUCCESS){
//			myGroup.chooseNewManager(requester);
//			return SUCCESS;
//		}
//		return ROLE_NOT_HANDLED;
//	}

//	@Override
//	void checkEmptyness() {
//		if(buildAndGetAddresses().isEmpty()){
//			myGroup.chooseNewManager(oldManager);
//		}
//		super.checkEmptyness();
//	}

}
