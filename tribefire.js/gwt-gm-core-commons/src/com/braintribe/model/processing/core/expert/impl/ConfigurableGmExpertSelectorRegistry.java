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
package com.braintribe.model.processing.core.expert.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.braintribe.cfg.Required;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.GenericModelTypeReflection;
import com.braintribe.model.processing.core.expert.api.GmExpertBuilder;
import com.braintribe.model.processing.core.expert.api.GmExpertRegistry;
import com.braintribe.model.processing.core.expert.api.GmExpertRegistryAware;
import com.braintribe.model.processing.core.expert.api.GmExpertRegistryException;
import com.braintribe.model.processing.core.expert.api.GmExpertSelector;


public class ConfigurableGmExpertSelectorRegistry implements GmExpertRegistry {
	private final GenericModelTypeReflection genericModelTypeReflection = GMF.getTypeReflection();
	
	private final Map<Class<?>, List<GmExpertSelector>> expertSelectors = new HashMap<Class<?>, List<GmExpertSelector>>();
	
	@Required
	public void setExpertDefinitions(List<GmExpertSelector> expertSelectors) {
		for (GmExpertSelector selector: expertSelectors) {
			initializeExpert(selector.expert());
			acquireSelectorList(selector.expertType()).add(selector);
		}
	}
	
	protected List<GmExpertSelector> acquireSelectorList(Class<?> expertType) {
		List<GmExpertSelector> specificSelectors = expertSelectors.get(expertType);
		
		if (specificSelectors == null) {
			specificSelectors = new ArrayList<GmExpertSelector>();
			expertSelectors.put(expertType, specificSelectors);
		}
		
		return specificSelectors;
	}
	
	protected List<GmExpertSelector> getSelectorList(Class<?> expertType) {
		List<GmExpertSelector> specificSelectors = expertSelectors.get(expertType);
		
		if (specificSelectors != null)
			return specificSelectors;
		else
			return Collections.emptyList();
	}
	
	protected void initializeExpert(Object expert) {
		if (expert instanceof GmExpertRegistryAware) {
			((GmExpertRegistryAware) expert).initExpertRegistry(this);
		}
	}
	

	@Override
	public <T> GmExpertBuilder<T> getExpert(Class<T> expertClass) {
		return new GmExpertBuilderImpl<T>(expertClass, true);
	}
	
	@Override
	public <T> GmExpertBuilder<T> findExpert(Class<T> expertClass) {
		return new GmExpertBuilderImpl<T>(expertClass, false);
	}
	
	
	protected Object findExpert(GmExpertSelectorContextImpl expertKey) {
		for (GmExpertSelector selector: getSelectorList(expertKey.getExpertType())) {
			if (selector.matches(expertKey))
				return selector.expert();
		}
		
		return null;
	}
	
	private class GmExpertBuilderImpl<T> implements GmExpertBuilder<T> {
		
		private final Class<T> expertClass;
		private final boolean expertRequired;
		
		public GmExpertBuilderImpl(Class<T> expertClass, boolean expertRequired) {
			this.expertClass = expertClass;
			this.expertRequired = expertRequired;
		}
		
		@Override
		public <R extends T> R  forInstance(GenericEntity instance) throws GmExpertRegistryException {
			return forType(instance.entityType(), instance);
		}
		
		@Override
		public <R extends T> R  forType(Class<?> clazz)	throws GmExpertRegistryException {
			GenericModelType type = genericModelTypeReflection.getType(clazz);
			return forType(type);
		}
		
		@Override
		public <R extends T> R  forType(GenericModelType type) throws GmExpertRegistryException {
			return forType(type, null);
		}
		
		protected <R extends T> R  forType(GenericModelType type, GenericEntity instance) throws GmExpertRegistryException {
			R expert = (R)findExpert(new GmExpertSelectorContextImpl(expertClass, type, instance));
			if (expertRequired && expert == null ) {
				throw new GmExpertRegistryException("No expert found for type: " + type.getTypeSignature());
			}
			return expert;
		}
		
		@Override
		public <R extends T> R  forType(String typeSignature) throws GmExpertRegistryException {
			return forType(genericModelTypeReflection.getEntityType(typeSignature));
		}
	}
}
