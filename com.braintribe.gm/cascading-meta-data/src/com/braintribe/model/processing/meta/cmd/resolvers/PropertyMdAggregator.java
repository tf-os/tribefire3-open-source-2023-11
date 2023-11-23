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

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.meta.GmModelElement;
import com.braintribe.model.meta.GmProperty;
import com.braintribe.model.meta.data.MetaData;
import com.braintribe.model.meta.info.GmEntityTypeInfo;
import com.braintribe.model.meta.info.GmPropertyInfo;
import com.braintribe.model.processing.meta.cmd.context.SelectorContextAspect;
import com.braintribe.model.processing.meta.cmd.context.StaticContext;
import com.braintribe.model.processing.meta.cmd.context.aspects.GmPropertyAspect;
import com.braintribe.model.processing.meta.cmd.extended.MdDescriptor;
import com.braintribe.model.processing.meta.cmd.extended.PropertyMdDescriptor;
import com.braintribe.model.processing.meta.cmd.index.MetaDataIndexKey;
import com.braintribe.model.processing.meta.cmd.index.MetaDataIndexStructure.EntityMdIndex;
import com.braintribe.model.processing.meta.cmd.index.MetaDataIndexStructure.GlobalPropertyMdIndex;
import com.braintribe.model.processing.meta.cmd.index.MetaDataIndexStructure.MdIndex;
import com.braintribe.model.processing.meta.cmd.index.MetaDataIndexStructure.PropertyMdIndex;
import com.braintribe.model.processing.meta.cmd.tools.MetaDataBox;
import com.braintribe.model.processing.meta.oracle.QualifiedMetaData;

public class PropertyMdAggregator extends MdAggregator {

	private final StaticContext localContext;
	private final PropertyMdIndex propertyMdIndex;
	private final GlobalPropertyMdIndex ePropertyMdIndex;

	/**
	 * Contains {@link PropertyMdIndex} for every super-type which has the property and {@link GlobalPropertyMdIndex} for every super-type.
	 */
	private volatile List<MdIndex> relevantSuperPropertyMdIndices;
	private final ReentrantLock relevantSuperPropertyMdIndicesLock = new ReentrantLock();

	public PropertyMdAggregator(EntityMdAggregator parent, PropertyMdIndex propertyMdIndex) {
		super(parent.resolutionContext, PropertyMdDescriptor.T);

		this.propertyMdIndex = propertyMdIndex;
		this.ePropertyMdIndex = propertyMdIndex.entityMdIndex.globalPropertyMdIndex;
		this.localContext = new StaticContext(parent.localContext(), localApects());
	}

	private Map<Class<? extends SelectorContextAspect<?>>, Object> localApects() {
		GmProperty gmProperty = getGmProperty();
		if (gmProperty == null) {
			// in case of dynamic entity type
			return Collections.emptyMap();
		}

		Map<Class<? extends SelectorContextAspect<?>>, Object> result = new HashMap<Class<? extends SelectorContextAspect<?>>, Object>();
		result.put(GmPropertyAspect.class, gmProperty);

		return result;
	}

	public GmProperty getGmProperty() {
		return propertyMdIndex.getGmProperty();
	}

	@Override
	protected List<MetaDataBox> acquireFullMetaData(EntityType<? extends MetaData> metaDataType, boolean extended) {
		ensureRelevantPropertyMdIndices();

		MetaDataIndexKey allMdKey = MetaDataIndexKey.forAll(metaDataType);
		List<MetaDataBox> result = newList();
		result.add(acquireMetaData(propertyMdIndex, allMdKey, extended));
		result.add(acquireMetaData(ePropertyMdIndex, allMdKey, extended));

		MetaDataIndexKey inheritedOnlyKey = MetaDataIndexKey.forInherited(metaDataType);
		for (MdIndex pmi : relevantSuperPropertyMdIndices) {
			result.add(acquireMetaData(pmi, inheritedOnlyKey, extended));
		}

		return result;
	}

	private void ensureRelevantPropertyMdIndices() {
		if (relevantSuperPropertyMdIndices == null)
			ensureRelevantPropertyMdIndicesSync();
	}

	private void ensureRelevantPropertyMdIndicesSync() {
		relevantSuperPropertyMdIndicesLock.lock();
		try {
			if (relevantSuperPropertyMdIndices != null) {
				return;
			}

			String propertyName = propertyMdIndex.getPropertyName();
			EntityMdIndex entityMdIndex = getEntityMdIndex();

			List<MdIndex> indices = newList();

			for (GmEntityType superType : entityMdIndex.getAllSuperTypes()) {
				EntityMdIndex superEntityIndex = entityMdIndex.acquireOtherEntityIndex(superType.getTypeSignature());

				if (superEntityIndex.hasProperty(propertyName)) {
					PropertyMdIndex superIndex = superEntityIndex.acquirePropertyMdIndex(propertyName);
					indices.add(superIndex);
				}

				GlobalPropertyMdIndex ePropertyMdIndex = superEntityIndex.globalPropertyMdIndex;
				indices.add(ePropertyMdIndex);
			}

			relevantSuperPropertyMdIndices = indices;

		} finally {
			relevantSuperPropertyMdIndicesLock.unlock();
		}
	}

	@Override
	protected MdDescriptor extendedFor(QualifiedMetaData qmd, MdIndex ownerIndex) {
		GmEntityType resolvedForType = getGmEntityType();

		MetaData md = qmd.metaData();
		GmModelElement ownerElement = qmd.ownerElement();

		if (ownerElement == null) {
			return MetaDataWrapper.forProperty(md, resolvedForType, null, null, null);

		} else if (ownerElement instanceof GmPropertyInfo) {
			GmPropertyInfo ownerPropertyInfo = (GmPropertyInfo) ownerElement;
			return MetaDataWrapper.forProperty(md, resolvedForType, ownerPropertyInfo.declaringModel(), ownerPropertyInfo.declaringTypeInfo(),
					ownerPropertyInfo);

		} else if (ownerElement instanceof GmEntityTypeInfo) {
			GmEntityTypeInfo ownerEntityTypeInfo = (GmEntityTypeInfo) ownerElement;
			return MetaDataWrapper.forProperty(md, resolvedForType, ownerEntityTypeInfo.declaringModel(), ownerEntityTypeInfo, null);

		} else {
			throw new IllegalArgumentException("Unexpected owner for property MD: " + ownerElement);
		}
	}

	@Override
	protected StaticContext localContext() {
		return localContext;
	}

	private GmEntityType getGmEntityType() {
		return getEntityMdIndex().getGmEntityType();
	}

	private EntityMdIndex getEntityMdIndex() {
		return propertyMdIndex.entityMdIndex;
	}

}
