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
package com.distrimind.madkit.kernel.network.connection.access;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collection;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.distrimind.madkit.database.KeysPairs;
import com.distrimind.madkit.kernel.JunitMadkit;
import com.distrimind.madkit.kernel.KernelAddressTest;
import com.distrimind.madkit.kernel.MadkitProperties;
import com.distrimind.madkit.kernel.network.AccessDataMKEventListener;
import com.distrimind.madkit.kernel.network.ConnectionsProtocolsTests;
import com.distrimind.madkit.kernel.network.SystemMessage.Integrity;
import com.distrimind.madkit.kernel.network.connection.access.AccessData;
import com.distrimind.madkit.kernel.network.connection.access.AccessException;
import com.distrimind.madkit.kernel.network.connection.access.AccessGroupsNotifier;
import com.distrimind.madkit.kernel.network.connection.access.AbstractAccessProtocol;
import com.distrimind.madkit.kernel.network.connection.access.Identifier;
import com.distrimind.madkit.kernel.network.connection.access.LoginEventsTrigger;
import com.distrimind.ood.database.DatabaseConfiguration;
import com.distrimind.ood.database.EmbeddedHSQLDBDatabaseFactory;
import com.distrimind.ood.database.EmbeddedHSQLDBWrapper;
import com.distrimind.ood.database.exceptions.DatabaseException;

import gnu.vm.jgnu.security.NoSuchAlgorithmException;
import gnu.vm.jgnu.security.NoSuchProviderException;
import gnu.vm.jgnu.security.spec.InvalidKeySpecException;

/**
 * 
 * @author Jason Mahdjoub
 * @version 1.0
 * @since MadkitLanEdition 1.0
 */
@RunWith(Parameterized.class)
public class AccessProtocolTests implements AccessGroupsNotifier, LoginEventsTrigger {
	private static final int numberMaxExchange = 100;
	final ArrayList<AccessData> adasker;
	final ArrayList<AccessData> adreceiver;
	final MadkitProperties mpasker;
	final MadkitProperties mpreceiver;
	AbstractAccessProtocol apasker;
	AbstractAccessProtocol apreceiver;
	final ArrayList<Identifier> acceptedAskerIdentifiers;
	final ArrayList<Identifier> acceptedReceiverIdentifiers;
	final ArrayList<IdentifierPassword> identifierPassordsAsker;
	final ArrayList<IdentifierPassword> identifierPassordsReceiver;

	static final File dbfileasker = new File("testaccessasker.database");
	static final File dbfilereceiver = new File("testaccessreceiver.database");

	@Parameters
	public static Collection<Object[]> data() {
		Collection<Object[]> res = data(false);
		res.addAll(data(true));
		return res;
	}

	public static Collection<Object[]> data(boolean databaseEnabled) {
		AccessProtocolWithASymmetricKeyExchangerProperties app1=new AccessProtocolWithASymmetricKeyExchangerProperties();
		app1.aSymetricKeySize = 1024;
		app1.passwordHashIterations = 1024;
		Collection<Object[]> res=data(databaseEnabled, app1);
		AccessProtocolWithJPakeProperties app2=new AccessProtocolWithJPakeProperties();
		res.addAll(data(databaseEnabled, app2));
		return res;
	}

