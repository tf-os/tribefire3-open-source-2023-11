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
package com.braintribe.model.processing.management.impl.validator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import com.braintribe.model.management.MetaModelValidationViolation;
import com.braintribe.model.management.MetaModelValidationViolationType;
import com.braintribe.model.management.violation.ViolationWithEnityTypeList;
import com.braintribe.model.management.violation.ViolationWithEntityType;
import com.braintribe.model.management.violation.ViolationWithEnumConstant;
import com.braintribe.model.management.violation.ViolationWithEnumType;
import com.braintribe.model.management.violation.ViolationWithProperty;
import com.braintribe.model.management.violation.ViolationWithTypeAndDependers;
import com.braintribe.model.management.violation.link.EntityTypeLink;
import com.braintribe.model.management.violation.link.EnumConstantLink;
import com.braintribe.model.management.violation.link.EnumTypeLink;
import com.braintribe.model.management.violation.link.Link;
import com.braintribe.model.management.violation.link.PropertyLink;
import com.braintribe.model.management.violation.link.TypeLink;
import com.braintribe.model.meta.GmCustomType;
import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.meta.GmEnumConstant;
import com.braintribe.model.meta.GmEnumType;
import com.braintribe.model.meta.GmProperty;
import com.braintribe.model.meta.GmType;
import com.braintribe.model.processing.management.impl.util.MetaModelDependencyRegistry.DependencyLink;

public class ViolationBuilder {
	
	private List<MetaModelValidationViolation> violations;
	private MetaModelValidationViolation violation = null;
	
	private ViolationBuilder() {
	}
	
	public static ViolationBuilder to(List<MetaModelValidationViolation> violations) {
		
		ViolationBuilder res = new ViolationBuilder();
		res.violations = violations;
		return res;
	}

	public void add(MetaModelValidationViolationType violationType, String message) {
		if (violation == null ) {
			violation = MetaModelValidationViolation.T.create();
		}
		violation.setType(violationType);
		violation.setMessage(message);
		
		violations.add(violation);
	}

	public ViolationBuilder withTypeAndDependers(GmType type, Collection<DependencyLink> dependencyLinks) {
		TypeLink typeLink = TypeLink.T.create();
		typeLink.setTypeSignature(type == null ? null : type.getTypeSignature());

		ViolationWithTypeAndDependers v = ViolationWithTypeAndDependers.T.create();
		v.setTypeLink(typeLink);
		
		if (dependencyLinks != null && dependencyLinks.size() > 0) {
			HashSet<Link> dependerLinks = new HashSet<Link>();
			v.setDependerLinks(dependerLinks);
			for (DependencyLink dl : dependencyLinks) {
				if (dl != null) {
					if (dl.dependent instanceof GmEntityType) {
						GmEntityType depEnt = (GmEntityType)dl.dependent;
						
						EntityTypeLink entityTypeLink = EntityTypeLink.T.create();
						entityTypeLink.setTypeSignature(depEnt == null ? null : depEnt.getTypeSignature());
						
						dependerLinks.add(entityTypeLink);
					} else if (dl.dependent instanceof GmProperty) {		
						GmProperty depProp = (GmProperty)dl.dependent;
						
						PropertyLink propertyLink = PropertyLink.T.create();
						propertyLink.setEntityTypeSignature(depProp == null ? null : 
							depProp.getDeclaringType() == null ? null : depProp.getDeclaringType().getTypeSignature());
						propertyLink.setName(depProp == null ? null : depProp.getName());
						propertyLink.setTypeSignature(depProp == null ? null : depProp.getType() == null ? null : 
							depProp.getType().getTypeSignature());
						
						dependerLinks.add(propertyLink);
					}
				}
			}
		}
		violation = v;
		
		return this;
	}

	@SuppressWarnings("incomplete-switch")
	public ViolationBuilder withCustomType(GmCustomType type) {
		switch (type.typeKind()) {
			case ENTITY: withEntityType((GmEntityType) type);
				break;
			case ENUM: withEnumType((GmEnumType) type);
				break;
		}
		
		return this;
	}
	
	public ViolationBuilder withEntityType(GmEntityType entityType) {
		EntityTypeLink entityTypeLink = EntityTypeLink.T.create();
		entityTypeLink.setTypeSignature(entityType == null ? null : entityType.getTypeSignature());
	
		ViolationWithEntityType v = ViolationWithEntityType.T.create();
		v.setEntityTypeLink(entityTypeLink);
		violation = v;
		
		return this;
	}

	public ViolationBuilder withEnumType(GmEnumType enumType) {
		EnumTypeLink enumTypeLink = EnumTypeLink.T.create();
		enumTypeLink.setTypeSignature(enumType.getTypeSignature());
		
		ViolationWithEnumType v = ViolationWithEnumType.T.create();
		v.setEnumTypeLink(enumTypeLink);
		violation = v;
		
		return this;
	}
	
	public ViolationBuilder withProperty(GmEntityType containingEntityType, GmProperty property) {
		PropertyLink propertyLink = PropertyLink.T.create();
		propertyLink.setEntityTypeSignature(containingEntityType == null ? null : containingEntityType.getTypeSignature());
		propertyLink.setName(property == null ? null : property.getName());
		propertyLink.setTypeSignature(property == null ? null : 
			(property.getType() == null ? null : property.getType().getTypeSignature()));
		
		ViolationWithProperty v = ViolationWithProperty.T.create();
		v.setPropertyLink(propertyLink);
		violation = v;
		
		return this;
	}

	public ViolationBuilder withEnumConstant(GmEnumType enumType, GmEnumConstant enumConstant) {
		EnumConstantLink enumConstantLink = EnumConstantLink.T.create();
		enumConstantLink.setEnumTypeSignature(enumType.getTypeSignature());
		enumConstantLink.setName(enumConstant.getName());
		
		ViolationWithEnumConstant v = ViolationWithEnumConstant.T.create();
		v.setEnumConstantLink(enumConstantLink);
		violation = v;
		
		return this;
	}

	public ViolationBuilder withEntityTypeList(List<String> typeSignatures) {
		ArrayList<EntityTypeLink> entityTypeLinkList = new ArrayList<EntityTypeLink>();
		if (typeSignatures != null) {
			for (String ts : typeSignatures) {
				EntityTypeLink l = EntityTypeLink.T.create();
				l.setTypeSignature(ts);
				entityTypeLinkList.add(l);
			}
		}
		
		ViolationWithEnityTypeList v = ViolationWithEnityTypeList.T.create();
		v.setEntityTypeLinkList(entityTypeLinkList);
		violation = v;
		
		return this;
	}

}
