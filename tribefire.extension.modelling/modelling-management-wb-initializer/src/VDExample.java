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
import com.braintribe.model.generic.manipulation.ChangeValueManipulation;
import com.braintribe.model.generic.manipulation.LocalEntityProperty;
import com.braintribe.model.generic.value.Variable;

import tribefire.extension.modelling.management.ModellingProject;
import tribefire.extension.modelling.management.api.DeleteProject;

public class VDExample {
	public static void main(String[] args) {
		
		
		
		DeleteProject deleteProject = DeleteProject.T.create();
		// deleteProject.setProject(Variable.T.create());
		
		
		LocalEntityProperty ep = LocalEntityProperty.T.create();
		ep.setEntity(deleteProject);
		ep.setPropertyName("project");
		
		ChangeValueManipulation cvm = ChangeValueManipulation.T.create();
		cvm.setOwner(ep);
		cvm.setNewValue(Variable.T.create());
	}
}
