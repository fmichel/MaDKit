/*
 * MadKitLanEdition (created by Jason MAHDJOUB (jason.mahdjoub@distri-mind.fr)) Copyright (c)
 * 2015 is a fork of MadKit and MadKitGroupExtension. 
 * 
 * Copyright or © or Copr. Jason Mahdjoub, Fabien Michel, Olivier Gutknecht, Jacques Ferber (1997)
 * 
 * jason.mahdjoub@distri-mind.fr
 * fmichel@lirmm.fr
 * olg@no-distance.net
 * ferber@lirmm.fr
 * 
 * This software is a computer program whose purpose is to
 * provide a lightweight Java library for designing and simulating Multi-Agent Systems (MAS).
 * This software is governed by the CeCILL-C license under French law and
 * abiding by the rules of distribution of free software.  You can  use,
 * modify and/ or redistribute the software under the terms of the CeCILL-C
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 * As a counterpart to the access to the source code and  rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty  and the software's author,  the holder of the
 * economic rights,  and the successive licensors  have only  limited
 * liability.
 * 
 * In this respect, the user's attention is drawn to the risks associated
 * with loading,  using,  modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean  that it is complicated to manipulate,  and  that  also
 * therefore means  that it is reserved for developers  and  experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or
 * data to be ensured and,  more generally, to use and operate it in the
 * same conditions as regards security.
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-C license and that you accept its terms.
 */
package com.distrimind.madkit.bench.networking;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;

import org.junit.Test;
import org.testng.Assert;

import com.distrimind.madkit.io.RandomByteArrayInputStream;
import com.distrimind.madkit.io.RandomByteArrayOutputStream;
import com.distrimind.madkit.kernel.AbstractAgent;
import com.distrimind.madkit.kernel.AgentAddress;
import com.distrimind.madkit.kernel.BigDataResultMessage;
import com.distrimind.madkit.kernel.JunitMadkit;
import com.distrimind.madkit.kernel.MadkitEventListener;
import com.distrimind.madkit.kernel.MadkitProperties;
import com.distrimind.madkit.kernel.Message;
import com.distrimind.madkit.kernel.network.AbstractIP;
import com.distrimind.madkit.kernel.network.AccessDataMKEventListener;
import com.distrimind.madkit.kernel.network.AccessProtocolPropertiesMKEventListener;
import com.distrimind.madkit.kernel.network.ConnectionsProtocolsMKEventListener;
import com.distrimind.madkit.kernel.network.DoubleIP;
import com.distrimind.madkit.kernel.network.NetworkEventListener;
import com.distrimind.madkit.kernel.network.RealTimeTransfertStat;
import com.distrimind.madkit.kernel.network.TransferSpeedStat;
import com.distrimind.madkit.kernel.network.connection.access.AbstractAccessProtocolProperties;
import com.distrimind.madkit.kernel.network.connection.access.AccessProtocolWithJPakeProperties;
import com.distrimind.madkit.kernel.network.connection.secured.P2PSecuredConnectionProtocolWithECDHAlgorithmProperties;
import com.distrimind.madkit.testing.util.agent.BigDataTransferReceiverAgent;
import com.distrimind.madkit.testing.util.agent.NormalAgent;
import com.distrimind.util.Timer;
import com.distrimind.util.crypto.SecureRandomType;
import com.distrimind.util.crypto.SymmetricAuthentifiedSignatureCheckerAlgorithm;
import com.distrimind.util.crypto.SymmetricAuthentifiedSignatureType;
import com.distrimind.util.crypto.SymmetricAuthentifiedSignerAlgorithm;
import com.distrimind.util.crypto.SymmetricEncryptionAlgorithm;
import com.distrimind.util.crypto.SymmetricEncryptionType;
import com.distrimind.util.crypto.SymmetricSecretKey;

import gnu.vm.jgnu.security.InvalidAlgorithmParameterException;
import gnu.vm.jgnu.security.InvalidKeyException;
import gnu.vm.jgnu.security.NoSuchAlgorithmException;
import gnu.vm.jgnu.security.NoSuchProviderException;
import gnu.vm.jgnu.security.SignatureException;
import gnu.vm.jgnu.security.spec.InvalidKeySpecException;
import gnu.vm.jgnu.security.spec.InvalidParameterSpecException;
import gnu.vm.jgnux.crypto.BadPaddingException;
import gnu.vm.jgnux.crypto.IllegalBlockSizeException;
import gnu.vm.jgnux.crypto.NoSuchPaddingException;
import gnu.vm.jgnux.crypto.ShortBufferException;

