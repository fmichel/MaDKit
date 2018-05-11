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
package com.distrimind.madkit.kernel.network;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Externalizable;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.distrimind.madkit.database.KeysPairs;
import com.distrimind.madkit.exceptions.BlockParserException;
import com.distrimind.madkit.exceptions.ConnectionException;
import com.distrimind.madkit.exceptions.NIOException;
import com.distrimind.madkit.exceptions.PacketException;
import com.distrimind.madkit.io.RandomByteArrayInputStream;
import com.distrimind.madkit.io.RandomByteArrayOutputStream;
import com.distrimind.madkit.kernel.JunitMadkit;
import com.distrimind.madkit.kernel.MadkitProperties;
import com.distrimind.madkit.kernel.network.connection.AskConnection;
import com.distrimind.madkit.kernel.network.connection.ConnectionMessage;
import com.distrimind.madkit.kernel.network.connection.ConnectionProtocol;
import com.distrimind.madkit.kernel.network.connection.ConnectionProtocol.ConnectionState;
import com.distrimind.madkit.kernel.network.connection.ConnectionProtocolProperties;
import com.distrimind.madkit.kernel.network.connection.PointToPointTransferedBlockChecker;
import com.distrimind.madkit.kernel.network.connection.TransferedBlockChecker;
import com.distrimind.madkit.kernel.network.connection.UnexpectedMessage;
import com.distrimind.madkit.kernel.network.connection.secured.ClientSecuredProtocolPropertiesWithKnownPublicKey;
import com.distrimind.madkit.kernel.network.connection.secured.P2PSecuredConnectionProtocolWithASymmetricKeyExchangerProperties;
import com.distrimind.madkit.kernel.network.connection.secured.P2PSecuredConnectionProtocolWithKeyAgreementProperties;
import com.distrimind.madkit.kernel.network.connection.secured.ServerSecuredProcotolPropertiesWithKnownPublicKey;
import com.distrimind.madkit.kernel.network.connection.unsecured.CheckSumConnectionProtocolProperties;
import com.distrimind.madkit.kernel.network.connection.unsecured.UnsecuredConnectionProtocolProperties;
import com.distrimind.madkit.util.SerializationTools;
import com.distrimind.ood.database.DatabaseConfiguration;
import com.distrimind.ood.database.EmbeddedHSQLDBWrapper;
import com.distrimind.util.crypto.ASymmetricEncryptionType;
import com.distrimind.util.crypto.ASymmetricKeyPair;
import com.distrimind.util.crypto.ASymmetricKeyWrapperType;
import com.distrimind.util.crypto.AbstractSecureRandom;
import com.distrimind.util.crypto.KeyAgreementType;
import com.distrimind.util.crypto.MessageDigestType;
import com.distrimind.util.crypto.SecureRandomType;
import com.distrimind.util.crypto.SymmetricEncryptionType;

import gnu.vm.jgnu.security.InvalidAlgorithmParameterException;
import gnu.vm.jgnu.security.NoSuchAlgorithmException;
import gnu.vm.jgnu.security.NoSuchProviderException;

/**
 * 
 * @author Jason Mahdjoub
 * @version 1.2
 * @since MadkitLanEdition 1.0
 */
@RunWith(Parameterized.class)
public class ConnectionsProtocolsTests extends JunitMadkit {
	private static final EmbeddedHSQLDBWrapper sql_connection_asker;
	private static final EmbeddedHSQLDBWrapper sql_connection_recveiver;
	private static final File dbasker = new File("dbasker.database");
	private static final File dbreceiver = new File("dbreceiver.database");

	static {
		EmbeddedHSQLDBWrapper asker = null;
		EmbeddedHSQLDBWrapper receiver = null;
		try {

			if (dbasker.exists())
				EmbeddedHSQLDBWrapper.deleteDatabaseFiles(dbasker);
			if (dbreceiver.exists())
				EmbeddedHSQLDBWrapper.deleteDatabaseFiles(dbreceiver);
			asker = new EmbeddedHSQLDBWrapper(dbasker);
			receiver = new EmbeddedHSQLDBWrapper(dbreceiver);
			asker.loadDatabase(new DatabaseConfiguration(KeysPairs.class.getPackage()), true);
			receiver.loadDatabase(new DatabaseConfiguration(KeysPairs.class.getPackage()), true);
		} catch (Exception e) {
			e.printStackTrace();
		}
		sql_connection_asker = asker;
		sql_connection_recveiver = receiver;
	}

	@AfterClass
	public static void removeDatabase() {
		sql_connection_asker.close();
		sql_connection_recveiver.close();
		EmbeddedHSQLDBWrapper.deleteDatabaseFiles(dbasker);
		EmbeddedHSQLDBWrapper.deleteDatabaseFiles(dbreceiver);
	}

	private static final int numberMaxExchange = 100;
	private ConnectionProtocol<?> cpasker;
	private ConnectionProtocol<?> cpreceiver;
	private final NetworkProperties npasker;
	private final NetworkProperties npreceiver;
	private static final MadkitProperties mkProperties=new MadkitProperties();
	private final static AbstractSecureRandom rand ;
	static
	{
		AbstractSecureRandom r=null;
		try {
			r=SecureRandomType.DEFAULT.getInstance(null);
		} catch (NoSuchAlgorithmException | NoSuchProviderException e) {
			e.printStackTrace();
			System.exit(-1);
		}
		rand=r;
	}

