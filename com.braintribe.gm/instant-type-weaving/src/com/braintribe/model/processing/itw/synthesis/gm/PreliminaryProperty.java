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
package com.braintribe.model.processing.itw.synthesis.gm;

import java.util.Set;
import java.util.stream.Stream;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.meta.data.prompt.Confidential;
import com.braintribe.model.processing.itw.asm.AsmMethod;
import com.braintribe.model.weaving.ProtoGmEntityType;
import com.braintribe.model.weaving.ProtoGmProperty;
import com.braintribe.model.weaving.info.ProtoGmPropertyInfo;

public class PreliminaryProperty {
	private final PreliminaryEntityType pet;
	public final ProtoGmProperty gmProperty;
	private final ProtoGmPropertyInfo initializerInfo;
	public final String propertyName;
	public final boolean nullable;
	public final boolean confidential;
	public final Object initializer;

	/* Is needed when implementing methods to read/write id property. */
	public AsmMethod plainGetter;
	public AsmMethod plainSetter;

	public AsmMethod aopGetter;
	public AsmMethod aopSetter;

	public PreliminaryProperty(PreliminaryEntityType pet, ProtoGmProperty gmProperty, ProtoGmPropertyInfo[] propertyLineage) {
		this.pet = pet;
		this.gmProperty = gmProperty;
		this.initializerInfo = findInitializerInfo(gmProperty, propertyLineage);
		this.propertyName = gmProperty.getName();
		this.nullable = gmProperty.getNullable();
		this.confidential = isConfidential(propertyLineage);

		this.initializer = this.initializerInfo.getInitializer();
	}

	private static ProtoGmPropertyInfo findInitializerInfo(ProtoGmProperty gmProperty, ProtoGmPropertyInfo[] propertyLineage) {
		for (ProtoGmPropertyInfo propertyInfo : propertyLineage)
			if (propertyInfo.getInitializer() != null)
				return propertyInfo;

		return gmProperty; // we need something to call getInitializer on
	}

	private static boolean isConfidential(ProtoGmPropertyInfo[] propertyLineage) {
		return Stream.of(propertyLineage) //
				.map(ProtoGmPropertyInfo::getMetaData) //
				.filter(mds -> mds != null) //
				.flatMap(Set::stream) //
				.filter(Confidential.class::isInstance) //
				.filter(md -> md.getSelector() == null) //
				.findAny() //
				.isPresent();
	}

	/**
	 * This method picks the {@link Property} instance for the {@link EntityType} if possible. It is possible to use a super-property if it exists,
	 * and we don't change its initializer nor confidentiality on this level.
	 */
	public Property findPropertyFromSuperIsPossible() {
		if (isIntroducedOrReInitialized())
			return null;

		Property p = pet.findProperty(propertyName);
		if (p.isConfidential() || !confidential)
			return p;

		return null;
	}

	public boolean isIntroducedOrReInitialized() {
		return isIntroducedAt(pet.gmEntityType) || isInitializedAt(pet.gmEntityType);
	}

	public boolean isIntroducedAt(ProtoGmEntityType gmEntityType) {
		return gmProperty.getDeclaringType() == gmEntityType;
	}

	private boolean isInitializedAt(ProtoGmEntityType gmEntityType) {
		return initializerInfo.declaringTypeInfo().addressedType() == gmEntityType;
	}

}