/**
 * @author Jason Mahdjoub
 * @since MadkitLanEdition 1.5
 * @version 1.0
 * 
 */
public class BigDataTransferSpeed extends JunitMadkit {
	final MadkitEventListener eventListener1;
	final NetworkEventListener eventListener2;
	final BigDataTransferReceiverAgent bigDataTransferAgent;

	public BigDataTransferSpeed() throws UnknownHostException {

		this.eventListener1 = new MadkitEventListener() {

			@Override
			public void onMadkitPropertiesLoaded(MadkitProperties _properties) {
				AbstractAccessProtocolProperties app = new AccessProtocolWithJPakeProperties();

				try {
					new NetworkEventListener(true, false, false, null,
							new ConnectionsProtocolsMKEventListener(new P2PSecuredConnectionProtocolWithECDHAlgorithmProperties()),
							new AccessProtocolPropertiesMKEventListener(app),
							new AccessDataMKEventListener(AccessDataMKEventListener.getDefaultAccessData(GROUP)), 5000,
							null, InetAddress.getByName("0.0.0.0")).onMadkitPropertiesLoaded(_properties);
				} catch (Exception e) {
					e.printStackTrace();
				}
				_properties.networkProperties.networkLogLevel = Level.INFO;
				_properties.networkProperties.maxBufferSize=Short.MAX_VALUE;
			}
		};

		P2PSecuredConnectionProtocolWithECDHAlgorithmProperties u = new P2PSecuredConnectionProtocolWithECDHAlgorithmProperties();
		u.isServer = false;

		AbstractAccessProtocolProperties app = new AccessProtocolWithJPakeProperties();
		
		this.eventListener2 = new NetworkEventListener(true, false, false, null,
				new ConnectionsProtocolsMKEventListener(u), new AccessProtocolPropertiesMKEventListener(app),
				new AccessDataMKEventListener(AccessDataMKEventListener.getDefaultAccessData(GROUP)), 5000,
				Arrays.asList((AbstractIP) new DoubleIP(5000, (Inet4Address) InetAddress.getByName("127.0.0.1"),
						(Inet6Address) InetAddress.getByName("::1"))),
				InetAddress.getByName("0.0.0.0"));
		this.eventListener2.maxBufferSize=Short.MAX_VALUE;
		bigDataTransferAgent = new BigDataTransferReceiverAgent();
	}

	@Test
	public void bigDataTransfer() {
		final AtomicBoolean transfered=new AtomicBoolean(false);
		// addMadkitArgs("--kernelLogLevel",Level.INFO.toString(),"--networkLogLevel",Level.FINEST.toString());
		launchTest(new NormalAgent() {
			@Override
			protected void activate() throws InterruptedException {
				setLogLevel(Level.OFF);
				requestRole(GROUP, ROLE);
				launchThreadedMKNetworkInstance(Level.INFO, AbstractAgent.class, bigDataTransferAgent, eventListener2);
				sleep(2000);
				
				AgentAddress aa=getAgentsWithRole(GROUP, ROLE).iterator().next();
				
				try {
					this.sendBigData(aa, new RandomByteArrayInputStream(new byte[400000000]));
				} catch (IOException e) {
					e.printStackTrace();
				}
				Message m=this.waitNextMessage(60000);
				transfered.set(m!=null && m instanceof BigDataResultMessage && ((BigDataResultMessage)m).getType()==BigDataResultMessage.Type.BIG_DATA_TRANSFERED);
				Assert.assertTrue(transfered.get(), ""+m);
			}

			@Override
			protected void liveCycle() throws InterruptedException {
				this.killAgent(this);
			}
		}, eventListener1);
		Assert.assertTrue(transfered.get());
		cleanHelperMDKs();
	}
	