	private static ArrayList<ConnectionProtocolProperties<?>[]> dataWithSubLevel()
			throws SecurityException, IllegalArgumentException, NoSuchAlgorithmException, NoSuchProviderException, InvalidAlgorithmParameterException {
		ArrayList<ConnectionProtocolProperties<?>[]> res = new ArrayList<>();
		ArrayList<ConnectionProtocolProperties<?>[]> l=dataOneLevel();
		@SuppressWarnings("unchecked")
		ArrayList<ConnectionProtocolProperties<?>[]> firstLevel[] = new ArrayList[l.size()];
		firstLevel[0]=l;
		for (int i = 1;i<firstLevel.length;i++)
			firstLevel[i]=dataOneLevel();
		for (int i = 0; i < l.size(); i++) {
			ArrayList<ConnectionProtocolProperties<?>[]> subLevel = dataOneLevel();

			for (int j = 0; j < subLevel.size(); j++) {
				ConnectionProtocolProperties<?>[] base = firstLevel[j].get(i);
				ConnectionProtocolProperties<?>[] sub = subLevel.get(j);
				base[0].subProtocolProperties = sub[0];
				base[1].subProtocolProperties = sub[1];
				res.add(base);
			}

		}
		return res;
	}

	private static ArrayList<ConnectionProtocolProperties<?>[]> dataOneLevel()
			throws SecurityException, IllegalArgumentException, NoSuchAlgorithmException, NoSuchProviderException, InvalidAlgorithmParameterException {
		ArrayList<ConnectionProtocolProperties<?>[]> res = new ArrayList<>();

		ConnectionProtocolProperties<?>[] o = new ConnectionProtocolProperties<?>[2];
		P2PSecuredConnectionProtocolWithASymmetricKeyExchangerProperties p2pp = new P2PSecuredConnectionProtocolWithASymmetricKeyExchangerProperties();
		p2pp.aSymetricKeySize = 2048;
		p2pp.symmetricEncryptionType = SymmetricEncryptionType.AES_GCM;
		p2pp.enableEncryption=true;
		o[0] = p2pp;
		p2pp = new P2PSecuredConnectionProtocolWithASymmetricKeyExchangerProperties();
		p2pp.aSymetricKeySize = 2048;
		p2pp.enableEncryption=true;
		p2pp.symmetricEncryptionType = SymmetricEncryptionType.AES_GCM;
		o[1] = p2pp;
		res.add(o);
		
		o = new ConnectionProtocolProperties<?>[2];
		p2pp = new P2PSecuredConnectionProtocolWithASymmetricKeyExchangerProperties();
		p2pp.aSymetricKeySize = 2048;
		p2pp.symmetricEncryptionType = SymmetricEncryptionType.AES_CBC_PKCS5Padding;
		p2pp.enableEncryption=true;
		o[0] = p2pp;
		p2pp = new P2PSecuredConnectionProtocolWithASymmetricKeyExchangerProperties();
		p2pp.aSymetricKeySize = 2048;
		p2pp.symmetricEncryptionType = SymmetricEncryptionType.AES_CBC_PKCS5Padding;
		p2pp.enableEncryption=true;
		o[1] = p2pp;
		res.add(o);
		
		

		o = new ConnectionProtocolProperties<?>[2];
		p2pp = new P2PSecuredConnectionProtocolWithASymmetricKeyExchangerProperties();
		p2pp.aSymetricKeySize = 2048;
		p2pp.enableEncryption = false;
		p2pp.symmetricEncryptionType = SymmetricEncryptionType.DEFAULT;
		o[0] = p2pp;
		p2pp = new P2PSecuredConnectionProtocolWithASymmetricKeyExchangerProperties();
		p2pp.aSymetricKeySize = 2048;
		p2pp.enableEncryption = false;
		p2pp.symmetricEncryptionType = SymmetricEncryptionType.DEFAULT;
		o[1] = p2pp;
		res.add(o);

		o = new ConnectionProtocolProperties<?>[2];
		P2PSecuredConnectionProtocolWithKeyAgreementProperties p2pp_ecdh = new P2PSecuredConnectionProtocolWithKeyAgreementProperties();
		p2pp_ecdh.symmetricEncryptionType = SymmetricEncryptionType.AES_CBC_PKCS5Padding;
		p2pp_ecdh.keyAgreementType=KeyAgreementType.BC_ECCDH_384_CURVE_M_511;
		p2pp_ecdh.enableEncryption=true;
		o[0] = p2pp_ecdh;
		p2pp_ecdh = new P2PSecuredConnectionProtocolWithKeyAgreementProperties();
		p2pp_ecdh.symmetricEncryptionType = SymmetricEncryptionType.AES_CBC_PKCS5Padding;
		p2pp_ecdh.keyAgreementType=KeyAgreementType.BC_ECCDH_384_CURVE_M_511;
		p2pp_ecdh.enableEncryption=true;
		o[1] = p2pp_ecdh;
		res.add(o);

		o = new ConnectionProtocolProperties<?>[2];
		p2pp_ecdh = new P2PSecuredConnectionProtocolWithKeyAgreementProperties();
		p2pp_ecdh.symmetricEncryptionType = SymmetricEncryptionType.AES_GCM;
		p2pp_ecdh.keyAgreementType=KeyAgreementType.BC_ECCDH_384_CURVE_M_511;
		p2pp_ecdh.enableEncryption=true;
		o[0] = p2pp_ecdh;
		p2pp_ecdh = new P2PSecuredConnectionProtocolWithKeyAgreementProperties();
		p2pp_ecdh.symmetricEncryptionType = SymmetricEncryptionType.AES_GCM;
		p2pp_ecdh.keyAgreementType=KeyAgreementType.BC_ECCDH_384_CURVE_M_511;
		p2pp_ecdh.enableEncryption=true;
		o[1] = p2pp_ecdh;
		res.add(o);

		o = new ConnectionProtocolProperties<?>[2];
		p2pp_ecdh = new P2PSecuredConnectionProtocolWithKeyAgreementProperties();
		p2pp_ecdh.symmetricEncryptionType = SymmetricEncryptionType.DEFAULT;
		p2pp_ecdh.keyAgreementType=KeyAgreementType.BC_ECCDH_384_CURVE_M_511;
		p2pp_ecdh.enableEncryption=false;
		o[0] = p2pp_ecdh;
		p2pp_ecdh = new P2PSecuredConnectionProtocolWithKeyAgreementProperties();
		p2pp_ecdh.symmetricEncryptionType = SymmetricEncryptionType.DEFAULT;
		p2pp_ecdh.keyAgreementType=KeyAgreementType.BC_ECCDH_384_CURVE_M_511;
		p2pp_ecdh.enableEncryption=false;
		o[1] = p2pp_ecdh;
		res.add(o);
		o = new ConnectionProtocolProperties<?>[2];
		p2pp_ecdh = new P2PSecuredConnectionProtocolWithKeyAgreementProperties();
		p2pp_ecdh.symmetricEncryptionType = SymmetricEncryptionType.DEFAULT;
		p2pp_ecdh.keyAgreementType=KeyAgreementType.BCPQC_NEW_HOPE;
		p2pp_ecdh.enableEncryption=true;
		p2pp_ecdh.symmetricKeySizeBits=256;
		o[0] = p2pp_ecdh;
		p2pp_ecdh = new P2PSecuredConnectionProtocolWithKeyAgreementProperties();
		p2pp_ecdh.symmetricEncryptionType = SymmetricEncryptionType.DEFAULT;
		p2pp_ecdh.keyAgreementType=KeyAgreementType.BCPQC_NEW_HOPE;
		p2pp_ecdh.enableEncryption=true;
		p2pp_ecdh.symmetricKeySizeBits=256;
		o[1] = p2pp_ecdh;
		res.add(o);

		o = new ConnectionProtocolProperties<?>[2];
		p2pp_ecdh = new P2PSecuredConnectionProtocolWithKeyAgreementProperties();
		p2pp_ecdh.symmetricEncryptionType = SymmetricEncryptionType.DEFAULT;
		p2pp_ecdh.keyAgreementType=KeyAgreementType.BCPQC_NEW_HOPE;
		p2pp_ecdh.enableEncryption=false;
		o[0] = p2pp_ecdh;
		p2pp_ecdh = new P2PSecuredConnectionProtocolWithKeyAgreementProperties();
		p2pp_ecdh.symmetricEncryptionType = SymmetricEncryptionType.DEFAULT;
		p2pp_ecdh.keyAgreementType=KeyAgreementType.BCPQC_NEW_HOPE;
		p2pp_ecdh.enableEncryption=false;
		o[1] = p2pp_ecdh;
		res.add(o);
	
		
		o = new ConnectionProtocolProperties<?>[2];
		ServerSecuredProcotolPropertiesWithKnownPublicKey sp = new ServerSecuredProcotolPropertiesWithKnownPublicKey();
		ClientSecuredProtocolPropertiesWithKnownPublicKey cp = new ClientSecuredProtocolPropertiesWithKnownPublicKey();
		ASymmetricKeyPair kpe = ASymmetricEncryptionType.DEFAULT
				.getKeyPairGenerator(SecureRandomType.DEFAULT.getSingleton(null), (short) 2048).generateKeyPair();
		sp.addEncryptionProfile(kpe, SymmetricEncryptionType.AES_CBC_PKCS5Padding, ASymmetricKeyWrapperType.DEFAULT);
		cp.setEncryptionProfile(sp);
		sp.enableEncryption = true;
		cp.enableEncryption = true;
		o[0] = cp;
		o[1] = sp;
		res.add(o);

		
		o = new ConnectionProtocolProperties<?>[2];
		sp = new ServerSecuredProcotolPropertiesWithKnownPublicKey();
		cp = new ClientSecuredProtocolPropertiesWithKnownPublicKey();
		kpe = ASymmetricEncryptionType.DEFAULT
				.getKeyPairGenerator(SecureRandomType.DEFAULT.getSingleton(null), (short) 2048).generateKeyPair();
		sp.addEncryptionProfile(kpe, SymmetricEncryptionType.AES_GCM, ASymmetricKeyWrapperType.DEFAULT);
		cp.setEncryptionProfile(sp);
		sp.enableEncryption = true;
		cp.enableEncryption = true;
		o[0] = cp;
		o[1] = sp;
		res.add(o);

		o = new ConnectionProtocolProperties<?>[2];
		sp = new ServerSecuredProcotolPropertiesWithKnownPublicKey();
		cp = new ClientSecuredProtocolPropertiesWithKnownPublicKey();
		sp.addEncryptionProfile(kpe, SymmetricEncryptionType.DEFAULT, ASymmetricKeyWrapperType.DEFAULT);
		cp.setEncryptionProfile(sp);
		sp.enableEncryption = false;
		cp.enableEncryption = false;
		o[0] = cp;
		o[1] = sp;
		res.add(o);


		o = new ConnectionProtocolProperties<?>[2];
		UnsecuredConnectionProtocolProperties ucp = new UnsecuredConnectionProtocolProperties();
		o[0] = ucp;
		ucp = new UnsecuredConnectionProtocolProperties();
		o[1] = ucp;
		res.add(o);

		o = new ConnectionProtocolProperties<?>[2];
		CheckSumConnectionProtocolProperties cs = new CheckSumConnectionProtocolProperties();
		o[0] = cs;
		cs = new CheckSumConnectionProtocolProperties();
		o[1] = cs;
		res.add(o);

		return res;
	}

