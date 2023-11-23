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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.meta.GmModelElement;
import com.braintribe.model.meta.data.MetaData;
import com.braintribe.model.meta.info.GmEntityTypeInfo;
import com.braintribe.model.processing.index.ConcurrentCachedIndex;
import com.braintribe.model.processing.meta.cmd.context.SelectorContextAspect;
import com.braintribe.model.processing.meta.cmd.context.StaticContext;
import com.braintribe.model.processing.meta.cmd.context.aspects.GmEntityTypeAspect;
import com.braintribe.model.processing.meta.cmd.extended.EntityMdDescriptor;
import com.braintribe.model.processing.meta.cmd.extended.EntityRelatedMdDescriptor;
import com.braintribe.model.processing.meta.cmd.index.MetaDataIndexKey;
import com.braintribe.model.processing.meta.cmd.index.MetaDataIndexStructure.EntityMdIndex;
import com.braintribe.model.processing.meta.cmd.index.MetaDataIndexStructure.MdIndex;
import com.braintribe.model.processing.meta.cmd.index.MetaDataIndexStructure.PropertyMdIndex;
import com.braintribe.model.processing.meta.cmd.tools.MetaDataBox;
import com.braintribe.model.processing.meta.oracle.EntityTypeOracle;
import com.braintribe.model.processing.meta.oracle.PropertyOracle;
import com.braintribe.model.processing.meta.oracle.QualifiedMetaData;

/**
 * A resolver of {@link MetaData} for concrete {@link GmEntityType}
 */
public class EntityMdAggregator extends MdAggregator {

	private final StaticContext localContext;
	private final EntityMdIndex entityMdIndex;
	private volatile List<EntityMdIndex> relevantSuperEntityMdIndices;

	private final PropertyMdAggregatorIndex propertyMdAggregatorIndex = new PropertyMdAggregatorIndex();

	private class PropertyMdAggregatorIndex extends ConcurrentCachedIndex<String, PropertyMdAggregator> {
		@Override
		protected PropertyMdAggregator provideValueFor(String propertyName) {
			return new PropertyMdAggregator(EntityMdAggregator.this, entityMdIndex.acquirePropertyMdIndex(propertyName));
		}
	}

	public EntityMdAggregator(ModelMdAggregator parent, EntityMdIndex entityMdIndex) {
		super(parent.resolutionContext, EntityMdDescriptor.T);

		this.entityMdIndex = entityMdIndex;
		this.localContext = new StaticContext(parent.localContext(), localApects());
	}

	public EntityTypeOracle getEntityOracle() {
		return entityMdIndex.entityOracle;
	}

	private Map<Class<? extends SelectorContextAspect<?>>, Object> localApects() {
		GmEntityType gmEntityType = getGmEntityType();
		if (gmEntityType == null) {
			// in case of dynamic entity type
			return Collections.emptyMap();
		}

		Map<Class<? extends SelectorContextAspect<?>>, Object> result = new HashMap<Class<? extends SelectorContextAspect<?>>, Object>();
		result.put(GmEntityTypeAspect.class, gmEntityType);

		return result;
	}

	public PropertyMdAggregator acquirePropertyMdAggregator(String propertyName) {
		return propertyMdAggregatorIndex.acquireFor(propertyName);
	}

	public PropertyMdAggregator getDynamicPropertyMdAggregator(String propertyName) {
		PropertyOracle propertyOracle = getEntityOracle().getProperty(propertyName);
		PropertyMdIndex index = new PropertyMdIndex(entityMdIndex, propertyOracle, resolutionContext);

		return new PropertyMdAggregator(EntityMdAggregator.this, index);
	}

	@Override
	protected List<MetaDataBox> acquireFullMetaData(EntityType<? extends MetaData> metaDataType, boolean extended) {
		ensureRelevantEntityMdIndices();

		List<MetaDataBox> result = newList();
		result.add(acquireMetaData(entityMdIndex, MetaDataIndexKey.forAll(metaDataType), extended));

		MetaDataIndexKey inheritableOnlyKey = MetaDataIndexKey.forInherited(metaDataType);
		for (EntityMdIndex emi : relevantSuperEntityMdIndices) {
			// take only such MD from super type where inheritance is enabled
			result.add(acquireMetaData(emi, inheritableOnlyKey, extended));
		}

		return result;
	}

	private void ensureRelevantEntityMdIndices() {
		if (relevantSuperEntityMdIndices != null) {
			return;
		}

		ensureRelevantEntityMdIndicesSync();
	}

	private synchronized void ensureRelevantEntityMdIndicesSync() {
		if (relevantSuperEntityMdIndices != null) {
			return;
		}

		List<EntityMdIndex> indices = new ArrayList<EntityMdIndex>();

		for (GmEntityType superType : entityMdIndex.getAllSuperTypes()) {
			EntityMdIndex superIndex = entityMdIndex.acquireOtherEntityIndex(superType.getTypeSignature());
			indices.add(superIndex);
		}

		relevantSuperEntityMdIndices = indices;
	}

	@Override
	protected EntityRelatedMdDescriptor extendedFor(QualifiedMetaData qmd, MdIndex ownerIndex) {
		GmEntityType resolvedForType = getGmEntityType();

		MetaData md = qmd.metaData();
		GmModelElement ownerElement = qmd.ownerElement();

		if (ownerElement == null) {
			return MetaDataWrapper.forEntityType(md, resolvedForType, null, null);

		} else if (ownerElement instanceof GmEntityTypeInfo) {
			GmEntityTypeInfo ownerEntityTypeInfo = (GmEntityTypeInfo) ownerElement;
			return MetaDataWrapper.forEntityType(md, resolvedForType, ownerEntityTypeInfo.declaringModel(), ownerEntityTypeInfo);

		} else {
			throw new IllegalArgumentException("Unexpected owner for entity MD: " + ownerElement);
		}
	}

	@Override
	protected StaticContext localContext() {
		return localContext;
	}

	public GmEntityType getGmEntityType() {
		return entityMdIndex.getGmEntityType();
	}

}
