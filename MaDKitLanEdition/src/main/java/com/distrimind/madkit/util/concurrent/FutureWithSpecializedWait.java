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
package com.distrimind.madkit.util.concurrent;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 
 * @author Jason Mahdjoub
 * @version 1.0
 * @since MadkitLanEdition 1.0
 * 
 * @param <V> a type
 */
public class FutureWithSpecializedWait<V> implements Future<V> {
	final Future<V> parentFuture;
	private final ThreadPoolExecutor serviceExecutor;
	private final ThreadPoolExecutor lifeExecutor;

	public FutureWithSpecializedWait(ThreadPoolExecutor serviceExecutor, ThreadPoolExecutor lifeExecutor,
			Future<V> future) {
		if (serviceExecutor == null)
			throw new NullPointerException("serviceExecutor");
		if (lifeExecutor == null)
			throw new NullPointerException("lifeExecutor");
		if (future == null)
			throw new NullPointerException("future");
		if (future instanceof FutureWithSpecializedWait)
			throw new IllegalArgumentException("future cannot be a FutureWithSpecializedWait");
		this.parentFuture = future;
		this.serviceExecutor = serviceExecutor;
		this.lifeExecutor = lifeExecutor;
	}

	@Override
	public boolean cancel(boolean _mayInterruptIfRunning) {
		return parentFuture.cancel(_mayInterruptIfRunning);
	}

	@Override
	public boolean isCancelled() {
		return parentFuture.isCancelled();
	}

	@Override
	public boolean isDone() {
		return parentFuture.isDone();
	}

	@Override
	public V get() throws InterruptedException, ExecutionException {
		AtomicReference<V> result = new AtomicReference<>(null);

		if (lifeExecutor.futureGet(parentFuture, result) || serviceExecutor.futureGet(parentFuture, result))
			return result.get();
		else {
			/*
			 * ArrayList<ThreadPoolExecutor> tpes=null; synchronized(map) { if
			 * (map.size()>0) { tpes=new ArrayList<ThreadPoolExecutor>(map.size());
			 * tpes.addAll(map.values()); } } if (tpes!=null) { for (ThreadPoolExecutor tpe
			 * : tpes) { if (tpe.futureGet(parentFuture, result)) return result.get(); } }
			 */
			return parentFuture.get();
		}
	}

	@Override
	public V get(long _timeout, TimeUnit _unit) throws InterruptedException, ExecutionException, TimeoutException {
		AtomicReference<V> result = new AtomicReference<>(null);

		if (lifeExecutor.futureGet(parentFuture, result, _timeout, _unit)
				|| serviceExecutor.futureGet(parentFuture, result, _timeout, _unit))
			return result.get();
		else {
			/*
			 * ArrayList<ThreadPoolExecutor> tpes=null; synchronized(map) { if
			 * (map.size()>0) { tpes=new ArrayList<ThreadPoolExecutor>(map.size());
			 * tpes.addAll(map.values()); } } if (tpes!=null) { for (ThreadPoolExecutor tpe
			 * : tpes) { if (tpe.futureGet(parentFuture, result, _timeout, _unit)) return
			 * result.get(); } }
			 */
			return parentFuture.get(_timeout, _unit);
		}
	}

}
