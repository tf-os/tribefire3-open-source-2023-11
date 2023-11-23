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
package com.braintribe.devrock.zarathud.model.dependency;

import java.util.List;

import com.braintribe.devrock.zarathud.model.common.ArtifactNode;
import com.braintribe.devrock.zarathud.model.common.FingerPrintRating;
import com.braintribe.devrock.zarathud.model.common.ReferenceNode;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * represents a node in the dependency analysis view
 * @author pit
 *
 */
public interface DependencyAnalysisNode extends ArtifactNode {
	
	EntityType<DependencyAnalysisNode> T = EntityTypes.T(DependencyAnalysisNode.class);

	String kind = "kind";
	String references = "references";
	String incompleteForwardReference = "incompleteForwardReference";
	String redacted = "redacted";
	String overridden = "overridden";
	String rating = "rating";
	
	DependencyKind getKind();
	void setKind(DependencyKind value);

	List<ReferenceNode> getReferences();
	void setReferences(List<ReferenceNode> value);
	
	boolean getIncompleteForwardReference();
	void setIncompleteForwardReference(boolean value);

	boolean getRedacted();
	void setRedacted(boolean value);
	
	boolean getOverridden();
	void setOverridden(boolean value);


	FingerPrintRating getRating();
	void setRating(FingerPrintRating value);

	

}