	public static Collection<Object[]> data(boolean enableDatabase) throws SecurityException, IllegalArgumentException,
			UnknownHostException, NoSuchAlgorithmException, NIOException, NoSuchProviderException, InvalidAlgorithmParameterException {
		ArrayList<ConnectionProtocolProperties<?>[]> data = dataOneLevel();
		data.addAll(dataWithSubLevel());
		Collection<Object[]> res = new ArrayList<>();
		for (int i = 0; i < data.size(); i++) {
			ConnectionProtocolProperties<?>[] base = data.get(i);
			Object o[] = new Object[4];
			
			NetworkProperties np = new NetworkProperties();
			np.addConnectionProtocol(base[0]);
			o[0] = getConnectionProtocolInstance(mkProperties, np, enableDatabase ? 1 : 0);
			o[1] = np;
			np = new NetworkProperties();
			np.addConnectionProtocol(base[1]);
			o[2] = getConnectionProtocolInstance(mkProperties, np, enableDatabase ? 2 : 0);
			o[3] = np;
			res.add(o);
		}
		return res;
	}

	@Parameters
	public static Collection<Object[]> data() throws SecurityException, IllegalArgumentException, UnknownHostException,
			NoSuchAlgorithmException, NIOException, NoSuchProviderException, InvalidAlgorithmParameterException {
		Collection<Object[]> res = data(false);
		res.addAll(data(true));
		return res;
	}

