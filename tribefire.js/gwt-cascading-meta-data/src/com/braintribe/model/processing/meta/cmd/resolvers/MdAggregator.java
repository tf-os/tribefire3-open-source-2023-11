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
import java.util.List;
import java.util.Map;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.meta.data.MetaData;
import com.braintribe.model.meta.data.Predicate;
import com.braintribe.model.processing.meta.cmd.CascadingMetaDataException;
import com.braintribe.model.processing.meta.cmd.builders.MdResolver;
import com.braintribe.model.processing.meta.cmd.context.ExtendedSelectorContext;
import com.braintribe.model.processing.meta.cmd.context.MutableSelectorContext;
import com.braintribe.model.processing.meta.cmd.context.ResolutionContext;
import com.braintribe.model.processing.meta.cmd.context.StaticContext;
import com.braintribe.model.processing.meta.cmd.extended.MdDescriptor;
import com.braintribe.model.processing.meta.cmd.index.MetaDataIndexKey;
import com.braintribe.model.processing.meta.cmd.index.MetaDataIndexStructure.MdIndex;
import com.braintribe.model.processing.meta.cmd.tools.CmdGwtUtils;
import com.braintribe.model.processing.meta.cmd.tools.MetaDataBox;
import com.braintribe.model.processing.meta.cmd.tools.MetaDataTools.MetaDataPriorityComparator;
import com.braintribe.model.processing.meta.oracle.ModelOracle;
import com.braintribe.model.processing.meta.oracle.QualifiedMetaData;

/**
 * Base class for a resolver for {@link MetaData} of given type. Each
 * 
 * @Name - this was originally a MetaDataResolver, but the name was confusing with {@link MdResolver}, so I decided to call this hierarchy
 *       "aggregators" to clearly separate the two.
 */
public abstract class MdAggregator {

	protected final ResolutionContext resolutionContext;
	protected final EntityType<? extends MdDescriptor> descriptorType;

	private final ProcessedMetaData processedMetaData = new ProcessedMetaData();
	private final ProcessedMetaData processedExtendedMetaData = new ProcessedMetaData();

	public MdAggregator(ResolutionContext resolutionContext, EntityType<? extends MdDescriptor> descriptorType) {
		this.resolutionContext = resolutionContext;
		this.descriptorType = descriptorType;
	}

	public ModelOracle getModelOracle() {
		return resolutionContext.modelOracle;
	}

	private class ProcessedMetaData {
		private Map<EntityType<? extends MetaData>, MultiMetaDataDescriptor> multi;
		private Map<EntityType<? extends MetaData>, ExclusiveMetaDataDescriptor> exclusive;

		public Map<EntityType<? extends MetaData>, MultiMetaDataDescriptor> multi() {
			return multi == null ? multiSync() : multi;
		}

		private synchronized Map<EntityType<? extends MetaData>, MultiMetaDataDescriptor> multiSync() {
			return multi == null ? multi = CmdGwtUtils.newCacheMap() : multi;
		}

		public Map<EntityType<? extends MetaData>, ExclusiveMetaDataDescriptor> exclusive() {
			return exclusive == null ? exclusiveSync() : exclusive;
		}

		private synchronized Map<EntityType<? extends MetaData>, ExclusiveMetaDataDescriptor> exclusiveSync() {
			return exclusive == null ? exclusive = CmdGwtUtils.newCacheMap() : exclusive;
		}
	}

	public MetaData exclusive(EntityType<? extends MetaData> metaDataType, ExtendedSelectorContext selectorContext) {
		ExclusiveMetaDataDescriptor mdd = acquireExclusiveDescriptor(metaDataType, false);

		return provideExclusiveValue(mdd, metaDataType, selectorContext);
	}

	public MetaData exclusiveExtended(EntityType<? extends MetaData> metaDataType, ExtendedSelectorContext selectorContext) {
		ExclusiveMetaDataDescriptor mdd = acquireExclusiveDescriptor(metaDataType, true);

		return provideExclusiveValue(mdd, metaDataType, selectorContext);
	}

	private MetaData provideExclusiveValue(ExclusiveMetaDataDescriptor mdd, EntityType<? extends MetaData> metaDataType,
			ExtendedSelectorContext context) {

		try {
			return mdd.provideValue(context);

		} catch (CascadingMetaDataException e) {
			throw new CascadingMetaDataException("Error while retrieving exclusive metada-data of type: " + metaDataType.getTypeSignature(), e);
		}
	}

	private ExclusiveMetaDataDescriptor acquireExclusiveDescriptor(EntityType<? extends MetaData> metaDataType, boolean extended) {
		ProcessedMetaData pmd = extended ? processedExtendedMetaData : processedMetaData;

		return pmd.exclusive().computeIfAbsent(metaDataType, key -> newExclusiveDescriptor(metaDataType, extended));
	}

	private ExclusiveMetaDataDescriptor newExclusiveDescriptor(EntityType<? extends MetaData> metaDataType, boolean extended) {
		ExclusiveMetaDataDescriptor result = newDescriptorFor(new ExclusiveMetaDataDescriptor(), metaDataType, extended);
		result.defaultMetaData = getDefault(metaDataType, extended);
		return result;
	}

	private MetaData getDefault(EntityType<? extends MetaData> metaDataType, boolean extended) {
		MetaData metaData = resolutionContext.getDefaultValue(metaDataType);

		if (!extended || metaData == null)
			return metaData;
		else
			return MetaDataWrapper.forDefault(descriptorType, metaData);
	}

