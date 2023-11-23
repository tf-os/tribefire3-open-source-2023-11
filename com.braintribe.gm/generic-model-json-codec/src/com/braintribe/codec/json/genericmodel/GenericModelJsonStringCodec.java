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
package com.braintribe.codec.json.genericmodel;

import com.braintribe.cfg.Configurable;
import com.braintribe.codec.json.AbstractJsonToStringCodec;
import com.braintribe.codec.marshaller.api.GmCodec;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.GenericModelTypeReflection;
import com.fasterxml.jackson.databind.JsonNode;

public class GenericModelJsonStringCodec<T> extends AbstractJsonToStringCodec<T> {
	private GenericModelJsonCodec<T> jsonCodec = new GenericModelJsonCodec<T>();

	@Configurable
	public void setAssignAbsenceInformationForMissingProperties(boolean assignAbsenceInformatonForMissingProperties) {
		jsonCodec.setAssignAbsenceInformationForMissingProperties(assignAbsenceInformatonForMissingProperties);
	}

	@Configurable
	public void setGenericModelTypeReflection(
			GenericModelTypeReflection genericModelTypeReflection) {
		jsonCodec.setGenericModelTypeReflection(genericModelTypeReflection);
	}

	@Configurable
	public void setCreateEnhancedEntities(boolean createEnhancedEntities) {
		jsonCodec.setCreateEnhancedEntities(createEnhancedEntities);
	}

	@Configurable
	public void setWriteAbsenceInformation(boolean writeAbsenceInformation) {
		jsonCodec.setWriteAbsenceInformation(writeAbsenceInformation);
	}

	@Configurable
	public void setType(GenericModelType type) {
		jsonCodec.setType(type);
	}
	
	@Override
	public GmCodec<T, JsonNode> getJsonDelegateCodec() {
		return jsonCodec;
	}
}
