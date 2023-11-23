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
package com.braintribe.model.access.security.testdata.query;

import static com.braintribe.model.access.security.testdata.MdFactory.administrable;
import static com.braintribe.model.access.security.testdata.MdFactory.confidential;
import static com.braintribe.model.access.security.testdata.MdFactory.invisible;
import static com.braintribe.model.access.security.testdata.MdFactory.nonQueryable;
import static com.braintribe.model.access.security.testdata.MdFactory.useCaseSelector;
import static com.braintribe.utils.lcd.CollectionTools2.asList;

import java.util.ArrayList;
import java.util.List;

import com.braintribe.model.access.security.common.AbstractSecurityAspectTest;
import com.braintribe.model.acl.Acl;
import com.braintribe.model.acl.HasAcl;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.meta.data.MetaData;
import com.braintribe.model.meta.selector.ConjunctionSelector;
import com.braintribe.model.meta.selector.MetaDataSelector;
import com.braintribe.model.meta.selector.RoleSelector;
import com.braintribe.model.meta.selector.UseCaseSelector;
import com.braintribe.model.processing.meta.editor.BasicModelMetaDataEditor;
import com.braintribe.model.processing.meta.editor.ModelMetaDataEditor;
import com.braintribe.model.util.meta.NewMetaModelGeneration;
import com.braintribe.utils.lcd.CollectionTools;

/**

 * 

 */
public class SecurityAspectQueryTestModel {

	// @formatter:off
	public static final List<EntityType<?>> types = asList(
			AclEntity.T,
			AclPropsOwner.T,
			
			NonQueryableEntity.T,
			NonVisibleEntity.T,
			
			EntityWithProps.T		
	);
	// @formatter:on

	private static final GmMetaModel accessControlModel = Acl.T.getModel().getMetaModel();

	public static GmMetaModel enriched() {
		GmMetaModel rawModel = new NewMetaModelGeneration().buildMetaModel("gm:security-query-test-model", types, asList(accessControlModel));

		ModelMetaDataEditor mdEditor = new BasicModelMetaDataEditor(rawModel);

		// ####################################
		// ## . . Basic Visible/Queryable . .##
		// ####################################

		mdEditor.addModelMetaData(forRoles(invisible(), AbstractSecurityAspectTest.MODEL_IGNORER));

		mdEditor.onEntityType(NonQueryableEntity.T) //
				.addMetaData(nonQueryable());

		mdEditor.onEntityType(NonVisibleEntity.T) //
				.addMetaData(invisible());

		// ####################################
		// ## . . . . . . ACL . . . . . . . .##
		// ####################################

		UseCaseSelector aclUseCase = useCaseSelector("acl");

		mdEditor.onEntityType(Acl.T) //
				.addMetaData(administrable(AbstractSecurityAspectTest.ADMINISTERING_ACL_ROLE, aclUseCase));

		mdEditor.onEntityType(HasAcl.T) //
				.addMetaData(administrable(AbstractSecurityAspectTest.ADMINISTERING_HAS_ACL_ROLE, aclUseCase));

		// ####################################
		// ## . . . . . Property . . . . . . ##
		// ####################################

		mdEditor.onEntityType(EntityWithProps.T) //
				.addPropertyMetaData("password", confidential());

		return rawModel;
	}

	private static <T extends MetaData> T forRoles(T md, String... roles) {
		RoleSelector selector = RoleSelector.T.create();
		selector.setRoles(CollectionTools.getSet(roles));

		return withSelector(md, selector);
	}

	private static <T extends MetaData> T withSelector(T md, MetaDataSelector selector) {
		MetaDataSelector originalSelector = md.getSelector();

		if (originalSelector == null) {
			md.setSelector(selector);

		} else if (originalSelector instanceof ConjunctionSelector) {
			((ConjunctionSelector) originalSelector).getOperands().add(selector);

		} else {
			ConjunctionSelector cs = ConjunctionSelector.T.create();
			cs.setOperands(new ArrayList<MetaDataSelector>());
			cs.getOperands().add(originalSelector);
			cs.getOperands().add(selector);
		}

		return md;
	}

}
