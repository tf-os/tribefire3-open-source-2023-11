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
package tribefire.module.wire.contract;

import java.io.File;

import com.braintribe.mimetype.MimeTypeDetector;
import com.braintribe.model.processing.resource.enrichment.ResourceEnrichingStreamer;
import com.braintribe.model.resource.Resource;
import com.braintribe.model.resource.api.MimeTypeRegistry;
import com.braintribe.model.resource.api.ResourceBuilder;
import com.braintribe.utils.stream.api.StreamPipe;
import com.braintribe.utils.stream.api.StreamPipeFactory;
import com.braintribe.wire.api.space.WireSpace;

/**
 * A collection of beans related to manipulation of binary data.
 * <p>
 * 
 * Transient data roughly means temporary data stored in memory or a temporary file, as opposed to data stored in a DB or on a disk.
 * 
 * @author peter.gazdik
 */
public interface ResourceProcessingContract extends WireSpace {

	MimeTypeDetector mimeTypeDetector();

	MimeTypeRegistry mimeTypeRegistry();

	ResourceEnrichingStreamer enrichingStreamer();

	/**
	 * A {@link StreamPipeFactory} for all-purpose {@link StreamPipe}s. Use this in the general case so the underlying resources can be shared most
	 * efficiently.
	 * <p>
	 * For more specific use-case (e.g. large number of small files or small number of large files) consider configuring your own factory instead.
	 */
	StreamPipeFactory streamPipeFactory();

	/** @return folder where {@link #streamPipeFactory()} stores it's temporary files. */
	File streamPipeFolder();

	/**
	 * Standard {@link ResourceBuilder} (click it to see usage example) for transient {@link Resource}s, most probably backed by
	 * {@link #streamPipeFactory()}.
	 */
	ResourceBuilder transientResourceBuilder();

}
