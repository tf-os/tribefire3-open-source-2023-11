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

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * lunr.Builder performs indexing on a set of documents and returns instances of lunr.Index ready for querying.
 *
 * All configuration of the index is done via the builder, the fields to index, the document reference, the text
 * processing pipeline and document scoring parameters are all set on the builder before indexing.
 *
 * @constructor
 * @property {string} _ref - Internal reference to the document reference field.
 * @property {string[]} _fields - Internal reference to the document fields to index.
 * @property {object} invertedIndex - The inverted index maps terms to document fields.
 * @property {object} documentTermFrequencies - Keeps track of document term frequencies.
 * @property {object} documentLengths - Keeps track of the length of documents added to the index.
 * @property {lunr.tokenizer} tokenizer - Function for splitting strings into tokens for indexing.
 * @property {lunr.Pipeline} pipeline - The pipeline performs text processing on tokens before indexing.
 * @property {lunr.Pipeline} searchPipeline - A pipeline for processing search terms before querying the index.
 * @property {number} documentCount - Keeps track of the total number of documents indexed.
 * @property {number} _b - A parameter to control field length normalization, setting this to 0 disabled normalization,
 *           1 fully normalizes field lengths, the default value is 0.75.
 * @property {number} _k1 - A parameter to control how quickly an increase in term frequency results in term frequency
 *           saturation, the default value is 1.2.
 * @property {number} termIndex - A counter incremented for each unique term, used to identify a terms position in the
 *           vector space.
 * @property {array} metadataWhitelist - A list of metadata keys that have been whitelisted for entry in the index.
 */

public class Builder<T> {

	public static class FieldAttributes {

	}

	public static class Field<T> {
		public Function<? super T, String> accessor;
		public String name;
		public FieldAttributes attributes;
		public int boost = 1;

		public Field(String name, Function<? super T, String> accessor, FieldAttributes attributes) {
			super();
			this.accessor = accessor;
			this.name = name;
			this.attributes = attributes;
		}
	}

	private Function<? super T, String> _ref;
	private Map<String, Field<T>> _fields = new LinkedHashMap<>();
	private Map<String, DocumentAttributes> _documents = new LinkedHashMap<>();
	private Map<String, Map<String, Object>> invertedIndex = new LinkedHashMap<>();
	private Map<String, Map<String, Integer>> fieldTermFrequencies = new LinkedHashMap<>();
	private Map<String, Integer> fieldLengths = new LinkedHashMap<>();
	private BiFunction<Object, Map<String, Object>, List<Token>> tokenizer = Lunr.tokenizer;
	public Pipeline pipeline = new Pipeline();
	public Pipeline searchPipeline = new Pipeline();
	private int documentCount = 0;
	private double _b = 0.75;
	private double _k1 = 1.2;
	private int termIndex = 0;
	private List<String> metadataWhitelist = new ArrayList<>();
	private Map<String, Double> averageFieldLength;
	private Map<String, Vector> fieldVectors;
	private TokenSet tokenSet;

	public Builder(Function<? super T, String> refFunction) {
		ref(refFunction);
	}

	/**
	 * Sets the document field used as the document reference. Every document must have this field. The type of this
	 * field in the document should be a string, if it is not a string it will be coerced into a string by calling
	 * toString.
	 *
	 * The default ref is 'id'.
	 *
	 * The ref should _not_ be changed during indexing, it should be set before any documents are added to the index.
	 * Changing it during indexing can lead to inconsistent results.
	 *
	 * @param {string}
	 *            ref - The name of the reference field in the document.
	 */
	public void ref(Function<? super T, String> ref) {
		this._ref = ref;
	}

