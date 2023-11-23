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
package com.braintribe.model.processing.cmd.test.provider;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.meta.data.EntityTypeMetaData;
import com.braintribe.model.meta.selector.BooleanPropertyDiscriminator;
import com.braintribe.model.meta.selector.DatePropertyDiscriminator;
import com.braintribe.model.meta.selector.IntegerPropertyDiscriminator;
import com.braintribe.model.meta.selector.LongDiscriminatorValue;
import com.braintribe.model.meta.selector.MetaDataSelector;
import com.braintribe.model.meta.selector.NullPropertyDiscriminator;
import com.braintribe.model.meta.selector.SimplePropertyDiscriminator;
import com.braintribe.model.meta.selector.StringPropertyDiscriminator;
import com.braintribe.model.meta.selector.StringRegexPropertyDiscriminator;
import com.braintribe.model.processing.cmd.test.meta.entity.AllExpertsMetaData;
import com.braintribe.model.processing.cmd.test.meta.entity.EntityTypeRelatedEntityMetaData;
import com.braintribe.model.processing.cmd.test.meta.entity.ExtendedEntityMetaData;
import com.braintribe.model.processing.cmd.test.meta.entity.ExtendedEntityOverrideMetaData;
import com.braintribe.model.processing.cmd.test.meta.entity.InstanceRelatedEntityMetaData;
import com.braintribe.model.processing.cmd.test.meta.entity.MultiInheritedMetaData;
import com.braintribe.model.processing.cmd.test.meta.entity.SimpleEntityMetaData;
import com.braintribe.model.processing.cmd.test.meta.entity.SimpleInheritedMetaData;
import com.braintribe.model.processing.cmd.test.meta.entity.SimpleInheritedSelectorMetaData;
import com.braintribe.model.processing.cmd.test.meta.entity.SimpleNotInheritableMetaData;
import com.braintribe.model.processing.cmd.test.meta.selector.InstancePresentSelector;
import com.braintribe.model.processing.cmd.test.meta.selector.PropertyDeclaredSelector;
import com.braintribe.model.processing.cmd.test.model.Person;
import com.braintribe.model.processing.cmd.test.model.ServiceProvider;
import com.braintribe.model.processing.cmd.test.model.Teacher;
import com.braintribe.model.processing.meta.oracle.EntityTypeOracle;

/**
 * 
 */
public class EntityMdProvider extends AbstractModelSupplier {

	private EntityTypeOracle currentEntityTypeOracle;

	@Override
	protected void addMetaData() {
		addSimpleEntityMd();
		addInheritedNoSelectorMd();
		addNotInheritableMd();
		addInheritedYesSelectorMd();
		addMultiInheritedMd();
		addEntityTypeSelectorMd();
		addSimpleInstanceMd();
		addAllExpertsMD();
		addMdForExtendedInfo();
		addEntityOverrideExtendedMd();
	}

	private void addSimpleEntityMd() {
		baseMdEditor.onEntityType(Person.T) //
				.addMetaData(newMd(SimpleEntityMetaData.T, true));
	}

	/** MD for Teacher will also contain MD defined for Person */
	private void addInheritedNoSelectorMd() {
		baseMdEditor.onEntityType(Person.T) //
				.addMetaData(newMd(SimpleInheritedMetaData.T, true, Person.T));

		fullMdEditor.onEntityType(Teacher.T) //
				.addMetaData(newMd(SimpleInheritedMetaData.T, true, Teacher.T));
	}

	/** MD for Teacher will not contain MD defined for Person */
	private void addNotInheritableMd() {
		baseMdEditor.onEntityType(Person.T) //
				.addMetaData(notInheritable(false, Person.T));

		fullMdEditor.onEntityType(Teacher.T) //
				.addMetaData(notInheritable(true, Teacher.T));
	}

	private EntityTypeMetaData notInheritable(boolean active, EntityType<?> entityType) {
		SimpleNotInheritableMetaData md = newMd(SimpleNotInheritableMetaData.T, active, entityType);
		md.setInherited(false);
		return md;
	}

