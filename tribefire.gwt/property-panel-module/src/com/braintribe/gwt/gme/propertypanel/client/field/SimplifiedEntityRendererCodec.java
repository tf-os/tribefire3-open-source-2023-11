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
package com.braintribe.gwt.gme.propertypanel.client.field;

import com.braintribe.codec.Codec;
import com.braintribe.codec.CodecException;
import com.braintribe.gwt.gmview.client.UseCaseHandler;
import com.braintribe.gwt.gmview.metadata.client.SelectiveInformationResolver;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.processing.meta.cmd.builders.ModelMdResolver;

/**
 * This Codec is responsible for transforming an GenericEntity into String,
 * for visualization only purposes. It uses the GenericEntity selective information.
 * Notice that the encoding, due to some constraints in the GXT editors, may not return empty for a non null entity.
 * @author michel.docouto
 * 
 */
public class SimplifiedEntityRendererCodec implements Codec<GenericEntity, String>, UseCaseHandler {
	
	private String useCase;
	private boolean useShortType;
	
	/**
	 * Configures whether we should use the short type as default for the rendering the entity.
	 * Defaults to false.
	 */
	public void setUseShortType(boolean useShortType) {
		this.useShortType = useShortType;
	}
	
	@Override
	public void configureUseCase(String useCase) {
		this.useCase = useCase;
	}
	
	@Override
	public String getUseCase() {
		return useCase;
	}
	
	@Override
	public String encode(GenericEntity entity) throws CodecException {
		if (entity == null)
			return "";
		
		EntityType<GenericEntity> entityType = entity.entityType();
		if (useShortType)
			return entityType.getShortName();
		
		String selectiveInformation = SelectiveInformationResolver.resolve(entityType, entity, (ModelMdResolver) null, useCase/*, null*/);
		if (selectiveInformation != null && !selectiveInformation.isEmpty())
			return selectiveInformation;
		else
			return entityType.getShortName();
	}

	@Override
	public GenericEntity decode(String encodedValue) throws CodecException {
		throw new CodecException("Decode is not supported");
	}

	@Override
	public Class<GenericEntity> getValueClass() {
		return GenericEntity.class;
	}

}
