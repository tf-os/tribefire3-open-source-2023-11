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
package tribefire.extension.xml.schemed.service;

import com.braintribe.model.generic.annotation.meta.Alias;
import com.braintribe.model.generic.annotation.meta.Description;
import com.braintribe.model.generic.annotation.meta.Mandatory;
import com.braintribe.model.generic.eval.EvalContext;
import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.resource.Resource;
import com.braintribe.model.service.api.ServiceRequest;

import tribefire.extension.xml.schemed.model.api.xsd.analyzer.api.model.ReferencedSchemata;
import tribefire.extension.xml.schemed.model.api.xsd.analyzer.api.model.SchemedXmlXsdAnalyzerRequest;

/**
 * a version of the {@link SchemedXmlXsdAnalyzerRequest} for Jinni's command-line tooling 
 * 
 * @author pit
 *
 */
public interface AnalyzeXsd extends AnalyzeXsdRequest {
	
	EntityType<AnalyzeXsd> T = EntityTypes.T(AnalyzeXsd.class);
	
	/** 
	 * @return - the resource that contains the XSD that should be parsed 
	 */
	@Description("the resource that contains the XSD that should be parsed")
	@Alias("s")
	@Mandatory
	Resource getSchema();
	void setSchema( Resource resource);
	
	/**
	 * @return - {@link ReferencedSchemata} that contains all referenced Schemata - if any
	 */
	@Description("all referenced schema - if any")
	@Alias("r")
	ReferencedSchemata getReferencedSchemata();
	void setReferencedSchemata( ReferencedSchemata schemata);
	
	@Override
	EvalContext<? extends AnalyzedXsd> eval(Evaluator<ServiceRequest> evaluator);
	
}
