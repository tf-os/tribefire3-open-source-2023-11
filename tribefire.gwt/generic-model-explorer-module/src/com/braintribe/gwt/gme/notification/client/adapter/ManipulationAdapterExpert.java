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
package com.braintribe.gwt.gme.notification.client.adapter;

import java.util.HashSet;
import java.util.Set;

import com.braintribe.logging.Logger;
import com.braintribe.model.generic.manipulation.Manipulation;
import com.braintribe.model.generic.tracking.ManipulationListener;

/**
 * A type parameterized version of the DelegatingManipulationListener.
 * 
 */
public class ManipulationAdapterExpert<M extends Manipulation> implements ManipulationListener {

	private static final Logger logger = Logger.getLogger(ManipulationAdapterExpert.class);
	private Set<ManipulationAdapterListener<M>> delegates = new HashSet<>();

	@Override
	public void noticeManipulation(Manipulation manipulation) {
		M m = (M) manipulation;

		Set<ManipulationAdapterListener<M>> delegates = new HashSet<>(this.delegates);
		for (ManipulationAdapterListener<M> listenerDelegate : delegates) {
			try {
				listenerDelegate.noticeManipulation(m);
			} catch (Exception e) {
				logger.error("error while delegating manipulation", e);
				e.printStackTrace();
			}
		}
	}

	public boolean addDelegate(ManipulationAdapterListener<M> listener) {
		return delegates.add(listener);
	}

	public boolean removeDelegate(ManipulationAdapterListener<M> listener) {
		return delegates.remove(listener);
	}

	public Set<ManipulationAdapterListener<M>> getDelegates() {
		return delegates;
	}

	public void clearDelegates() {
		delegates.clear();
	}

}
