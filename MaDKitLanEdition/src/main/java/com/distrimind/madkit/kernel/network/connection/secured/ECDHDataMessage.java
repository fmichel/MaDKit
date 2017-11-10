package com.distrimind.madkit.kernel.network.connection.secured;

import com.distrimind.madkit.kernel.network.connection.ConnectionMessage;

public class ECDHDataMessage extends ConnectionMessage {
	/**
	 * 
	 */
	private static final long serialVersionUID = 4560100871284266098L;
	
	private byte[] dataForEncryption;
	private byte[] dataForSignature;
	private byte[] materialKey;


	public ECDHDataMessage(byte[] dataForEncryption, byte[] dataForSignature, byte[] materialKey) {
		super();
		this.dataForEncryption = dataForEncryption;
		this.dataForSignature = dataForSignature;
		this.materialKey=materialKey;
	}


	public byte[] getDataForEncryption() {
		return dataForEncryption;
	}


	public byte[] getDataForSignature() {
		return dataForSignature;
	}

	public byte[] getMaterialKey()
	{
		return materialKey;
	}

	@Override
	public Integrity checkDataIntegrity() {
		if (dataForEncryption==null)
			return Integrity.FAIL_AND_CANDIDATE_TO_BAN;
		if (dataForSignature==null)
			return Integrity.FAIL_AND_CANDIDATE_TO_BAN;
		return Integrity.OK;
	}
	
}