	public static Collection<Object[]> data(boolean databaseEnabled, AbstractAccessProtocolProperties accessProtocolProperties) {
		ArrayList<Object[]> res = new ArrayList<>();
		ArrayList<AccessData> adasker = new ArrayList<>();
		ArrayList<AccessData> adreceiver = new ArrayList<>();
		ArrayList<Identifier> acceptedAskerIdentifiers = new ArrayList<>();
		ArrayList<Identifier> acceptedReceiverIdentifiers = new ArrayList<>();
		ArrayList<IdentifierPassword> identifierPassordsAsker = null;
		ArrayList<IdentifierPassword> identifierPassordsReceiver = null;
		Object[] o = new Object[8];
		adasker.add(AccessDataMKEventListener.getDefaultAccessData(JunitMadkit.DEFAULT_NETWORK_GROUP_FOR_ACCESS_DATA));
		adreceiver
				.add(AccessDataMKEventListener.getDefaultAccessData(JunitMadkit.DEFAULT_NETWORK_GROUP_FOR_ACCESS_DATA));
		o[0] = adasker;
		o[1] = adreceiver;
		o[2] = acceptedAskerIdentifiers;
		o[3] = acceptedReceiverIdentifiers;
		o[4] = identifierPassordsAsker;
		o[5] = identifierPassordsReceiver;
		o[6] = new Boolean(databaseEnabled);
		o[7] = accessProtocolProperties;
		res.add(o);

		o = new Object[8];
		adasker = new ArrayList<>();
		adreceiver = new ArrayList<>();
		acceptedAskerIdentifiers = new ArrayList<>();
		acceptedReceiverIdentifiers = new ArrayList<>();
		adasker.add(AccessDataMKEventListener.getDefaultLoginData(
				identifierPassordsAsker = AccessDataMKEventListener
						.getClientOrPeerToPeerLogins(AccessDataMKEventListener.getCustumHostIdentifier(0), 2, 5, 6, 9),
				null, JunitMadkit.NETWORK_GROUP_FOR_LOGIN_DATA, true, new Runnable() {

					@Override
					public void run() {
						Assert.assertFalse(true);
					}
				}));
		adreceiver.add(AccessDataMKEventListener.getDefaultLoginData(
				identifierPassordsReceiver = AccessDataMKEventListener
						.getClientOrPeerToPeerLogins(AccessDataMKEventListener.getCustumHostIdentifier(1), 3, 5, 6, 7),
				null, JunitMadkit.NETWORK_GROUP_FOR_LOGIN_DATA, false, new Runnable() {

					@Override
					public void run() {
						Assert.assertFalse(true);
					}
				}));
		acceptedAskerIdentifiers
				.add(AccessDataMKEventListener.getIdentifier(AccessDataMKEventListener.getCustumHostIdentifier(0), 5));
		acceptedAskerIdentifiers
				.add(AccessDataMKEventListener.getIdentifier(AccessDataMKEventListener.getCustumHostIdentifier(0), 6));
		acceptedReceiverIdentifiers
				.add(AccessDataMKEventListener.getIdentifier(AccessDataMKEventListener.getCustumHostIdentifier(1), 5));
		acceptedReceiverIdentifiers
				.add(AccessDataMKEventListener.getIdentifier(AccessDataMKEventListener.getCustumHostIdentifier(1), 6));
		o[0] = adasker;
		o[1] = adreceiver;
		o[2] = acceptedAskerIdentifiers;
		o[3] = acceptedReceiverIdentifiers;
		o[4] = identifierPassordsAsker;
		o[5] = identifierPassordsReceiver;
		o[6] = new Boolean(databaseEnabled);
		o[7] = accessProtocolProperties;
		res.add(o);

		o = new Object[8];
		adasker = new ArrayList<>();
		adreceiver = new ArrayList<>();
		acceptedAskerIdentifiers = new ArrayList<>();
		acceptedReceiverIdentifiers = new ArrayList<>();
		adasker.add(AccessDataMKEventListener.getDefaultLoginData(
				identifierPassordsAsker = AccessDataMKEventListener
						.getClientOrPeerToPeerLogins(AccessDataMKEventListener.getCustumHostIdentifier(0), 2, 5, 6, 9),
				null, JunitMadkit.NETWORK_GROUP_FOR_LOGIN_DATA, false, new Runnable() {

					@Override
					public void run() {
						Assert.assertFalse(true);
					}
				}));
		adreceiver.add(AccessDataMKEventListener.getDefaultLoginData(
				identifierPassordsReceiver = AccessDataMKEventListener
						.getClientOrPeerToPeerLogins(AccessDataMKEventListener.getCustumHostIdentifier(1), 3, 5, 6, 7),
				null, JunitMadkit.NETWORK_GROUP_FOR_LOGIN_DATA, true, new Runnable() {

					@Override
					public void run() {
						Assert.assertFalse(true);
					}
				}));
		acceptedAskerIdentifiers
				.add(AccessDataMKEventListener.getIdentifier(AccessDataMKEventListener.getCustumHostIdentifier(0), 5));
		acceptedAskerIdentifiers
				.add(AccessDataMKEventListener.getIdentifier(AccessDataMKEventListener.getCustumHostIdentifier(0), 6));
		acceptedReceiverIdentifiers
				.add(AccessDataMKEventListener.getIdentifier(AccessDataMKEventListener.getCustumHostIdentifier(1), 5));
		acceptedReceiverIdentifiers
				.add(AccessDataMKEventListener.getIdentifier(AccessDataMKEventListener.getCustumHostIdentifier(1), 6));
		o[0] = adasker;
		o[1] = adreceiver;
		o[2] = acceptedAskerIdentifiers;
		o[3] = acceptedReceiverIdentifiers;
		o[4] = identifierPassordsAsker;
		o[5] = identifierPassordsReceiver;
		o[6] = new Boolean(databaseEnabled);
		o[7] = accessProtocolProperties;
		res.add(o);

		o = new Object[8];
		adasker = new ArrayList<>();
		adreceiver = new ArrayList<>();
		acceptedAskerIdentifiers = new ArrayList<>();
		acceptedReceiverIdentifiers = new ArrayList<>();
		adasker.add(AccessDataMKEventListener.getDefaultLoginData(
				identifierPassordsAsker = AccessDataMKEventListener
						.getClientOrPeerToPeerLogins(AccessDataMKEventListener.getCustumHostIdentifier(0), 2, 5, 6, 9),
				JunitMadkit.DEFAULT_NETWORK_GROUP_FOR_ACCESS_DATA, JunitMadkit.NETWORK_GROUP_FOR_LOGIN_DATA, true,
				new Runnable() {

					@Override
					public void run() {
						Assert.assertFalse(true);
					}
				}));
		adreceiver.add(AccessDataMKEventListener.getDefaultLoginData(
				identifierPassordsReceiver = AccessDataMKEventListener
						.getClientOrPeerToPeerLogins(AccessDataMKEventListener.getCustumHostIdentifier(1), 3, 5, 6, 7),
				JunitMadkit.DEFAULT_NETWORK_GROUP_FOR_ACCESS_DATA, JunitMadkit.NETWORK_GROUP_FOR_LOGIN_DATA, false,
				new Runnable() {

					@Override
					public void run() {
						Assert.assertFalse(true);
					}
				}));
		acceptedAskerIdentifiers
				.add(AccessDataMKEventListener.getIdentifier(AccessDataMKEventListener.getCustumHostIdentifier(0), 5));
		acceptedAskerIdentifiers
				.add(AccessDataMKEventListener.getIdentifier(AccessDataMKEventListener.getCustumHostIdentifier(0), 6));
		acceptedReceiverIdentifiers
				.add(AccessDataMKEventListener.getIdentifier(AccessDataMKEventListener.getCustumHostIdentifier(1), 5));
		acceptedReceiverIdentifiers
				.add(AccessDataMKEventListener.getIdentifier(AccessDataMKEventListener.getCustumHostIdentifier(1), 6));
		o[0] = adasker;
		o[1] = adreceiver;
		o[2] = acceptedAskerIdentifiers;
		o[3] = acceptedReceiverIdentifiers;
		o[4] = identifierPassordsAsker;
		o[5] = identifierPassordsReceiver;
		o[6] = new Boolean(databaseEnabled);
		o[7] = accessProtocolProperties;
		res.add(o);

		o = new Object[8];
		adasker = new ArrayList<>();
		adreceiver = new ArrayList<>();
		acceptedAskerIdentifiers = new ArrayList<>();
		acceptedReceiverIdentifiers = new ArrayList<>();
		adasker.add(AccessDataMKEventListener.getDefaultLoginData(
				identifierPassordsAsker = AccessDataMKEventListener
						.getClientOrPeerToPeerLogins(AccessDataMKEventListener.getCustumHostIdentifier(0), 2, 5, 6, 9),
				JunitMadkit.DEFAULT_NETWORK_GROUP_FOR_ACCESS_DATA, JunitMadkit.NETWORK_GROUP_FOR_LOGIN_DATA, true,
				new Runnable() {

					@Override
					public void run() {
						Assert.assertFalse(true);
					}
				}));
		adreceiver.add(AccessDataMKEventListener.getDefaultLoginData(
				identifierPassordsReceiver = AccessDataMKEventListener
						.getClientOrPeerToPeerLogins(AccessDataMKEventListener.getCustumHostIdentifier(1), 3, 5, 6, 7),
				JunitMadkit.DEFAULT_NETWORK_GROUP_FOR_ACCESS_DATA, JunitMadkit.NETWORK_GROUP_FOR_LOGIN_DATA, true,
				new Runnable() {

					@Override
					public void run() {
						Assert.assertFalse(true);
					}
				}));
		acceptedAskerIdentifiers
				.add(AccessDataMKEventListener.getIdentifier(AccessDataMKEventListener.getCustumHostIdentifier(0), 5));
		acceptedAskerIdentifiers
				.add(AccessDataMKEventListener.getIdentifier(AccessDataMKEventListener.getCustumHostIdentifier(0), 6));
		acceptedReceiverIdentifiers
				.add(AccessDataMKEventListener.getIdentifier(AccessDataMKEventListener.getCustumHostIdentifier(1), 5));
		acceptedReceiverIdentifiers
				.add(AccessDataMKEventListener.getIdentifier(AccessDataMKEventListener.getCustumHostIdentifier(1), 6));
		o[0] = adasker;
		o[1] = adreceiver;
		o[2] = acceptedAskerIdentifiers;
		o[3] = acceptedReceiverIdentifiers;
		o[4] = identifierPassordsAsker;
		o[5] = identifierPassordsReceiver;
		o[6] = new Boolean(databaseEnabled);
		o[7] = accessProtocolProperties;
		res.add(o);

		o = new Object[8];
		adasker = new ArrayList<>();
		adreceiver = new ArrayList<>();
		acceptedAskerIdentifiers = new ArrayList<>();
		acceptedReceiverIdentifiers = new ArrayList<>();
		adasker.add(AccessDataMKEventListener.getDefaultLoginData(
				identifierPassordsAsker = AccessDataMKEventListener
						.getClientOrPeerToPeerLogins(AccessDataMKEventListener.getCustumHostIdentifier(0), 2, 5, 6, 9),
				JunitMadkit.DEFAULT_NETWORK_GROUP_FOR_ACCESS_DATA, JunitMadkit.NETWORK_GROUP_FOR_LOGIN_DATA, false,
				new Runnable() {

					@Override
					public void run() {
						Assert.assertFalse(true);
					}
				}));
		adreceiver.add(AccessDataMKEventListener.getDefaultLoginData(
				identifierPassordsReceiver = AccessDataMKEventListener
						.getClientOrPeerToPeerLogins(AccessDataMKEventListener.getCustumHostIdentifier(1), 3, 5, 6, 7),
				JunitMadkit.DEFAULT_NETWORK_GROUP_FOR_ACCESS_DATA, JunitMadkit.NETWORK_GROUP_FOR_LOGIN_DATA, false,
				new Runnable() {

					@Override
					public void run() {
						Assert.assertFalse(true);
					}
				}));
		acceptedAskerIdentifiers
				.add(AccessDataMKEventListener.getIdentifier(AccessDataMKEventListener.getCustumHostIdentifier(0), 5));
		acceptedAskerIdentifiers
				.add(AccessDataMKEventListener.getIdentifier(AccessDataMKEventListener.getCustumHostIdentifier(0), 6));
		acceptedReceiverIdentifiers
				.add(AccessDataMKEventListener.getIdentifier(AccessDataMKEventListener.getCustumHostIdentifier(1), 5));
		acceptedReceiverIdentifiers
				.add(AccessDataMKEventListener.getIdentifier(AccessDataMKEventListener.getCustumHostIdentifier(1), 6));
		o[0] = adasker;
		o[1] = adreceiver;
		o[2] = acceptedAskerIdentifiers;
		o[3] = acceptedReceiverIdentifiers;
		o[4] = identifierPassordsAsker;
		o[5] = identifierPassordsReceiver;
		o[6] = new Boolean(databaseEnabled);
		o[7] = accessProtocolProperties;
		res.add(o);

		return res;
	}

