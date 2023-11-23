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
package com.braintribe.model.generic.annotation.meta.synthesis;

import static com.braintribe.utils.lcd.CollectionTools2.asList;
import static com.braintribe.utils.lcd.CollectionTools2.newList;
import static com.braintribe.utils.lcd.CollectionTools2.newMap;
import static com.braintribe.utils.lcd.CollectionTools2.nullSafe;
import static java.util.Collections.emptySet;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.braintribe.model.generic.annotation.meta.api.MdaHandler;
import com.braintribe.model.generic.annotation.meta.api.MetaDataAnnotations;
import com.braintribe.model.generic.annotation.meta.api.synthesis.AnnotationDescriptor;
import com.braintribe.model.generic.annotation.meta.api.synthesis.MdaSynthesisContext;
import com.braintribe.model.generic.annotation.meta.api.synthesis.RepeatedAnnotationDescriptor;
import com.braintribe.model.generic.annotation.meta.api.synthesis.SingleAnnotationDescriptor;
import com.braintribe.model.generic.annotation.meta.base.MdaAnalysisTools;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.meta.data.HasMetaData;
import com.braintribe.model.meta.data.MetaData;

/**
 * @author peter.gazdik
 */
public class MdaSynthesis {

	/**
	 * @return annotation descriptors for all metaData that are supported as annotations, or an empty collection.
	 */
	public static Collection<AnnotationDescriptor> synthesizeMetaDataAnnotations(HasMetaData metaDataOwner) {
		return synthesizeMetaDataAnnotations(metaDataOwner.getGlobalId(), metaDataOwner.getMetaData());
	}

	public static Collection<AnnotationDescriptor> synthesizeMetaDataAnnotations(String ownerGlobalId, Collection<? extends MetaData> metaData) {
		BasicMdAnnoSyncContext context = null;

		for (MetaData metaDatum : nullSafe(metaData)) {
			if (metaDatum.getSelector() != null)
				continue;

			MdaHandler<?, MetaData> synthesizer = resolveSynthesizer(metaDatum);
			if (synthesizer == null)
				continue;

			if (context == null) {
				context = new BasicMdAnnoSyncContext();
				context.ownerGlobalId = ownerGlobalId;
			}

			context.currentMetaDatum = metaDatum;

			synthesizer.buildAnnotation(context, metaDatum);
		}

		if (context == null)
			return emptySet();

		context.removeNaturalGlobalIdsForMulti();

		return context.annotations();
	}

	private static MdaHandler<?, MetaData> resolveSynthesizer(MetaData metaDatum) {
		return (MdaHandler<?, MetaData>) MetaDataAnnotations.registry().mdTypeToHandler().get(metaDatum.entityType());
	}

	private static class BasicMdAnnoSyncContext implements MdaSynthesisContext {

		public String ownerGlobalId;
		public MetaData currentMetaDatum;

		private final Map<EntityType<? extends MetaData>, AnnotationDescriptor> annoMap = newMap();
		private final List<EntityType<? extends MetaData>> multiTypes = newList();

		@Override
		public SingleAnnotationDescriptor newDescriptor(Class<? extends Annotation> annotationClass) {
			SingleAnnotationDescriptor result = new SingleAnnotationDescriptor(annotationClass);
			if (needsGlobalId())
				result.setGlobalId(currentMetaDatum.getGlobalId());

			return result;
		}

		private boolean needsGlobalId() {
			String gid = currentMetaDatum.getGlobalId();
			if (gid == null)
				return false;

			String naturalGlobalId = MdaAnalysisTools.naturalGlobalId(currentMetaDatum.entityType(), ownerGlobalId, "");
			return !gid.equals(naturalGlobalId);
		}

		@Override
		public void setCurrentDescriptor(AnnotationDescriptor descriptor) {
			annoMap.put(currentMetaDatum.entityType(), descriptor);
		}

		@Override
		public void setCurrentDescriptorMulti(SingleAnnotationDescriptor descriptor, Class<? extends Annotation> repeatabeAnnoClass) {
			AnnotationDescriptor currentDescriptor = getCurrentDescriptor();
			if (currentDescriptor == null) {
				multiTypes.add(currentMetaDatum.entityType());
				setCurrentDescriptor(descriptor);

			} else if (currentDescriptor instanceof RepeatedAnnotationDescriptor) {
				RepeatedAnnotationDescriptor repeatableDescriptor = (RepeatedAnnotationDescriptor) currentDescriptor;
				repeatableDescriptor.getNestedAnnotations().add(descriptor);

			} else {
				SingleAnnotationDescriptor singleDescriptor = (SingleAnnotationDescriptor) currentDescriptor;
				setCurrentDescriptor(new RepeatedAnnotationDescriptor(repeatabeAnnoClass, asList(singleDescriptor, descriptor)));
			}
		}

		private AnnotationDescriptor getCurrentDescriptor() {
			return annoMap.get(currentMetaDatum.entityType());
		}

		public void removeNaturalGlobalIdsForMulti() {
			for (EntityType<? extends MetaData> multiType : multiTypes) {
				AnnotationDescriptor ad = annoMap.get(multiType);

				if (ad instanceof RepeatedAnnotationDescriptor)
					removeNaturalGlobalIdsForMultiIfNeeded(multiType, ((RepeatedAnnotationDescriptor) ad).getNestedAnnotations());
				else
					removeNaturalGlobalIdsForMultiIfNeeded(multiType, Arrays.asList(((SingleAnnotationDescriptor) ad)));
			}
		}

		private void removeNaturalGlobalIdsForMultiIfNeeded(EntityType<? extends MetaData> multiType,
				List<SingleAnnotationDescriptor> singleDescriptors) {

			Set<String> actualGlobalIds = singleDescriptors.stream() //
					.map(SingleAnnotationDescriptor::getGlobalId) //
					.collect(Collectors.toSet());

			Set<String> naturalGlobalIds = IntStream.range(0, singleDescriptors.size()) //
					.mapToObj(i -> MdaAnalysisTools.naturalGlobalId(multiType, ownerGlobalId, "_" + i)) //
					.collect(Collectors.toSet());

			if (naturalGlobalIds.equals(actualGlobalIds))
				for (SingleAnnotationDescriptor sd : singleDescriptors)
					sd.removeGlobalId();
		}

		public Collection<AnnotationDescriptor> annotations() {
			return new TreeSet<>(annoMap.values());
		}

	}

}
