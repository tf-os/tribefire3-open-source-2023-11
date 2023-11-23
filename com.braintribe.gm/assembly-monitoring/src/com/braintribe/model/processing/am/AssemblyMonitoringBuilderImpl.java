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
package com.braintribe.model.processing.am;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.processing.session.api.notifying.NotifyingGmSession;
import com.braintribe.model.processing.session.impl.session.collection.EnhancedList;
import com.braintribe.model.processing.session.impl.session.collection.EnhancedMap;
import com.braintribe.model.processing.session.impl.session.collection.EnhancedSet;

/**
 * 
 */
class AssemblyMonitoringBuilderImpl implements AssemblyMonitoringBuilder {

	private boolean isAbsenceResolvable = false;
	private NotifyingGmSession session;

	@Override
	public void setIsAbsenceResolvable(boolean isAbsenceResolvable) {
		this.isAbsenceResolvable = isAbsenceResolvable;
	}

	@Override
	public boolean isAbsenceResolvable() {
		return isAbsenceResolvable;
	}

	@Override
	public NotifyingGmSession getSession() {
		return session;
	}

	@Override
	public AssemblyMonitoring build(NotifyingGmSession session, GenericEntity root) {
		this.session = session;

		return new AssemblyMonitoring(this, root);
	}

	@Override
	public AssemblyMonitoring build(NotifyingGmSession session, GenericEntity root, Property property) {
		this.session = session;

		return new AssemblyMonitoring(this, root, property);
	}

	@Override
	public AssemblyMonitoring build(NotifyingGmSession session, EnhancedList<?> list) {
		this.session = session;

		return new AssemblyMonitoring(this, list);
	}

	@Override
	public AssemblyMonitoring build(NotifyingGmSession session, EnhancedSet<?> set) {
		this.session = session;

		return new AssemblyMonitoring(this, set);
	}

	@Override
	public AssemblyMonitoring build(NotifyingGmSession session, EnhancedMap<?, ?> map) {
		this.session = session;

		return new AssemblyMonitoring(this, map);
	}

}