	private void addInheritedYesSelectorMd() {
		baseMdEditor.onEntityType(Person.T) //
				.addMetaData(append(newMd(SimpleInheritedSelectorMetaData.T, false, Person.T), FALSE_SELECTOR));

		fullMdEditor.onEntityType(Teacher.T) //
				.addMetaData(append(newMd(SimpleInheritedSelectorMetaData.T, true, Teacher.T), TRUE_SELECTOR));

		baseMdEditor.onEntityType(ServiceProvider.T) //
				.addMetaData(append(newMd(SimpleInheritedSelectorMetaData.T, true, ServiceProvider.T), TRUE_SELECTOR));
	}

	private void addMultiInheritedMd() {
		baseMdEditor.onEntityType(GenericEntity.T) //
				.addMetaData(newMd(MultiInheritedMetaData.T, true, GenericEntity.T));

		baseMdEditor.onEntityType(Person.T) //
				.addMetaData(newMd(MultiInheritedMetaData.T, true, Person.T));

		baseMdEditor.onEntityType(ServiceProvider.T) //
				.addMetaData(newMd(MultiInheritedMetaData.T, true, ServiceProvider.T));
	}

	private void addEntityTypeSelectorMd() {
		PropertyDeclaredSelector pps = PropertyDeclaredSelector.T.create();
		pps.setPropertyName("age");

		baseMdEditor.onEntityType(GenericEntity.T) //
				.addMetaData(append(newMd(EntityTypeRelatedEntityMetaData.T, true), pps));
	}

	private void addSimpleInstanceMd() {
		baseMdEditor.onEntityType(Person.T) //
				.addMetaData(append(newMd(InstanceRelatedEntityMetaData.T, true), InstancePresentSelector.T.create()));
	}

	private void addAllExpertsMD() {
		currentEntityTypeOracle = baseModelOracle.getEntityTypeOracle(Person.T);

		/* I want to set all selectors so that they are evaluated as false -> so we go through all of them */
		BooleanPropertyDiscriminator boolPd = newSpd(BooleanPropertyDiscriminator.T, "isAlive");
		boolPd.setDiscriminatorValue(true);

		DatePropertyDiscriminator datePd = newSpd(DatePropertyDiscriminator.T, "birthDate");

		IntegerPropertyDiscriminator intPd = newSpd(IntegerPropertyDiscriminator.T, "age");
		intPd.setDiscriminatorValue(568);

		LongDiscriminatorValue longPd = newSpd(LongDiscriminatorValue.T, "longValue");
		longPd.setDiscriminatorValue(989);

		StringPropertyDiscriminator stringPd = newSpd(StringPropertyDiscriminator.T, "name");
		stringPd.setDiscriminatorValue("strValue");

		NullPropertyDiscriminator nullPd = newSpd(NullPropertyDiscriminator.T, "name");
		nullPd.setInverse(true);

		StringRegexPropertyDiscriminator stringRegExPd = newSpd(StringRegexPropertyDiscriminator.T, "name");

		MetaDataSelector selectors = or(boolPd, datePd, intPd, longPd, stringPd, nullPd, stringRegExPd, useCase("gui"));

		baseMdEditor.onEntityType(Person.T) //
				.addMetaData(append(AllExpertsMetaData.T.create(), selectors));
	}

	private <T extends SimplePropertyDiscriminator> T newSpd(EntityType<T> et, String propertyName) {
		T spd = et.create();
		spd.setDiscriminatorProperty(currentEntityTypeOracle.getProperty(propertyName).asGmProperty());

		return spd;
	}

	private void addMdForExtendedInfo() {
		fullMdEditor.onEntityType(Teacher.T) //
				.addMetaData(newMd(ExtendedEntityMetaData.T, true));

		baseMdEditor.onEntityType(Person.T) //
				.addMetaData(newMd(ExtendedEntityMetaData.T, true));
	}

	private void addEntityOverrideExtendedMd() {
		fullMdEditor.onEntityType(Person.T) //
				.addMetaData(newMd(ExtendedEntityOverrideMetaData.T, true));
	}

}
