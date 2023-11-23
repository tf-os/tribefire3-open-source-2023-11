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
package com.braintribe.codec.marshaller.dom;

import java.util.Set;

import org.w3c.dom.Document;

import com.braintribe.codec.CodecException;
import com.braintribe.codec.marshaller.dom.coder.DeferredDecoder;
import com.braintribe.codec.marshaller.dom.coder.entity.EntityDomCodingPreparation;
import com.braintribe.codec.marshaller.dom.coder.entity.PropertyAbsenceHelper;
import com.braintribe.model.generic.pr.AbsenceInformation;
import com.braintribe.model.generic.reflection.EntityType;

public interface DomDecodingContext {
	
	Document getDocument();

	TypeInfo getTypeInfoByKey(String typeKey) throws CodecException;
	
	void registerTypeInfo(TypeInfo4Read typeInfo);
	
	EntityRegistration acquireEntity(String ref) throws CodecException;

	AbsenceInformation getAbsenceInformationForMissingProperties();

	PropertyAbsenceHelper providePropertyAbsenceHelper();

	void appendDeferredDecoder(DeferredDecoder coder);

	int getMaxDeferred();

	DeferredDecoder getFirstDeferredDecoder();

	EntityDomCodingPreparation getEntityDomCodingPreparation(EntityType<?> entityType) throws CodecException;

	Set<String> getRequiredTypes();
}
