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
package com.braintribe.persistence.hibernate;

import com.braintribe.model.access.hibernate.interceptor.GmAdaptionInterceptor;
import com.braintribe.model.generic.GMF;

/**
 * @author peter.gazdik
 */
public class GmAwareHibernateSessionFactoryBean extends HibernateSessionFactoryBean {

	public GmAwareHibernateSessionFactoryBean() {
		this.setClassLoader(itwOrModuleClassLoader());
		this.setEntityInterceptor(new GmAdaptionInterceptor());
	}

	private static ClassLoader itwOrModuleClassLoader() {
		if (isLoadedByModule())
			// Module CL has ITW CL as parent
			return GmAwareHibernateSessionFactoryBean.class.getClassLoader();
		else
			return (ClassLoader) GMF.getTypeReflection().getItwClassLoader();
	}

	private static boolean isLoadedByModule() {
		return GmAwareHibernateSessionFactoryBean.class.getClassLoader().getClass().getSimpleName().startsWith("ModuleClassLoader");
	}

}
