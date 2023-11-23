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
package com.braintribe.model.processing.query.test.model;

import static com.braintribe.utils.lcd.CollectionTools2.asList;

import java.util.List;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.meta.data.constraint.Unique;
import com.braintribe.model.meta.data.query.Index;
import com.braintribe.model.meta.data.query.IndexType;
import com.braintribe.model.processing.meta.editor.BasicModelMetaDataEditor;
import com.braintribe.model.processing.meta.editor.ModelMetaDataEditor;
import com.braintribe.model.processing.query.test.model.diamond.DiamondBase;
import com.braintribe.model.processing.query.test.model.diamond.DiamondLeaf;
import com.braintribe.model.processing.query.test.model.diamond.DiamondLeft;
import com.braintribe.model.processing.query.test.model.diamond.DiamondRight;
import com.braintribe.model.processing.query.test.model.indexed.IndexedA;
import com.braintribe.model.processing.query.test.model.indexed.IndexedAB;
import com.braintribe.model.processing.query.test.model.indexed.IndexedASub;
import com.braintribe.model.processing.query.test.model.indexed.IndexedB;
import com.braintribe.model.processing.query.test.model.indexed.IndexedBSub;
import com.braintribe.model.util.meta.NewMetaModelGeneration;

public class MetaModelProvider {

	public static final List<EntityType<?>> types = asList( //
			Owner.T, //
			Company.T, //
			Rectangle.T, //

			IndexedASub.T, //
			IndexedBSub.T, //
			IndexedAB.T, //

			DiamondLeaf.T //
	);

	public static GmMetaModel provideEnrichedModel() {
		GmMetaModel model = provideRawModel();

		ModelMetaDataEditor mdEditor = new BasicModelMetaDataEditor(model);

		mdEditor.onEntityType(Person.T) //
				.addPropertyMetaData("indexedName", indexed()) //
				.addPropertyMetaData("indexedUniqueName", indexed(), unique()) //
				.addPropertyMetaData("indexedFriend", indexed()) //
				.addPropertyMetaData("indexedInteger", indexed()) //
				.addPropertyMetaData("indexedCompany", indexed());

		// #############################################

		mdEditor.onEntityType(Company.T) //
				.addPropertyMetaData("indexedName", indexed(), unique()) //
				.addPropertyMetaData("indexedDate", indexed());

		// This means when working with DiamondLeaf, we are unique in both DiamondLeft and DiamongRight, which are independent
		// See CorruptManipulationTests#diamondUnique
		mdEditor.onEntityType(DiamondLeft.T) //
				.addPropertyMetaData(DiamondBase.baseProperty, indexed(), unique());
		mdEditor.onEntityType(DiamondRight.T) //
				.addPropertyMetaData(DiamondBase.baseProperty, indexed(), unique());

		mdEditor.onEntityType(IndexedA.T) //
				.addPropertyMetaData(IndexedA.unique, indexed()) //
				.addPropertyMetaData(IndexedA.ambig, indexed()) //
				.addPropertyMetaData(IndexedA.metric, metricIndexed()) //
		;

		mdEditor.onEntityType(IndexedB.T) //
				.addPropertyMetaData(IndexedB.unique, indexed(), unique()) //
				.addPropertyMetaData(IndexedA.ambig, indexed()) //
				.addPropertyMetaData(IndexedA.metric, metricIndexed()) //
		;

		return model;
	}

	public static GmMetaModel provideRawModel() {
		return new NewMetaModelGeneration().buildMetaModel("com.braintribe.model:SmoodTestModel#2.0", types);
	}

	private static Index metricIndexed() {
		Index index = indexed();
		index.setIndexType(IndexType.metric);
		return index;
	}

	private static Index indexed() {
		Index ip = Index.T.create();
		return ip;
	}

	private static Unique unique() {
		return Unique.T.create();
	}

}
