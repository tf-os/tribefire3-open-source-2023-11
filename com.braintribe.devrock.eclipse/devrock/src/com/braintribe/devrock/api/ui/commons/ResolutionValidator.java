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
package com.braintribe.devrock.api.ui.commons;

import java.util.List;

import com.braintribe.gm.model.reason.Reason;
import com.braintribe.model.artifact.analysis.AnalysisArtifact;
import com.braintribe.model.artifact.analysis.AnalysisArtifactResolution;
import com.braintribe.model.artifact.analysis.AnalysisDependency;
import com.braintribe.model.artifact.analysis.AnalysisTerminal;

/**
 * temporary helper - required as long as failure reasons on terminals are not reflected in the resolution
 * @author pit
 *
 */
public class ResolutionValidator {

	/**
	 * @param resolution - the {@link AnalysisArtifactResolution}
	 * @return - true if the resolution or the terminals have failed
	 */
	public static boolean isResolutionInvalid( AnalysisArtifactResolution resolution) {
		if (resolution.hasFailed())
			return true;
		
		List<AnalysisTerminal> terminals = resolution.getTerminals();
		for (AnalysisTerminal terminal : terminals) {
			if (terminal instanceof AnalysisArtifact) {
				AnalysisArtifact aa = (AnalysisArtifact) terminal;
				if (aa.hasFailed()) {
					return true;
				}
			}
			else if (terminal instanceof AnalysisDependency) {
				continue;
			}
		}
		return false;
	}
	
	/**
	 * @param resolution - the {@link AnalysisArtifactResolution}
	 * @return - a collator {@link Reason} with all reasons attached
	 */
	public static Reason getReasonForFailure( AnalysisArtifactResolution resolution) {
		
		// no longer required as resolution is now properly flagged if a terminal has failed
		/*
		List<Reason> terminalFailureReasons = new ArrayList<>( resolution.getTerminals().size());
		
		for (AnalysisTerminal terminal : resolution.getTerminals()) {
			if (terminal instanceof AnalysisArtifact) {
				AnalysisArtifact analysisArtifact = (AnalysisArtifact) terminal;
				if (analysisArtifact.hasFailed()) {
					terminalFailureReasons.add( analysisArtifact.getFailure());
				}
			}
		}
		Reason resolutionFailureReason = resolution.getFailure();
		
		if (terminalFailureReasons.size() == 0 && resolutionFailureReason == null) 
			return null;
		
		if (terminalFailureReasons.size() == 0) {
			return resolutionFailureReason;
		}
		
		if (resolutionFailureReason != null) {
			resolutionFailureReason.getReasons().addAll(terminalFailureReasons);
			return resolutionFailureReason;
		} 
		resolutionFailureReason = Reasons.build( IncompleteResolution.T).text("invalid resolution due to invalid terminals").causes( terminalFailureReasons.toArray( new Reason[0])).toReason();
		return resolutionFailureReason;
		*/
		
		return resolution.getFailure();
		
	}
}