	public AccessProtocolTests(ArrayList<AccessData> adasker, ArrayList<AccessData> adreceiver,
			ArrayList<Identifier> acceptedAskerIdentifiers, ArrayList<Identifier> acceptedReceiverIdentifiers,
			ArrayList<IdentifierPassword> identifierPassordsAsker,
			ArrayList<IdentifierPassword> identifierPassordsReceiver, boolean databaseEnabled, AbstractAccessProtocolProperties accessProtocolProperties)
			throws IllegalArgumentException, DatabaseException {
		this.adasker = adasker;
		this.adreceiver = adreceiver;
		this.identifierPassordsAsker = identifierPassordsAsker;
		this.identifierPassordsReceiver = identifierPassordsReceiver;
		this.mpasker = new MadkitProperties();
		this.mpreceiver = new MadkitProperties();
		for (AccessData ad : adasker)
			this.mpasker.networkProperties.addAccessData(ad);
		for (AccessData ad : adreceiver)
			this.mpreceiver.networkProperties.addAccessData(ad);
		this.mpasker.networkProperties.addAccessProtocolProperties(accessProtocolProperties);
		this.mpreceiver.networkProperties.addAccessProtocolProperties(accessProtocolProperties);
		this.acceptedAskerIdentifiers = acceptedAskerIdentifiers;
		this.acceptedReceiverIdentifiers = acceptedReceiverIdentifiers;
		if (databaseEnabled) {
			mpasker.setDatabaseFactory(new EmbeddedHSQLDBDatabaseFactory(dbfileasker));
			mpreceiver.setDatabaseFactory(new EmbeddedHSQLDBDatabaseFactory(dbfilereceiver));
		}
	}

