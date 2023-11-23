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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.w3c.dom.Document;

import com.braintribe.codec.CodecException;
import com.braintribe.codec.marshaller.api.GmDeserializationOptions;
import com.braintribe.codec.marshaller.dom.coder.DeferredDecoder;
import com.braintribe.codec.marshaller.dom.coder.entity.ActivePropertyAbsenceHelper;
import com.braintribe.codec.marshaller.dom.coder.entity.EntityDomCodingPreparation;
import com.braintribe.codec.marshaller.dom.coder.entity.InactivePropertyAbsenceHelper;
import com.braintribe.codec.marshaller.dom.coder.entity.PropertyAbsenceHelper;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.pr.AbsenceInformation;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.session.GmSession;

public class DomDecodingContextImpl implements DomDecodingContext {

	private Document document;
	private Map<String, TypeInfo4Read> typeInfoByAlias = new HashMap<String, TypeInfo4Read>();
	private Map<String, TypeInfo4Read> typeInfoByKey = new HashMap<String, TypeInfo4Read>();
	private Map<String, EntityRegistration> entitiesById = new HashMap<String, EntityRegistration>();
	private DeferredDecoder anchorDeferredDecoder = new DeferredDecoder();
	private DeferredDecoder lastDeferredDecoder = anchorDeferredDecoder;
	private GmDeserializationOptions options;
	private AbsenceInformation absenceInformationForMissingProperties = GMF.absenceInformation();
	private boolean createEnhancedEntities = true;
	private int maxDeferred = -1;
	private EntityDomCodingPreparations entityDomCodingPreparations;
	private GmSession session;

	public DomDecodingContextImpl(Document document, GmDeserializationOptions options, EntityDomCodingPreparations entityDomCodingPreparations) {
		this.document = document;
		this.options = options;
		this.session = options.getSession();
		this.entityDomCodingPreparations = entityDomCodingPreparations;
	}

	public void setMaxDeferred(int maxDeferred) {
		this.maxDeferred = maxDeferred;
	}

	@Override
	public int getMaxDeferred() {
		return maxDeferred;
	}

	@Override
	public Document getDocument() {
		return document;
	}

	@Override
	public void registerTypeInfo(TypeInfo4Read typeInfo) {
		typeInfoByAlias.put(typeInfo.alias, typeInfo);
		typeInfoByKey.put(typeInfo.as, typeInfo);
	}

	@Override
	public TypeInfo4Read getTypeInfoByKey(String key) {
		return typeInfoByKey.get(key);
	}

	@Override
	public EntityRegistration acquireEntity(String ref) throws CodecException {
		EntityRegistration registration = entitiesById.get(ref);

		if (registration == null) {
			int index = ref.indexOf('-');
			String alias = ref.substring(0, index);
			TypeInfo4Read typeInfo = typeInfoByAlias.get(alias);

			if (typeInfo == null)
				throw new CodecException("invalid ref id " + ref);

			EntityType<?> entityType = (EntityType<?>) typeInfo.type;
			GenericEntity entity = session != null ? session.createRaw(entityType)
					: createEnhancedEntities ? entityType.createRaw() : entityType.createPlainRaw();
			registration = new EntityRegistration();
			registration.entity = entity;
			registration.typeInfo = typeInfo;
			entitiesById.put(ref, registration);
		}

		return registration;
	}

	@Override
	public AbsenceInformation getAbsenceInformationForMissingProperties() {
		return absenceInformationForMissingProperties;
	}

	@Override
	public PropertyAbsenceHelper providePropertyAbsenceHelper() {
		return options.getAbsentifyMissingProperties() ? new ActivePropertyAbsenceHelper(this) : InactivePropertyAbsenceHelper.instance;
	}

	@Override
	public DeferredDecoder getFirstDeferredDecoder() {
		return anchorDeferredDecoder.next;
	}

	@Override
	public void appendDeferredDecoder(DeferredDecoder coder) {
		lastDeferredDecoder.next = coder;
		lastDeferredDecoder = coder;
	}

	@Override
	public EntityDomCodingPreparation getEntityDomCodingPreparation(EntityType<?> entityType) throws CodecException {
		return entityDomCodingPreparations.acquireEntityDomCodingPreparation(entityType);
	}

	@Override
	public Set<String> getRequiredTypes() {
		Set<String> requiredTypes = new HashSet<String>();
		for (TypeInfo typeInfo : typeInfoByAlias.values()) {
			requiredTypes.add(typeInfo.type.getTypeSignature());
		}
		return requiredTypes;
	}
}
