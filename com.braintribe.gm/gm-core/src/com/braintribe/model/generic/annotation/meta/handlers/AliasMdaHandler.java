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

import static com.braintribe.model.generic.annotation.meta.base.MdaAnalysisTools.newMd;
import static com.braintribe.utils.lcd.CollectionTools2.newList;

import java.util.List;

import com.braintribe.model.generic.annotation.meta.Alias;
import com.braintribe.model.generic.annotation.meta.Aliases;
import com.braintribe.model.generic.annotation.meta.api.RepeatableMdaHandler;
import com.braintribe.model.generic.annotation.meta.api.analysis.MdaAnalysisContext;
import com.braintribe.model.generic.annotation.meta.api.synthesis.MdaSynthesisContext;
import com.braintribe.model.generic.annotation.meta.api.synthesis.SingleAnnotationDescriptor;
import com.braintribe.model.generic.annotation.meta.base.BasicRepeatableAggregatorMdaHandler;

/**
 * @author peter.gazdik
 */
public class AliasMdaHandler implements RepeatableMdaHandler<Alias, Aliases, com.braintribe.model.meta.data.mapping.Alias> {

	public static final AliasMdaHandler INSTANCE = new AliasMdaHandler();

	private final RepeatableAggregatorMdaHandler<Aliases, com.braintribe.model.meta.data.mapping.Alias> aggregatorHandler = new BasicRepeatableAggregatorMdaHandler<>(
			Aliases.class, com.braintribe.model.meta.data.mapping.Alias.class, this::buildMdListForRepeatable);

	// @formatter:off
	@Override public Class<Alias> annotationClass() { return Alias.class; }
	@Override public RepeatableAggregatorMdaHandler<Aliases, com.braintribe.model.meta.data.mapping.Alias> aggregatorHandler() { return aggregatorHandler; }
	@Override public Class<com.braintribe.model.meta.data.mapping.Alias> metaDataClass() { return com.braintribe.model.meta.data.mapping.Alias.class; }
	// @formatter:on

	@Override
	public List<com.braintribe.model.meta.data.mapping.Alias> buildMdList(Alias annotation, MdaAnalysisContext context) {
		return buildMetaDataFor(context, annotation);
	}

	private List<com.braintribe.model.meta.data.mapping.Alias> buildMdListForRepeatable(Aliases aliases, MdaAnalysisContext context) {
		return buildMetaDataFor(context, aliases.value());
	}

	private static List<com.braintribe.model.meta.data.mapping.Alias> buildMetaDataFor(MdaAnalysisContext context, Alias... aliases) {
		List<com.braintribe.model.meta.data.mapping.Alias> result = newList();

		int i = 0;
		for (Alias alias : aliases)
			result.add(toAliasMd(context, alias, i++));

		return result;
	}

	private static com.braintribe.model.meta.data.mapping.Alias toAliasMd(MdaAnalysisContext context, Alias alias, int i) {
		String globalId = alias.globalId();

		com.braintribe.model.meta.data.mapping.Alias result = newMd(context, com.braintribe.model.meta.data.mapping.Alias.T, globalId, i);
		result.setName(alias.value());

		return result;
	}

	@Override
	public void buildAnnotation(MdaSynthesisContext context, com.braintribe.model.meta.data.mapping.Alias md) {
		SingleAnnotationDescriptor result = context.newDescriptor(Alias.class);
		result.addAnnotationValue("value", md.getName());

		context.setCurrentDescriptorMulti(result, Aliases.class);
	}

}
