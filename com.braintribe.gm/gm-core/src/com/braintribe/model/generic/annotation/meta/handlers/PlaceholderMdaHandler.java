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
package com.braintribe.model.generic.annotation.meta.handlers;

import java.util.List;

import com.braintribe.model.generic.annotation.meta.Placeholder;
import com.braintribe.model.generic.annotation.meta.Placeholders;
import com.braintribe.model.generic.annotation.meta.api.RepeatableMdaHandler;
import com.braintribe.model.generic.annotation.meta.api.analysis.MdaAnalysisContext;
import com.braintribe.model.generic.annotation.meta.api.synthesis.MdaSynthesisContext;
import com.braintribe.model.generic.annotation.meta.base.BasicRepeatableAggregatorMdaHandler;
import com.braintribe.model.generic.annotation.meta.base.MdaAnalysisTools;

/**
 * @author peter.gazdik
 */
public class PlaceholderMdaHandler implements RepeatableMdaHandler<Placeholder, Placeholders, com.braintribe.model.meta.data.prompt.Placeholder> {

	public static final PlaceholderMdaHandler INSTANCE = new PlaceholderMdaHandler();

	private final RepeatableAggregatorMdaHandler<Placeholders, com.braintribe.model.meta.data.prompt.Placeholder> repeatableHandler = new BasicRepeatableAggregatorMdaHandler<>(
			Placeholders.class, com.braintribe.model.meta.data.prompt.Placeholder.class, this::buildMdForRepeatable);

	// @formatter:off
	@Override public Class<Placeholder> annotationClass() { return Placeholder.class; }
	@Override public RepeatableAggregatorMdaHandler<Placeholders, com.braintribe.model.meta.data.prompt.Placeholder> aggregatorHandler() { return repeatableHandler; }
	@Override public Class<com.braintribe.model.meta.data.prompt.Placeholder> metaDataClass() { return com.braintribe.model.meta.data.prompt.Placeholder.class; }
	// @formatter:on

	@Override
	public List<com.braintribe.model.meta.data.prompt.Placeholder> buildMdList(Placeholder annotation, MdaAnalysisContext context) {
		return buildMd(context, annotation);
	}

	private List<com.braintribe.model.meta.data.prompt.Placeholder> buildMdForRepeatable(Placeholders placeholders, MdaAnalysisContext context) {
		return buildMd(context, placeholders.value());
	}

	public List<com.braintribe.model.meta.data.prompt.Placeholder> buildMd(MdaAnalysisContext context, Placeholder... placeholders) {
		return MdaAnalysisTools.toLsBasedMd(context, this, //
				Placeholder::locale, //
				Placeholder::value, //
				Placeholder::globalId, //
				com.braintribe.model.meta.data.prompt.Placeholder::setDescription, //
				placeholders);
	}

	@Override
	public void buildAnnotation(MdaSynthesisContext context, com.braintribe.model.meta.data.prompt.Placeholder md) {
		MdaAnalysisTools.buildLsBasedAnnotation(context, md, this, com.braintribe.model.meta.data.prompt.Placeholder::getDescription);
	}

}
