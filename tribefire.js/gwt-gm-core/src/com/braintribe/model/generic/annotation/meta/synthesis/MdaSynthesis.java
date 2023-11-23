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
import static com.braintribe.utils.lcd.CollectionTools2.newMap;
import static com.braintribe.utils.lcd.CollectionTools2.nullSafe;
import static java.util.Collections.emptySet;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Map;
import java.util.TreeSet;

import com.braintribe.model.generic.annotation.meta.api.MdaHandler;
import com.braintribe.model.generic.annotation.meta.api.MetaDataAnnotations;
import com.braintribe.model.generic.annotation.meta.api.synthesis.AnnotationDescriptor;
import com.braintribe.model.generic.annotation.meta.api.synthesis.MdaSynthesisContext;
import com.braintribe.model.generic.annotation.meta.api.synthesis.RepeatedAnnotationDescriptor;
import com.braintribe.model.generic.annotation.meta.api.synthesis.SingleAnnotationDescriptor;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.meta.data.MetaData;

/**
 * @author peter.gazdik
 */
public class MdaSynthesis {

	/**
	 * @return annotation descriptors for all metaData that are supported as annotations, or an empty collection.
	 */
	public static Collection<AnnotationDescriptor> synthesizeMetaDataAnnotations(Collection<? extends MetaData> metaData) {
		BasicMdAnnoSyncContext context = null;

		for (MetaData metaDatum : nullSafe(metaData)) {
			if (metaDatum.getSelector() != null)
				continue;

			MdaHandler<?, MetaData> synthesizer = (MdaHandler<?, MetaData>) MetaDataAnnotations.registry().mdTypeToHandler()
					.get(metaDatum.entityType());
			if (synthesizer == null)
				continue;

			if (context == null)
				context = new BasicMdAnnoSyncContext();

			context.currentMetaDatum = metaDatum;

			synthesizer.buildAnnotation(context, metaDatum);
		}

		return context == null ? emptySet() : context.annotations();
	}

	private static class BasicMdAnnoSyncContext implements MdaSynthesisContext {

		public MetaData currentMetaDatum;

		private final Map<EntityType<? extends MetaData>, AnnotationDescriptor> annoMap = newMap();

		@Override
		public SingleAnnotationDescriptor newDescriptor(Class<? extends Annotation> annotationClass) {
			return newDescriptor(annotationClass, true);
		}

		@Override
		public SingleAnnotationDescriptor newDescriptor(Class<? extends Annotation> annotationClass, boolean setAsCurrentDescriptor) {
			SingleAnnotationDescriptor result = new SingleAnnotationDescriptor(annotationClass);
			result.setGlobalId(currentMetaDatum.getGlobalId());

			if (setAsCurrentDescriptor)
				setCurrentDescriptor(result);

			return result;
		}

		@Override
		public AnnotationDescriptor getCurrentDescriptor() {
			return annoMap.get(currentMetaDatum.entityType());
		}

		@Override
		public void setCurrentDescriptor(AnnotationDescriptor descriptor) {
			annoMap.put(currentMetaDatum.entityType(), descriptor);
		}

		@Override
		public void setCurrentDescriptorMulti(SingleAnnotationDescriptor descriptor, Class<? extends Annotation> repeatabeAnnoClass) {
			AnnotationDescriptor currentDescriptor = getCurrentDescriptor();
			if (currentDescriptor == null) {
				setCurrentDescriptor(descriptor);

			} else if (currentDescriptor instanceof RepeatedAnnotationDescriptor) {
				RepeatedAnnotationDescriptor repeatableDescriptor = (RepeatedAnnotationDescriptor) currentDescriptor;
				repeatableDescriptor.getNestedAnnotations().add(descriptor);

			} else {
				SingleAnnotationDescriptor singleDescriptor = (SingleAnnotationDescriptor) currentDescriptor;
				setCurrentDescriptor(new RepeatedAnnotationDescriptor(repeatabeAnnoClass, asList(singleDescriptor, descriptor)));
			}
		}

		public Collection<AnnotationDescriptor> annotations() {
			return new TreeSet<>(annoMap.values());
		}

	}

}
