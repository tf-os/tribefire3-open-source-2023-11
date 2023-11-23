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
package com.braintribe.model.access.collaboration;

import java.util.Map;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.manipulation.Manipulation;
import com.braintribe.model.generic.tracking.ManipulationListener;
import com.braintribe.model.processing.session.api.collaboration.PersistenceInitializationContext;
import com.braintribe.model.processing.session.api.managed.ManagedGmSession;
import com.braintribe.model.smoodstorage.stages.PersistenceStage;

/**
 * @author peter.gazdik
 */
public class SmoodInitializationContextImpl implements PersistenceInitializationContext, ManipulationListener {

	private final ManagedGmSession session;
	private final StageRegistry stageRegistry;

	private PersistenceStage currentStage;
	private final String accessId;
	
	private Map<String, Object> attributes;

	public SmoodInitializationContextImpl(ManagedGmSession session, StageRegistry stageRegistry, String accessId) {
		this.session = session;
		this.stageRegistry = stageRegistry;
		this.accessId = accessId;

		session.listeners().add(this);
	}
	
	public void setAttributes(Map<String, Object> attributes) {
		this.attributes = attributes;
	}

	@Override
	public ManagedGmSession getSession() {
		return session;
	}
	
	@Override
	public String getAccessId() {
		return accessId;
	}

	@Override
	public PersistenceStage getStage(GenericEntity entity) {
		return stageRegistry.getStage(entity);
	}

	@Override
	public void setCurrentPersistenceStage(PersistenceStage stage) {
		this.currentStage = stage;
	}

	public void close() {
		session.listeners().remove(this);
	}

	@Override
	public void noticeManipulation(Manipulation m) {
		stageRegistry.onManipulation(m, currentStage);
	}

	@Override
	public <T> T getAttribute(String key) {
		return attributes != null ? (T) attributes.get(key) : null;
	}
	
	
}
