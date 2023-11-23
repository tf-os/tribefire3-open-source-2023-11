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
package tribefire.platform.impl.deployment;

import java.util.function.Supplier;

import com.braintribe.model.deployment.HardwiredDeployable;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.processing.session.api.managed.ManagedGmSession;
import com.braintribe.utils.StringTools;
import com.braintribe.wire.api.scope.InstanceQualification;

public class HardwiredComponent<D extends HardwiredDeployable, E> {
	private final Supplier<E> expertSupplier;
	private final D deployable;
	private final String idSuffix;

	public HardwiredComponent(Supplier<E> expertSupplier, EntityType<D> deployableType, InstanceQualification qualification) {
		this.expertSupplier = expertSupplier;
		this.idSuffix = StringTools.camelCaseToDashSeparated(qualification.space().getClass().getSimpleName()) + "."
				+ StringTools.camelCaseToDashSeparated(qualification.name());
		this.deployable = createDeployable(deployableType);
	}

	private D createDeployable(EntityType<D> deployableType) {
		D deployable = deployableType.create();
		deployable.setExternalId("hardwired." + idSuffix);
		deployable.setGlobalId("hardwired:" + idSuffix);
		return deployable;
	}

	public String getIdSuffix() {
		return idSuffix;
	}

	public D getTransientDeployable() {
		return deployable;
	}

	public D lookupDeployable(ManagedGmSession session) {
		return session.query().getEntity(deployable.getGlobalId());
	}

	public Supplier<E> getExpertSupplier() {
		return expertSupplier;
	}
}
