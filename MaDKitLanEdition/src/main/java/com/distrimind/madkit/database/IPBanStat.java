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

import java.net.InetAddress;
import java.util.Collection;
import java.util.HashMap;

import com.distrimind.ood.database.AlterRecordFilter;
import com.distrimind.ood.database.DatabaseRecord;
import com.distrimind.ood.database.Filter;
import com.distrimind.ood.database.SynchronizedTransaction;
import com.distrimind.ood.database.Table;
import com.distrimind.ood.database.TransactionIsolation;
import com.distrimind.ood.database.annotations.Field;
import com.distrimind.ood.database.annotations.NotNull;
import com.distrimind.ood.database.annotations.PrimaryKey;
import com.distrimind.ood.database.exceptions.DatabaseException;

/**
 * 
 * @author Jason Mahdjoub
 * @version 1.0
 * @since MadkitLanEdition 1.0
 */
public final class IPBanStat extends Table<IPBanStat.Record> {

	protected IPBanStat() throws DatabaseException {
		super();
	}

	public static class Record extends DatabaseRecord {
		public @NotNull @PrimaryKey(limit=20) byte[] inet_address;
		public @Field long last_update_time;
		// public @Field long time_accumulation;
		public @Field short number_hits;
		public @Field short ban_number;
	}

	public void updateDatabase(final long stat_duration_for_expulsion, final long stat_duration_for_banishments,
			final short expulsion_hit_limit, final short ban_hit_limit) throws DatabaseException {
		final IPBanned ipb = (IPBanned) this.getDatabaseWrapper().getTableInstance(IPBanned.class);
		final IPExpulsedStat ipes = (IPExpulsedStat) this.getDatabaseWrapper().getTableInstance(IPExpulsedStat.class);
		try {
			this.getDatabaseWrapper().runSynchronizedTransaction(new SynchronizedTransaction<Void>() {

				@Override
				public Void run() throws Exception {
					ipb.removeRecords(new Filter<IPBanned.Record>() {

						@Override
						public boolean nextRecord(IPBanned.Record _record) {
							if (_record.expiration_time < System.currentTimeMillis()) {
								return true;
							}
							return false;
						}
					});
					ipes.updateRecords(new AlterRecordFilter<IPExpulsedStat.Record>() {

						@Override
						public void nextRecord(IPExpulsedStat.Record _record) {

							long current_time = System.currentTimeMillis();
							long delta = current_time - _record.last_update_time;
							if (delta > stat_duration_for_expulsion) {
								int nbStatDuration = (int) (delta / stat_duration_for_expulsion);
								_record.last_update_time = _record.last_update_time
										+ nbStatDuration * stat_duration_for_expulsion;

								_record.expulsed_number = (short) (_record.expulsed_number
										- (nbStatDuration / expulsion_hit_limit));
								_record.number_hits = (short) (_record.number_hits
										- (nbStatDuration % expulsion_hit_limit));
								if (_record.number_hits < 0) {
									_record.expulsed_number -= 1;
									_record.number_hits += expulsion_hit_limit;
								}
								if (_record.expulsed_number < 0) {
									_record.expulsed_number = 0;
									_record.number_hits = 0;
								}

								if (_record.number_hits <= 0 && _record.expulsed_number <= 0) {
									this.remove();
								} else
									this.update();
							}
						}
					});
					IPBanStat.this.updateRecords(new AlterRecordFilter<IPBanStat.Record>() {

						@Override
						public void nextRecord(IPBanStat.Record _record) {
							long current_time = System.currentTimeMillis();
							long delta = current_time - _record.last_update_time;
							if (delta > stat_duration_for_expulsion) {
								int nbStatDuration = (int) (delta / stat_duration_for_expulsion);
								_record.last_update_time = _record.last_update_time
										+ nbStatDuration * stat_duration_for_expulsion;

								_record.ban_number = (short) (_record.ban_number - (nbStatDuration / ban_hit_limit));
								_record.number_hits = (short) (_record.number_hits - (nbStatDuration % ban_hit_limit));
								if (_record.number_hits < 0) {
									_record.ban_number -= 1;
									_record.number_hits += expulsion_hit_limit;
								}
								if (_record.ban_number < 0) {
									_record.ban_number = 0;
									_record.number_hits = 0;
								}

								if (_record.number_hits <= 0 && _record.ban_number <= 0) {
									this.remove();
								} else
									this.update();
							}
						}
					});

					return null;
				}

				@Override
				public TransactionIsolation getTransactionIsolation() {
					return TransactionIsolation.TRANSACTION_SERIALIZABLE;
				}

				@Override
				public boolean doesWriteData() {
					return true;
				}
			});
		} catch (Exception e) {
			throw DatabaseException.getDatabaseException(e);
		}

	}