	@Before
	public void activateDatabase() throws DatabaseException {
		if (mpasker.isDatatabaseEnabled()) {
			mpasker.getDatabaseWrapper().close();
			mpreceiver.getDatabaseWrapper().close();
			if (dbfileasker.exists())
				EmbeddedHSQLDBWrapper.deleteDatabaseFiles(dbfileasker);
			if (dbfilereceiver.exists())
				EmbeddedHSQLDBWrapper.deleteDatabaseFiles(dbfilereceiver);
			JunitMadkit.setDatabaseFactory(mpasker, new EmbeddedHSQLDBDatabaseFactory(dbfileasker));
			mpasker.getDatabaseWrapper().loadDatabase(new DatabaseConfiguration(KeysPairs.class.getPackage()), true);
			JunitMadkit.setDatabaseFactory(mpreceiver, new EmbeddedHSQLDBDatabaseFactory(dbfilereceiver));
			mpreceiver.getDatabaseWrapper().loadDatabase(new DatabaseConfiguration(KeysPairs.class.getPackage()), true);
		}
	}

	@After
	public void removeDatabase() throws DatabaseException {
		if (mpasker.isDatatabaseEnabled()) {
			mpasker.getDatabaseWrapper().close();
			mpreceiver.getDatabaseWrapper().close();
			if (dbfileasker.exists())
				EmbeddedHSQLDBWrapper.deleteDatabaseFiles(dbfileasker);
			if (dbfilereceiver.exists())
				EmbeddedHSQLDBWrapper.deleteDatabaseFiles(dbfilereceiver);
		}

	}

