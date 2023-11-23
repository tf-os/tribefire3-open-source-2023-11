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
package com.braintribe.xml.sax.parser.test;

import com.braintribe.xml.sax.parser.test.model.Entry;
import com.braintribe.xml.stagedstax.parser.experts.ComplexEntityExpertFactory;
import com.braintribe.xml.stagedstax.parser.experts.EnumValueExpertFactory;
import com.braintribe.xml.stagedstax.parser.experts.ManipulationParserExpertFactory;
import com.braintribe.xml.stagedstax.parser.experts.StringValueExpertFactory;
import com.braintribe.xml.stagedstax.parser.experts.VirtualCollectionExpertFactory;
import com.braintribe.xml.stagedstax.parser.registry.AbstractContentExpertRegistry;

public class StagedStaxParserTestRegistry extends AbstractContentExpertRegistry {

	
	public StagedStaxParserTestRegistry() {
		addExpertFactory( "*/?auto", new StringValueExpertFactory("autoValue"));
		 
		
		addExpertFactory( "container", new ComplexEntityExpertFactory("com.braintribe.xml.sax.parser.test.model.Container", null));
		addExpertFactory( "container/string", new StringValueExpertFactory("stringValue"));
		addExpertFactory( "container/boolean", new StringValueExpertFactory("booleanValue"));
		addExpertFactory( "container/int", new StringValueExpertFactory("integerValue"));
		addExpertFactory( "container/setEntries", new VirtualCollectionExpertFactory<String>("stringSet", null));
		addExpertFactory( "container/setEntries/entry", new StringValueExpertFactory());
		addExpertFactory( "container/listEntries", new VirtualCollectionExpertFactory<String>("stringList", null));
		addExpertFactory( "container/listEntries/entry", new StringValueExpertFactory());
		addExpertFactory( "container/?instruction", new StringValueExpertFactory("processingInstruction"));
		addExpertFactory( "container/?grouping", new EnumValueExpertFactory("com.braintribe.xml.sax.parser.test.model.Grouping"));
		
		addExpertFactory( "container/complexEntries", new VirtualCollectionExpertFactory<Entry>("entries", null));
		addExpertFactory( "container/complexEntries/entry", new ComplexEntityExpertFactory( "com.braintribe.xml.sax.parser.test.model.Entry", null, null));
		addExpertFactory( "container/complexEntries/entry/value",  new StringValueExpertFactory());
		addExpertFactory( "container/complexEntries/entry/?enrich",  new ManipulationParserExpertFactory());
		
	}
}
