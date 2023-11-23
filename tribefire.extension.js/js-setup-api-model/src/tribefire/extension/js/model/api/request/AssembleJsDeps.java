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
package tribefire.extension.js.model.api.request;

import com.braintribe.model.generic.annotation.Initializer;
import com.braintribe.model.generic.annotation.meta.Alias;
import com.braintribe.model.generic.annotation.meta.Description;
import com.braintribe.model.generic.annotation.meta.Mandatory;
import com.braintribe.model.generic.annotation.meta.PositionalArguments;
import com.braintribe.model.generic.eval.EvalContext;
import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.model.service.api.result.Neutral;

@PositionalArguments({"projectPath"})
@Description("Assembles transitive dependencies for a given project into its /lib folder."
		+ "A project is identified by its pom.xml. This request is workspace-aware. In case the parent directory from where the request "
		+ "is called contains file 'js-workspace.yaml', local projects will be preferred over the js-libraries repository if they satisfy a dependency. "
		+ "Symbolic links are used for referring to respective sources. A project links to its own source folder within its /lib folder, also via symbolic link. "
		+ "The symbolic link folder names represent potential version uncertainties of respective dependencies.")
public interface AssembleJsDeps extends JsSetupRequest {
	
	EntityType<AssembleJsDeps> T = EntityTypes.T(AssembleJsDeps.class);

	@Alias("min")
	@Description("Prefer minified over pretty package, if given.")
	boolean getPreferMinOverPretty();
	void setPreferMinOverPretty(boolean preferMinOverPretty);
	
	@Initializer("'.'")
	@Mandatory
	@Description("The path to the project containing the pom.xml which dependencies should be assembled transitively.")
	String getProjectPath();
	void setProjectPath(String projectPath);
	
	@Override
	EvalContext<? extends Neutral> eval(Evaluator<ServiceRequest> evaluator);
	
}
