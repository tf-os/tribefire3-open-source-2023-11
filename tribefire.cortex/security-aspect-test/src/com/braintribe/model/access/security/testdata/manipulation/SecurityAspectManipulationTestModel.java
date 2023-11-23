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
package com.braintribe.model.access.security.testdata.manipulation;

import static com.braintribe.model.access.security.testdata.MdFactory.administrable;
import static com.braintribe.model.access.security.testdata.MdFactory.entityDeletionDisabled;
import static com.braintribe.model.access.security.testdata.MdFactory.instantiationDisabled;
import static com.braintribe.model.access.security.testdata.MdFactory.mandatory;
import static com.braintribe.model.access.security.testdata.MdFactory.nonModifiable;
import static com.braintribe.model.access.security.testdata.MdFactory.unique;
import static com.braintribe.model.access.security.testdata.MdFactory.useCaseSelector;
import static com.braintribe.utils.lcd.CollectionTools2.asList;

import java.util.List;

import com.braintribe.model.access.security.common.AbstractSecurityAspectTest;
import com.braintribe.model.access.security.testdata.query.AclEntity;
import com.braintribe.model.access.security.testdata.query.AclPropsOwner;
import com.braintribe.model.acl.Acl;
import com.braintribe.model.acl.HasAcl;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.meta.selector.UseCaseSelector;
import com.braintribe.model.processing.meta.editor.BasicModelMetaDataEditor;
import com.braintribe.model.processing.meta.editor.ModelMetaDataEditor;
import com.braintribe.model.util.meta.NewMetaModelGeneration;

public class SecurityAspectManipulationTestModel {

	// @formatter:off
	public static final List<EntityType<?>> types = asList(
			AclEntity.T,
			AclPropsOwner.T,

			EntityWithConstraints.T,
			EntityWithPropertyConstraints.T
	);
	// @formatter:on

	private static final GmMetaModel accessControlModel = Acl.T.getModel().getMetaModel();

	public static GmMetaModel enriched() {
		GmMetaModel rawModel = new NewMetaModelGeneration().buildMetaModel("gm:security-manipulation-test-model", types, asList(accessControlModel));

		ModelMetaDataEditor mdEditor = new BasicModelMetaDataEditor(rawModel);

		mdEditor.onEntityType(EntityWithPropertyConstraints.T) //
				.addPropertyMetaData("mandatory", mandatory()) //
				.addPropertyMetaData("unique", unique()) //
				.addPropertyMetaData("uniqueEntity", unique()) //
				.addPropertyMetaData("nonModifiable", nonModifiable()) //
				.addPropertyMetaData("nonModifiableButMandatory", nonModifiable(), mandatory()) //
		;

		mdEditor.onEntityType(EntityWithConstraints.T) //
				.addMetaData(instantiationDisabled(), entityDeletionDisabled());

		// ####################################
		// ## . . . . . . ACL . . . . . . . .##
		// ####################################

		UseCaseSelector aclUseCase = useCaseSelector("acl");

		mdEditor.onEntityType(Acl.T) //
				.addMetaData(administrable(AbstractSecurityAspectTest.ADMINISTERING_ACL_ROLE, aclUseCase));

		mdEditor.onEntityType(HasAcl.T) //
				.addMetaData(administrable(AbstractSecurityAspectTest.ADMINISTERING_HAS_ACL_ROLE, aclUseCase));

		return rawModel;
	}

}
