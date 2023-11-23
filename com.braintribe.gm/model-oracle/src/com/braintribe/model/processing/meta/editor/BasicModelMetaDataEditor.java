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

import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EnumType;
import com.braintribe.model.generic.reflection.GenericModelException;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.meta.data.MetaData;
import com.braintribe.model.meta.info.GmEntityTypeInfo;
import com.braintribe.model.meta.info.GmEnumConstantInfo;
import com.braintribe.model.meta.info.GmEnumTypeInfo;
import com.braintribe.model.meta.info.GmPropertyInfo;
import com.braintribe.model.processing.index.Index;
import com.braintribe.model.processing.index.SingleThreadIndex;
import com.braintribe.model.processing.meta.editor.empty.EmptyEntityTypeMetaDataEditor;
import com.braintribe.model.processing.meta.editor.empty.EmptyEnumTypeMetaDataEditor;
import com.braintribe.model.processing.meta.editor.leaf.LeafModel;
import com.braintribe.model.processing.meta.oracle.flat.FlatCustomType;
import com.braintribe.model.processing.meta.oracle.flat.FlatEntityType;
import com.braintribe.model.processing.meta.oracle.flat.FlatEnumType;
import com.braintribe.model.processing.meta.oracle.flat.FlatModel;
import com.braintribe.model.processing.meta.oracle.flat.FlatModelFactory;
import com.braintribe.utils.lcd.StringTools;

/**
 * Basic MD editor. To instantiate use the {@link #create(GmMetaModel)} method, which returns a {@link BasicMdEditorBuilder}. See this builder for
 * details on how to configure this MD editor.
 * 
 * @author peter.gazdik
 */
public class BasicModelMetaDataEditor implements ModelMetaDataEditor {

	protected final FlatModel flatModel;
	protected final LeafModel leafModel;
	protected boolean appendToDeclaration;
	protected final boolean typeLenient;

	private final Index<String, EntityTypeMetaDataEditor> entityEditorsIndex = new EntityEditorsIndex();
	private final Index<String, EnumTypeMetaDataEditor> enumyEditorsIndex = new EnumEditorsIndex();

	/**
	 * Returns a {@link BasicMdEditorBuilder}, which handles the configuration of a editor instance for given model.
	 * <p>
	 * For convenience, if given model is {@link GenericEntity#session() attached} to a session, the {@link BasicMdEditorBuilder#withSession} is
	 * automatically called with this session. To suppress this behavior, instead of calling this static method start with
	 * {@code new BasicMdEditorBuilder(model, false) }.
	 */
	public static BasicMdEditorBuilder create(GmMetaModel model) {
		return new BasicMdEditorBuilder(model);
	}

	/** Equivalent to {@code #create(model).done()}. */
	public BasicModelMetaDataEditor(GmMetaModel model) {
		this(create(model));
	}

	/**
	 * Equivalent to {@code new(model, entityFactory, null)}.
	 * 
	 * @deprecated use {@code #create(model).withEntityFactory(entityFactory).done()}.
	 */
	@Deprecated
	public BasicModelMetaDataEditor(GmMetaModel model, Function<EntityType<?>, GenericEntity> entityFactory) {
		this(create(model).withEtityFactory(entityFactory));
	}

	/** @deprecated use {@link #create(GmMetaModel)} with options. */
	@Deprecated
	public BasicModelMetaDataEditor(GmMetaModel model, Function<EntityType<?>, GenericEntity> entityFactory, GlobalIdFactory globalIdFactory) {
		this(create(model).withEtityFactory(entityFactory).withGlobalIdFactory(globalIdFactory));
	}

	/* package */ BasicModelMetaDataEditor(BasicMdEditorBuilder b) {
		this.flatModel = FlatModelFactory.buildFor(b.model);
		this.leafModel = new LeafModel(b.model, b.entityFactory, b.wasEntityUninstantiated, b.globalIdFactory);
		this.appendToDeclaration = b.appendToDeclaration;
		this.typeLenient = b.typeLenient;
	}

