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

import static com.braintribe.model.generic.annotation.meta.base.MdaAnalysisTools.newMd;

import java.lang.annotation.Annotation;
import java.util.function.Function;

import com.braintribe.model.generic.annotation.meta.api.MdaHandler;
import com.braintribe.model.generic.annotation.meta.api.analysis.MdaAnalysisContext;
import com.braintribe.model.generic.annotation.meta.api.synthesis.MdaSynthesisContext;
import com.braintribe.model.generic.annotation.meta.api.synthesis.SingleAnnotationDescriptor;
import com.braintribe.model.meta.data.MetaData;

/**
 * @author peter.gazdik
 */
public class BasicMdaHandler<A extends Annotation, M extends MetaData> implements MdaHandler<A, M> {

	private final Class<A> annotationClass;
	private final Function<A, String> globalIdResolver;
	private final AnnotationToMetaDataPropertyCopier<A, M> a2mPropertyCopier;
	private final MetaDataToDescriptorPropertyCopier<M> m2dPropertyCopier;
	private Class<M> metaDataClass;

	public BasicMdaHandler(Class<A> annotationClass, Class<M> metaDataClass, Function<A, String> globalIdResolver) {
		this(annotationClass, metaDataClass, globalIdResolver, (c, a, m) -> { /* NO OP */ }, (c, a, m) -> { /* NO OP */ });
	}

	public BasicMdaHandler(Class<A> annotationClass, Class<M> metaDataClass, Function<A, String> globalIdResolver, //
			AnnotationToMetaDataPropertyCopier<A, M> a2mPropertyCopier, MetaDataToDescriptorPropertyCopier<M> m2dPropertyCopier) {

		this.annotationClass = annotationClass;
		this.metaDataClass = metaDataClass;
		this.globalIdResolver = globalIdResolver;
		this.a2mPropertyCopier = a2mPropertyCopier;
		this.m2dPropertyCopier = m2dPropertyCopier;
	}

	@FunctionalInterface
	public static interface AnnotationToMetaDataPropertyCopier<A extends Annotation, M extends MetaData> {
		void copyProperties(MdaAnalysisContext context, A annotation, M metaData);
	}

	@FunctionalInterface
	public static interface MetaDataToDescriptorPropertyCopier<M extends MetaData> {
		void copyProperties(MdaSynthesisContext context, SingleAnnotationDescriptor annotationDescriptor, M metaData);
	}

	@Override
	public Class<A> annotationClass() {
		return annotationClass;
	}

	@Override
	public Class<M> metaDataClass() {
		return metaDataClass;
	}

	@Override
	public M buildMd(A annotation, MdaAnalysisContext context) {
		M m = newMd(context, metaDataType(), globalIdResolver.apply(annotation));
		a2mPropertyCopier.copyProperties(context, annotation, m);
		return m;
	}

	@Override
	public void buildAnnotation(MdaSynthesisContext context, M metaData) {
		SingleAnnotationDescriptor annotationDescriptor = context.newDescriptor(annotationClass);
		context.setCurrentDescriptor(annotationDescriptor);

		m2dPropertyCopier.copyProperties(context, annotationDescriptor, metaData);
	}

}
