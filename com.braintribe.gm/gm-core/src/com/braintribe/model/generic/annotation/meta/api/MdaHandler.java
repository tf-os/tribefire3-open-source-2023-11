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
package com.braintribe.model.generic.annotation.meta.api;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.List;

import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.annotation.meta.api.analysis.MdaAnalysisContext;
import com.braintribe.model.generic.annotation.meta.api.synthesis.MdaSynthesisContext;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.meta.data.MetaData;
import com.braintribe.model.meta.data.Predicate;

/**
 * MdaHandler is responsible for converting an annotation of type A into one or more MD.
 * <p>
 * IMPLEMENTATION NOTE: From the outside (JTA) only the {@link #buildMdList} is called, but it has a default implementation that delegates to
 * {@link #buildMd} so overriding either of these two methods is fine.
 * 
 * <h2>Registering custom handlers</h2
 * 
 * Custom handlers can be registered by adding a text file on path "META-INF/gmf.mda" to your classpath.
 * <p>
 * Ideally put this file in the model that declares both the Annotation type and MetaData type you are declaring.
 * <p>
 * The file contains one line per annotation, each line consisting of comma separated values, and the first two values are fully qualified class names
 * of the annotation and the meta-data type, respective.
 * <p>
 * For {@link Predicate} meta data there is nothing to add, so an entry for a custom MdaHandler for Predicate MD could look like this:
 * 
 * <pre>
 * some.pack.anno.AnnotationForMdClass,some.pack.model.MdType
 * </pre>
 * 
 * Support for more complex MD will be added later.
 * 
 * @see RepeatableMdaHandler
 * 
 * @author peter.gazdik
 */
public interface MdaHandler<A extends Annotation, M extends MetaData> {

	Class<A> annotationClass();

	Class<M> metaDataClass();

	default EntityType<M> metaDataType() {
		return GMF.getTypeReflection().getEntityType(metaDataClass());
	}

	// Analyzer

	default List<M> buildMdList(A annotation, MdaAnalysisContext context) {
		return Collections.singletonList(buildMd(annotation, context));
	}

	@SuppressWarnings("unused")
	default M buildMd(A annotation, MdaAnalysisContext context) {
		throw new UnsupportedOperationException("Implementation class must either implement this method or the one that returns a list");
	}

	// Synthesizer

	void buildAnnotation(MdaSynthesisContext context, M metaData);

}
