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
package com.braintribe.model.processing.meta.editor;

import java.util.function.Function;
import java.util.function.Predicate;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.session.GmSession;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.meta.GmModelElement;
import com.braintribe.model.processing.meta.editor.leaf.LeafModel;

/**
 * @author peter.gazdik
 */
public class BasicMdEditorBuilder implements MetaDataEditorBuilder {

	/* package */ final GmMetaModel model;
	/* package */ Function<EntityType<?>, GenericEntity> entityFactory = EntityType::create;
	/* package */ Predicate<? super GmModelElement> wasEntityUninstantiated = entity -> false;
	/* package */ GlobalIdFactory globalIdFactory = null;
	/* package */ boolean appendToDeclaration;
	/* package */ boolean typeLenient;

	public BasicMdEditorBuilder(GmMetaModel model) {
		this(model, false);
	}

	public BasicMdEditorBuilder(GmMetaModel model, boolean useModelSessionIfAttached) {
		this.model = model;

		if (useModelSessionIfAttached && model.session() != null)
			withSession(model.session());
	}

	/** {@inheritDoc} */
	@Override
	public BasicMdEditorBuilder withEtityFactory(Function<EntityType<?>, GenericEntity> entityFactory) {
		this.entityFactory = entityFactory;
		return this;
	}

	/** {@inheritDoc} */
	@Override
	public BasicMdEditorBuilder withWasEntityUninstantiatedTest(Predicate<? super GmModelElement> wasEntityUninstantiated) {
		this.wasEntityUninstantiated = wasEntityUninstantiated;
		return this;
	}

	/** {@inheritDoc} */
	@Override
	public BasicMdEditorBuilder withSession(GmSession session) {
		return withEtityFactory(session::create).withWasEntityUninstantiatedTest(entity -> entity.session() != session);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see LeafModel#deriveGlobalId
	 */
	@Override
	public BasicMdEditorBuilder withGlobalIdFactory(GlobalIdFactory globalIdFactory) {
		this.globalIdFactory = globalIdFactory;
		return this;
	}

	/** {@inheritDoc} */
	@Override
	public BasicMdEditorBuilder setAppendToDeclaration(boolean appendToDeclaration) {
		this.appendToDeclaration = appendToDeclaration;
		return this;
	}

	/** {@inheritDoc} */
	@Override
	public BasicMdEditorBuilder typeLenient(boolean typeLenient) {
		this.typeLenient = typeLenient;
		return this;
	}

	@Override
	public BasicModelMetaDataEditor done() {
		return new BasicModelMetaDataEditor(this);
	}

}
