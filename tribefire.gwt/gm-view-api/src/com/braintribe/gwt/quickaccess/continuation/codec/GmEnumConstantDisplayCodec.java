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

import com.braintribe.codec.Codec;
import com.braintribe.codec.CodecException;
import com.braintribe.gwt.gmview.client.UseCaseHandler;
import com.braintribe.gwt.logging.client.Logger;
import com.braintribe.model.meta.GmEnumConstant;
import com.braintribe.model.meta.data.prompt.Name;
import com.braintribe.model.processing.meta.cmd.builders.ModelMdResolver;

import com.braintribe.utils.i18n.I18nTools;

public class GmEnumConstantDisplayCodec implements Function<GmEnumConstant, String>, UseCaseHandler {
	private static Logger logger = new Logger(GmEnumConstantDisplayCodec.class);

	private ModelMdResolver metaDataResolver;
	private String useCase;
	private Codec<GmEnumConstant, String> enumConstantRenderer;
	
	public void configureMetaDataResolver(ModelMdResolver metaDataResolver) {
		this.metaDataResolver = metaDataResolver;
	}
	
	public void configureEnumConstantRenderer(Codec<GmEnumConstant, String> enumConstantRenderer) {
		this.enumConstantRenderer = enumConstantRenderer;
	}
	
	@Override
	public void configureUseCase(String useCase) {
		this.useCase = useCase;
	}

	@Override
	public String apply(GmEnumConstant enumConstant) throws RuntimeException {
		String display = enumConstant.getName();
		Name name = null;
		if (metaDataResolver != null)
			name = metaDataResolver.lenient(true).enumConstant(enumConstant).useCase(useCase).meta(Name.T).exclusive();
		if (name != null && name.getName() != null)
			display = I18nTools.getLocalized(name.getName());
		else if (enumConstantRenderer != null) {
			try {
				display = enumConstantRenderer.encode(enumConstant);
			} catch (CodecException e) {
				logger.error("Error while encoding enumConstant '" + display + "'", e);
				e.printStackTrace();
			}
		}
		
		return display;
	}
	
	@Override
	public String getUseCase() {
		return useCase;
	}

}
