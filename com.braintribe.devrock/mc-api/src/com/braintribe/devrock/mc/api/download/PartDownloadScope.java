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
package com.braintribe.devrock.mc.api.download;

import com.braintribe.devrock.mc.api.resolver.ArtifactDataResolution;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.model.artifact.compiled.CompiledArtifactIdentification;
import com.braintribe.model.artifact.essential.PartIdentification;
import com.braintribe.processing.async.api.Promise;

/**
 * a {@link PartDownloadScope} can be used to download files in a fair manner 
 * @author pit / dirk
 *
 */
public interface PartDownloadScope {
	/**
	 * @param identification - the {@link CompiledArtifactIdentification} that identifies the artifact 
	 * @param partIdentification - the {@link PartIdentification} that identifies the part
	 * @return - a {@link Promise} with the download job - which is automatically queued 
	 */
	Promise<Maybe<ArtifactDataResolution>> download(CompiledArtifactIdentification identification, PartIdentification partIdentification);
}