	/**
	 * Adds a field to the list of document fields that will be indexed. Every document being indexed should have this
	 * field. Null values for this field in indexed documents will not cause errors but will limit the chance of that
	 * document being retrieved by searches.
	 *
	 * All fields should be added before adding documents to the index. Adding fields after a document has been indexed
	 * will have no effect on already indexed documents.
	 *
	 * Fields can be boosted at build time. This allows terms within that field to have more importance when ranking
	 * search results. Use a field boost to specify that matches within one field are more important than other fields.
	 *
	 * @param {string}
	 *            fieldName - The name of a field to index in all documents.
	 * @param {object}
	 *            attributes - Optional attributes associated with this field.
	 * @param {number}
	 *            [attributes.boost=1] - Boost applied to all terms within this field.
	 * @param {fieldExtractor}
	 *            [attributes.extractor] - Function to extract a field from a document.
	 * @throws {RangeError}
	 *             fieldName cannot contain unsupported characters '/'
	 */
	public void field(String fieldName, Function<? super T, String> accessor, FieldAttributes attributes) {
		if (fieldName.indexOf('/') != -1) {
			throw new IllegalArgumentException("Field '" + fieldName + "' contains illegal character '/'");
		}

		_fields.put(fieldName, new Field<T>(fieldName, accessor, attributes != null ? attributes : new FieldAttributes()));
	}

	/**
	 * A parameter to tune the amount of field length normalisation that is applied when calculating relevance scores. A
	 * value of 0 will completely disable any normalisation and a value of 1 will fully normalise field lengths. The
	 * default is 0.75. Values of b will be clamped to the range 0 - 1.
	 *
	 * @param {number}
	 *            number - The value to set for this tuning parameter.
	 */
	public void b(double number) {
		if (number < 0) {
			this._b = 0;
		} else if (number > 1) {
			this._b = 1;
		} else {
			this._b = number;
		}
	}

	/**
	 * A parameter that controls the speed at which a rise in term frequency results in term frequency saturation. The
	 * default value is 1.2. Setting this to a higher value will give slower saturation levels, a lower value will
	 * result in quicker saturation.
	 *
	 * @param {number}
	 *            number - The value to set for this tuning parameter.
	 */
	public void k1(double number) {
		this._k1 = number;
	}

	/**
	 * Adds a document to the index.
	 *
	 * Before adding fields to the index the index should have been fully setup, with the document ref and all fields to
	 * index already having been specified.
	 *
	 * The document must have a field name as specified by the ref (by default this is 'id') and it should have all
	 * fields defined for indexing, though null or undefined values will not cause errors.
	 *
	 * Entire documents can be boosted at build time. Applying a boost to a document indicates that this document should
	 * rank higher in search results than other documents.
	 *
	 * @param {object}
	 *            doc - The document to add to the index.
	 * @param {object}
	 *            attributes - Optional attributes associated with this document.
	 * @param {number}
	 *            [attributes.boost=1] - Boost applied to all terms within this document.
	 */
	public void add(T doc, DocumentAttributes attributes) {
		String docRef = _ref.apply(doc);

		_documents.put(docRef, attributes != null ? attributes : new DocumentAttributes());

		this.documentCount += 1;

		for (Field<T> _field : _fields.values()) {
			String fieldName = _field.name;
			String field = _field.accessor.apply(doc);
			
			if (field == null) {
				throw new IllegalStateException("Mandatory field '" + _field.name + "' was set to null for " + doc);
			}

			Map<String, Object> tokenizerAttributes = new LinkedHashMap<>();
			tokenizerAttributes.put("fields", Collections.singletonList(fieldName));

			List<Token> tokens = tokenizer.apply(field, tokenizerAttributes);

			List<Token> terms = this.pipeline.run(tokens);
			FieldRef fieldRef = new FieldRef(docRef, fieldName);
			Map<String, Integer> fieldTerms = new LinkedHashMap<>();

			fieldTermFrequencies.put(fieldRef.toString(), fieldTerms);

			// store the length of this field for this document
			fieldLengths.put(fieldRef.toString(), terms.size());

			// calculate term frequencies for this field
			int termsLen = terms.size();
			for (Token term : terms) {

				fieldTerms.compute(term.toString(), (k, v) -> v != null ? v + 1 : 1);

				// add to inverted index
				// create an initial posting if one doesn't exist
				Map<String, Object> postings = invertedIndex.computeIfAbsent(term.toString(), k -> {
					Map<String, Object> posting = new LinkedHashMap<>();
					posting.put("_index", termIndex++);

					for (Field<T> curField : _fields.values()) {
						posting.put(curField.name, new LinkedHashMap<String, Object>());
					}
					return posting;
				});

				// add an entry for this term/fieldName/docRef to the invertedIndex
				Map<String, Map<String, List<Object>>> fieldIndex = (Map<String, Map<String, List<Object>>>) postings.get(fieldName);

				Map<String, List<Object>> docIndex = fieldIndex.computeIfAbsent(docRef, k -> new LinkedHashMap<String, List<Object>>());

				// store all whitelisted metadata about this token in the
				// inverted index
				for (String metadataKey : metadataWhitelist) {
					Object metadata = term.metadata.get(metadataKey);
					List<Object> mdList = docIndex.computeIfAbsent(metadataKey, k -> new ArrayList<>());
					mdList.add(metadata);
				}
			}
		}
	}

