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

import org.antlr.v4.runtime.UnbufferedCharStream;

public interface GmmlParserConfiguration {

	String stageName();

	boolean parseSingleBlock();

	Map<String, Object> variables();

	/**
	 * If true, parser will not use an {@link UnbufferedCharStream}, thus the entire input will be loaded in memory. This can speed parsing up for
	 * small inputs.
	 */
	boolean bufferEntireInput();

}
