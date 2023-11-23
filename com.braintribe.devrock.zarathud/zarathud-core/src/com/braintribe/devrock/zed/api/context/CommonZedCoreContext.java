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
package com.braintribe.devrock.zed.api.context;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.Map;
import java.util.function.Predicate;

import com.braintribe.devrock.zed.api.core.ArtifactRegistry;
import com.braintribe.devrock.zed.api.core.CachingZedRegistry;
import com.braintribe.devrock.zed.api.core.ZedEntityResolver;
import com.braintribe.model.generic.session.GmSession;
import com.braintribe.zarathud.model.data.Artifact;

/**
 * interface for all contexts
 * @author pit
 *
 */
public interface CommonZedCoreContext {
	/**
	 * @return
	 */
	Artifact terminalArtifact();
	/**
	 * @return
	 */
	ArtifactRegistry artifacts();
	/**
	 * @return
	 */
	GmSession session();
	/**
	 * @return
	 */
	Map<URL, Artifact> urlToArtifactMap();
	/**
	 * @return
	 */
	ZedEntityResolver resolver();
	/**
	 * @return
	 */
	Predicate<Artifact> validImplicitArtifactReferenceFilter();
	/**
	 * @return
	 */
	URLClassLoader classloader();
	/**
	 * @return
	 */
	CachingZedRegistry registry();
}
