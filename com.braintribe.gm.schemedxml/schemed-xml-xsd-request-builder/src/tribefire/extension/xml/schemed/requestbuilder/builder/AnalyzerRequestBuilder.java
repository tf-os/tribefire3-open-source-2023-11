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
package tribefire.extension.xml.schemed.requestbuilder.builder;

import tribefire.extension.xml.schemed.model.api.xsd.analyzer.api.model.SchemedXmlXsdAnalyzerRequest;
import tribefire.extension.xml.schemed.requestbuilder.builder.impl.AnalyzerRequestContext;

/**
 * a builder to build {@link SchemedXmlXsdAnalyzerRequest} instances to parameterize the SchemedXmlXsdAnalyer 
 * from com.braintribe.gm.schemedxml:schemed-xsd-xml-analyzer.
 * 
 * @author pit
 *
 */
public class AnalyzerRequestBuilder {

	/**
	 * start building a {@link SchemedXmlXsdAnalyzerRequest}
	 * @return - a {@link AnalyzerRequestContext} to work with
	 */
	public static AnalyzerRequestContext request() {
		return new AnalyzerRequestContext();
	}
}
