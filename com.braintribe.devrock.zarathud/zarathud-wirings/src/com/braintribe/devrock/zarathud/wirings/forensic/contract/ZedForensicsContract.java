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
package com.braintribe.devrock.zarathud.wirings.forensic.contract;

import java.util.Map;

import com.braintribe.devrock.zed.api.context.ZedForensicsContext;
import com.braintribe.devrock.zed.api.forensics.ClasspathForensics;
import com.braintribe.devrock.zed.api.forensics.DependencyForensics;
import com.braintribe.devrock.zed.api.forensics.ModelForensics;
import com.braintribe.devrock.zed.forensics.fingerprint.register.FingerPrintRegistry;
import com.braintribe.devrock.zed.forensics.fingerprint.register.RatingRegistry;
import com.braintribe.model.resource.Resource;
import com.braintribe.wire.api.space.WireSpace;
import com.braintribe.zarathud.model.forensics.FingerPrint;
import com.braintribe.zarathud.model.forensics.ForensicsRating;

public interface ZedForensicsContract extends WireSpace {

	/**
	 * @param context - {@link ZedForensicsContext} required to set it up
	 * @return - a fully wired {@link DependencyForensics} 
	 */
	DependencyForensics dependencyForensics(ZedForensicsContext context);
	
	/**
	 * @param context - {@link ZedForensicsContext} required to set it up 
	 * @return - a fully wired {@link ClasspathForensics}
	 */
	ClasspathForensics classpathForensics(ZedForensicsContext context);
	
	/**
	 * @param context - {@link ZedForensicsContext} required to set it up
	 * @return - a fully wired {@link ModelForensics}
	 */
	ModelForensics modelForensics(ZedForensicsContext context);
	
	
	/**
	 * @return - the high-level registry with all collected fingerprints from the forensics
	 */
	FingerPrintRegistry fingerPrintRegistry();
	
	/**
	 * @param codeBasedRatings - the ratings as extracted from the code, via the suppress annotation (overrides standard if duplicates contained)
	 * @param customRatings - the {@link Resource} with the ratings assigned to the artifact (overrides standard, code if duplicates contained)
	 * @param pullRequestRatings - the {@link Resource} with the ratings to be used for the current PR (overrides standard, code, custom if duplicates contained)
	 * @return - a fully wired {@link RatingRegistry}
	 */
	RatingRegistry ratingRegistry( Map<FingerPrint,ForensicsRating> codeBasedRatings, Resource customRatings, Resource pullRequestRatings);
}
