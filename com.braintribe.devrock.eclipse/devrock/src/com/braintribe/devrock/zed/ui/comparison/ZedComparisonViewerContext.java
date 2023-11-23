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
package com.braintribe.devrock.zed.ui.comparison;

import java.util.List;

import com.braintribe.devrock.zed.api.comparison.SemanticVersioningLevel;
import com.braintribe.zarathud.model.data.Artifact;
import com.braintribe.zarathud.model.forensics.FingerPrint;

public class ZedComparisonViewerContext {

	private Artifact baseArtifact;
	private Artifact otherArtifact;
	private List<FingerPrint> fingerPrints;
	private SemanticVersioningLevel semanticComparisonLevel;
	
	public Artifact getBaseArtifact() {
		return baseArtifact;
	}
	public void setBaseArtifact(Artifact baseArtifact) {
		this.baseArtifact = baseArtifact;
	}
	public Artifact getOtherArtifact() {
		return otherArtifact;
	}
	public void setOtherArtifact(Artifact otherArtifact) {
		this.otherArtifact = otherArtifact;
	}
	public List<FingerPrint> getFingerPrints() {
		return fingerPrints;
	}
	public void setFingerPrints(List<FingerPrint> fingerPrints) {
		this.fingerPrints = fingerPrints;
	}
	public SemanticVersioningLevel getSemanticComparisonLevel() {
		return semanticComparisonLevel;
	}
	public void setSemanticComparisonLevel(SemanticVersioningLevel semanticComparisonLevel) {
		this.semanticComparisonLevel = semanticComparisonLevel;
	}
	
	
	
	
}
