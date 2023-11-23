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
package tribefire.cortex.initializer.support.impl;

import java.util.Map;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.manipulation.InstantiationManipulation;
import com.braintribe.model.generic.manipulation.Manipulation;
import com.braintribe.model.generic.manipulation.ManipulationType;
import com.braintribe.model.generic.tracking.ManipulationListener;
import com.braintribe.model.processing.session.api.managed.ManagedGmSession;
import com.braintribe.utils.collection.api.MultiMap;
import com.braintribe.utils.collection.impl.HashMultiMap;
import com.braintribe.wire.api.context.InstancePath;
import com.braintribe.wire.api.context.WireContext;
import com.braintribe.wire.api.module.WireModule;
import com.braintribe.wire.api.scope.InstanceQualification;

public class GlobalIdManager implements ManipulationListener, AutoCloseable {

	private MultiMap<InstanceQualification, GenericEntity> managedInstanceEntities = new HashMultiMap<>(true);
	
	private String initializerId;
	private ManagedGmSession session;
	private WireContext<?> wireContext;
	
	public GlobalIdManager(String initializerId, ManagedGmSession session, WireContext<?> wireContext) {
		super();
		this.initializerId = initializerId;
		this.session = session;
		this.wireContext = wireContext;
		
		session.listeners().add(this);
	}

	@Override
	public void noticeManipulation(Manipulation manipulation) {
		if (manipulation.manipulationType() == ManipulationType.INSTANTIATION) {
			InstantiationManipulation instanceManipulation = (InstantiationManipulation)manipulation;
			GenericEntity entity = instanceManipulation.getEntity();
			
			if (entity.getGlobalId() == null) {
				InstancePath currentInstancePath = wireContext.currentInstancePath();
				
				if (currentInstancePath.length() > 0) {
					InstanceQualification instanceQualification = currentInstancePath.lastElement();
					this.managedInstanceEntities.put(instanceQualification, entity);
				}
			}
		}
	}
	
	public void ensureGlobalIds() {
		GlobalIdGenerator idGenerator = new GlobalIdGenerator();
		
		InstanceQualification currentQualification = null;
		
		for (Map.Entry<InstanceQualification, GenericEntity> entry: managedInstanceEntities.entrySet()) {
			GenericEntity candidateEntity = entry.getValue();
			
			if (candidateEntity.getGlobalId() != null)
				continue;
			
			InstanceQualification qualification = entry.getKey();

			// multi key change detection
			if (qualification != currentQualification) {
				String globalIdPrefix = initializerId;
				
				if (globalIdPrefix == null) {
					WireModule module = wireContext.findModuleFor(qualification.space().getClass());
					if (module != null)
						globalIdPrefix = module.getClass().getSimpleName();
				}
				
				if (globalIdPrefix == null)
					throw new IllegalStateException("You have to either specify a initializerId or use WireModules to automatically generate globalIds.");
				
				// reset id generator for new qualification (naming + sequence reset)
				idGenerator.initialize(qualification, globalIdPrefix);
				currentQualification = qualification;
			}
			
			candidateEntity.setGlobalId(idGenerator.nextGlobalId());
		}
	}
	
	@Override
	public void close() throws Exception {
		session.listeners().remove(this);
	}

}