	@Test
	public void testAccessProtocol() throws AccessException, ClassNotFoundException, IOException,
			NoSuchAlgorithmException, InvalidKeySpecException, NoSuchProviderException {
		int nb = testRegularAccessProtocol(0, -1, false);
		for (int i = 0; i < nb - 1; i++) {
			testRegularAccessProtocol(1, i, true);
			testRegularAccessProtocol(1, i, false);
			testRegularAccessProtocol(2, i, true);
			testRegularAccessProtocol(2, i, false);
		}
	}

	static class UnkownAccessMessage extends AccessMessage {
		/**
		 * 
		 */
		private static final long serialVersionUID = 3702277006786527287L;

		@Override
		public boolean checkDifferedMessages() {
			return false;
		}

		@Override
		public Integrity checkDataIntegrity() {
			return Integrity.OK;
		}

	}

	public int testRegularAccessProtocol(int type, int index, boolean asker)
			throws AccessException, ClassNotFoundException, IOException, NoSuchAlgorithmException,
			InvalidKeySpecException, NoSuchProviderException {
		boolean allCannotTakeInitiatives = identifierPassordsAsker != null
				&& !((LoginData) this.mpasker.networkProperties.getAccessData(
						new InetSocketAddress(InetAddress.getByName("56.41.158.221"), 5000),
						new InetSocketAddress(InetAddress.getByName("192.168.0.55"), 5000))).canTakesLoginInitiative()
				&& !((LoginData) this.mpreceiver.networkProperties.getAccessData(
						new InetSocketAddress(InetAddress.getByName("56.41.158.221"), 5000),
						new InetSocketAddress(InetAddress.getByName("192.168.0.55"), 5000))).canTakesLoginInitiative();
		apasker = this.mpasker.networkProperties.getAccessProtocolProperties(new InetSocketAddress(InetAddress.getByName("56.41.158.221"), 5000),
				new InetSocketAddress(InetAddress.getByName("192.168.0.55"), 5000))
				.getAccessProtocolInstance(new InetSocketAddress(InetAddress.getByName("56.41.158.221"), 5000),
						new InetSocketAddress(InetAddress.getByName("192.168.0.55"), 5000), this, mpasker);
		apreceiver = this.mpreceiver.networkProperties.getAccessProtocolProperties(new InetSocketAddress(InetAddress.getByName("56.41.158.221"), 5000),
				new InetSocketAddress(InetAddress.getByName("192.168.0.55"), 5000))
				.getAccessProtocolInstance(new InetSocketAddress(InetAddress.getByName("56.41.158.221"), 5000),
				new InetSocketAddress(InetAddress.getByName("192.168.0.55"), 5000), this, mpreceiver);
		apasker.setKernelAddress(KernelAddressTest.getKernelAddressInstance());
		apreceiver.setKernelAddress(KernelAddressTest.getKernelAddressInstance());

		Assert.assertFalse(apasker.isAccessFinalized());
		Assert.assertFalse(apreceiver.isAccessFinalized());

		AccessMessage masker = apasker.setAndGetNextMessage(new AccessAskInitiliazation());
		AccessMessage mreceiver = apreceiver.setAndGetNextMessage(new AccessAskInitiliazation());
		boolean askerAsNotifiedGroupsChangements = false;
		boolean receiverAsNotifiedGroupsChangements = false;
		int cycles = 0;
		do {
			/*
			 * System.out.println("asker="+masker);
			 * System.out.println("receiver="+mreceiver);
			 */
			AccessMessage mreceiver2 = null;
			AccessMessage masker2 = null;

			if (cycles == index && asker && type == 1) {
				masker = new UnkownAccessMessage();
			}
			if (masker != null && !(masker instanceof DoNotSendMessage)) {
				if (cycles == index && asker && type == 2) {
					masker.corrupt();
				}

				masker = (AccessMessage) ConnectionsProtocolsTests
						.unserialize(ConnectionsProtocolsTests.serialize(masker));
				Assert.assertEquals(masker.checkDataIntegrity(), Integrity.OK);
				mreceiver2 = apreceiver.setAndGetNextMessage(masker);
				receiverAsNotifiedGroupsChangements |= apreceiver.isNotifyAccessGroupChangements();
			}
			if (cycles == index && !asker && type == 1) {
				mreceiver = new UnkownAccessMessage();
			}
			if (mreceiver != null && !(mreceiver instanceof DoNotSendMessage)) {
				if (cycles == index && !asker && type == 2) {
					mreceiver.corrupt();
				}
				mreceiver = (AccessMessage) ConnectionsProtocolsTests
						.unserialize(ConnectionsProtocolsTests.serialize(mreceiver));
				Assert.assertEquals(mreceiver.checkDataIntegrity(), Integrity.OK);
				masker2 = apasker.setAndGetNextMessage(mreceiver);
				askerAsNotifiedGroupsChangements |= apasker.isNotifyAccessGroupChangements();
			}
			mreceiver = mreceiver2;
			masker = masker2;
			int nberror = 0;
			if (mreceiver instanceof AccessErrorMessage)
				nberror++;
			if (masker instanceof AccessErrorMessage)
				nberror++;
			if (((mreceiver == null || masker == null) && nberror > 0) || nberror == 2) {
				/*
				 * if (masker!=null) {
				 * masker=(AccessMessage)ConnectionsProtocolsTests.unserialize(
				 * ConnectionsProtocolsTests.serialize(masker));
				 * Assert.assertEquals(masker.checkDataIntegrity(), Integrity.OK);
				 * mreceiver2=apreceiver.setAndGetNextMessage(masker); }
				 */
				mreceiver = null;
				masker = null;
			}
			cycles++;
		} while ((masker != null || mreceiver != null) && cycles < numberMaxExchange);
		Assert.assertTrue(cycles < numberMaxExchange);
		Assert.assertTrue(masker == null && mreceiver == null);
		if (allCannotTakeInitiatives || type == 1) {
			Assert.assertFalse(apreceiver.isAccessFinalized());
			Assert.assertFalse(apasker.isAccessFinalized());
			return -1;
		}
		Assert.assertTrue(apreceiver.isAccessFinalized());
		Assert.assertTrue(apasker.isAccessFinalized());
		if (!allCannotTakeInitiatives && type == 0) {
			Assert.assertTrue(askerAsNotifiedGroupsChangements);
			Assert.assertTrue(receiverAsNotifiedGroupsChangements);
		}
		testExpectedLogins();
		if (identifierPassordsAsker != null && ((LoginData) this.mpasker.networkProperties.getAccessData(
				new InetSocketAddress(InetAddress.getByName("56.41.158.221"), 5000),
				new InetSocketAddress(InetAddress.getByName("192.168.0.55"), 5000))).canTakesLoginInitiative()) {
			testAddingOneNewIdentifier(10);
			testAddingTwoNewIdentifier(11, 12, false);
			testAddingTwoNewIdentifier(13, 14, true);

			testRemovingOneNewIdentifier(10);
			testRemovingTwoNewIdentifier(11, 12, false);
			testRemovingTwoNewIdentifier(13, 14, true);
		}
		return cycles;
	}

