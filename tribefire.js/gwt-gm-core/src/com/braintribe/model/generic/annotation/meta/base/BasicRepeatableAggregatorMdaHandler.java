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

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.function.BiFunction;

import com.braintribe.model.generic.annotation.meta.api.RepeatableMdaHandler.RepeatableAggregatorMdaHandler;
import com.braintribe.model.generic.annotation.meta.api.analysis.MdaAnalysisContext;
import com.braintribe.model.generic.annotation.meta.api.synthesis.MdaSynthesisContext;
import com.braintribe.model.meta.data.MetaData;

public class BasicRepeatableAggregatorMdaHandler<RA extends Annotation, M extends MetaData> implements RepeatableAggregatorMdaHandler<RA, M> {

	private final Class<RA> annotationClass;
	private final Class<M> metaDataClass;
	private final BiFunction<RA, MdaAnalysisContext, List<M>> mdListBuilder;

	public BasicRepeatableAggregatorMdaHandler( //
			Class<RA> annotationClass, //
			Class<M> metaDataClass, //
			BiFunction<RA, MdaAnalysisContext, List<M>> mdListBuilder) {

		this.annotationClass = annotationClass;
		this.metaDataClass = metaDataClass;
		this.mdListBuilder = mdListBuilder;
	}

	@Override
	public Class<RA> annotationClass() {
		return annotationClass;
	}

	@Override
	public Class<M> metaDataClass() {
		return metaDataClass;
	}

	@Override
	public List<M> buildMdList(RA annotation, MdaAnalysisContext context) {
		return mdListBuilder.apply(annotation, context);
	}

	@Override
	public void buildAnnotation(MdaSynthesisContext context, M metaData) {
		throw new UnsupportedOperationException("This method should not be invoked for the repeatabe-aggregator type: " + annotationClass.getName());
	}

}
