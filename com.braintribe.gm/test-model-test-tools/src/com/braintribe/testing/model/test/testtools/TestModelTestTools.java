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
package com.braintribe.testing.model.test.testtools;

import java.util.Arrays;
import java.util.List;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.util.meta.NewMetaModelGeneration;
import com.braintribe.testing.model.test.technical.features.AnotherComplexEntity;
import com.braintribe.testing.model.test.technical.features.CollectionEntity;
import com.braintribe.testing.model.test.technical.features.ComplexEntity;
import com.braintribe.testing.model.test.technical.features.DuplicateSimpleNameEntity;
import com.braintribe.testing.model.test.technical.features.EnumEntity;
import com.braintribe.testing.model.test.technical.features.ExtendedComplexEntity;
import com.braintribe.testing.model.test.technical.features.PrimitiveTypesEntity;
import com.braintribe.testing.model.test.technical.features.SimpleEntity;
import com.braintribe.testing.model.test.technical.features.SimpleEnum;
import com.braintribe.testing.model.test.technical.features.SimpleStandardIdentifiable;
import com.braintribe.testing.model.test.technical.features.SimpleTypesEntity;
import com.braintribe.testing.model.test.technical.features.UnidentifiableEntity;
import com.braintribe.testing.model.test.technical.features.instantiation.InstantiableSubSubSubTypeABA;
import com.braintribe.testing.model.test.technical.features.instantiation.InstantiableSubSubTypeAA;
import com.braintribe.testing.model.test.technical.features.instantiation.InstantiableSubSubTypeCA;
import com.braintribe.testing.model.test.technical.features.instantiation.InstantiableSubSubTypeCB;
import com.braintribe.testing.model.test.technical.features.instantiation.InstantiableSubTypeA;
import com.braintribe.testing.model.test.technical.features.instantiation.InstantiableSubTypeB;
import com.braintribe.testing.model.test.technical.features.instantiation.InstantiationTestEntity;
import com.braintribe.testing.model.test.technical.features.instantiation.UninstantiableSubSubTypeAB;
import com.braintribe.testing.model.test.technical.features.instantiation.UninstantiableSubTypeC;
import com.braintribe.testing.model.test.technical.features.instantiation.UninstantiableSuperType;
import com.braintribe.testing.model.test.technical.features.multipleinheritance.A;
import com.braintribe.testing.model.test.technical.features.multipleinheritance.AB;
import com.braintribe.testing.model.test.technical.features.multipleinheritance.ABC;
import com.braintribe.testing.model.test.technical.features.multipleinheritance.ABCDEF;
import com.braintribe.testing.model.test.technical.features.multipleinheritance.AB_BC_ABC;
import com.braintribe.testing.model.test.technical.features.multipleinheritance.AB_BC_but_not_ABC;
import com.braintribe.testing.model.test.technical.features.multipleinheritance.B;
import com.braintribe.testing.model.test.technical.features.multipleinheritance.BC;
import com.braintribe.testing.model.test.technical.features.multipleinheritance.C;
import com.braintribe.testing.model.test.technical.features.multipleinheritance.D;
import com.braintribe.testing.model.test.technical.features.multipleinheritance.E;
import com.braintribe.testing.model.test.technical.features.multipleinheritance.ExtendedABCDEF;
import com.braintribe.testing.model.test.technical.features.multipleinheritance.F;
import com.braintribe.testing.model.test.technical.features.multipleinheritance.F1;
import com.braintribe.testing.model.test.technical.features.multipleinheritance.F2;
import com.braintribe.testing.model.test.technical.features.multipleinheritance.FurtherExtendedABCDEF;
import com.braintribe.testing.model.test.technical.features.multipleinheritance.MultipleInheritanceEntity;
import com.braintribe.testing.model.test.technical.features.selectiveinformation.ExtendedSelectiveInformationEntityWithNewSelectiveInformation;
import com.braintribe.testing.model.test.technical.features.selectiveinformation.ExtendedSelectiveInformationEntityWithNoSelectiveInformation;
import com.braintribe.testing.model.test.technical.features.selectiveinformation.SelectiveInformationEntity;
import com.braintribe.testing.model.test.technical.limits.ManyPropertiesEntity;
import com.braintribe.testing.model.test.technical.limits.ManyValuesEnum;
import com.braintribe.testing.model.test.technical.naming.ExtreeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeemlyLongNameEntity;
import com.braintribe.testing.tools.gm.GmTestTools;

/**
 * This class provides TestModel related convenience methods that can be used in tests.
 * 
 * @author michael.lafite
 */
public class TestModelTestTools extends GmTestTools {

	// @formatter:off
	public static List<EntityType<?>> types = Arrays.asList(
			AnotherComplexEntity.T,
			CollectionEntity.T,
			ComplexEntity.T,
			EnumEntity.T,
			ExtendedComplexEntity.T,
			ExtreeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeemlyLongNameEntity.T,
			ManyPropertiesEntity.T,
			PrimitiveTypesEntity.T,
			SimpleEntity.T,
			SimpleStandardIdentifiable.T,
			SimpleTypesEntity.T,
			UnidentifiableEntity.T,
			InstantiableSubSubSubTypeABA.T,
			InstantiableSubSubTypeAA.T,
			InstantiableSubSubTypeCA.T,
			InstantiableSubSubTypeCB.T,
			InstantiableSubTypeA.T,
			InstantiableSubTypeB.T,
			InstantiationTestEntity.T,
			UninstantiableSubSubTypeAB.T,
			UninstantiableSubTypeC.T,
			UninstantiableSuperType.T,
			A.T,
			AB.T,
			ABC.T,
			ABCDEF.T,
			AB_BC_ABC.T,
			AB_BC_but_not_ABC.T,
			B.T,
			BC.T,
			C.T,
			D.T,
			E.T,
			ExtendedABCDEF.T,
			F.T,
			F1.T,
			F2.T,
			FurtherExtendedABCDEF.T,
			MultipleInheritanceEntity.T,
			ExtendedSelectiveInformationEntityWithNewSelectiveInformation.T,
			ExtendedSelectiveInformationEntityWithNoSelectiveInformation.T,
			SelectiveInformationEntity.T,
			DuplicateSimpleNameEntity.T,
			com.braintribe.testing.model.test.technical.features.duplicate.DuplicateSimpleNameEntity.T
	);
	// @formatter:on
	
	// @formatter:off
	public static List<Class<? extends Enum<?>>> enums = Arrays.asList(
			SimpleEnum.class,
			ManyValuesEnum.class
	);
	// @formatter:on

	
	protected TestModelTestTools() {
		// no instantiation required
	}


	public static GmMetaModel createTestModelMetaModel() {
		return new NewMetaModelGeneration().buildMetaModel("gm:TestModel", types);
	}

}
