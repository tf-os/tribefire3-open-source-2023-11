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
package com.braintribe.codec.marshaller.sax;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import com.braintribe.codec.marshaller.api.GmDeserializationOptions;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.pr.AbsenceInformation;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.session.GmSession;

class DecodingContextImpl<T> extends DecodingContext {

	private final Map<String, GenericEntity> entitiesById = new HashMap<>();

	private final AbsenceInformation absenceInformationForMissingProperties = GMF.absenceInformation();
	private int version = 1;
	private final GmDeserializationOptions options;

	protected boolean createEnhancedEntities = false;
	protected Consumer<Set<String>> requiredTypesReceiver;

	private final GmSession session;

	public DecodingContextImpl(boolean createEnhancedEntities, Consumer<Set<String>> requiredTypesReceiver, GmDeserializationOptions options) {
		super();
		this.createEnhancedEntities = createEnhancedEntities;
		this.requiredTypesReceiver = requiredTypesReceiver;
		this.options = options;
		this.session = options.getSession();
	}

	public void setVersion(int version) {
		this.version = version;
	}

	@Override
	public DateTimeFormatter getDateFormat() {
		return version > 2 ? DateFormats.dateFormat : DateFormats.legacyDateFormat;
	}

	@Override
	public PropertyAbsenceHelper providePropertyAbsenceHelper() {
		return options.getAbsentifyMissingProperties() ? new ActivePropertyAbsenceHelper(this) : InactivePropertyAbsenceHelper.instance;
	}

	@Override
	public AbsenceInformation getAbsenceInformationForMissingProperties() {
		return absenceInformationForMissingProperties;
	}

	@Override
	public GenericEntity lookupEntity(String ref) {
		return entitiesById.get(ref);
	}

	@Override
	public boolean isEnhanced() {
		return this.createEnhancedEntities;
	}

	@Override
	public void register(GenericEntity entity, String idString) {
		entitiesById.put(idString, entity);
	}

	@Override
	public Consumer<Set<String>> getRequiredTypesReceiver() {
		return this.requiredTypesReceiver;
	}

	@Override
	public GenericEntity createRaw(EntityType<?> entityType) {
		return session != null ? session.createRaw(entityType) : createEnhancedEntities ? entityType.createRaw() : entityType.createPlainRaw();
	}

}
