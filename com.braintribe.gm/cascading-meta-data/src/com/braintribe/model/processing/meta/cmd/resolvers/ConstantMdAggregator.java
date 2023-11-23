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
package com.braintribe.model.processing.meta.cmd.resolvers;

import static com.braintribe.utils.lcd.CollectionTools2.newList;

import java.util.List;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.meta.GmEnumConstant;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.meta.GmModelElement;
import com.braintribe.model.meta.data.MetaData;
import com.braintribe.model.meta.info.GmEnumConstantInfo;
import com.braintribe.model.meta.info.GmEnumTypeInfo;
import com.braintribe.model.processing.meta.cmd.context.StaticContext;
import com.braintribe.model.processing.meta.cmd.extended.ConstantMdDescriptor;
import com.braintribe.model.processing.meta.cmd.index.MetaDataIndexKey;
import com.braintribe.model.processing.meta.cmd.index.MetaDataIndexStructure.EnumConstantMdIndex;
import com.braintribe.model.processing.meta.cmd.index.MetaDataIndexStructure.MdIndex;
import com.braintribe.model.processing.meta.cmd.tools.MetaDataBox;
import com.braintribe.model.processing.meta.oracle.QualifiedMetaData;

public class ConstantMdAggregator extends MdAggregator {

	private final StaticContext localContext = StaticContext.EMPTY_CONTEXT;
	private final EnumConstantMdIndex enumConstantMdIndex;

	public ConstantMdAggregator(EnumMdAggregator parent, EnumConstantMdIndex enumConstantMdIndex) {
		super(parent.resolutionContext, ConstantMdDescriptor.T);

		this.enumConstantMdIndex = enumConstantMdIndex;
	}

	public GmEnumConstant getGmEnumConstant() {
		return enumConstantMdIndex.getGmEnumConstant();
	}

	@Override
	protected List<MetaDataBox> acquireFullMetaData(EntityType<? extends MetaData> metaDataType, boolean extended) {
		/* Regarding the key, see comment inside ModelMetaDataResolver#acquireFullMetaData(Class) */
		MetaDataIndexKey mdKey = MetaDataIndexKey.forAll(metaDataType);

		List<MetaDataBox> result = newList();
		result.add(acquireMetaData(enumConstantMdIndex, mdKey, extended));
		result.add(acquireMetaData(enumConstantMdIndex.enumMdIndex.globalEnumConstantMdIndex, mdKey, extended));
		result.add(acquireMetaData(enumConstantMdIndex.enumMdIndex.modelMdIndex.modelConstantMdIndex, mdKey, extended));

		return result;
	}

	@Override
	protected ConstantMdDescriptor extendedFor(QualifiedMetaData qmd, MdIndex ownerIndex) {
		MetaData md = qmd.metaData();
		GmModelElement ownerElement = qmd.ownerElement();

		if (ownerElement == null) {
			return MetaDataWrapper.forConstant(md, null, null, null);

		} else if (ownerElement instanceof GmEnumConstantInfo) {
			GmEnumConstantInfo ownerConstantInfo = (GmEnumConstantInfo) ownerElement;
			return MetaDataWrapper.forConstant(md, ownerConstantInfo.declaringModel(), ownerConstantInfo.declaringTypeInfo(), ownerConstantInfo);

		} else if (ownerElement instanceof GmEnumTypeInfo) {
			GmEnumTypeInfo ownerTypeInfo = (GmEnumTypeInfo) ownerElement;
			return MetaDataWrapper.forConstant(md, ownerTypeInfo.declaringModel(), ownerTypeInfo, null);

		} else if (ownerElement instanceof GmMetaModel) {
			GmMetaModel ownerModel = (GmMetaModel) ownerElement;
			return MetaDataWrapper.forConstant(md, ownerModel, null, null);

		} else {
			throw new IllegalArgumentException("Unexpected owner for enum constant MD: " + ownerElement);
		}
	}

	@Override
	protected StaticContext localContext() {
		return localContext;
	}

}
