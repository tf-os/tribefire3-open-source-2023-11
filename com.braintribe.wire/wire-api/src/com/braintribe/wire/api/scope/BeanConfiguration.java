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
package com.braintribe.wire.api.scope;

import com.braintribe.wire.api.annotation.Bean;
import com.braintribe.wire.api.space.WireSpace;

/**
 * A BeanConfiguration stands for the additional configuration than can be made on a bean in construction.
 * If you are within a {@link Bean} annotated method in a {@link WireSpace} you can easily get the instance
 * to the according BeanConfiguration by calling {@link #currentBean()}. 
 * @author dirk.scheffler
 * @deprecated use {@link InstanceConfiguration} instead. This class will be removed in future versions of Wire
 */
@Deprecated
public interface BeanConfiguration {
	/**
	 * Configures a callback method that will be called on destruction of the bean associated with the BeanConfiguration
	 */
	void onDestroy(Runnable function);
	
	/**
	 * This call will give access to the BeanConfiguration (facet of {@link BeanHolder}) to allow for further configuration.
	 * 
	 * The call is intentionally implemented by an exception here as the calling of it in {@link Bean} annotated methods
	 * will be replaced by the Wire's bytecode enricher to return the current {@link BeanConfiguration} associated
	 * with the {@link BeanHolder} that is managing the identity of the constructed bean. 
	 * @return
	 */
	static BeanConfiguration currentBean() { 
		throw new UnsupportedOperationException("this method call should have bean replace by Wire's bytecode enriching");
	};
	
	static BeanConfiguration adapt(InstanceConfiguration config) {
		return new BeanConfiguration() {
			
			@Override
			public void onDestroy(Runnable function) {
				config.onDestroy(function);
			}
		};
	}
}
