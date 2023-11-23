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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.meta.GmEnumType;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.meta.GmModelElement;
import com.braintribe.model.meta.data.MetaData;
import com.braintribe.model.meta.info.GmEnumTypeInfo;
import com.braintribe.model.processing.index.ConcurrentCachedIndex;
import com.braintribe.model.processing.meta.cmd.context.SelectorContextAspect;
import com.braintribe.model.processing.meta.cmd.context.StaticContext;
import com.braintribe.model.processing.meta.cmd.context.aspects.GmEnumTypeAspect;
import com.braintribe.model.processing.meta.cmd.extended.EnumMdDescriptor;
import com.braintribe.model.processing.meta.cmd.extended.EnumRelatedMdDescriptor;
import com.braintribe.model.processing.meta.cmd.index.MetaDataIndexKey;
import com.braintribe.model.processing.meta.cmd.index.MetaDataIndexStructure.EnumMdIndex;
import com.braintribe.model.processing.meta.cmd.index.MetaDataIndexStructure.MdIndex;
import com.braintribe.model.processing.meta.cmd.tools.MetaDataBox;
import com.braintribe.model.processing.meta.oracle.EnumTypeOracle;
import com.braintribe.model.processing.meta.oracle.QualifiedMetaData;

/**
 * A resolver of {@link MetaData} for concrete {@link GmEnumType}
 */
public class EnumMdAggregator extends MdAggregator {

	private final StaticContext localContext;
	private final EnumMdIndex enumMdIndex;

	private final ConstantMdAggregatorIndex constantMdAggregatorIndex = new ConstantMdAggregatorIndex();

	private class ConstantMdAggregatorIndex extends ConcurrentCachedIndex<String, ConstantMdAggregator> {
		@Override
		protected ConstantMdAggregator provideValueFor(String constant) {
			return new ConstantMdAggregator(EnumMdAggregator.this, enumMdIndex.acquireEnumConstantMdIndex(constant));
		}
	}

	public EnumMdAggregator(ModelMdAggregator parent, EnumMdIndex enumMdIndex) {
		super(parent.resolutionContext, EnumMdDescriptor.T);

		this.enumMdIndex = enumMdIndex;
		this.localContext = new StaticContext(parent.localContext(), localApects());
	}

	public EnumTypeOracle getEnumOracle() {
		return enumMdIndex.enumOracle;
	}

	private Map<Class<? extends SelectorContextAspect<?>>, Object> localApects() {
		GmEnumType gmEnumType = getGmEnumType();

		Map<Class<? extends SelectorContextAspect<?>>, Object> result = new HashMap<Class<? extends SelectorContextAspect<?>>, Object>();
		result.put(GmEnumTypeAspect.class, gmEnumType);

		return result;
	}

	public GmEnumType getGmEnumType() {
		return enumMdIndex.getGmEnumType();
	}

	public ConstantMdAggregator acquireConstantMdAggregator(String constant) {
		return constantMdAggregatorIndex.acquireFor(constant);
	}

	@Override
	protected List<MetaDataBox> acquireFullMetaData(EntityType<? extends MetaData> metaDataType, boolean extended) {
		/* Regarding the key, see comment inside ModelMetaDataResolver#acquireFullMetaData(Class) */
		MetaDataIndexKey mdKey = MetaDataIndexKey.forAll(metaDataType);

		List<MetaDataBox> result = newList();
		result.add(acquireMetaData(enumMdIndex, mdKey, extended));
		result.add(acquireMetaData(enumMdIndex.modelMdIndex.modelEnumMdIndex, mdKey, extended));

		return result;
	}

	@Override
	protected EnumRelatedMdDescriptor extendedFor(QualifiedMetaData qmd, MdIndex ownerIndex) {
		MetaData md = qmd.metaData();
		GmModelElement ownerElement = qmd.ownerElement();

		if (ownerElement == null) {
			return MetaDataWrapper.forEnumType(md, null, null);

		} else if (ownerElement instanceof GmEnumTypeInfo) {

			GmEnumTypeInfo ownerTypeInfo = (GmEnumTypeInfo) ownerElement;
			return MetaDataWrapper.forEnumType(md, ownerTypeInfo.declaringModel(), ownerTypeInfo);

		} else if (ownerElement instanceof GmMetaModel) {
			GmMetaModel ownerModel = (GmMetaModel) ownerElement;
			return MetaDataWrapper.forEnumType(md, ownerModel, null);

		} else {
			throw new IllegalArgumentException("Unexpected owner for enum MD: " + ownerElement);
		}
	}

	@Override
	protected StaticContext localContext() {
		return localContext;
	}

}
