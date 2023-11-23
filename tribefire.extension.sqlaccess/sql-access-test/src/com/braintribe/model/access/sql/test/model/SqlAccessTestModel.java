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
package com.braintribe.model.access.sql.test.model;

import static com.braintribe.utils.lcd.CollectionTools2.asList;

import java.util.ArrayList;
import java.util.Set;

import com.braintribe.model.access.sql.test.model.basic.BasicEntity;
import com.braintribe.model.access.sql.test.model.basic.BasicScalarEntity;
import com.braintribe.model.access.sql.test.model.collections.CollectionsEntity;
import com.braintribe.model.access.sql.test.model.diamond.DiamondFriend;
import com.braintribe.model.access.sql.test.model.diamond.DiamondTail;
import com.braintribe.model.access.sql.test.model.object.ObjectPropertiesEntity;
import com.braintribe.model.access.sql.test.model.tree.TreeLeft;
import com.braintribe.model.access.sql.test.model.tree.TreeReferee;
import com.braintribe.model.access.sql.test.model.tree.TreeRight;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.Model;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.util.meta.NewMetaModelGeneration;
import com.braintribe.utils.lcd.CollectionTools2;

/**
 * @author peter.gazdik
 */
public class SqlAccessTestModel {

	// @formatter:off
	public static final Set<EntityType<?>> types =  CollectionTools2.<EntityType<?>>asSet (
			BasicEntity.T,
			BasicScalarEntity.T,
			
			CollectionsEntity.T,
			
			TreeLeft.T,
			TreeRight.T,
			TreeReferee.T,
			
			DiamondTail.T,
			DiamondFriend.T,
			
			ObjectPropertiesEntity.T			
			
	);
	// @formatter:on

	public static GmMetaModel raw() {
		Model rootModel = NewMetaModelGeneration.rootModel();
		Model i18nModel = GMF.getTypeReflection().getModel("com.braintribe.gm:i18n-model");

		ArrayList<Model> initialModels = asList(rootModel, i18nModel);
		ArrayList<GmMetaModel> dependencies = asList(i18nModel.getMetaModel());

		return new NewMetaModelGeneration(initialModels).withValidation().buildMetaModel("test:SqlAccessModel", types, dependencies);
	}

}