	public static ConnectionProtocol<?> getConnectionProtocolInstance(MadkitProperties mkProperties, NetworkProperties np, int database)
			throws NIOException, UnknownHostException, IllegalArgumentException {
		return np.getConnectionProtocolInstance(new InetSocketAddress(InetAddress.getByName("56.41.158.221"), 5000),
				new InetSocketAddress(InetAddress.getByName("192.168.0.55"), 5000),
				database == 1 ? sql_connection_asker : database == 2 ? sql_connection_recveiver : null, mkProperties,false, false);
		// ConnectionProtocolProperties<?> cpp=np.getConnectionProtocolProperties(new
		// InetSocketAddress(InetAddress.getByName("56.41.158.221"), 5000), new
		// InetSocketAddress(InetAddress.getByName("192.168.0.55"), 5000));
		// return cpp.getConnectionProtocolInstance(, , 5000), null, np);
	}

	public ConnectionsProtocolsTests(ConnectionProtocol<?> cpasker, NetworkProperties npasker,
			ConnectionProtocol<?> cpreceiver, NetworkProperties npreceiver) {
		this.cpasker = cpasker;
		this.cpreceiver = cpreceiver;
		this.npasker = npasker;
		this.npreceiver = npreceiver;
		
	}

	@Test
	public void testRegularConnection()
			throws ConnectionException, PacketException, NIOException, IOException, ClassNotFoundException,
			BlockParserException, IllegalArgumentException, NoSuchAlgorithmException, NoSuchProviderException {
		System.out.println("Testing connection protocol with " + cpasker.getClass() + " for asker and "
				+ cpreceiver.getClass() + " for receiver (crypted=" + cpasker.isCrypted() + ")");
		if (cpasker.getSubProtocol() != null)
			System.out.println("\tSub connection protocol with " + cpasker.getSubProtocol().getClass()
					+ " for asker and " + cpreceiver.getSubProtocol().getClass() + " for receiver (crypted="
					+ cpasker.getSubProtocol().isCrypted() + ")");
		/*this.cpasker.setCounterSelector(new CounterSelector(this.cpasker));
		this.cpreceiver.setCounterSelector(new CounterSelector(this.cpreceiver));*/
		TransferedBlockChecker 	tbcasker = this.cpasker.getTransferedBlockChecker();
		if (tbcasker.isCompletelyInoperant())
		{
			tbcasker=new PointToPointTransferedBlockChecker();
			this.cpasker.setPointToPointTransferedBlockChecker((PointToPointTransferedBlockChecker)tbcasker);
		}
		else
		{
			this.cpasker.setPointToPointTransferedBlockChecker(null);
			tbcasker = (TransferedBlockChecker) unserialize(serialize(tbcasker));
		}

		TransferedBlockChecker tbreceiver = this.cpreceiver.getTransferedBlockChecker();
		if (tbreceiver.isCompletelyInoperant())
		{
			tbreceiver=new PointToPointTransferedBlockChecker();
			this.cpreceiver.setPointToPointTransferedBlockChecker((PointToPointTransferedBlockChecker)tbreceiver);
		}
		else
		{
			this.cpreceiver.setPointToPointTransferedBlockChecker(null);
			tbreceiver = (TransferedBlockChecker) unserialize(serialize(tbreceiver));
		}
		
	
		Iterator<ConnectionProtocol<?>> itasker = this.cpasker.reverseIterator();
		Iterator<ConnectionProtocol<?>> itreceiver = this.cpreceiver.reverseIterator();
		int totalCycles = 0;

		while (itasker.hasNext()) {
			ConnectionProtocol<?> cpasker = itasker.next();
			Assert.assertTrue(itreceiver.hasNext());
			ConnectionProtocol<?> cpreceiver = itreceiver.next();
			Assert.assertFalse(cpasker.isConnectionEstablished());
			Assert.assertFalse(cpreceiver.isConnectionEstablished());
			Assert.assertEquals(ConnectionState.NOT_CONNECTED, cpasker.getConnectionState());
			Assert.assertEquals(ConnectionState.NOT_CONNECTED, cpreceiver.getConnectionState());

			ConnectionMessage masker = cpasker.setAndGetNextMessage(new AskConnection(true));

			if (this.cpasker.isTransferBlockCheckerChanged())
			{
				tbcasker = this.cpasker.getTransferedBlockChecker();
				if (tbcasker.isCompletelyInoperant())
				{
					tbcasker=new PointToPointTransferedBlockChecker();
					this.cpasker.setPointToPointTransferedBlockChecker((PointToPointTransferedBlockChecker)tbcasker);
				}
				else
				{
					this.cpasker.setPointToPointTransferedBlockChecker(null);
					tbcasker = (TransferedBlockChecker) unserialize(serialize(tbcasker));
				}
				
			}
			// testRandomPingPongMessage();
			ConnectionMessage mreceiver = null;
			int cycles = 0;
			try {
				do {
					byte[] message = serialize(masker);
					boolean excludeFromEncryption=masker.excludedFromEncryption();
					masker = (ConnectionMessage) unserialize(getMessage(message,
							getBytesToSend(getBlocks(message,excludeFromEncryption, this.cpasker, npasker, 2, -1, null)), this.cpreceiver,
							npreceiver, 2, -1, null));
					
					Assert.assertFalse(cpreceiver.isConnectionEstablished());
					mreceiver = cpreceiver.setAndGetNextMessage(masker);
					if (this.cpreceiver.isTransferBlockCheckerChanged())
					{
						tbreceiver = this.cpreceiver.getTransferedBlockChecker();
						if (tbreceiver.isCompletelyInoperant())
						{
							tbreceiver=new PointToPointTransferedBlockChecker();
							this.cpreceiver.setPointToPointTransferedBlockChecker((PointToPointTransferedBlockChecker)tbreceiver);
						}
						else
						{
							this.cpreceiver.setPointToPointTransferedBlockChecker(null);
							tbreceiver = (TransferedBlockChecker) unserialize(serialize(tbreceiver));
							
						}
						
					}

					if (mreceiver == null) {
						masker = null;
						break;
					}
					message = serialize(mreceiver);
					excludeFromEncryption=mreceiver.excludedFromEncryption();
					try
					{
						mreceiver = (ConnectionMessage) unserialize(getMessage(message,
							getBytesToSend(getBlocks(message,excludeFromEncryption, this.cpreceiver, npreceiver, 2, -1, null)),
							this.cpasker, npasker, 2, -1, null));
					}
					catch(IOException e)
					{
						System.err.println(mreceiver.getClass().getName());
						throw e;
					}
					
					Assert.assertFalse(cpasker.isConnectionEstablished());
					masker = cpasker.setAndGetNextMessage(mreceiver);
					if (this.cpasker.isTransferBlockCheckerChanged())
					{
						tbcasker = this.cpasker.getTransferedBlockChecker();
						if (tbcasker.isCompletelyInoperant())
						{
							tbcasker=new PointToPointTransferedBlockChecker();
							this.cpasker.setPointToPointTransferedBlockChecker((PointToPointTransferedBlockChecker)tbcasker);
						}
						else
						{
							this.cpasker.setPointToPointTransferedBlockChecker(null);
							tbcasker = (TransferedBlockChecker) unserialize(serialize(tbcasker));
							
						}
						
					}

					cycles++;
				} while ((masker != null && mreceiver != null) && cycles < numberMaxExchange);
			} catch (Exception e) {
				System.out.println("asker has next " + itasker.hasNext());
				System.out.println("receiver has next " + itreceiver.hasNext());
				e.printStackTrace();

				throw e;
			}
			Assert.assertTrue(cycles < numberMaxExchange);
			Assert.assertTrue(masker == null || mreceiver == null);
			Assert.assertTrue(cpreceiver.isConnectionEstablished());
			Assert.assertTrue(cpasker.isConnectionEstablished());

			Assert.assertEquals(ConnectionState.CONNECTION_ESTABLISHED, cpasker.getConnectionState());
			Assert.assertEquals(ConnectionState.CONNECTION_ESTABLISHED, cpreceiver.getConnectionState());
			/*if (!itasker.hasNext())
			{
				this.cpasker.getCounterSelector().setActivated();
				this.cpreceiver.getCounterSelector().setActivated();
			}*/
			testRandomPingPongMessage();
			testRandomPingPongMessage();
			testRandomPingPongMessage();
			testRandomPingPongMessage();
			testRandomPingPongMessage();
			testRandomPingPongMessage();
			testRandomPingPongMessage();
			testRandomPingPongMessage();
			testRandomPingPongMessage();
			totalCycles += cycles;
		}
		
		testRandomPingPongMessage(tbcasker, tbreceiver);
		testRandomPingPongMessage(tbcasker, tbreceiver);
		testRandomPingPongMessage(tbcasker, tbreceiver);
		testRandomPingPongMessage(tbcasker, tbreceiver);

		testIrregularConnectionWithUnkowMessages(totalCycles, cpasker.getDatabaseWrapper() != null);
		testIrregularConnectionWithCurruptedMessages(totalCycles, cpasker.getDatabaseWrapper() != null);
	}

