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

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.braintribe.codec.CodecException;
import com.braintribe.codec.marshaller.api.GmSerializationOptions;
import com.braintribe.codec.marshaller.dom.coder.DeferredEncoder;
import com.braintribe.codec.marshaller.dom.coder.entity.DynamicEntityDomCodingPreparation;
import com.braintribe.codec.marshaller.dom.coder.entity.EntityDomCodingPreparation;
import com.braintribe.codec.marshaller.dom.coder.entity.EntityDomDeferredEncoder;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.pr.AbsenceInformation;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GenericModelType;

public class DomEncodingContextImpl implements DomEncodingContext {
	private static final String aliasDigits = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";

	private final Map<EntityType<?>, DynamicEntityDomCodingPreparation> contextPreparations = new HashMap<>();
	private final EntityDomCodingPreparations entityDomCodingPreparations;
	
	private final Map<GenericEntity, String> qualifiedIdByEntities = new HashMap<>();
	private final DeferredEncoder anchorDeferredEncoder = new DeferredEncoder();
	private DeferredEncoder lastDeferredEncoder = anchorDeferredEncoder;
	private final Set<GenericModelType> requiredGenericModelTypes = new HashSet<>();
	private final Map<GenericModelType, TypeInfo> requiredTypes = new HashMap<>();
	private final byte[] aliasSequence = new byte[10]; 
	private int aliasDigitCount = 1; 
	private final Set<String> shortTypeNames = new HashSet<>();
	@SuppressWarnings("unused")
	private final GmSerializationOptions options;
	private boolean writeAbsenceInformation = true;

	private final Document document;

	private Element pool;

	private final int maxDeferred = -1;

	
	public DomEncodingContextImpl(Document document, EntityDomCodingPreparations entityDomCodingPreparations, GmSerializationOptions options) {
		this.document = document;
		this.entityDomCodingPreparations = entityDomCodingPreparations;
		this.options = options;
	}

	@Override
	public boolean isSimpleAbsenceInformation(AbsenceInformation absenceInformation) {
		return absenceInformation.getSize() == null;
	}

	public void setWriteAbsenceInformation(boolean writeAbsenceInformation) {
		this.writeAbsenceInformation = writeAbsenceInformation;
	}

	@Override
	public TypeInfo registerRequiredType(GenericModelType type) {
		TypeInfo typeInfo = requiredTypes.get(type);
		
		if (typeInfo == null) {
			typeInfo = new TypeInfo();
			typeInfo.type = type;
			typeInfo.as = getShortName(type);
			typeInfo.alias = nextAlias();
			requiredTypes.put(type, typeInfo);
		}
		
		return typeInfo;
	}

	private String getShortName(GenericModelType type) {
		String suggestion = type.getJavaType().getSimpleName();
		return ensureUniqueShortName(suggestion, 1);
	}

	private String ensureUniqueShortName(String suggestion, int alternative) {
		String shortName = suggestion;
		if (alternative > 1) {
			shortName += alternative;
		}
		if (shortTypeNames.add(shortName)) {
			return shortName;
		}
		else {
			return ensureUniqueShortName(suggestion, alternative + 1);
		}
	}

	private String nextAlias() {
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < aliasDigitCount; i++) {
			builder.append(aliasDigits.charAt(aliasSequence[i]));
		}
		
		for (int i = 0; i < aliasSequence.length; i++) {
			byte num = aliasSequence[i];
			if (++num == aliasDigits.length()) {
				aliasSequence[i] = 0;
			}
			else {
				aliasSequence[i] = num;
				int countCandidate = i + 1;
				if (countCandidate > aliasDigitCount)
					aliasDigitCount = countCandidate;
				break;
			}
		}

		
		return builder.toString();
	}

	public Set<GenericModelType> getRequiredGenericModelTypes() {
		return requiredGenericModelTypes;
	}

	@Override
	public String lookupQualifiedId(GenericEntity entity) throws CodecException {
		String id = qualifiedIdByEntities.get(entity);
		
		if (id == null) {
			EntityType<?> entityType = entity.entityType();
			DynamicEntityDomCodingPreparation preparation = acquireContextEntityDomCodingPreparation(entityType);
			
			id = preparation.typeInfo.nextId();
			qualifiedIdByEntities.put(entity, id);
			
			EntityDomDeferredEncoder deferredEncoder = new EntityDomDeferredEncoder(preparation.preparation, preparation.typeInfo, entity, id);

			lastDeferredEncoder.next = deferredEncoder;
			lastDeferredEncoder = deferredEncoder;
		}
		
		return id;
	}
	
	private DynamicEntityDomCodingPreparation acquireContextEntityDomCodingPreparation(EntityType<?> entityType) throws CodecException {
		DynamicEntityDomCodingPreparation contextPreparation = contextPreparations.get(entityType);
		
		if (contextPreparation == null) {
			EntityDomCodingPreparation globalPreparation = entityDomCodingPreparations.acquireEntityDomCodingPreparation(entityType);
			
			TypeInfo typeInfo = registerRequiredType(entityType);

			contextPreparation = new DynamicEntityDomCodingPreparation(typeInfo, globalPreparation);
			contextPreparations.put(entityType, contextPreparation);
		}
		
		return contextPreparation;
	}

	@Override
	public void appendDeferredEncoder(DeferredEncoder coder) {
		lastDeferredEncoder.next = coder;
		lastDeferredEncoder = coder;
	}
	
	@Override
	public DeferredEncoder getFirstDeferredEncoder() {
		return anchorDeferredEncoder.next;
	}

	@Override
	public boolean shouldWriteAbsenceInformation() {
		return writeAbsenceInformation;
	}

	@Override
	public Collection<TypeInfo> getRequiredTypeInfos() {
		return requiredTypes.values();
	}
	
	@Override
	public Document getDocument() {
		return document;
	}

	@Override
	public int getMaxDeferred() {
		return maxDeferred ;
	}

	@Override
	public void appendToPool(Element element) {
		this.pool.appendChild(element);
	}

	@Override
	public void setPool(Element element) {
		this.pool = element;
	}

}




