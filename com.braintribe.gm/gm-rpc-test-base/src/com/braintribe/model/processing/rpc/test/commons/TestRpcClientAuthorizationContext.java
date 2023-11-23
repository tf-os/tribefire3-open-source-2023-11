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
package com.braintribe.model.processing.rpc.test.commons;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.braintribe.model.processing.webrpc.client.GmWebRpcClientAuthorizationContext;

/**
 * A {@link GmWebRpcClientAuthorizationContext} which keeps the notified authorization failures in a ThreadLocal.
 */
public class TestRpcClientAuthorizationContext extends GmWebRpcClientAuthorizationContext {

	private static ThreadLocal<Set<Throwable>> failures = new ThreadLocal<Set<Throwable>>();

	@Override
	public void onAuthorizationFailure(Throwable authorizationFailure) {

		super.onAuthorizationFailure(authorizationFailure);

		if (failures.get() == null) {
			Set<Throwable> set = new HashSet<>();
			set.add(authorizationFailure);
			failures.set(set);
		} else {
			failures.get().add(authorizationFailure);
		}

	}

	public Set<Throwable> getNotifiedFailures() {
		Set<Throwable> set = failures.get();
		if (set == null) {
			set = Collections.emptySet();
		}
		return set;
	}

}
