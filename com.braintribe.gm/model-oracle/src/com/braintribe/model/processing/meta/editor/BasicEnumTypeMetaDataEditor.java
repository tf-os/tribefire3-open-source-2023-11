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

import static com.braintribe.model.processing.meta.editor.BasicModelMetaDataEditor.add;
import static com.braintribe.model.processing.meta.editor.BasicModelMetaDataEditor.remove;

import java.util.function.Consumer;
import java.util.function.Predicate;

import com.braintribe.model.generic.reflection.GenericModelException;
import com.braintribe.model.meta.GmEnumType;
import com.braintribe.model.meta.data.MetaData;
import com.braintribe.model.meta.info.GmEnumConstantInfo;
import com.braintribe.model.meta.info.GmEnumTypeInfo;
import com.braintribe.model.processing.meta.oracle.flat.FlatEnumConstant;
import com.braintribe.model.processing.meta.oracle.flat.FlatEnumType;

/**
 * @author peter.gazdik
 */
public class BasicEnumTypeMetaDataEditor implements EnumTypeMetaDataEditor {

	private final BasicModelMetaDataEditor modelMdEditor;
	private final FlatEnumType flatEnumType;

	public BasicEnumTypeMetaDataEditor(BasicModelMetaDataEditor modelMdEditor, FlatEnumType flatEnumType) {
		this.modelMdEditor = modelMdEditor;
		this.flatEnumType = flatEnumType;
	}

	@Override
	public GmEnumType getEnumType() {
		return flatEnumType.type;
	}

	@Override
	public EnumTypeMetaDataEditor configure(Consumer<GmEnumTypeInfo> consumer) {
		consumer.accept(acquireGmEnumTypeInfo());
		return this;
	}

	@Override
	public EnumTypeMetaDataEditor configure(String constantName, Consumer<GmEnumConstantInfo> consumer) {
		consumer.accept(acquireGmEnumConstantInfo(constantName));
		return this;
	}

	@Override
	public EnumTypeMetaDataEditor configure(Enum<?> constant, Consumer<GmEnumConstantInfo> consumer) {
		configure(constant.name(), consumer);
		return this;
	}
	@Override
	public EnumTypeMetaDataEditor configure(GmEnumConstantInfo constant, Consumer<GmEnumConstantInfo> consumer) {
		configure(constant.relatedConstant().getName(), consumer);
		return this;
	}

	@Override
	public EnumTypeMetaDataEditor addMetaData(MetaData... mds) {
		add(acquireGmEnumTypeInfo().getMetaData(), mds);
		return this;
	}

	@Override
	public EnumTypeMetaDataEditor addMetaData(Iterable<? extends MetaData> mds) {
		add(acquireGmEnumTypeInfo().getMetaData(), mds);
		return this;
	}

	@Override
	public EnumTypeMetaDataEditor removeMetaData(Predicate<? super MetaData> filter) {
		remove(acquireGmEnumTypeInfo().getMetaData(), filter);
		return this;
	}

	@Override
	public EnumTypeMetaDataEditor addConstantMetaData(MetaData... mds) {
		add(acquireGmEnumTypeInfo().getEnumConstantMetaData(), mds);
		return this;
	}

	@Override
	public EnumTypeMetaDataEditor addConstantMetaData(Iterable<? extends MetaData> mds) {
		add(acquireGmEnumTypeInfo().getEnumConstantMetaData(), mds);
		return this;
	}

	@Override
	public EnumTypeMetaDataEditor removeConstantMetaData(Predicate<? super MetaData> filter) {
		remove(acquireGmEnumTypeInfo().getEnumConstantMetaData(), filter);
		return this;
	}

	@Override
	public EnumTypeMetaDataEditor addConstantMetaData(String constant, MetaData... mds) {
		add(acquireGmEnumConstantInfo(constant).getMetaData(), mds);
		return this;
	}

	@Override
	public EnumTypeMetaDataEditor addConstantMetaData(String constant, Iterable<? extends MetaData> mds) {
		add(acquireGmEnumConstantInfo(constant).getMetaData(), mds);
		return this;
	}

	@Override
	public EnumTypeMetaDataEditor removeConstantMetaData(String constant, Predicate<? super MetaData> filter) {
		remove(acquireGmEnumConstantInfo(constant).getMetaData(), filter);
		return this;
	}

	@Override
	public EnumTypeMetaDataEditor addConstantMetaData(Enum<?> constant, MetaData... mds) {
		addConstantMetaData(constant.name(), mds);
		return this;
	}

	@Override
	public EnumTypeMetaDataEditor addConstantMetaData(Enum<?> constant, Iterable<? extends MetaData> mds) {
		addConstantMetaData(constant.name(), mds);
		return this;
	}

	@Override
	public EnumTypeMetaDataEditor removeConstantMetaData(Enum<?> constant, Predicate<? super MetaData> filter) {
		removeConstantMetaData(constant.name(), filter);
		return this;
	}

	@Override
	public EnumTypeMetaDataEditor addConstantMetaData(GmEnumConstantInfo gmConstantInfo, MetaData... mds) {
		addConstantMetaData(gmConstantInfo.relatedConstant().getName(), mds);
		return this;
	}

	@Override
	public EnumTypeMetaDataEditor addConstantMetaData(GmEnumConstantInfo gmConstantInfo, Iterable<? extends MetaData> mds) {
		addConstantMetaData(gmConstantInfo.relatedConstant().getName(), mds);
		return this;
	}

	@Override
	public EnumTypeMetaDataEditor removeConstantMetaData(GmEnumConstantInfo gmConstantInfo, Predicate<? super MetaData> filter) {
		removeConstantMetaData(gmConstantInfo.relatedConstant().getName(), filter);
		return this;
	}

	protected GmEnumTypeInfo acquireGmEnumTypeInfo() {
		if (modelMdEditor.appendToDeclaration) {
			return flatEnumType.type;
		} else {
			return modelMdEditor.leafModel.acquireGmEnumTypeInfo(flatEnumType.type);
		}
	}

	protected GmEnumConstantInfo acquireGmEnumConstantInfo(String constantName) {
		FlatEnumConstant flatEnumConstant = flatEnumType.acquireFlatEnumConstants().get(constantName);
		if (flatEnumConstant == null) {
			throw new GenericModelException("Constant '" + constantName + "' not found for enum type: " + flatEnumType.type.getTypeSignature());
		}

		if (modelMdEditor.appendToDeclaration) {
			return flatEnumConstant.gmEnumConstant;
		} else {
			return modelMdEditor.leafModel.acquireGmConstantInfo(flatEnumType.type, flatEnumConstant.gmEnumConstant);
		}
	}

}
