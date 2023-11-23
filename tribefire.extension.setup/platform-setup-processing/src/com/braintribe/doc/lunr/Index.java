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
package com.braintribe.doc.lunr;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * An index contains the built index of all documents and provides a query interface to the index.
 *
 * Usually instances of lunr.Index will not be created using this constructor, instead lunr.Builder should be used to
 * construct new indexes, or lunr.Index.load should be used to load previously built and serialized indexes.
 *
 * @constructor
 * @param {Object}
 *            attrs - The attributes of the built search index.
 * @param {Object}
 *            attrs.invertedIndex - An index of term/field to document reference.
 * @param {Object<string,
 *            lunr.Vector>} attrs.fieldVectors - Field vectors
 * @param {lunr.TokenSet}
 *            attrs.tokenSet - An set of all corpus tokens.
 * @param {string[]}
 *            attrs.fields - The names of indexed document fields.
 * @param {lunr.Pipeline}
 *            attrs.pipeline - The pipeline to use for search terms.
 */

public class Index {

	public Map<String, Map<String, Object>> invertedIndex;
	public Map<String, Vector> fieldVectors;
	public TokenSet tokenSet;
	public List<String> fields;
	public Pipeline pipeline;

	public Index(Map<String, Map<String, Object>> invertedIndex, Map<String, Vector> fieldVectors, TokenSet tokenSet, List<String> fields,
			Pipeline pipeline) {
		super();
		this.invertedIndex = invertedIndex;
		this.fieldVectors = fieldVectors;
		this.tokenSet = tokenSet;
		this.fields = fields;
		this.pipeline = pipeline;
	}

	/**
	 * Prepares the index for JSON serialization.
	 *
	 * The schema for this JSON blob will be described in a separate JSON schema file.
	 *
	 * @returns {Object}
	 */
	public Map<String, Object> toJSON() {
		List<List<Object>> invertedIndex = this.invertedIndex.entrySet().stream().sorted((e1, e2) -> e1.getKey().compareTo(e2.getKey()))
				.map(e -> Arrays.<Object> asList(e.getKey(), e.getValue())).collect(Collectors.toList());

		List<List<Object>> fieldVectors = this.fieldVectors.entrySet().stream().map(e -> Arrays.<Object> asList(e.getKey(), e.getValue().toJSON()))
				.collect(Collectors.toList());

		Map<String, Object> json = new LinkedHashMap<String, Object>();

		json.put("version", Lunr.version);
		json.put("fields", fields);
		json.put("fieldVectors", fieldVectors);
		json.put("invertedIndex", invertedIndex);
		json.put("pipeline", pipeline.toJSON());

		return json;
	}
}
