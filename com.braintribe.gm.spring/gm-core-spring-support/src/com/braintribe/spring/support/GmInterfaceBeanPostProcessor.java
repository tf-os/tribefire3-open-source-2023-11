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
package com.braintribe.spring.support;

import java.lang.reflect.Constructor;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessorAdapter;

import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.GenericModelTypeReflection;


public class GmInterfaceBeanPostProcessor extends InstantiationAwareBeanPostProcessorAdapter {
	private final GenericModelTypeReflection TYPE_REFLECTION;

	public GmInterfaceBeanPostProcessor() {
		TYPE_REFLECTION = GMF.getTypeReflection();
	}
	
	/**
	 * @deprecated interfaces only is anyway the way we live therefore this method does nothing
	 */
	@Deprecated 
	public void setInterfacesOnly(@SuppressWarnings("unused") boolean interfacesOnly) {
		// noop
	}

	@Override
	@SuppressWarnings("unchecked")
	public Constructor<?>[] determineCandidateConstructors(@SuppressWarnings("rawtypes") Class beanClass,
			String beanName) throws BeansException {
		
		//creation of dummy instance just in order to known what is the plain entity class.
		//it would be desirable if the GM type reflection could tell us the type directly without creating an instance
		if (beanClass.isInterface() && GenericEntity.class.isAssignableFrom(beanClass)) {
			return TYPE_REFLECTION.getEntityType(beanClass).enhancedClass().getConstructors();
		}
		
		return super.determineCandidateConstructors(beanClass, beanName);
	}
}