	/**
	 * Calculates the average document length for this index
	 *
	 * @private
	 */
	public void calculateAverageFieldLengths() {

		Map<String, Double> accumulator = new LinkedHashMap<>();
		Map<String, Integer> documentsWithField = new LinkedHashMap<>();

		for (String fieldKey : fieldLengths.keySet()) {
			FieldRef fieldRef = FieldRef.fromString(fieldKey);
			String field = fieldRef.fieldName;

			documentsWithField.compute(field, (k, v) -> v != null ? v + 1 : 1);

			accumulator.compute(field, (k, v) -> {
				int increment = this.fieldLengths.get(fieldRef.toString());
				return v != null ? v + increment : increment;
			});
		}

		for (Field<T> field : _fields.values()) {
			String fieldName = field.name;

			accumulator.compute(fieldName, (k, v) -> v / documentsWithField.get(fieldName));
		}

		this.averageFieldLength = accumulator;
	}

	/**
	 * Builds a vector space model of every document using lunr.Vector
	 *
	 * @private
	 */
	public void createFieldVectors() {
		Map<String, Vector> fieldVectors = new LinkedHashMap<>();
		Map<String, Double> termIdfCache = new LinkedHashMap<>();

		for (String fieldKey : fieldTermFrequencies.keySet()) {
			FieldRef fieldRef = FieldRef.fromString(fieldKey);
			String fieldName = fieldRef.fieldName;
			int fieldLength = fieldLengths.get(fieldRef.toString());
			Vector fieldVector = new Vector();
			Map<String, Integer> termFrequencies = fieldTermFrequencies.get(fieldRef.toString());
			Set<String> terms = termFrequencies.keySet();

			int fieldBoost = _fields.get(fieldName).boost;
			int docBoost = _documents.get(fieldRef.docRef).boost;

			for (String term : terms) {
				int tf = termFrequencies.get(term);
				Map<String, Object> posting = invertedIndex.get(term);
				int termIndex = (Integer) posting.get("_index");

				double idf = termIdfCache.computeIfAbsent(term, k -> Lunr.idf(posting, documentCount));

				double score = idf * ((this._k1 + 1) * tf)
						/ (this._k1 * (1 - this._b + this._b * (fieldLength / this.averageFieldLength.get(fieldName))) + tf);

				// apply boost factors
				score *= fieldBoost * docBoost;

				double scoreWithPrecision = Math.round(score * 1000) / 1000D;
				// Converts 1.23456789 to 1.234.
				// Reducing the precision so that the vectors take up less
				// space when serialised. Doing it now so that they behave
				// the same before and after serialisation. Also, this is
				// the fastest approach to reducing a number's precision in
				// JavaScript.

				fieldVector.insert(termIndex, scoreWithPrecision);
			}

			fieldVectors.put(fieldRef.toString(), fieldVector);
		}

		this.fieldVectors = fieldVectors;
	}

	/**
	 * Creates a token set of all tokens in the index using lunr.TokenSet
	 *
	 * @private
	 */
	public void createTokenSet() {
	  List<String> keys = new ArrayList<>(invertedIndex.keySet());
	  keys.sort(null);
	  this.tokenSet = TokenSet.fromArray(keys);
	}

	/**
	 * Builds the index, creating an instance of lunr.Index.
	 *
	 * This completes the indexing process and should only be called once all documents have been added to the index.
	 *
	 * @returns {lunr.Index}
	 */
	public Index build() {
	  calculateAverageFieldLengths();
	  createFieldVectors();
	  createTokenSet();

	  return new Index(invertedIndex, fieldVectors, tokenSet, new ArrayList<>(_fields.keySet()), searchPipeline);
	}


}
