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
package com.braintribe.model.generic.annotation.meta.base;

import static com.braintribe.utils.lcd.CollectionTools2.first;
import static com.braintribe.utils.lcd.CollectionTools2.newList;
import static com.braintribe.utils.lcd.CollectionTools2.newMap;
import static com.braintribe.utils.lcd.StringTools.isEmpty;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BiConsumer;
import java.util.function.Function;

import com.braintribe.logging.Logger;
import com.braintribe.model.generic.annotation.meta.api.MdaHandler;
import com.braintribe.model.generic.annotation.meta.api.RepeatableMdaHandler;
import com.braintribe.model.generic.annotation.meta.api.analysis.MdaAnalysisContext;
import com.braintribe.model.generic.annotation.meta.api.synthesis.AnnotationDescriptor;
import com.braintribe.model.generic.annotation.meta.api.synthesis.MdaSynthesisContext;
import com.braintribe.model.generic.annotation.meta.api.synthesis.RepeatedAnnotationDescriptor;
import com.braintribe.model.generic.annotation.meta.api.synthesis.SingleAnnotationDescriptor;
import com.braintribe.model.generic.i18n.LocalizedString;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.meta.data.MetaData;
import com.braintribe.utils.lcd.StringTools;

/**
 * @author peter.gazdik
 */
public class MdaAnalysisTools {

	private static final Logger log = Logger.getLogger(MdaAnalysisTools.class);

	public static <M extends MetaData> M newMd(MdaAnalysisContext context, EntityType<M> metaDataType, String globalId) {
		return buildMd(context, metaDataType, globalId, "");
	}

	public static <M extends MetaData> M newMd(MdaAnalysisContext context, EntityType<M> mdType, String globalId, int counter) {
		return buildMd(context, mdType, globalId, "_" + counter);
	}

	private static <M extends MetaData> M buildMd(MdaAnalysisContext context, EntityType<M> mdType, String globalId, String globalIdSuffix) {
		M result = mdType.create();
		if (!StringTools.isEmpty(globalId)) {
			result.setGlobalId(globalId);

		} else {
			String targetGlobalId = context.getTarget().getGlobalId();

			if (!StringTools.isEmpty(targetGlobalId))
				result.setGlobalId(mdType.getShortName() + ":" + targetGlobalId + globalIdSuffix);
		}

		return result;
	}

	public static <M extends MetaData, A extends Annotation> List<M> toLsBasedMd( //
			MdaAnalysisContext context, //
			MdaHandler<A, M> handler, //
			Function<A, String> localeReseolver, //
			Function<A, String> textResolver, //
			Function<A, String> globalIdResolver, //
			BiConsumer<M, LocalizedString> lsSetter, //
			A... annos) {

		String globalId = null;
		Map<String, String> localizedValues = newMap();

		for (A anno : annos) {
			localizedValues.put(localeReseolver.apply(anno), textResolver.apply(anno));

			String currentGlobalId = globalIdResolver.apply(anno);
			checkGlobalId(globalId, currentGlobalId, handler.annotationClass());

			if (!StringTools.isEmpty(currentGlobalId))
				globalId = currentGlobalId;
		}

		M result = newMd(context, handler.metaDataType(), globalId);

		LocalizedString ls = LocalizedString.T.create("localized-description:" + result.getGlobalId());
		ls.setLocalizedValues(localizedValues);

		lsSetter.accept(result, ls);

		return Collections.singletonList(result);
	}

	private static void checkGlobalId(String globalId1, String globalId2, Class<? extends Annotation> annotationClass) {
		boolean twoDifferentValues = !isEmpty(globalId1) && !isEmpty(globalId2) && !globalId1.equals(globalId2);

		if (twoDifferentValues)
			log.warn("Different globalId configured for the same meta-data of type '" + annotationClass.getSimpleName() + "'. First: " + globalId1
					+ ", second: " + globalId2);
	}

	public static <M extends MetaData, A extends Annotation, RA extends Annotation> void buildLsBasedAnnotation(MdaSynthesisContext context, M md, //
			RepeatableMdaHandler<A, RA, M> handler, //
			Function<M, LocalizedString> lsResolver) {

		Map<String, String> localizedValues = lsResolver.apply(md).getLocalizedValues();

		List<SingleAnnotationDescriptor> list = newList();

		for (Entry<String, String> entry : localizedValues.entrySet()) {
			String locale = entry.getKey();
			String value = entry.getValue();

			SingleAnnotationDescriptor ad = context.newDescriptor(handler.annotationClass(), false);
			ad.addAnnotationValue("locale", locale);
			ad.addAnnotationValue("value", value);

			list.add(ad);
		}

		AnnotationDescriptor descriptor = list.size() > 1 ? //
				new RepeatedAnnotationDescriptor(handler.aggregatorHandler().annotationClass(), list) : first(list);
		context.setCurrentDescriptor(descriptor);
	}

}