	public void testIrregularConnectionWithUnkowMessages(int cyclesNumber, boolean enableDatabase)
			throws ConnectionException, PacketException, NIOException, IOException, ClassNotFoundException,
			BlockParserException, IllegalArgumentException, NoSuchAlgorithmException, NoSuchProviderException {
		for (int i = 0; i < cyclesNumber; i++) {
			testIrregularConnectionWithUnkowMessage(i, true, enableDatabase);
			testIrregularConnectionWithUnkowMessage(i, false, enableDatabase);
		}
	}

	public void testIrregularConnectionWithUnkowMessage(int index, boolean asker, boolean enableDatabase)
			throws ConnectionException, PacketException, NIOException, IOException, ClassNotFoundException,
			BlockParserException, IllegalArgumentException, NoSuchAlgorithmException, NoSuchProviderException {
		cpasker = getConnectionProtocolInstance(mkProperties, npasker, enableDatabase ? 1 : 0);
		cpreceiver = getConnectionProtocolInstance(mkProperties, npreceiver, enableDatabase ? 2 : 0);
		/*this.cpasker.setCounterSelector(new CounterSelector(this.cpasker));
		this.cpreceiver.setCounterSelector(new CounterSelector(this.cpreceiver));*/

		Assert.assertFalse(cpasker.isConnectionEstablished());
		Assert.assertFalse(cpreceiver.isConnectionEstablished());
		Assert.assertEquals(ConnectionState.NOT_CONNECTED, cpasker.getConnectionState());
		Assert.assertEquals(ConnectionState.NOT_CONNECTED, cpreceiver.getConnectionState());

		Iterator<ConnectionProtocol<?>> itasker = this.cpasker.reverseIterator();
		Iterator<ConnectionProtocol<?>> itreceiver = this.cpreceiver.reverseIterator();
		int cycles = 0;

		while (itasker.hasNext()) {
			ConnectionProtocol<?> cpasker = itasker.next();
			Assert.assertTrue(itreceiver.hasNext());
			ConnectionProtocol<?> cpreceiver = itreceiver.next();

			ConnectionMessage masker = cpasker.setAndGetNextMessage(new AskConnection(true));

			// testRandomPingPongMessage();
			ConnectionMessage mreceiver = null;

			do {
				if (cycles == index && asker) {
					masker = new UnknowConnectionMessage();
				}
				byte[] message = serialize(masker);
				boolean excludeFromEncryption=masker.excludedFromEncryption();
				masker = (ConnectionMessage) unserialize(
						getMessage(message, getBytesToSend(getBlocks(message,excludeFromEncryption, this.cpasker, npasker, 2, -1, null)),
								this.cpreceiver, npreceiver, 2, -1, null));
				
				mreceiver = cpreceiver.setAndGetNextMessage(masker);
				if (mreceiver == null) {
					masker = null;
					break;
				} else {
					if (cycles == index) {
						if (asker) {
							Assert.assertEquals(mreceiver.getClass(), UnexpectedMessage.class);
							return;
						} else
							mreceiver = new UnknowConnectionMessage();
					}
				}

				message = serialize(mreceiver);
				excludeFromEncryption=mreceiver.excludedFromEncryption();
				mreceiver = (ConnectionMessage) unserialize(getMessage(message,
						getBytesToSend(getBlocks(message,excludeFromEncryption, this.cpreceiver, npreceiver, 2, -1, null)), this.cpasker,
						npasker, 2, -1, null));
				
				masker = cpasker.setAndGetNextMessage(mreceiver);
				if (masker != null && cycles == index && !asker) {
					Assert.assertEquals(masker.getClass(), UnexpectedMessage.class);
					return;
				}
				cycles++;
			} while ((masker != null && mreceiver != null) && cycles < numberMaxExchange);
			Assert.assertTrue(cycles < numberMaxExchange);
			Assert.assertTrue(masker == null || mreceiver == null);
			Assert.assertTrue(cpreceiver.isConnectionEstablished());
			Assert.assertTrue(cpasker.isConnectionEstablished());

			Assert.assertEquals(ConnectionState.CONNECTION_ESTABLISHED, cpasker.getConnectionState());
			Assert.assertEquals(ConnectionState.CONNECTION_ESTABLISHED, cpreceiver.getConnectionState());

			testRandomPingPongMessage();
			testRandomPingPongMessage();
			testRandomPingPongMessage();
			testRandomPingPongMessage();
			testRandomPingPongMessage();
			testRandomPingPongMessage();
			testRandomPingPongMessage();
			testRandomPingPongMessage();
			testRandomPingPongMessage();
		}
	}