	public List<MetaData> list(EntityType<? extends MetaData> metaDataType, ExtendedSelectorContext selectorContext) {
		MultiMetaDataDescriptor mdd = acquireMultiDescriptor(metaDataType, false);

		return provideMultiValue(mdd, metaDataType, selectorContext);
	}

	public List<MetaData> listExtended(EntityType<? extends MetaData> metaDataType, ExtendedSelectorContext selectorContext) {
		MultiMetaDataDescriptor mdd = acquireMultiDescriptor(metaDataType, true);

		return provideMultiValue(mdd, metaDataType, selectorContext);
	}

	private List<MetaData> provideMultiValue(MultiMetaDataDescriptor mdd, EntityType<? extends MetaData> mdType, ExtendedSelectorContext context) {
		try {
			return mdd.provideValue(context);

		} catch (CascadingMetaDataException e) {
			throw new CascadingMetaDataException("Error while retrieving list of metada-data of type: " + mdType.getTypeSignature(), e);
		}
	}

	private MultiMetaDataDescriptor acquireMultiDescriptor(EntityType<? extends MetaData> metaDataType, boolean extended) {
		ProcessedMetaData pmd = extended ? processedExtendedMetaData : processedMetaData;

		return pmd.multi().computeIfAbsent(metaDataType, key -> newMultiDescriptor(metaDataType, extended));
	}

	private MultiMetaDataDescriptor newMultiDescriptor(EntityType<? extends MetaData> metaDataType, boolean extended) {
		return newDescriptorFor(new MultiMetaDataDescriptor(), metaDataType, extended);
	}

	private <M extends AbstractMetaDataDescriptor<?>> M newDescriptorFor(M newInstance, EntityType<? extends MetaData> metaDataType,
			boolean extended) {
		newInstance.resolutionContext = resolutionContext;
		newInstance.ownerMetaData = normalize(acquireFullMetaData(metaDataType, extended));

		return newInstance;
	}

	/**
	 * Getting rid of all empty lists, so we do not hold unnecessary references. Also, the first list will be a merge of first list from normal MD and
	 * all the important MDs, thus creating the illusion as if all the "important" MDs were actually local ones.
	 */
	private List<List<QualifiedMetaData>> normalize(List<MetaDataBox> acquireFullMetaData) {
		List<QualifiedMetaData> localAndImportantMds = newList();
		List<List<QualifiedMetaData>> normalized = newList();

		boolean first = true;
		for (MetaDataBox mdBox : acquireFullMetaData) {
			localAndImportantMds.addAll(mdBox.importantMetaData);

			if (first) {
				localAndImportantMds.addAll(mdBox.normalMetaData);
				first = false;

			} else if (!mdBox.normalMetaData.isEmpty()) {
				normalized.add(mdBox.normalMetaData);
			}
		}

		if (!localAndImportantMds.isEmpty()) {
			Collections.sort(localAndImportantMds, MetaDataPriorityComparator.instance);
			normalized.add(0, localAndImportantMds);
		}

		if (normalized.isEmpty())
			return Collections.emptyList();
		else
			return normalized;
	}

	protected abstract List<MetaDataBox> acquireFullMetaData(EntityType<? extends MetaData> metaDataType, boolean extended);

	protected MetaDataBox acquireMetaData(MdIndex mdIndex, MetaDataIndexKey key, boolean extended) {
		MetaDataBox list = mdIndex.acquireMetaData(key);
		if (!extended || list.isEmpty())
			return list;

		List<QualifiedMetaData> extendedNormalMds = asExtended(mdIndex, key, list.normalMetaData);
		List<QualifiedMetaData> extendedImportantMds = asExtended(mdIndex, key, list.importantMetaData);

		return new MetaDataBox(extendedNormalMds, extendedImportantMds);
	}

	private List<QualifiedMetaData> asExtended(MdIndex mdIndex, MetaDataIndexKey key, List<QualifiedMetaData> mds) {
		List<QualifiedMetaData> extendedMetaData = new ArrayList<>(mds.size());

		boolean isPredicate = Predicate.T.isAssignableFrom(key.mdType());

		for (QualifiedMetaData qmd : mds) {
			MetaData md = qmd.metaData();

			MdDescriptor extendedMd = extendedFor(qmd, mdIndex);

			extendedMd.setIsTrue(isPredicate && ((Predicate) md).isTrue());
			extendedMd.setConflictPriority(md.getConflictPriority());
			extendedMd.setInherited(md.getInherited());
			extendedMd.setSelector(md.getSelector());

			extendedMetaData.add(extendedMd);
		}

		return extendedMetaData;
	}

	/**
	 * @param ownerIndex
	 *            this is used when resolving entity related meta data (entity/property), and in those cases this method is overridden.
	 */
	protected abstract MdDescriptor extendedFor(QualifiedMetaData md, MdIndex ownerIndex);

	public void addLocalContextTo(MutableSelectorContext selectorContext) {
		localContext().addTo(selectorContext);
	}

	/**
	 * Provides local context - the one that remains constant for given MetaData holder. For example resolver for given entity should provide
	 * corresponding GmEntityType through this method. In general, all the aspects
	 */
	protected abstract StaticContext localContext();

}
