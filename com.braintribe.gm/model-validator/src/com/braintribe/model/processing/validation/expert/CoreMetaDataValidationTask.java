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
package com.braintribe.model.processing.validation.expert;

import static com.braintribe.model.processing.validation.ValidationMessageLevel.ERROR;
import static com.braintribe.model.processing.validation.expert.CommonChecks.isNotNull;
import static com.braintribe.utils.lcd.CollectionTools2.asList;
import static com.braintribe.utils.lcd.StringTools.isEmpty;

import java.util.List;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.meta.data.MetaData;
import com.braintribe.model.meta.data.mapping.Alias;
import com.braintribe.model.meta.data.mapping.PositionalArguments;
import com.braintribe.model.processing.validation.ValidationContext;

public class CoreMetaDataValidationTask implements ValidationTask {

	private MetaData metaData;
	private final List<EntityType<?>> allowedMetadata = asList(Alias.T, PositionalArguments.T); // TODO create essential meta data list

	public CoreMetaDataValidationTask(MetaData metaData) {
		this.metaData = metaData;
	}

	@Override
	public void execute(ValidationContext context) {
		if (isEmpty(metaData.getGlobalId())) {
			context.addValidationMessage(metaData, ERROR, "Global id is missing");
		}
		if (!allowedMetadata.contains(metaData.type())) {
			context.addValidationMessage(metaData, ERROR, "Only essential meta data is allowed");
		}
		if (isNotNull(metaData.getSelector())) {
			context.addValidationMessage(metaData, ERROR, "Meta data selector is not allowed");
		}
	}
}
