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
package com.braintribe.product.rat.imp;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;

/**
 * A MultiImp follows the same rules as the {@link AbstractImp imp}, but can manage a list of instances instead of just
 * one. It can hold zero up to multiple instances.
 *
 * @param <T>
 *            Type of the instances that will be managed by this imp
 * @param <I>
 *            Type of the imps that will manage these instances
 */
public class GenericMultiImp<T extends GenericEntity, I extends Imp<T>> extends AbstractHasSession implements Iterable<I> {

	private final List<I> memberImps;

	public GenericMultiImp(PersistenceGmSession session, Collection<I> memberImps) {
		super(session);
		this.memberImps = new ArrayList<I>(memberImps);
	}

	/**
	 * @return the list of instances managed by this multi-imp
	 */
	public List<T> get() {
		return memberImps.stream().map(imp -> imp.get()).collect(Collectors.toList());
	}

	/**
	 * deletes all the instances/entities managed by this multi-imp from the access/session
	 */
	public void delete() {
		memberImps.stream().forEach(imp -> imp.delete());
	}

	@Override
	public Iterator<I> iterator() {
		return memberImps.iterator();
	}
}