	public void testIrregularConnectionWithCurruptedMessages(int cyclesNumber, boolean enableDatabase)
			throws ConnectionException, PacketException, NIOException, IOException, ClassNotFoundException,
			BlockParserException, IllegalArgumentException, NoSuchAlgorithmException, NoSuchProviderException {
		for (int i = 0; i < cyclesNumber; i++) {
			testIrregularConnectionWithUnkowMessage(i, true, enableDatabase);
			testIrregularConnectionWithUnkowMessage(i, false, enableDatabase);
		}
	}

	public void testIrregularConnectionWithCurrptedMessage(int index, boolean asker, boolean enableDatabase)
			throws ConnectionException, PacketException, NIOException, IOException, ClassNotFoundException,
			BlockParserException, NoSuchAlgorithmException, NoSuchProviderException {
		cpasker = getConnectionProtocolInstance(mkProperties, npasker, enableDatabase ? 1 : 0);
		cpreceiver = getConnectionProtocolInstance(mkProperties, npreceiver, enableDatabase ? 2 : 0);
		Assert.assertFalse(cpasker.isConnectionEstablished());
		Assert.assertFalse(cpreceiver.isConnectionEstablished());
		Assert.assertEquals(ConnectionState.NOT_CONNECTED, cpasker.getConnectionState());
		Assert.assertEquals(ConnectionState.NOT_CONNECTED, cpreceiver.getConnectionState());

		Iterator<ConnectionProtocol<?>> itasker = this.cpasker.reverseIterator();
		Iterator<ConnectionProtocol<?>> itreceiver = this.cpreceiver.reverseIterator();

		int cycles = 0;
		while (itasker.hasNext()) {
			ConnectionProtocol<?> cpasker = itasker.next();
			Assert.assertTrue(itreceiver.hasNext());
			ConnectionProtocol<?> cpreceiver = itreceiver.next();

			ConnectionMessage masker = cpasker.setAndGetNextMessage(new AskConnection(true));

			testRandomPingPongMessage();
			ConnectionMessage mreceiver = null;

			do {
				if (cycles == index && asker) {
					masker.corrupt();
				}
				byte[] message = serialize(masker);
				boolean excludeFromEncryption=masker.excludedFromEncryption();

				masker = (ConnectionMessage) unserialize(
						getMessage(message, getBytesToSend(getBlocks(message, excludeFromEncryption, this.cpasker, npasker, 2, -1, null)),
								this.cpreceiver, npreceiver, 2, -1, null));
				
				mreceiver = cpreceiver.setAndGetNextMessage(masker);
				if (mreceiver == null) {
					masker = null;
					break;
				} else {
					if (cycles == index && !asker) {
						mreceiver.corrupt();
					}
				}

				message = serialize(mreceiver);
				excludeFromEncryption=mreceiver.excludedFromEncryption();
				mreceiver = (ConnectionMessage) unserialize(getMessage(message,
						getBytesToSend(getBlocks(message,excludeFromEncryption, this.cpreceiver, npreceiver, 2, -1, null)), this.cpasker,
						npasker, 2, -1, null));
				
				masker = cpasker.setAndGetNextMessage(mreceiver);
				cycles++;
			} while ((masker != null && mreceiver != null) && cycles < numberMaxExchange);
			Assert.assertTrue(cycles < numberMaxExchange);
			Assert.assertTrue(masker == null || mreceiver == null);
			Assert.assertTrue(cpreceiver.isConnectionEstablished());
			Assert.assertTrue(cpasker.isConnectionEstablished());

			Assert.assertEquals(ConnectionState.CONNECTION_ESTABLISHED, cpasker.getConnectionState());
			Assert.assertEquals(ConnectionState.CONNECTION_ESTABLISHED, cpreceiver.getConnectionState());
			testRandomPingPongMessage();
			testRandomPingPongMessage();
			testRandomPingPongMessage();
			testRandomPingPongMessage();
			testRandomPingPongMessage();
			testRandomPingPongMessage();
			testRandomPingPongMessage();
			testRandomPingPongMessage();
			testRandomPingPongMessage();
		}
	}