	/** @deprecated use {@link #create(GmMetaModel)} with options. */
	@Deprecated
	public void setAppendToDeclaration(@SuppressWarnings("unused") boolean appendToDeclaration) {
		throw new UnsupportedOperationException(
				"This method is no longer supported. Please use the create() method to create a new MD editor and configure this property that way.");
	}

	class EntityEditorsIndex extends SingleThreadIndex<String, EntityTypeMetaDataEditor> {
		@Override
		protected EntityTypeMetaDataEditor provideValueFor(String typeSignature) {
			FlatEntityType flatEntityType = getFlatCustomType(typeSignature);
			if (flatEntityType != null)
				return new BasicEntityTypeMetaDataEditor(BasicModelMetaDataEditor.this, flatEntityType);
			else if (typeLenient)
				return EmptyEntityTypeMetaDataEditor.INSTANCE;
			else
				throw new GenericModelException("Entity type '" + typeSignature + "' not found in model: " + flatModel.model.getName());
		}
	}

	class EnumEditorsIndex extends SingleThreadIndex<String, EnumTypeMetaDataEditor> {
		@Override
		protected EnumTypeMetaDataEditor provideValueFor(String typeSignature) {
			FlatEnumType flatEnumType = getFlatCustomType(typeSignature);
			if (flatEnumType != null)
				return new BasicEnumTypeMetaDataEditor(BasicModelMetaDataEditor.this, flatEnumType);
			else if (typeLenient)
				return EmptyEnumTypeMetaDataEditor.INSTANCE;
			else
				throw new GenericModelException("Enum type '" + typeSignature + "' not found in model: " + flatModel.model.getName());
		}
	}

	private <T extends FlatCustomType<?, ?>> T getFlatCustomType(String customTypeSignature) {
		return (T) flatModel.flatCustomTypes.get(customTypeSignature);
	}

	@Override
	public GmMetaModel getMetaModel() {
		return flatModel.model;
	}

	@Override
	public void addModelMetaData(MetaData... mds) {
		add(flatModel.model.getMetaData(), mds);
	}

	@Override
	public void addModelMetaData(Iterable<? extends MetaData> mds) {
		add(flatModel.model.getMetaData(), mds);
	}

	@Override
	public void removeModelMetaData(Predicate<? super MetaData> filter) {
		remove(flatModel.model.getMetaData(), filter);
	}

	@Override
	public void addEnumMetaData(MetaData... mds) {
		add(flatModel.model.getEnumTypeMetaData(), mds);
	}

	@Override
	public void addEnumMetaData(Iterable<? extends MetaData> mds) {
		add(flatModel.model.getEnumTypeMetaData(), mds);
	}

	@Override
	public void removeEnumMetaData(Predicate<? super MetaData> filter) {
		remove(flatModel.model.getEnumTypeMetaData(), filter);
	}

	@Override
	public void addConstantMetaData(MetaData... mds) {
		add(flatModel.model.getEnumConstantMetaData(), mds);
	}

	@Override
	public void addConstantMetaData(Iterable<? extends MetaData> mds) {
		add(flatModel.model.getEnumConstantMetaData(), mds);
	}

	@Override
	public void removeConstantMetaData(Predicate<? super MetaData> filter) {
		remove(flatModel.model.getEnumConstantMetaData(), filter);
	}

	@Override
	public EntityTypeMetaDataEditor onEntityType(String typeSignature) {
		return entityEditorsIndex.acquireFor(typeSignature);
	}

	@Override
	public EntityTypeMetaDataEditor onEntityType(EntityType<?> entityType) {
		return entityEditorsIndex.acquireFor(entityType.getTypeSignature());
	}

	@Override
	public EntityTypeMetaDataEditor onEntityType(GmEntityTypeInfo gmEntityTypeInfo) {
		return entityEditorsIndex.acquireFor(gmEntityTypeInfo.addressedType().getTypeSignature());
	}

