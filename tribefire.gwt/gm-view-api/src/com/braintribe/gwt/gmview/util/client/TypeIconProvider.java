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
package com.braintribe.gwt.gmview.util.client;

import java.util.Map;

import com.braintribe.gwt.gmview.client.IconAndType;
import com.braintribe.gwt.gmview.client.IconProvider;
import com.braintribe.gwt.ioc.client.Configurable;
import com.braintribe.gwt.ioc.client.Required;
import com.braintribe.model.generic.path.ModelPath;
import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;

/**
 * Icon provider based on types.
 * @author michel.docouto
 *
 */
public class TypeIconProvider implements IconProvider {
	
	private Map<String, IconProvider> typeProvidersMap;
	private IconProvider defaultProvider;
	private PersistenceGmSession gmSession;
	private String useCase;
	
	/**
	 * Configures the required map for providers per type.
	 */
	@Required
	public void setTypeProvidersMap(Map<String, IconProvider> typeProvidersMap) {
		this.typeProvidersMap = typeProvidersMap;
	}
	
	/**
	 * Configures the required default provider to be used when there is no match to the providers defined via {@link #setTypeProvidersMap(Map)}.
	 */
	@Configurable
	public void setDefaultProvider(IconProvider defaultProvider) {
		this.defaultProvider = defaultProvider;
	}
	
	@Override
	public void configureGmSession(PersistenceGmSession gmSession) {
		this.gmSession = gmSession;
	}
	
	@Override
	public void configureUseCase(String useCase) {
		this.useCase = useCase;
	}
	
	@Override
	public PersistenceGmSession getGmSession() {
		return gmSession;
	}
	
	@Override
	public String getUseCase() {
		return useCase;
	}

	@Override
	public IconAndType apply(ModelPath modelPath) {
		if (modelPath != null) {
			String typeSignature;
			if (modelPath.last().getValue() instanceof GmEntityType)
				typeSignature = ((GmEntityType) modelPath.last().getValue()).getTypeSignature();
			else
				typeSignature = modelPath.last().getType().getTypeSignature();

			IconProvider provider = typeProvidersMap.get(typeSignature);
			if (provider != null) {
				provider.configureGmSession(gmSession);
				provider.configureUseCase(useCase);
				IconAndType iconAndType = provider.apply(modelPath);
				if (iconAndType != null)
					return iconAndType;
			}
		}
		
		if (defaultProvider != null) {
			defaultProvider.configureGmSession(gmSession);
			defaultProvider.configureUseCase(useCase);
			return defaultProvider.apply(modelPath);
		}
		
		return null;
	}

}