	static class UnknowConnectionMessage extends ConnectionMessage {

		/**
		 * 
		 */
		private static final long serialVersionUID = -1881592582213193045L;

		

		@Override
		public boolean excludedFromEncryption() {
			return false;
		}



		@Override
		public void writeExternal(ObjectOutput out) throws IOException {
			
		}



		@Override
		public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
			
		}



		

	}

	private byte[] getRandomMessage() {
		byte[] message = new byte[20000 + rand.nextInt(100000)];
		rand.nextBytes(message);
		return message;
	}

	public static ArrayList<Block> getBlocks(byte message[], boolean excludeFromEncryption, ConnectionProtocol<?> cp, NetworkProperties np,
			int idPacket, int transferType, TransferedBlockChecker tbc) throws PacketException, IOException,
			NIOException, BlockParserException, NoSuchAlgorithmException, NoSuchProviderException {
		ArrayList<Block> res = new ArrayList<>();
		WritePacket wp = new WritePacket(PacketPartHead.TYPE_PACKET, idPacket, np.maxBufferSize,
				np.maxRandomPacketValues, rand, new RandomByteArrayInputStream(message), MessageDigestType.BC_FIPS_SHA3_512);
		Assert.assertEquals(idPacket, wp.getID());
		while (!wp.isFinished()) {
			if (tbc!=null)
			{
				if (tbc instanceof PointToPointTransferedBlockChecker)
				{
					cp.setPointToPointTransferedBlockChecker((PointToPointTransferedBlockChecker)tbc);
					cp.getPointToPointTransferedBlockChecker().setConnectionProtocolInput(null);
					cp.getPointToPointTransferedBlockChecker().setConnectionProtocolOutput(cp);
				}
				else
				{
					cp.setPointToPointTransferedBlockChecker(null);
				}
			}
			else
			{
				cp.setPointToPointTransferedBlockChecker(null);
			}
			Block b = cp.getBlock(wp, transferType,
					np.maxRandomPacketValues > 0 ? SecureRandomType.DEFAULT.getSingleton(null) : null, excludeFromEncryption);
			Assert.assertEquals(transferType, b.getTransferID());
			
			
			Assert.assertTrue(b.isValid());
			res.add(b);
		}
		/*for (Block b : res)
		{
			b.setCounterSelector(cp.getCounterSelector());
			cp.getCounterSelector().releaseCounterID(b.getCounterID());
		}*/
		
		return res;
	}

	public static ArrayList<byte[]> getBytesToSend(ArrayList<Block> blocks) {
		ArrayList<byte[]> res = new ArrayList<>(blocks.size());
		for (Block b : blocks)
		{
			if (b.getBlockSize()!=b.getBytes().length)
			{
				byte[] tab=new byte[b.getBlockSize()];
				System.arraycopy(b.getBytes(), 0, tab, 0, tab.length);
				res.add(tab);
			}
			else
				res.add(b.getBytes());
		}
		return res;
	}

	public static byte[] getMessage(byte[] originalMessage, ArrayList<byte[]> receivedBytes, ConnectionProtocol<?> cp,
			NetworkProperties np, int idPacket, int transferType, TransferedBlockChecker tbc) throws PacketException, NIOException, BlockParserException {
		Block b = new Block(receivedBytes.get(0));
		Assert.assertEquals(transferType, b.getTransferID());
		Assert.assertTrue(b.isValid());
		if (tbc != null) {
			if (tbc instanceof PointToPointTransferedBlockChecker)
			{
				((PointToPointTransferedBlockChecker)tbc).setConnectionProtocolInput(cp);
				((PointToPointTransferedBlockChecker)tbc).setConnectionProtocolOutput(null);
			}
			SubBlockInfo sbi = tbc.recursiveCheckSubBlock(new SubBlock(b));
			Assert.assertTrue(tbc.getClass().toString(), sbi.isValid());
			Assert.assertFalse(sbi.isCandidateToBan());
			b=new Block(sbi.getSubBlock().getBytes());
			Assert.assertEquals(transferType, b.getTransferID());
			Assert.assertTrue(b.isValid());
		}

		PacketPart pp=null;
		try
		{
			 pp = cp.getPacketPart(b, np);
		}
		catch(NIOException e)
		{
			if (tbc==null)
				System.out.println("tbc = "+tbc);
			else
				System.out.println("tbc = "+tbc.getClass());
			
			throw e;
		}
		Assert.assertEquals("tbc = "+tbc, idPacket, pp.getHead().getID());
		// Assert.assertEquals(originalMessage.length, pp.getHead().getTotalLength());
		Assert.assertEquals(0, pp.getHead().getStartPosition());
		RandomByteArrayOutputStream output = new RandomByteArrayOutputStream();
		ReadPacket rp = new ReadPacket(np.maxBufferSize, np.maxRandomPacketValues, pp, output,
				MessageDigestType.BC_FIPS_SHA3_512);
		Assert.assertEquals(idPacket, rp.getID());
		for (int i = 1; i < receivedBytes.size(); i++) {
			b = new Block(receivedBytes.get(i));
			Assert.assertEquals(transferType, b.getTransferID());
			Assert.assertTrue(b.isValid());
			if (tbc != null) {
				if (tbc instanceof PointToPointTransferedBlockChecker)
				{
					((PointToPointTransferedBlockChecker)tbc).setConnectionProtocolInput(cp);
					((PointToPointTransferedBlockChecker)tbc).setConnectionProtocolOutput(null);
				}
				SubBlockInfo sbi = tbc.recursiveCheckSubBlock(new SubBlock(b));
				Assert.assertTrue(sbi.isValid());
				Assert.assertFalse(sbi.isCandidateToBan());
				b=new Block(sbi.getSubBlock().getBytes());
				Assert.assertEquals(transferType, b.getTransferID());
				Assert.assertTrue(b.isValid());
				
			}
			pp = cp.getPacketPart(b, np);
			rp.readNewPart(pp);
		}

		return output.getBytes();
	}

	private void testRandomPingPongMessage() throws PacketException, NIOException, IOException, BlockParserException,
			NoSuchAlgorithmException, NoSuchProviderException {
		testRandomPingPongMessage(null, null);
	}

	private void testRandomPingPongMessage(TransferedBlockChecker tbcasker, TransferedBlockChecker tbcreceiver)
			throws PacketException, NIOException, IOException, BlockParserException, NoSuchAlgorithmException,
			NoSuchProviderException {
		final int idPacket = rand.nextInt(1000000);
		int transferType = -1;
		byte[] message = getRandomMessage();
		boolean excludeFromEncryption=rand.nextBoolean();
		byte[] receivedMessage = getMessage(message,
				getBytesToSend(getBlocks(message, excludeFromEncryption, cpasker, npasker, idPacket, transferType, tbcasker)), cpreceiver,
				npreceiver, idPacket, transferType, tbcasker);
		Assert.assertArrayEquals(message, receivedMessage);
		receivedMessage = getMessage(message,
				getBytesToSend(getBlocks(message, excludeFromEncryption, cpreceiver, npreceiver, idPacket, transferType, tbcreceiver)),
				cpasker, npasker, idPacket, transferType, tbcreceiver);
		Assert.assertArrayEquals(message, receivedMessage);
	}

	public static byte[] serialize(Externalizable message) throws IOException {
		
		try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
			try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
				SerializationTools.writeExternalizableAndSizable(oos, message, false);
			}
			return baos.toByteArray();
		}
	}

	public static Serializable unserialize(byte[] message) throws IOException, ClassNotFoundException {
		try (ByteArrayInputStream bais = new ByteArrayInputStream(message)) {
			try (ObjectInputStream ois = new ObjectInputStream(bais)) {
				return (Serializable) SerializationTools.readExternalizableAndSizable(ois, false);
				
			}
		}
	}

}
