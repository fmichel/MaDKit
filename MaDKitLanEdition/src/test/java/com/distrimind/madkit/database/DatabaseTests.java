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
package com.distrimind.madkit.database;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collection;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.distrimind.madkit.kernel.network.NetworkProperties;
import com.distrimind.ood.database.DatabaseConfiguration;
import com.distrimind.ood.database.DatabaseWrapper;
import com.distrimind.ood.database.EmbeddedHSQLDBWrapper;
import com.distrimind.ood.database.exceptions.DatabaseException;
import com.distrimind.util.crypto.ASymmetricEncryptionType;
import com.distrimind.util.crypto.ASymmetricKeyPair;
import com.distrimind.util.crypto.AbstractSecureRandom;
import com.distrimind.util.crypto.SecureRandomType;

import gnu.vm.jgnu.security.NoSuchAlgorithmException;
import gnu.vm.jgnu.security.NoSuchProviderException;

public class DatabaseTests {
	static final File databaseFile = new File("tmpfortest.database");
	static DatabaseWrapper databaseWrapper;
	static IPBanned ipbanned;
	static IPBanStat ipbanstat;
	static IPExpulsedStat ipExpulsedStat;
	static KeysPairs keysPairs;
	final Collection<InetAddress> whiteInetAddressesList = new NetworkProperties().getWhiteInetAddressesList();

	@BeforeClass
	public static void loadDatabase() throws IllegalArgumentException, DatabaseException {
		closeAndDeleleteDatabase();
		databaseWrapper = new EmbeddedHSQLDBWrapper(databaseFile);
		databaseWrapper.loadDatabase(new DatabaseConfiguration(KeysPairs.class.getPackage()), true);
		ipbanned = (IPBanned) databaseWrapper.getTableInstance(IPBanned.class);
		ipbanstat = (IPBanStat) databaseWrapper.getTableInstance(IPBanStat.class);
		ipExpulsedStat = (IPExpulsedStat) databaseWrapper.getTableInstance(IPExpulsedStat.class);
		keysPairs = (KeysPairs) databaseWrapper.getTableInstance(KeysPairs.class);
	}
	
	@AfterClass
	public static void closeAndDeleleteDatabase() {
		if (databaseWrapper != null)
			databaseWrapper.close();
		EmbeddedHSQLDBWrapper.deleteDatabaseFiles(databaseFile);
		databaseWrapper = null;
		ipbanned = null;
		ipbanstat = null;
		ipExpulsedStat = null;
		keysPairs = null;
	}

	@Test
	public void testKeysPairs() throws DatabaseException, UnknownHostException, InterruptedException,
			NoSuchAlgorithmException, NoSuchProviderException {
		InetAddress ia1 = InetAddress.getByName("192.168.0.15");
		InetAddress ia2 = InetAddress.getByName("0.15.100.100");
		AbstractSecureRandom random = SecureRandomType.DEFAULT.getSingleton(null);
		ASymmetricKeyPair kp1 = keysPairs.getKeyPair(ia1, (byte) 0, ASymmetricEncryptionType.DEFAULT, (short) 1024,
				random, 500, (short) 100);
		ASymmetricKeyPair kp2 = keysPairs.getKeyPair(ia1, (byte) 0, ASymmetricEncryptionType.DEFAULT, (short) 1024,
				random, 500, (short) 100);
		Assert.assertEquals(kp1, kp2);
		Thread.sleep(500);
		kp2 = keysPairs.getKeyPair(ia1, (byte) 0, ASymmetricEncryptionType.DEFAULT, (short) 1024, random, 500,
				(short) 100);
		Assert.assertNotEquals(kp1, kp2);
		kp1 = keysPairs.getKeyPair(ia2, (byte) 0, ASymmetricEncryptionType.DEFAULT, (short) 1024, random, 10000,
				(short) 100);
		Assert.assertNotEquals(kp1, kp2);
		kp2 = keysPairs.getKeyPair(ia2, (byte) 1, ASymmetricEncryptionType.DEFAULT, (short) 1024, random, 10000,
				(short) 100);
		Assert.assertNotEquals(kp1, kp2);
		kp1 = keysPairs.getKeyPair(ia2, (byte) 1, ASymmetricEncryptionType.DEFAULT, (short) 2048, random, 10000,
				(short) 100);
		Assert.assertNotEquals(kp1, kp2);
		kp2 = keysPairs.getKeyPair(ia2, (byte) 1, ASymmetricEncryptionType.DEFAULT, (short) 2048, random, 10000,
				(short) 100);
		Assert.assertEquals(kp1, kp2);
		kp1 = keysPairs.getNewKeyPair(ia2, (byte) 1, ASymmetricEncryptionType.DEFAULT, (short) 2048, random, 10000,
				(short) 100);
		Assert.assertNotEquals(kp1, kp2);
	}

