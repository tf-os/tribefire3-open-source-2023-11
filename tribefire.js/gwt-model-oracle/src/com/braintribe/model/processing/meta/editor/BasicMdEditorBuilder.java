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
import com.braintribe.model.meta.GmType;
import com.braintribe.model.meta.data.MetaData;
import com.braintribe.model.meta.override.GmEntityTypeOverride;
import com.braintribe.model.processing.meta.editor.leaf.LeafModel;

/**
 * @author peter.gazdik
 */
public class BasicMdEditorBuilder {

	/* package */ final GmMetaModel model;
	/* package */ Function<EntityType<?>, GenericEntity> entityFactory = EntityType::create;
	/* package */ Predicate<? super GmModelElement> wasEntityUninstantiated = entity -> false;
	/* package */ GlobalIdFactory globalIdFactory = null;
	/* package */ boolean appendToDeclaration;
	/* package */ boolean typeLenient;

	public BasicMdEditorBuilder(GmMetaModel model) {
		this.model = model;
	}

	/**
	 * A function for creating the model element overrides (e.g. {@link GmEntityTypeOverride}). Typically this is used when you want to create these
	 * instances on a session, so the caller would do something like this: {@code new BasiModelMetaDataEditor(model, session::create)}.
	 */
	public BasicMdEditorBuilder withEtityFactory(Function<EntityType<?>, GenericEntity> entityFactory) {
		this.entityFactory = entityFactory;
		return this;
	}

	/**
	 * A predicate which tests whether a given model element should be re-create even if it was found in the internal cache. This is relevant in case
	 * somebody does a rollback on a session, thus
	 */
	public BasicMdEditorBuilder withWasEntityUninstantiatedTest(Predicate<? super GmModelElement> wasEntityUninstantiated) {
		this.wasEntityUninstantiated = wasEntityUninstantiated;
		return this;
	}

	/**
	 * Configures given session as the resolver for {@link #withEtityFactory(Function) entityFactory} and
	 * {@link #withWasEntityUninstantiatedTest(Predicate) entityUninstantiatedTest} functionality.
	 */
	public BasicMdEditorBuilder withSession(GmSession session) {
		return withEtityFactory(session::create).withWasEntityUninstantiatedTest(entity -> entity.session() != session);
	}

	/**
	 * A function for creating globalId for the created model elements (see {@link #withEtityFactory(Function)}), in case the caller also wants to
	 * control this. The function takes two parameters - the {@link GmModelElement} which we are overriding, and a {@link OverrideType} as a
	 * convenient way to describe what kind of element we are overriding. If <code>null</code> value is provided, default implementation is taken -
	 * see {@link LeafModel#deriveGlobalId}
	 */
	public BasicMdEditorBuilder withGlobalIdFactory(GlobalIdFactory globalIdFactory) {
		this.globalIdFactory = globalIdFactory;
		return this;
	}

	/**
	 * If set to false, all the new {@link MetaData} is added to this model - either by attaching it on the contained types, or on the acquired type
	 * overrides. Otherwise the MD is attached directly on the {@link GmType}.
	 */
	public BasicMdEditorBuilder setAppendToDeclaration(boolean appendToDeclaration) {
		this.appendToDeclaration = appendToDeclaration;
		return this;
	}

	/**
	 * If <tt>typeLenient</tt> is set to <tt>true</tt>, attempts to add metaData on entity and enum types that are not part of the underlying model
	 * will be silently ignored.
	 */
	public BasicMdEditorBuilder typeLenient(boolean typeLenient) {
		this.typeLenient = typeLenient;
		return this;
	}

	public BasicModelMetaDataEditor done() {
		return new BasicModelMetaDataEditor(this);
	}

}
