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
package tribefire.extension.artifact.management.api.model.request;

import java.util.List;

import com.braintribe.model.generic.annotation.Initializer;
import com.braintribe.model.generic.annotation.meta.Alias;
import com.braintribe.model.generic.annotation.meta.Description;
import com.braintribe.model.generic.eval.EvalContext;
import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.service.api.ServiceRequest;

import tribefire.extension.artifact.management.api.model.data.ArtifactVersions;

/**
 * request to get the group analysis data
 * @author pit
 *
 */
@Description("Extracts analysis data on the group level for the sources pointed to")
public interface GetGroupAnalysisData extends ArtifactManagementRequest {
	
	EntityType<GetGroupAnalysisData> T = EntityTypes.T(GetGroupAnalysisData.class);
	
	String groupLocation = "groupLocation";
	String includeFilterExpression = "includeFilterExpression";
	String excludeFilterExpression = "excludeFilterExpression";
	String sort = "sort";
	String simplifyOutput = "simplifyOutput";
	String enforceRanges = "enforceRanges";
	String leniency = "leniency";
	String output = "output";
	String display = "display";
	String includeSelfreferences = "includeSelfreferences";
	
	
	@Alias("l")
	@Initializer("'.'")
	@Description("Fully qualified path to the group's source directory")
	String getGroupLocation();
	void setGroupLocation(String value);
	
	@Description("regular expression that groups need to match in order to be processed")
	@Alias("i")
	String getIncludeFilterExpression();
	void setIncludeFilterExpression(String value);
	
	@Description("regular expression that groups need to match in order to be NOT processed")
	@Alias("x")
	String getExcludeFilterExpression();
	void setExcludeFilterExpression(String value);

	@Initializer("true")
	@Description("whether to sort the output")	
	boolean getSort();
	void setSort(boolean value);
	
	@Description("whether to simplify resulting version ranges to their lower bound only")
	@Alias("s")
	boolean getSimplifyOutput();
	void setSimplifyOutput(boolean value);


	@Description("whether to regard the use of a non-ranged version to a processed group as a problem")
	@Alias("f")
	boolean getRequireRanges();
	void setRequireRanges(boolean value);

	@Initializer("false")
	@Description("whether to include references within the group in the output")
	@Alias("r")	
	boolean getIncludeSelfreferences();
	void setIncludeSelfreferences(boolean value);


	
	@Override
	EvalContext<List<ArtifactVersions>> eval(Evaluator<ServiceRequest> evaluator);
}