	private void testAddingOneNewIdentifier(int newid) throws AccessException, ClassNotFoundException, IOException,
			NoSuchAlgorithmException, InvalidKeySpecException {
		IdentifierPassword idpwAsker = AccessDataMKEventListener
				.getIdentifierPassword(AccessDataMKEventListener.getCustumHostIdentifier(0), newid);
		IdentifierPassword idpwReceiver = AccessDataMKEventListener
				.getIdentifierPassword(AccessDataMKEventListener.getCustumHostIdentifier(1), newid);
		identifierPassordsAsker.add(idpwAsker);
		identifierPassordsReceiver.add(idpwReceiver);
		acceptedAskerIdentifiers.add(idpwAsker.getIdentifier());
		acceptedReceiverIdentifiers.add(idpwReceiver.getIdentifier());
		ArrayList<Identifier> addedForAsker = new ArrayList<>();
		ArrayList<Identifier> addedForReceiver = new ArrayList<>();
		addedForAsker.add(idpwAsker.getIdentifier());
		addedForReceiver.add(idpwReceiver.getIdentifier());
		testAddingNewIdentifier(addedForAsker, addedForReceiver);
	}

	private void testRemovingOneNewIdentifier(int newid) throws AccessException, ClassNotFoundException, IOException,
			NoSuchAlgorithmException, InvalidKeySpecException {
		IdentifierPassword idpwAsker = AccessDataMKEventListener
				.getIdentifierPassword(AccessDataMKEventListener.getCustumHostIdentifier(0), newid);
		IdentifierPassword idpwReceiver = AccessDataMKEventListener
				.getIdentifierPassword(AccessDataMKEventListener.getCustumHostIdentifier(1), newid);
		identifierPassordsAsker.remove(idpwAsker);
		identifierPassordsReceiver.remove(idpwReceiver);
		acceptedAskerIdentifiers.remove(idpwAsker.getIdentifier());
		acceptedReceiverIdentifiers.remove(idpwReceiver.getIdentifier());
		ArrayList<Identifier> addedForAsker = new ArrayList<>();
		ArrayList<Identifier> addedForReceiver = new ArrayList<>();
		addedForAsker.add(idpwAsker.getIdentifier());
		addedForReceiver.add(idpwReceiver.getIdentifier());
		testRemovingNewIdentifier(addedForAsker, addedForReceiver);
	}

	private void testAddingNewIdentifier(ArrayList<Identifier> addedForAsker, ArrayList<Identifier> addedForReceiver)
			throws AccessException, ClassNotFoundException, IOException, NoSuchAlgorithmException,
			InvalidKeySpecException {
		Assert.assertTrue(apasker.isAccessFinalized());
		Assert.assertTrue(apreceiver.isAccessFinalized());

		AccessMessage masker = apasker.setAndGetNextMessage(new NewLocalLoginAddedMessage(addedForAsker));
		AccessMessage mreceiver = null;
		testSubNewAddingRemovingIdentifier(masker, mreceiver);

	}

