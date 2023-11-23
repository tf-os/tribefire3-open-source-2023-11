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
package com.braintribe.devrock.greyface.process.notification;

import java.util.List;
import java.util.Set;

import com.braintribe.model.artifact.Part;
import com.braintribe.model.artifact.Solution;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.malaclypse.cfg.preferences.gf.RepositorySetting;


public interface UploadContext extends GenericEntity {
	
	
	final EntityType<UploadContext> T = EntityTypes.T(UploadContext.class);

	List<Solution> getSolutions();
	void setSolutions( List<Solution> solutions);
	
	Set<Solution> getRootSolutions();
	void setRootSolutions( Set<Solution> solutions);
	
	List<Part> getParts();
	void setParts( List<Part> parts);
	
	RepositorySetting getTarget();
	void setTarget( RepositorySetting target);
	
	Set<RepositorySetting> getSources();
	void setSources( Set<RepositorySetting> sources);
	
	boolean getPrunePom();
	void setPrunePom( boolean prune);

}
