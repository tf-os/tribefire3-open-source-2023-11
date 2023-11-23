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
package com.braintribe.model.processing.validation.expert;

import java.util.List;
import java.util.stream.Collectors;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.meta.GmCollectionType;
import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.meta.GmEnumConstant;
import com.braintribe.model.meta.GmEnumType;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.meta.GmProperty;
import com.braintribe.model.meta.GmType;
import com.braintribe.model.meta.data.MetaData;
import com.braintribe.model.meta.override.GmCustomTypeOverride;
import com.braintribe.model.meta.override.GmPropertyOverride;
import com.braintribe.model.meta.selector.MetaDataSelector;
import com.braintribe.model.processing.validation.ValidationContext;
import com.braintribe.model.processing.validation.ValidationMessage;
import com.braintribe.model.processing.validation.ValidationMessageLevel;

public abstract class AbstractValidationTaskTest {

	protected List<ValidationMessage> extractErrorMessages(ValidationContext context) {
		return context.getValidationMessages().stream().filter(vm -> vm.getLevel().equals(ValidationMessageLevel.ERROR))
				.collect(Collectors.toList());
	}
	
	public interface TestMetaModel extends GmMetaModel {
		EntityType<TestMetaModel> T = EntityTypes.T(TestMetaModel.class);
	}

	public interface TestType extends GmType {
		EntityType<TestType> T = EntityTypes.T(TestType.class);
	}

	public interface TestTypeOverride extends GmCustomTypeOverride {
		EntityType<TestTypeOverride> T = EntityTypes.T(TestTypeOverride.class);
	}

	public interface TestEntityType extends GmEntityType {
		EntityType<TestEntityType> T = EntityTypes.T(TestEntityType.class);
	}

	public interface TestEnumType extends GmEnumType {
		EntityType<TestEnumType> T = EntityTypes.T(TestEnumType.class);
	}
	
	public interface TestCollectionType extends GmCollectionType {
		EntityType<TestCollectionType> T = EntityTypes.T(TestCollectionType.class);
	}

	public interface TestEnumConstant extends GmEnumConstant {
		EntityType<TestEnumConstant> T = EntityTypes.T(TestEnumConstant.class);
	}

	public interface TestProperty extends GmProperty {
		EntityType<TestProperty> T = EntityTypes.T(TestProperty.class);
	}
	
	public interface TestPropertyOverride extends GmPropertyOverride {
		EntityType<TestPropertyOverride> T = EntityTypes.T(TestPropertyOverride.class);
	}

	public interface TestMetaData extends MetaData {
		EntityType<TestMetaData> T = EntityTypes.T(TestMetaData.class);
	}

	public interface TestMetaDataSelector extends MetaDataSelector {
		EntityType<TestMetaDataSelector> T = EntityTypes.T(TestMetaDataSelector.class);
	}
}