	private void testRemovingNewIdentifier(ArrayList<Identifier> addedForAsker, ArrayList<Identifier> addedForReceiver)
			throws AccessException, ClassNotFoundException, IOException, NoSuchAlgorithmException,
			InvalidKeySpecException {
		Assert.assertTrue(apasker.isAccessFinalized());
		Assert.assertTrue(apreceiver.isAccessFinalized());

		AccessMessage masker = apasker.setAndGetNextMessage(new NewLocalLoginRemovedMessage(addedForAsker));
		AccessMessage mreceiver = null;
		testSubNewAddingRemovingIdentifier(masker, mreceiver);
	}

	private void testAddingTwoNewIdentifier(int newid1, int newid2, boolean differed) throws AccessException,
			ClassNotFoundException, IOException, NoSuchAlgorithmException, InvalidKeySpecException {
		IdentifierPassword idpwAsker = AccessDataMKEventListener
				.getIdentifierPassword(AccessDataMKEventListener.getCustumHostIdentifier(0), newid1);
		IdentifierPassword idpwReceiver = AccessDataMKEventListener
				.getIdentifierPassword(AccessDataMKEventListener.getCustumHostIdentifier(1), newid1);
		identifierPassordsAsker.add(idpwAsker);
		identifierPassordsReceiver.add(idpwReceiver);
		acceptedAskerIdentifiers.add(idpwAsker.getIdentifier());
		acceptedReceiverIdentifiers.add(idpwReceiver.getIdentifier());
		ArrayList<Identifier> addedForAsker = new ArrayList<>();
		ArrayList<Identifier> addedForReceiver = new ArrayList<>();
		addedForAsker.add(idpwAsker.getIdentifier());
		addedForReceiver.add(idpwReceiver.getIdentifier());
		idpwAsker = AccessDataMKEventListener
				.getIdentifierPassword(AccessDataMKEventListener.getCustumHostIdentifier(0), newid1);
		idpwReceiver = AccessDataMKEventListener
				.getIdentifierPassword(AccessDataMKEventListener.getCustumHostIdentifier(1), newid1);
		identifierPassordsAsker.add(idpwAsker);
		identifierPassordsReceiver.add(idpwReceiver);
		acceptedAskerIdentifiers.add(idpwAsker.getIdentifier());
		acceptedReceiverIdentifiers.add(idpwReceiver.getIdentifier());
		addedForAsker.add(idpwAsker.getIdentifier());
		addedForReceiver.add(idpwReceiver.getIdentifier());

		if (differed)
			testDifferedAddingNewIdentifier(addedForAsker, addedForReceiver);
		else
			testAddingNewIdentifier(addedForAsker, addedForReceiver);
	}

	private void testRemovingTwoNewIdentifier(int newid1, int newid2, boolean differed) throws AccessException,
			ClassNotFoundException, IOException, NoSuchAlgorithmException, InvalidKeySpecException {
		IdentifierPassword idpwAsker = AccessDataMKEventListener
				.getIdentifierPassword(AccessDataMKEventListener.getCustumHostIdentifier(0), newid1);
		IdentifierPassword idpwReceiver = AccessDataMKEventListener
				.getIdentifierPassword(AccessDataMKEventListener.getCustumHostIdentifier(1), newid1);
		identifierPassordsAsker.remove(idpwAsker);
		identifierPassordsReceiver.remove(idpwReceiver);
		acceptedAskerIdentifiers.remove(idpwAsker.getIdentifier());
		acceptedReceiverIdentifiers.remove(idpwReceiver.getIdentifier());
		ArrayList<Identifier> addedForAsker = new ArrayList<>();
		ArrayList<Identifier> addedForReceiver = new ArrayList<>();
		addedForAsker.add(idpwAsker.getIdentifier());
		addedForReceiver.add(idpwReceiver.getIdentifier());
		idpwAsker = AccessDataMKEventListener
				.getIdentifierPassword(AccessDataMKEventListener.getCustumHostIdentifier(0), newid1);
		idpwReceiver = AccessDataMKEventListener
				.getIdentifierPassword(AccessDataMKEventListener.getCustumHostIdentifier(1), newid1);
		identifierPassordsAsker.remove(idpwAsker);
		identifierPassordsReceiver.remove(idpwReceiver);
		acceptedAskerIdentifiers.remove(idpwAsker.getIdentifier());
		acceptedReceiverIdentifiers.remove(idpwReceiver.getIdentifier());
		addedForAsker.add(idpwAsker.getIdentifier());
		addedForReceiver.add(idpwReceiver.getIdentifier());

		if (differed)
			testDifferedRemovingNewIdentifier(addedForAsker, addedForReceiver);
		else
			testRemovingNewIdentifier(addedForAsker, addedForReceiver);
	}