	@Override
	public EntityTypeMetaDataEditor addPropertyMetaData(GmPropertyInfo gmPropertyInfo, MetaData... mds) {
		return onEntityType(gmPropertyInfo.declaringTypeInfo()).addPropertyMetaData(gmPropertyInfo, mds);
	}

	@Override
	public EntityTypeMetaDataEditor addPropertyMetaData(GmPropertyInfo gmPropertyInfo, Iterable<? extends MetaData> mds) {
		return onEntityType(gmPropertyInfo.declaringTypeInfo()).addPropertyMetaData(gmPropertyInfo, mds);
	}

	@Override
	public EnumTypeMetaDataEditor onEnumType(String typeSignature) {
		return enumyEditorsIndex.acquireFor(typeSignature);
	}

	@Override
	public EnumTypeMetaDataEditor onEnumType(EnumType enumType) {
		return enumyEditorsIndex.acquireFor(enumType.getTypeSignature());
	}

	@Override
	public EnumTypeMetaDataEditor onEnumType(GmEnumTypeInfo gmEnumTypeInfo) {
		return enumyEditorsIndex.acquireFor(gmEnumTypeInfo.addressedType().getTypeSignature());
	}

	@Override
	public EnumTypeMetaDataEditor onEnumType(Class<? extends Enum<?>> enumClass) {
		return enumyEditorsIndex.acquireFor(enumClass.getName());
	}

	@Override
	public EnumTypeMetaDataEditor addConstantMetaData(Class<? extends Enum<?>> enumType, MetaData... mds) {
		return onEnumType(enumType).addConstantMetaData(mds);
	}

	@Override
	public EnumTypeMetaDataEditor addConstantMetaData(Class<? extends Enum<?>> enumType, Iterable<? extends MetaData> mds) {
		return onEnumType(enumType).addConstantMetaData(mds);
	}

	@Override
	public EnumTypeMetaDataEditor addConstantMetaData(Enum<?> constant, MetaData... mds) {
		return onEnumType(constant.getDeclaringClass()).addConstantMetaData(constant, mds);
	}

	@Override
	public EnumTypeMetaDataEditor addConstantMetaData(Enum<?> constant, Iterable<? extends MetaData> mds) {
		return onEnumType(constant.getDeclaringClass()).addConstantMetaData(constant, mds);
	}

	@Override
	public EnumTypeMetaDataEditor addConstantMetaData(GmEnumConstantInfo gmConstantInfo, MetaData... mds) {
		return onEnumType(gmConstantInfo.declaringTypeInfo()).addConstantMetaData(gmConstantInfo, mds);
	}

	@Override
	public EnumTypeMetaDataEditor addConstantMetaData(GmEnumConstantInfo gmConstantInfo, Iterable<? extends MetaData> mds) {
		return onEnumType(gmConstantInfo.declaringTypeInfo()).addConstantMetaData(gmConstantInfo, mds);
	}

	protected static void add(Set<MetaData> metaData, MetaData[] mds) {
		validateMetaData(mds);
		metaData.addAll(Arrays.asList(mds));
	}

	protected static void add(Set<MetaData> metaData, Iterable<? extends MetaData> mds) {
		for (MetaData md : mds)
			if (md != null)
				metaData.add(md);
			else
				throw new NullPointerException("The list of metadata contains at least one null value: " + mds);
	}

	private static void validateMetaData(MetaData[] mds) {
		for (MetaData md : mds)
			if (md == null)
				throw new NullPointerException("The list of metadata contains at least one null value: " + StringTools.createStringFromArray(mds));
	}

	protected static void remove(Set<MetaData> enumConstantMetaData, Predicate<? super MetaData> filter) {
		Iterator<MetaData> it = enumConstantMetaData.iterator();
		while (it.hasNext())
			if (filter.test(it.next()))
				it.remove();
	}

}
