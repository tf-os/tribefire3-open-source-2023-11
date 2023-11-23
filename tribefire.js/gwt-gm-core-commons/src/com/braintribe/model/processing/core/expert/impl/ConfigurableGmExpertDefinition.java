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

import com.braintribe.cfg.Required;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.processing.core.expert.api.GmExpertDefinition;
import com.braintribe.model.processing.core.expert.api.GmExpertSelector;
import com.braintribe.model.processing.core.expert.api.GmExpertSelectorContext;

public class ConfigurableGmExpertDefinition implements GmExpertSelector, GmExpertDefinition {

	private GenericModelType denotationType;
	private Class<?> expertType;
	private Object expert;
	private boolean assignable = true;
	
	@Required
	public void setDenotationType(Class<?> denotationType) {
		this.denotationType = GMF.getTypeReflection().getType(denotationType);
	}

	public void setExpertType(Class<?> expertType) {
		this.expertType = expertType;
	}

	public void setExpert(Object expert) {
		this.expert = expert;
	}
	
	@Override
	public GenericModelType denotationType() {
		return denotationType;
	}
	
	@Override
	public Class<?> expertType() {
		return expertType;
	}
	
	@Override
	public Object expert() {
		return expert;
	}
	
	@Override
	public boolean matches(GmExpertSelectorContext context) {
		return assignable? 
				denotationType.isAssignableFrom(context.getDenotationType()):
				denotationType == context.getDenotationType();
	}
}
