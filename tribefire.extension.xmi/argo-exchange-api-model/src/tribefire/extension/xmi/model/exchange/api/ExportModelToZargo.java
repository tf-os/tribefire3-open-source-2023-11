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
package tribefire.extension.xmi.model.exchange.api;

import com.braintribe.model.generic.annotation.meta.Alias;
import com.braintribe.model.generic.annotation.meta.Description;
import com.braintribe.model.generic.annotation.meta.Mandatory;
import com.braintribe.model.generic.annotation.meta.PositionalArguments;
import com.braintribe.model.generic.eval.EvalContext;
import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.resource.FileResource;
import com.braintribe.model.service.api.ServiceRequest;

@Description("Exports a model given by a model artifact dependency to a zargo file which may already exists. "
		+ "If it exists the export tries to keep as much information as possible (e.g. internal ids, layout to type bindings.")
@PositionalArguments({"model", "zargo"})
public interface ExportModelToZargo extends ArgoExchangeRequest {
	EntityType<ExportModelToZargo> T = EntityTypes.T(ExportModelToZargo.class);
	
	@Description("The zargo target file to which the model should be exported. The file may exist and certain informations such as internal ids will be preserved. If no file is given a file name <artifactId>.zargo in the current working directory will be implied.")
	@Alias("z")
	FileResource getZargo();
	void setZargo(FileResource zargo);

	@Description("The artifact dependency to the model to be exported in the format <groupId>:<artifactId>#<version-expression>.")
	@Mandatory
	@Alias("m")
	String getModel();
	void setModel(String model);
	
	@Override
	EvalContext<GmMetaModel> eval(Evaluator<ServiceRequest> evaluator);
}