	private void testSubNewAddingRemovingIdentifier(AccessMessage masker, AccessMessage mreceiver)
			throws ClassNotFoundException, IOException, AccessException, NoSuchAlgorithmException,
			InvalidKeySpecException {
		int cycles = 0;
		do {

			if (masker != null && !(masker instanceof DoNotSendMessage)) {
				masker = (AccessMessage) ConnectionsProtocolsTests
						.unserialize(ConnectionsProtocolsTests.serialize(masker));
				Assert.assertEquals(masker.checkDataIntegrity(), Integrity.OK);
				mreceiver = apreceiver.setAndGetNextMessage(masker);
			}
			masker = null;
			if (mreceiver == null) {
				mreceiver = apreceiver.manageDifferedAccessMessage();
			}

			if (mreceiver != null && !(mreceiver instanceof DoNotSendMessage)) {
				mreceiver = (AccessMessage) ConnectionsProtocolsTests
						.unserialize(ConnectionsProtocolsTests.serialize(mreceiver));
				Assert.assertEquals(mreceiver.checkDataIntegrity(), Integrity.OK);
				masker = apasker.setAndGetNextMessage(mreceiver);
			}
			mreceiver = null;
			if (masker == null) {
				masker = apasker.manageDifferedAccessMessage();
			}
			cycles++;
		} while ((masker != null || mreceiver != null) && cycles < numberMaxExchange);
		Assert.assertTrue(cycles < numberMaxExchange);
		Assert.assertTrue(masker == null && mreceiver == null);
		Assert.assertTrue(apreceiver.isAccessFinalized());
		Assert.assertTrue(apasker.isAccessFinalized());
		testExpectedLogins();
	}

	private void testExpectedLogins() {
		Assert.assertEquals(acceptedAskerIdentifiers.size(), apasker.getAllAcceptedIdentifiers().size());
		for (PairOfIdentifiers poi : apasker.getAllAcceptedIdentifiers()) {
			boolean found = false;
			for (Identifier id : acceptedAskerIdentifiers) {
				if (id.equals(poi.getLocalIdentifier())) {
					found = true;
					break;
				}
			}
			Assert.assertTrue(found);
			found = false;
			for (Identifier id : acceptedReceiverIdentifiers) {
				if (id.equals(poi.getDistantIdentifier())) {
					found = true;
					break;
				}
			}
			Assert.assertTrue(found);
		}
		Assert.assertEquals(acceptedReceiverIdentifiers.size(), apreceiver.getAllAcceptedIdentifiers().size());
		for (PairOfIdentifiers poi : apreceiver.getAllAcceptedIdentifiers()) {
			boolean found = false;
			for (Identifier id : acceptedAskerIdentifiers) {
				if (id.equals(poi.getDistantIdentifier())) {
					found = true;
					break;
				}
			}
			Assert.assertTrue(found);
			found = false;
			for (Identifier id : acceptedReceiverIdentifiers) {
				if (id.equals(poi.getLocalIdentifier())) {
					found = true;
					break;
				}
			}
			Assert.assertTrue(found);
		}

	}

	private void testDifferedAddingNewIdentifier(ArrayList<Identifier> addedForAsker,
			ArrayList<Identifier> addedForReceiver) throws AccessException, ClassNotFoundException, IOException,
			NoSuchAlgorithmException, InvalidKeySpecException {
		Assert.assertTrue(apasker.isAccessFinalized());
		Assert.assertTrue(apreceiver.isAccessFinalized());

		AccessMessage masker = null;
		for (Identifier id : addedForAsker) {
			ArrayList<Identifier> l = new ArrayList<>();
			l.add(id);
			AccessMessage am = apasker.setAndGetNextMessage(new NewLocalLoginAddedMessage(l));
			if (masker == null)
				Assert.assertNotNull(masker = am);
			else
				Assert.assertNull(am);
		}
		AccessMessage mreceiver = null;
		testSubNewAddingRemovingIdentifier(masker, mreceiver);
	}

	private void testDifferedRemovingNewIdentifier(ArrayList<Identifier> addedForAsker,
			ArrayList<Identifier> addedForReceiver) throws AccessException, ClassNotFoundException, IOException,
			NoSuchAlgorithmException, InvalidKeySpecException {
		Assert.assertTrue(apasker.isAccessFinalized());
		Assert.assertTrue(apreceiver.isAccessFinalized());

		AccessMessage masker = null;
		for (Identifier id : addedForAsker) {
			ArrayList<Identifier> l = new ArrayList<>();
			l.add(id);
			AccessMessage am = apasker.setAndGetNextMessage(new NewLocalLoginRemovedMessage(l));
			if (masker == null)
				Assert.assertNotNull(masker = am);
			else
				Assert.assertNull(am);
		}
		AccessMessage mreceiver = null;
		testSubNewAddingRemovingIdentifier(masker, mreceiver);
	}

	@Override
	public void notifyNewAccessChangements() {
	}

	@Override
	public void addingIdentifier(Identifier _identifier) {

	}

	@Override
	public void addingIdentifiers(Collection<Identifier> _identifiers) {

	}

	@Override
	public void removingIdentifier(Identifier _identifier) {

	}

	@Override
	public void removingIdentifiers(Collection<Identifier> _identifiers) {

	}
}
