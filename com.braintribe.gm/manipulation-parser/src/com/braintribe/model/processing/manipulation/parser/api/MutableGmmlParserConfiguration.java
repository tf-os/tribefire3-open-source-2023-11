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
package com.braintribe.model.processing.manipulation.parser.api;

import java.util.Map;

import com.braintribe.model.processing.manipulation.parser.impl.listener.error.LenientErrorHandler;

public interface MutableGmmlParserConfiguration extends GmmlParserConfiguration {

	void setStageName(String stageName);

	void setParseSingleBlock(boolean parseSingleBlock);

	/**
	 * @use {@link MutableGmmlManipulatorParserConfiguration#setErrorHandler(GmmlManipulatorErrorHandler)} instead. To achieve lenient parsing
	 *      ignoring all errors pass {@link LenientErrorHandler}.
	 */
	@Deprecated
	void setLenient(boolean lenient);

	/** Hey man, this map will be modified. */
	void setVariables(Map<String, Object> variables);

	void setBufferEntireInput(boolean bufferEntireInput);
}
