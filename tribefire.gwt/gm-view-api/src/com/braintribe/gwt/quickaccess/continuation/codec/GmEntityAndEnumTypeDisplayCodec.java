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
package com.braintribe.gwt.quickaccess.continuation.codec;

import java.util.function.Function;

import com.braintribe.gwt.gmview.client.UseCaseHandler;
import com.braintribe.gwt.gmview.util.client.GMEMetadataUtil;
import com.braintribe.gwt.gmview.util.client.GMEUtil;
import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.meta.GmEnumType;
import com.braintribe.model.meta.GmType;
import com.braintribe.model.meta.data.prompt.Name;
import com.braintribe.model.processing.meta.cmd.builders.ModelMdResolver;

import com.braintribe.utils.i18n.I18nTools;

public class GmEntityAndEnumTypeDisplayCodec implements Function<GmType, String>, UseCaseHandler {
	
	private ModelMdResolver metaDataResolver;
	private String useCase;
	
	public void configureMetaDataResolver(ModelMdResolver metaDataResolver) {
		this.metaDataResolver = metaDataResolver;
	}
	
	@Override
	public void configureUseCase(String useCase) {
		this.useCase = useCase;
	}
	
	@Override
	public String apply(GmType type) throws RuntimeException {
		if (type instanceof GmEntityType) {
			String entityTypeDisplay = GMEMetadataUtil
					.getEntityTypeDisplay(metaDataResolver != null ? metaDataResolver.entityType((GmEntityType) type).useCase(useCase) : null);
			if (entityTypeDisplay == null)
				entityTypeDisplay = GMEUtil.getShortName(type);
			
			return entityTypeDisplay;
		} else {
			GmEnumType enumType = (GmEnumType) type;
			String enumTypeDisplay = GMEUtil.getShortName(enumType);
			if (metaDataResolver != null) {
				Name name = metaDataResolver.enumTypeSignature(enumType.getTypeSignature()).useCase(useCase).meta(Name.T)
						.exclusive();
				if (name != null && name.getName() != null)
					enumTypeDisplay = I18nTools.getLocalized(name.getName());
			}
			
			return enumTypeDisplay;
		}
	}
	
	@Override
	public String getUseCase() {
		return useCase;
	}

}
