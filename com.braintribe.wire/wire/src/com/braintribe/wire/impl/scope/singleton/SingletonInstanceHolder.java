// ============================================================================
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2022
// 
// This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
// 
// This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
// 
// You should have received a copy of the GNU Lesser General Public License along with this library; See http://www.gnu.org/licenses/.
// ============================================================================
package com.braintribe.wire.impl.scope.singleton;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

import com.braintribe.wire.api.scope.InstanceHolder;
import com.braintribe.wire.api.scope.InstanceHolderSupplier;
import com.braintribe.wire.api.scope.WireScope;
import com.braintribe.wire.api.space.WireSpace;

public class SingletonInstanceHolder extends AbstractSingletonInstanceHolder implements InstanceHolderSupplier {
	public static final int STATUS_CREATION_FAILED = -1;
	public static final int STATUS_EMPTY = 0;
	public static final int STATUS_INSTANTIATED = 1;
	public static final int STATUS_INITIALIZED = 2;
	
	private Object bean;
	private volatile int status = STATUS_EMPTY;
	private Lock lock = new ReentrantLock();
	private Consumer<? super SingletonInstanceHolder> publishNotifier;

	public SingletonInstanceHolder(WireSpace space, WireScope scope, String name, Consumer<? super SingletonInstanceHolder> publishNotifier) {
		super(space, scope, name);
		this.publishNotifier = publishNotifier;
	}
	
	@Override
	public InstanceHolder getHolder(Object context) {
		return this;
	}
	
	public boolean isInitialized() {
		return status == STATUS_INITIALIZED; 
	}

	@Override
	public Object get() {
		return bean;
	}

	@Override
	public void publish(Object bean) {
		if (status > STATUS_EMPTY)
			return;
		
		this.bean = bean;
		this.status = STATUS_INSTANTIATED;
		publishNotifier.accept(this);
	}

	@Override
	public boolean lockCreation() {
		if (status == STATUS_INITIALIZED) 
			return false;
		
		lock.lock();

		if (status > STATUS_EMPTY) {
			lock.unlock();
			return false;
		} 
		return true;

	}
	
	@Override
	public void onCreationFailure(Throwable t) {
		status = STATUS_CREATION_FAILED;
		bean = null;
	}

	@Override
	public void unlockCreation() {
		status = status == STATUS_INSTANTIATED? STATUS_INITIALIZED: STATUS_EMPTY;
		lock.unlock();
	}
	
	@Override
	public String toString() {
		return "SingletonBeanHolder[name=" + name + "]";
	}

}
