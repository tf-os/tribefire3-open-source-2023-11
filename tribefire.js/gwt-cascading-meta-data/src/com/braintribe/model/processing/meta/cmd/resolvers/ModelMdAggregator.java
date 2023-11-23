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

import com.braintribe.model.generic.proxy.DynamicEntityType;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.meta.GmModelElement;
import com.braintribe.model.meta.data.MetaData;
import com.braintribe.model.processing.index.ConcurrentCachedIndex;
import com.braintribe.model.processing.meta.cmd.context.ResolutionContext;
import com.braintribe.model.processing.meta.cmd.context.StaticContext;
import com.braintribe.model.processing.meta.cmd.extended.MdDescriptor;
import com.braintribe.model.processing.meta.cmd.extended.ModelMdDescriptor;
import com.braintribe.model.processing.meta.cmd.index.MetaDataIndexKey;
import com.braintribe.model.processing.meta.cmd.index.MetaDataIndexStructure.EntityMdIndex;
import com.braintribe.model.processing.meta.cmd.index.MetaDataIndexStructure.MdIndex;
import com.braintribe.model.processing.meta.cmd.index.MetaDataIndexStructure.ModelMdIndex;
import com.braintribe.model.processing.meta.cmd.tools.MetaDataBox;
import com.braintribe.model.processing.meta.oracle.EntityTypeOracle;
import com.braintribe.model.processing.meta.oracle.QualifiedMetaData;
import com.braintribe.model.processing.meta.oracle.proxy.DynamicEntityTypeOracle;

public class ModelMdAggregator extends MdAggregator {

	private final StaticContext localContext = StaticContext.EMPTY_CONTEXT;
	private final ModelMdIndex modelMdIndex;

	private final EntityMdAggregatorIndex entityMdAggregatorIndex = new EntityMdAggregatorIndex();
	private final EnumMdAggregatorIndex enumMdAggregatorIndex = new EnumMdAggregatorIndex();

	private class EntityMdAggregatorIndex extends ConcurrentCachedIndex<String, EntityMdAggregator> {
		@Override
		protected EntityMdAggregator provideValueFor(String typeSignature) {
			return new EntityMdAggregator(ModelMdAggregator.this, modelMdIndex.acquireEntityMdIndex(typeSignature));
		}
	}

	private class EnumMdAggregatorIndex extends ConcurrentCachedIndex<String, EnumMdAggregator> {
		@Override
		protected EnumMdAggregator provideValueFor(String typeSignature) {
			return new EnumMdAggregator(ModelMdAggregator.this, modelMdIndex.acquireEnumMdIndex(typeSignature));
		}
	}

	public ModelMdAggregator(ModelMdIndex modelMdIndex, ResolutionContext resolutionContext) {
		super(resolutionContext, ModelMdDescriptor.T);

		this.modelMdIndex = modelMdIndex;
	}

	public EntityMdAggregator acquireEntityMdAggregator(String typeSignature) {
		return entityMdAggregatorIndex.acquireFor(typeSignature);
	}

	public EntityMdAggregator getDynamicEntityMdAggregator(DynamicEntityType dynamicEntityType) {
		EntityTypeOracle entityOracle = new DynamicEntityTypeOracle(getModelOracle(), dynamicEntityType);
		EntityMdIndex index = new EntityMdIndex(modelMdIndex.entityIndex, entityOracle, resolutionContext);

		return new EntityMdAggregator(this, index);
	}

	public EnumMdAggregator acquireEnumMdAggregator(String typeSignature) {
		return enumMdAggregatorIndex.acquireFor(typeSignature);
	}

	@Override
	protected List<MetaDataBox> acquireFullMetaData(EntityType<? extends MetaData> metaDataType, boolean extended) {
		/* From semantics point of view it doesn't matter if we take "all" meta-data, or just the "inherited" ones, but for performance reasons (and
		 * implementation details of the meta-data indexing) it's better to go with "all". Same applies to EnumMetaData. */
		MetaDataIndexKey mdKey = MetaDataIndexKey.forAll(metaDataType);

		List<MetaDataBox> result = newList();
		result.add(acquireMetaData(modelMdIndex, mdKey, extended));

		return result;
	}

	@Override
	protected MdDescriptor extendedFor(QualifiedMetaData qmd, MdIndex ownerIndex) {
		MetaData md = qmd.metaData();
		GmModelElement ownerElement = qmd.ownerElement();

		if (ownerElement == null)
			return MetaDataWrapper.forModel(md, null);
		else if (ownerElement instanceof GmMetaModel)
			return MetaDataWrapper.forModel(md, (GmMetaModel) ownerElement);
		else
			throw new IllegalArgumentException("Unexpected owner for model MD: " + ownerElement);
	}

	@Override
	protected StaticContext localContext() {
		return localContext;
	}

}
