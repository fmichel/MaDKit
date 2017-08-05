package com.distrimind.madkit.kernel.network;

public class DistantKernelAddressValidated implements SystemMessage {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2752671312631816393L;

	@Override
	public Integrity checkDataIntegrity() {
		return Integrity.OK;
	}

}
