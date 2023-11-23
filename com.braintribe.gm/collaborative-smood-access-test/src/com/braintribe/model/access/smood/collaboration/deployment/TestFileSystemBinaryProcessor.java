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
package com.braintribe.model.access.smood.collaboration.deployment;

import static com.braintribe.utils.lcd.CollectionTools2.newList;
import static java.util.Collections.emptyList;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Objects;

import com.braintribe.logging.Logger;
import com.braintribe.model.accessapi.AccessRequest;
import com.braintribe.model.processing.accessrequest.api.AccessRequestContext;
import com.braintribe.model.processing.resource.persistence.BinaryPersistenceEventSource;
import com.braintribe.model.processing.resource.persistence.BinaryPersistenceListener;
import com.braintribe.model.resource.Resource;
import com.braintribe.model.resourceapi.persistence.BinaryPersistenceRequest;
import com.braintribe.model.resourceapi.persistence.DeleteBinary;
import com.braintribe.model.resourceapi.persistence.StoreBinary;

/**
 * @author peter.gazdik
 */
public class TestFileSystemBinaryProcessor implements BinaryPersistenceEventSource {

	private static final Logger log = Logger.getLogger(TestFileSystemBinaryProcessor.class);

	private List<BinaryPersistenceListener> persistenceListeners = emptyList();

	@Override
	public synchronized void addPersistenceListener(BinaryPersistenceListener listener) {
		Objects.requireNonNull(listener, "listener must not be null");

		List<BinaryPersistenceListener> newListeners = newList(persistenceListeners);
		newListeners.add(listener);

		persistenceListeners = newListeners;
	}

	@Override
	public synchronized void removePersistenceListener(BinaryPersistenceListener listener) {
		Objects.requireNonNull(listener, "listener must not be null");

		List<BinaryPersistenceListener> newListeners = newList(persistenceListeners);
		newListeners.remove(listener);

		persistenceListeners = newListeners;
	}

	public void onStore(String accessId, Resource resource) {
		StoreBinary request = StoreBinary.T.create();
		request.setDomainId(accessId);
		request.setCreateFrom(resource);

		notifyListenersOnStore(newAccessRequestContext(request), resource);
	}

	private void notifyListenersOnStore(AccessRequestContext<StoreBinary> context, Resource managedResource) {
		for (BinaryPersistenceListener listener : persistenceListeners) {
			try {
				listener.onStore(context, context.getOriginalRequest(), managedResource);

			} catch (Exception e) {
				log.warn("BinaryPersistenceListener threw an exception on store event.", e);
				e.printStackTrace();
			}
		}
	}

	public void onDelete(String accessId, Resource resource) {
		DeleteBinary request = DeleteBinary.T.create();
		request.setDomainId(accessId);
		request.setResource(resource);

		notifyListenersOnDelete(newAccessRequestContext(request));
	}

	private void notifyListenersOnDelete(AccessRequestContext<DeleteBinary> context) {
		for (BinaryPersistenceListener listener : persistenceListeners) {
			try {
				listener.onDelete(context, context.getOriginalRequest());

			} catch (Exception e) {
				log.warn("BinaryPersistenceListener threw an exception on store event.", e);
				e.printStackTrace();
			}
		}
	}

	private <P extends AccessRequest> AccessRequestContext<P> newAccessRequestContext(BinaryPersistenceRequest request) {
		return (AccessRequestContext<P>) Proxy.newProxyInstance( //
				getClass().getClassLoader(), //
				new Class<?>[] { AccessRequestContext.class }, //
				(proxy, method, args) -> handleInvoke(method, request));
	}

	private Object handleInvoke(Method method, BinaryPersistenceRequest request) {
		if (method.getName().equals("getOriginalRequest"))
			return request;
		else
			throw new UnsupportedOperationException("Method " + method.getName() + " is not supported by this proxy!");
	}

}