	public IPBanned.Record processExpulsion(final InetAddress add, final boolean candidate_to_ban,
			final long expulsion_duration, final short expulsion_hit_limit, final short expulsion_limit,
			final long ban_duration, final short ban_hit_limit, final short ban_limit,
			final long stat_duration_for_expulsion, final long stat_duration_for_banishments,
			Collection<InetAddress> whiteInetAddressesList) throws DatabaseException {
		return this.processExpulsion((short) 1, add, candidate_to_ban, expulsion_duration, expulsion_hit_limit,
				expulsion_limit, ban_duration, ban_hit_limit, ban_limit, stat_duration_for_expulsion,
				stat_duration_for_banishments, whiteInetAddressesList);
	}

	public IPBanned.Record processExpulsion(final short nbAno, final InetAddress add, final boolean candidateToBan,
			final long expulsion_duration, final short expulsion_hit_limit, final short expulsion_limit,
			final long ban_duration, final short ban_hit_limit, final short ban_limit,
			final long stat_duration_for_expulsion, final long stat_duration_for_banishments,
			Collection<InetAddress> whiteInetAddressesList) throws DatabaseException {
		if (nbAno <= 0)
			return null;
		if (whiteInetAddressesList.contains(add))
			return null;
		try {
			return this.getDatabaseWrapper().runSynchronizedTransaction(new SynchronizedTransaction<IPBanned.Record>() {

				@Override
				public IPBanned.Record run() throws DatabaseException {
					boolean candidate_to_ban = candidateToBan;
					updateDatabase(stat_duration_for_expulsion, stat_duration_for_banishments, expulsion_hit_limit,
							ban_hit_limit);

					final IPBanned ipb = (IPBanned) IPBanStat.this.getDatabaseWrapper()
							.getTableInstance(IPBanned.class);
					final IPExpulsedStat ipes = (IPExpulsedStat) IPBanStat.this.getDatabaseWrapper()
							.getTableInstance(IPExpulsedStat.class);

					byte key[] = add.getAddress();
					IPBanned.Record ripbstat = ipb.getRecord(new Object[] { "inet_address", key });
					if (ripbstat != null && ripbstat.expiration_time == Long.MAX_VALUE)
						return ripbstat;
					IPExpulsedStat.Record ripesstat = candidate_to_ban ? null
							: ipes.getRecord(new Object[] { "inet_address", key });

					short nbAnomalies = nbAno;
					/*
					 * if (candidate_to_ban) nbAnomalies+=expulsion_hit_limit;
					 */

					if (!candidate_to_ban) {
						if (ripesstat != null) {
							int total = (ripesstat.number_hits + nbAno) / expulsion_hit_limit
									+ ripesstat.expulsed_number;
							if (total >= expulsion_limit) {
								ripesstat.last_update_time = System.currentTimeMillis();
								ripesstat.expulsed_number = expulsion_limit;
								ripesstat.number_hits = (short) ((ripesstat.number_hits + nbAno) % expulsion_hit_limit);
								ipes.updateRecord(ripesstat);
								candidate_to_ban = true;
								nbAnomalies = (short) (total - expulsion_hit_limit);
							}
						} else {
							int total = nbAno / expulsion_hit_limit;
							if (total >= expulsion_limit) {
								ripesstat = new IPExpulsedStat.Record();
								ripesstat.inet_address = key;
								ripesstat.last_update_time = System.currentTimeMillis();
								ripesstat.expulsed_number = expulsion_limit;
								ripesstat.number_hits = (short) (nbAno % expulsion_hit_limit);
								ipes.addRecord(ripesstat);
								candidate_to_ban = true;
								nbAnomalies = (short) (total - expulsion_hit_limit);
							}

						}
					}

					if (candidate_to_ban) {
						IPBanStat.Record ripbsstat = IPBanStat.this.getRecord(new Object[] { "inet_address", key });
						boolean toAdd = false;
						if (ripbsstat == null) {
							ripbsstat = new IPBanStat.Record();
							ripbsstat.inet_address = key;
							ripbsstat.number_hits = 0;
							ripbsstat.ban_number = 0;
							toAdd = true;
						}
						int oldBanNumber = ripbsstat.ban_number;
						ripbsstat.number_hits = (short) (ripbsstat.number_hits + nbAnomalies % ban_hit_limit);
						ripbsstat.ban_number = (short) (ripbsstat.ban_number + nbAnomalies / ban_hit_limit);
						if (ripbsstat.number_hits >= ban_hit_limit) {
							ripbsstat.number_hits -= ban_hit_limit;
							ripbsstat.ban_number++;
						}
						ripbsstat.last_update_time = System.currentTimeMillis();
						if (toAdd)
							ripbsstat = IPBanStat.this.addRecord(ripbsstat);
						else
							IPBanStat.this.updateRecord(ripbsstat);
						if (oldBanNumber < ripbsstat.ban_number) {
							long expirationTime;
							if (ban_limit <= ripbsstat.ban_number)
								expirationTime = Long.MAX_VALUE;
							else
								expirationTime = System.currentTimeMillis() + ban_duration;
							if (ripbstat == null) {
								ripbstat = new IPBanned.Record();
								ripbstat.expiration_time = expirationTime;
								ripbstat.inet_address = key;
								ripbstat = ipb.addRecord(ripbstat);
							} else if (ripbstat.expiration_time < expirationTime) {
								ripbstat.expiration_time = expirationTime;
								ipb.updateRecord(ripbstat);
							}
						}
					} else {
						boolean toAdd = false;
						if (ripesstat == null) {
							ripesstat = new IPExpulsedStat.Record();
							ripesstat.inet_address = key;
							ripesstat.number_hits = 0;
							ripesstat.expulsed_number = 0;
							toAdd = true;
						}
						int oldExpulNumber = ripesstat.expulsed_number;
						ripesstat.number_hits = (short) (ripesstat.number_hits + nbAnomalies % expulsion_hit_limit);
						ripesstat.expulsed_number = (short) (ripesstat.expulsed_number
								+ nbAnomalies / expulsion_hit_limit);
						if (ripesstat.number_hits >= expulsion_hit_limit) {
							ripesstat.number_hits -= expulsion_hit_limit;
							ripesstat.expulsed_number++;
						}
						ripesstat.last_update_time = System.currentTimeMillis();
						if (toAdd)
							ripesstat = ipes.addRecord(ripesstat);
						else
							ipes.updateRecord(ripesstat);
						if (oldExpulNumber < ripesstat.expulsed_number) {
							if (ripbstat == null) {
								ripbstat = new IPBanned.Record();
								ripbstat.expiration_time = System.currentTimeMillis() + expulsion_duration;
								ripbstat.inet_address = key;
								ripbstat = ipb.addRecord(ripbstat);
							} else if (ripbstat.expiration_time < System.currentTimeMillis() + expulsion_duration) {
								ripbstat.expiration_time = System.currentTimeMillis() + expulsion_duration;
								ipb.updateRecord(ripbstat);
							}
						}
					}

					return ripbstat;
				}

				@Override
				public TransactionIsolation getTransactionIsolation() {
					return TransactionIsolation.TRANSACTION_SERIALIZABLE;
				}

				@Override
				public boolean doesWriteData() {
					return true;
				}
			});
		} catch (Exception e) {
			throw DatabaseException.getDatabaseException(e);
		}

	}

