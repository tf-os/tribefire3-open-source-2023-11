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

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.braintribe.model.meta.data.MetaData;
import com.braintribe.model.processing.meta.cmd.context.SelectorContext;
import com.braintribe.model.processing.meta.oracle.QualifiedMetaData;

class MultiMetaDataDescriptor extends AbstractMetaDataDescriptor<List<MetaData>> {

	@Override
	protected List<MetaData> ignoreSelectorsValue() {
		return ownerMetaData.stream() //
				.flatMap(List::stream) //
				.map(QualifiedMetaData::metaData) //
				.collect(Collectors.toList());
	}

	@Override
	protected List<MetaData> volatileValue(SelectorContext selectorContext) {
		List<MetaData> result = newList();

		for (List<QualifiedMetaData> md : ownerMetaData)
			result.addAll(resolutionContext.filterBySelectors(md, selectorContext));

		return Collections.unmodifiableList(result);
	}

}
