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
package com.braintribe.devrock.zed.api;

import java.util.Map;

import com.braintribe.devrock.zed.api.context.ZedAnalyzerContext;
import com.braintribe.devrock.zed.commons.ZedException;
import com.braintribe.zarathud.model.data.Artifact;
import com.braintribe.zarathud.model.forensics.FingerPrint;
import com.braintribe.zarathud.model.forensics.ForensicsRating;

/**
 * {@link ZedCore} is the front-end for using zarathud's features
 * @author pit
 *
 */
public interface ZedCore {
	/**
	 * analyze a jar - i.e. extract all relevant information of the jar 
	 * @param terminalArtifact - the {@link Artifact} that this jar reflects 
	 * @return - the {@link Artifact} with all relevant information attached 
	 * @throws ZedException - thrown if anything goes wrong
	 */
	Artifact analyseJar(Artifact terminalArtifact) throws ZedException;
	
	/**
	 * @return - the {@link ZedAnalyzerContext} as built up by {@link ZedCore}, required for all Forensics
	 */
	ZedAnalyzerContext getAnalyzingContext();

	
	/**
	 * extract all suppressing annotations, turn them into 'real' fingerprints, and associate IGNORE with them
	 * @param terminalArtifact - a *already* parsed {@link Artifact}, i.e. the entries must be known
	 * @return - a {@link Map} of the {@link FingerPrint} as read from the pertinent annotations 
	 */
	Map<FingerPrint, ForensicsRating> collectSuppressAnnotations( Artifact terminalArtifact); 
}