	public boolean isBannedOrExpulsed(final InetAddress inet_address, Collection<InetAddress> whiteInetAddressesList)
			throws DatabaseException {
		try {
			if (whiteInetAddressesList.contains(inet_address))
				return false;
			return getDatabaseWrapper().runSynchronizedTransaction(new SynchronizedTransaction<Boolean>() {

				@Override
				public Boolean run() throws Exception {
					final IPBanned ipb = (IPBanned) IPBanStat.this.getDatabaseWrapper()
							.getTableInstance(IPBanned.class);
					HashMap<String, Object> hm = new HashMap<>();
					hm.put("inet_address", inet_address.getAddress());
					IPBanned.Record r = ipb.getRecord(hm);
					if (r == null) {
						return new Boolean(false);
					}
					return new Boolean(r.expiration_time > System.currentTimeMillis());
				}

				@Override
				public TransactionIsolation getTransactionIsolation() {
					return TransactionIsolation.TRANSACTION_READ_COMMITTED;
				}

				@Override
				public boolean doesWriteData() {
					return false;
				}
			}).booleanValue();
		} catch (Exception e) {
			throw DatabaseException.getDatabaseException(e);
		}

	}

	public void accept(final InetAddress inet_address) throws DatabaseException {
		try {
			this.getDatabaseWrapper().runSynchronizedTransaction(new SynchronizedTransaction<Void>() {

				@Override
				public Void run() throws Exception {
					HashMap<String, Object> hm = new HashMap<String, Object>();
					hm.put("inet_address", inet_address.getAddress());
					Record r1 = getRecord(hm);
					if (r1 != null)
						removeRecord(r1);
					final IPBanned ipb = (IPBanned) IPBanStat.this.getDatabaseWrapper()
							.getTableInstance(IPBanned.class);
					IPBanned.Record r2 = ipb.getRecord(hm);
					if (r2 != null)
						ipb.removeRecord(r2);
					final IPExpulsedStat ipes = (IPExpulsedStat) IPBanStat.this.getDatabaseWrapper()
							.getTableInstance(IPExpulsedStat.class);
					IPExpulsedStat.Record r3 = ipes.getRecord(hm);
					if (r3 != null)
						ipes.removeRecord(r3);
					return null;
				}

				@Override
				public TransactionIsolation getTransactionIsolation() {
					return TransactionIsolation.TRANSACTION_REPEATABLE_READ;
				}

				@Override
				public boolean doesWriteData() {
					return true;
				}
			});
		} catch (Exception e) {
			throw DatabaseException.getDatabaseException(e);
		}
	}
}
