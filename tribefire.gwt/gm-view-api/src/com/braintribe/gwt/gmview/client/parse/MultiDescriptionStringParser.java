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
package com.braintribe.gwt.gmview.client.parse;

import static com.braintribe.utils.lcd.CollectionTools2.asSet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import com.braintribe.gwt.gmview.action.client.ParserResult;
import com.braintribe.model.generic.reflection.SimpleTypes;


public class MultiDescriptionStringParser implements Function<ParserArgument, List<ParserResult>> {
	
	private static final String STRING_SIGNATURE = SimpleTypes.TYPE_STRING.getTypeSignature();
	private final Set<String> descriptions;
	
	public MultiDescriptionStringParser(String... descriptions) {
		this(asSet(descriptions));
	}
	
	public MultiDescriptionStringParser(Set<String> descriptions) {
		this.descriptions = descriptions;
	}

	@Override
	public List<ParserResult> apply(ParserArgument parserArgument) throws RuntimeException {
		if (!parserArgument.hasValue()) {
			return Collections.emptyList();
		}
		
		String value = parserArgument.getValue();
		
		List<ParserResult> results = new ArrayList<>();
		for (String description : descriptions) {
			results.add(new ParserResult(description, STRING_SIGNATURE, value));
		}
		
		return results;
	}

}
