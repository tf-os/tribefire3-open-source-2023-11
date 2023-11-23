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
package com.braintribe.product.rat.imp.impl.model;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.meta.GmType;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.product.rat.imp.ImpException;
import com.braintribe.utils.lcd.Arguments;

abstract class AbstractCustomTypeImpCave<I extends SimpleTypeImp<T>, T extends GmType> extends AbstractTypeImpCave<T, I> {

	AbstractCustomTypeImpCave(PersistenceGmSession session, EntityType<T> typeOfT) {
		super(session, typeOfT);

		Arguments.notNullWithNames("typeOfT", typeOfT, "session", session);

	}

	/**
	 * creates a new type with the passed typeSignature and adds it to the passed metamodel
	 *
	 * @param typeSignature
	 *            for the newly created type
	 * @param declaringModel
	 *            of the newly created type
	 */
	public I create(String typeSignature, GmMetaModel declaringModel) {

		if (find(typeSignature).isPresent()) {
			throw new ImpException("Type with this name already exists: " + typeSignature + ". Consider using the withName() method");
		}

		if (!typeSignature.contains(".")) {
			throw new ImpException("Please specify a full name including groupId (including at least one '.'). You supplied " + typeSignature);
		}

		T type = session().create(typeOfT);
		type.setTypeSignature(typeSignature);

		type.setDeclaringModel(declaringModel);
		declaringModel.getTypes().add(type);

		return with(type);
	}

	/**
	 * creates a new type with the passed typeSignature ('groupId.typeName') and adds it to the passed metamodel
	 */
	public I create(String groupId, String typeName, GmMetaModel declaringModel) {
		String typeSignature = groupId + "." + typeName;

		return create(typeSignature, declaringModel);
	}

	/**
	 * @param groupId
	 *            of the already existing type
	 * @param typeName
	 *            of the already existing type
	 * @return an imp for managing the specified type
	 * @throws ImpException
	 *             when no type could be found with provided groupId and typeName
	 */
	public I withName(String groupId, String typeName) {
		String typeSignature = groupId + "." + typeName;

		return with(typeSignature);
	}

}