	@Test
	public void testIPBAnned() throws DatabaseException, UnknownHostException, InterruptedException {

		InetAddress ia1 = InetAddress.getByName("192.168.0.15");
		InetAddress ia2 = InetAddress.getByName("0.15.0.2");
		Assert.assertFalse(ipbanstat.isBannedOrExpulsed(ia1, whiteInetAddressesList));
		Assert.assertFalse(ipbanstat.isBannedOrExpulsed(ia2, whiteInetAddressesList));
		ipbanstat.processExpulsion(ia1, false, 200, (short) 2, (short) 2, 200, (short) 1, (short) 2, 500, 500,
				whiteInetAddressesList);
		Assert.assertFalse(ipbanstat.isBannedOrExpulsed(ia1, whiteInetAddressesList));
		Assert.assertFalse(ipbanstat.isBannedOrExpulsed(ia2, whiteInetAddressesList));
		Assert.assertEquals(0, ipbanstat.getRecords().size());
		Assert.assertEquals(1, ipExpulsedStat.getRecords().size());
		Assert.assertEquals(0, ipbanned.getRecords().size());
		for (IPExpulsedStat.Record ipbs : ipExpulsedStat.getRecords()) {
			Assert.assertArrayEquals(ia1.getAddress(), ipbs.inet_address);
			Assert.assertTrue(ipbs.last_update_time < System.currentTimeMillis());
			Assert.assertEquals(0, ipbs.expulsed_number);
			Assert.assertEquals(1, ipbs.number_hits);
		}
		ipbanstat.processExpulsion(ia1, false, 200, (short) 2, (short) 2, 200, (short) 1, (short) 2, 500, 500,
				whiteInetAddressesList);
		Assert.assertTrue(ipbanstat.isBannedOrExpulsed(ia1, whiteInetAddressesList));
		Assert.assertFalse(ipbanstat.isBannedOrExpulsed(ia2, whiteInetAddressesList));
		Assert.assertEquals(0, ipbanstat.getRecords().size());
		Assert.assertEquals(1, ipExpulsedStat.getRecords().size());
		Assert.assertEquals(1, ipbanned.getRecords().size());
		for (IPExpulsedStat.Record ipbs : ipExpulsedStat.getRecords()) {
			Assert.assertArrayEquals(ia1.getAddress(), ipbs.inet_address);
			Assert.assertTrue(ipbs.last_update_time < System.currentTimeMillis());
			Assert.assertEquals(1, ipbs.expulsed_number);
			Assert.assertEquals(0, ipbs.number_hits);
		}
		for (IPBanned.Record ipb : ipbanned.getRecords()) {
			Assert.assertArrayEquals(ia1.getAddress(), ipb.inet_address);
			Assert.assertTrue(ipb.expiration_time < System.currentTimeMillis() + 200);
		}

		Thread.sleep(200);
		Assert.assertFalse(ipbanstat.isBannedOrExpulsed(ia1, whiteInetAddressesList));
		Assert.assertFalse(ipbanstat.isBannedOrExpulsed(ia2, whiteInetAddressesList));
		ipbanstat.processExpulsion(ia1, false, 200, (short) 2, (short) 2, 200, (short) 1, (short) 2, 500, 500,
				whiteInetAddressesList);
		ipbanstat.processExpulsion(ia1, false, 200, (short) 2, (short) 2, 200, (short) 1, (short) 2, 500, 500,
				whiteInetAddressesList);
		ipbanstat.processExpulsion(ia1, false, 200, (short) 2, (short) 2, 200, (short) 1, (short) 2, 500, 500,
				whiteInetAddressesList);
		ipbanstat.processExpulsion(ia1, false, 200, (short) 2, (short) 2, 200, (short) 1, (short) 2, 500, 500,
				whiteInetAddressesList);
		Assert.assertTrue(ipbanstat.isBannedOrExpulsed(ia1, whiteInetAddressesList));
		for (IPExpulsedStat.Record ipbs : ipExpulsedStat.getRecords()) {
			Assert.assertArrayEquals(ia1.getAddress(), ipbs.inet_address);
			Assert.assertTrue(ipbs.last_update_time < System.currentTimeMillis());
			Assert.assertEquals(2, ipbs.expulsed_number);
			Assert.assertEquals(0, ipbs.number_hits);
		}
		for (IPBanStat.Record ipbs : ipbanstat.getRecords()) {
			Assert.assertArrayEquals(ia1.getAddress(), ipbs.inet_address);
			Assert.assertTrue(ipbs.last_update_time < System.currentTimeMillis());
			Assert.assertEquals(1, ipbs.ban_number);
			Assert.assertEquals(0, ipbs.number_hits);
		}
		for (IPBanned.Record ipb : ipbanned.getRecords()) {
			Assert.assertArrayEquals(ia1.getAddress(), ipb.inet_address);
			Assert.assertTrue(ipb.expiration_time < System.currentTimeMillis() + 200);
		}
		Thread.sleep(500);
		ipbanstat.updateDatabase(500, 500, (short) 2, (short) 1);
		Assert.assertFalse(ipbanstat.isBannedOrExpulsed(ia1, whiteInetAddressesList));
		Assert.assertFalse(ipbanstat.isBannedOrExpulsed(ia2, whiteInetAddressesList));

		Assert.assertEquals(0, ipbanstat.getRecords().size());
		Assert.assertEquals(1, ipExpulsedStat.getRecords().size());
		Assert.assertEquals(0, ipbanned.getRecords().size());
		for (IPExpulsedStat.Record ipbs : ipExpulsedStat.getRecords()) {
			Assert.assertArrayEquals(ia1.getAddress(), ipbs.inet_address);
			Assert.assertTrue(ipbs.last_update_time < System.currentTimeMillis());
			Assert.assertEquals(1, ipbs.expulsed_number);
			Assert.assertEquals(1, ipbs.number_hits);
		}
		for (IPBanStat.Record ipbs : ipbanstat.getRecords()) {
			Assert.assertArrayEquals(ia1.getAddress(), ipbs.inet_address);
			Assert.assertTrue(ipbs.last_update_time < System.currentTimeMillis());
			Assert.assertEquals(0, ipbs.ban_number);
			Assert.assertEquals(0, ipbs.number_hits);
		}

		ipbanstat.processExpulsion(ia1, true, 200, (short) 2, (short) 2, 200, (short) 1, (short) 2, 500, 500,
				whiteInetAddressesList);
		for (IPExpulsedStat.Record ipbs : ipExpulsedStat.getRecords()) {
			Assert.assertArrayEquals(ia1.getAddress(), ipbs.inet_address);
			Assert.assertTrue(ipbs.last_update_time < System.currentTimeMillis());
			Assert.assertEquals(1, ipbs.expulsed_number);
			Assert.assertEquals(1, ipbs.number_hits);
		}
		for (IPBanStat.Record ipbs : ipbanstat.getRecords()) {
			Assert.assertArrayEquals(ia1.getAddress(), ipbs.inet_address);
			Assert.assertTrue(ipbs.last_update_time < System.currentTimeMillis());
			Assert.assertEquals(1, ipbs.ban_number);
			Assert.assertEquals(0, ipbs.number_hits);
		}
		for (IPBanned.Record ipb : ipbanned.getRecords()) {
			Assert.assertArrayEquals(ia1.getAddress(), ipb.inet_address);
			Assert.assertTrue(ipb.expiration_time < System.currentTimeMillis() + 200);
		}
		ipbanstat.processExpulsion(ia1, true, 200, (short) 2, (short) 2, 200, (short) 1, (short) 2, 500, 500,
				whiteInetAddressesList);
		for (IPExpulsedStat.Record ipbs : ipExpulsedStat.getRecords()) {
			Assert.assertArrayEquals(ia1.getAddress(), ipbs.inet_address);
			Assert.assertTrue(ipbs.last_update_time < System.currentTimeMillis());
			Assert.assertEquals(1, ipbs.expulsed_number);
			Assert.assertEquals(1, ipbs.number_hits);
		}
		for (IPBanStat.Record ipbs : ipbanstat.getRecords()) {
			Assert.assertArrayEquals(ia1.getAddress(), ipbs.inet_address);
			Assert.assertTrue(ipbs.last_update_time < System.currentTimeMillis());
			Assert.assertEquals(2, ipbs.ban_number);
			Assert.assertEquals(0, ipbs.number_hits);
		}
		for (IPBanned.Record ipb : ipbanned.getRecords()) {
			Assert.assertArrayEquals(ia1.getAddress(), ipb.inet_address);
			Assert.assertEquals(ipb.expiration_time, Long.MAX_VALUE);
		}

		Thread.sleep(500);
		Assert.assertTrue(ipbanstat.isBannedOrExpulsed(ia1, whiteInetAddressesList));
		Assert.assertFalse(ipbanstat.isBannedOrExpulsed(ia2, whiteInetAddressesList));
		ipbanstat.accept(ia1);
		Assert.assertFalse(ipbanstat.isBannedOrExpulsed(ia1, whiteInetAddressesList));
		Assert.assertFalse(ipbanstat.isBannedOrExpulsed(ia2, whiteInetAddressesList));
		Assert.assertEquals(0, ipbanstat.getRecords().size());
		Assert.assertEquals(0, ipExpulsedStat.getRecords().size());
		Assert.assertEquals(0, ipbanned.getRecords().size());

	}
}
