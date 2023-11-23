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
import java.util.Set;
import java.util.function.Consumer;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.pr.AbsenceInformation;
import com.braintribe.model.generic.reflection.EntityType;

public abstract class DecodingContext {
	public abstract DateTimeFormatter getDateFormat();
	public abstract PropertyAbsenceHelper providePropertyAbsenceHelper();
	public abstract AbsenceInformation getAbsenceInformationForMissingProperties();
	public abstract GenericEntity lookupEntity(String ref);
	public abstract boolean isEnhanced();
	public abstract void register(GenericEntity entity, String idString);
	public abstract Consumer<Set<String>> getRequiredTypesReceiver();
	public abstract GenericEntity createRaw(EntityType<?> entityType);
}
