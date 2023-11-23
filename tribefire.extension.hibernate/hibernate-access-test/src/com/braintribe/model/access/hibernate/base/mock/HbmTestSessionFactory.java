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
package com.braintribe.model.access.hibernate.base.mock;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.hibernate.SessionFactory;

import com.braintribe.model.access.hibernate.HibernateAccess;

/**
 * {@link SessionFactory} wrapper used in {@link HibernateAccess} tests which either acts as a regular SessionFactory or prevents the session from
 * persisting any data and instead runs in a single transaction which can be rolled back using the {@link #reset()} method. This process of starting a
 * transaction and rolling it back can be repeated any number of times.
 * 
 * @see NonCommittingSessionFactory
 * 
 * @author peter.gazdik
 */
public interface HbmTestSessionFactory extends SessionFactory {

	void reset();

	static HbmTestSessionFactory newInstance(SessionFactory delegate, boolean committing) {
		return committing ? newCommitting(delegate) : newNonCommitting(delegate);
	}

	static HbmTestSessionFactory newCommitting(SessionFactory delegate) {
		Class<?>[] ifaces = { HbmTestSessionFactory.class };
		InvocationHandler handler = new CommittingSessionFactoryHandler(delegate);

		return (NonCommittingSessionFactory) Proxy.newProxyInstance(HbmTestSessionFactory.class.getClassLoader(), ifaces, handler);
	}

	static HbmTestSessionFactory newNonCommitting(SessionFactory delegate) {
		return NonCommittingSessionFactory.newInstance(delegate);
	}

}

class CommittingSessionFactoryHandler implements InvocationHandler {

	private final SessionFactory delegate;

	public CommittingSessionFactoryHandler(SessionFactory delegate) {
		this.delegate = delegate;
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		switch (method.getName()) {
			case "reset":
				return null;
			default:
				return method.invoke(delegate, args);
		}
	}

}