	@Test
	public void testEncryptionSpeed() throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException, NoSuchProviderException, InvalidKeySpecException, IllegalStateException, IllegalBlockSizeException, BadPaddingException, IOException, SignatureException, ShortBufferException, InvalidParameterSpecException
	{
		byte toEncrypt[]=new byte[1024*1024*400];
		int shift=32*1024;
		SymmetricEncryptionAlgorithm cipher=new SymmetricEncryptionAlgorithm(SecureRandomType.FORTUNA_WITH_BC_FIPS_APPROVED.getInstance(null), SymmetricEncryptionType.AES.getKeyGenerator(SecureRandomType.FORTUNA_WITH_BC_FIPS_APPROVED_FOR_KEYS.getInstance(null), (short)128).generateKey());
		SymmetricAuthentifiedSignatureType sigType=SymmetricAuthentifiedSignatureType.BC_FIPS_HMAC_SHA_512;
		SymmetricSecretKey sks=sigType.getKeyGenerator(SecureRandomType.FORTUNA_WITH_BC_FIPS_APPROVED_FOR_KEYS.getInstance(null), (short)128).generateKey();
		SymmetricAuthentifiedSignerAlgorithm signer=new SymmetricAuthentifiedSignerAlgorithm(sks);
		SymmetricAuthentifiedSignatureCheckerAlgorithm checker=new SymmetricAuthentifiedSignatureCheckerAlgorithm(sks);
		double nb=0;

		RealTimeTransfertStat rtSent=new RealTimeTransfertStat(30000l,3000l);
		RealTimeTransfertStat rtReceived=new RealTimeTransfertStat(30000l,3000l);
		
		TransferSpeedStat tssSent=new TransferSpeedStat(524288l,32768l, 300000l);
		TransferSpeedStat tssReceived=new TransferSpeedStat(524288l,32768l, 300000l);
		
		RandomByteArrayOutputStream os=new RandomByteArrayOutputStream();
		RandomByteArrayInputStream is=new RandomByteArrayInputStream(toEncrypt);
		
		int signatureSize=sigType.getSignatureSizeInBits()/8;
		byte[] signatures=new byte[signatureSize*(toEncrypt.length/shift)];
		int indexSignature=0;
		Timer t=new Timer(true);
		int sizeEncoded=cipher.getOutputSizeForEncryption(shift);
		os.setLength(toEncrypt.length/shift*sizeEncoded);
		
		while (is.available()>=shift)
		{
			byte tmp[]=new byte[shift];
			int i=is.read(tmp);
			if (i==shift)
			{
				long old=System.currentTimeMillis();
				byte[] encoded=cipher.encode(tmp);
				signer.sign(encoded, 0, encoded.length, signatures, indexSignature, signatureSize);
				os.write(encoded);
				nb+=shift;
				indexSignature+=signatureSize;
				rtSent.newBytesIndentified(shift);
				tssSent.newBytesIndentified(shift, System.currentTimeMillis()-old);
			}
		}
		double ms=t.getMilid();
		double speedEncoding=(nb/(ms*1000.0));
		System.out.println("Encryption speed  : "+speedEncoding+" MiO/s");
		is.close();
		
		
		is=new RandomByteArrayInputStream(os.getBytes());
		os.close();
		os=new RandomByteArrayOutputStream();
		os.setLength(toEncrypt.length/shift*sizeEncoded);
		Timer t2=new Timer(true);
		indexSignature=0;
		while (is.available()>0)
		{
			long old=System.currentTimeMillis();
			byte tmp[]=new byte[sizeEncoded];
			is.read(tmp);
			
			os.write(cipher.decode(tmp));
			signer.sign(tmp);
			Assert.assertTrue(checker.verify(tmp, 0, tmp.length, signatures, indexSignature, signatureSize));
			indexSignature+=signatureSize;
			rtReceived.newBytesIndentified(shift);
			tssReceived.newBytesIndentified(shift, System.currentTimeMillis()-old);
			
		}
		double ms2=t2.getMilid();
		double speedEncodingAndDecoding=(nb/((ms2+ms)*1000.0));
		double speedDecoding=(nb/(ms2*1000.0));
		
		
		System.out.println("Decryption speed  : "+speedDecoding+" MiO/s");
		System.out.println("Encryption and decryption speed  : "+speedEncodingAndDecoding+" MiO/s");
		is.close();
		os.close();
	}

}
