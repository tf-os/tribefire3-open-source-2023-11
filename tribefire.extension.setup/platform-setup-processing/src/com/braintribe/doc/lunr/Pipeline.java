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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.braintribe.logging.Logger;

public class Pipeline {
	private static Logger logger = Logger.getLogger(Pipeline.class);
	private List<PipelineFunction> _stack = new ArrayList<>();
	
	public static Map<String, PipelineFunction> registeredFunctions = new HashMap<>();
	public static Map<PipelineFunction, String> functionLabels = new HashMap<>();
	
	static {
		registerFunction(Lunr.stemmer, "stemmer");
		registerFunction(Lunr.stopWordFilter, "stopWordFilter");
		registerFunction(Lunr.trimmer, "trimmer");
	}
	
	
	/**
	 * A pipeline function maps lunr.Token to lunr.Token. A lunr.Token contains the token
	 * string as well as all known metadata. A pipeline function can mutate the token string
	 * or mutate (or add) metadata for a given token.
	 *
	 * A pipeline function can indicate that the passed token should be discarded by returning
	 * null. This token will not be passed to any downstream pipeline functions and will not be
	 * added to the index.
	 *
	 * Multiple tokens can be returned by returning an array of tokens. Each token will be passed
	 * to any downstream pipeline functions and all will returned tokens will be added to the index.
	 *
	 * Any number of pipeline functions may be chained together using a lunr.Pipeline.
	 *
	 * @interface lunr.PipelineFunction
	 * @param {lunr.Token} token - A token from the document being processed.
	 * @param {number} i - The index of this token in the complete list of tokens for this document/field.
	 * @param {lunr.Token[]} tokens - All tokens for this document/field.
	 * @returns {(?lunr.Token|lunr.Token[])}
	 */

	/**
	 * Register a function with the pipeline.
	 *
	 * Functions that are used in the pipeline should be registered if the pipeline
	 * needs to be serialised, or a serialised pipeline needs to be loaded.
	 *
	 * Registering a function does not add it to a pipeline, functions must still be
	 * added to instances of the pipeline for them to be used when running a pipeline.
	 *
	 * @param {lunr.PipelineFunction} fn - The function to check for.
	 * @param {String} label - The label to register this function with
	 */
	public static void registerFunction(PipelineFunction fn, String label) {
	  registeredFunctions.put(label, fn);
	  functionLabels.put(fn, label);
	}

	/**
	 * Warns if the function is not registered as a Pipeline function.
	 *
	 * @param {lunr.PipelineFunction} fn - The function to check for.
	 * @private
	 */
	public static void warnIfFunctionNotRegistered(PipelineFunction fn) {
	  boolean isRegistered = registeredFunctions.containsValue(fn);

	  if (!isRegistered) {
	    logger.warn("Function is not registered with pipeline. This may cause problems when serialising the index.");
	  }
	}

	/**
	 * Adds new functions to the end of the pipeline.
	 *
	 * Logs a warning if the function has not been registered.
	 *
	 * @param {lunr.PipelineFunction[]} functions - Any number of functions to add to the pipeline.
	 */
	public void add(PipelineFunction... arguments) {
	  for (PipelineFunction fn: arguments) {
		  warnIfFunctionNotRegistered(fn);  
		  _stack.add(fn);
	  }
	}

	/**
	 * Adds a single function after a function that already exists in the
	 * pipeline.
	 *
	 * Logs a warning if the function has not been registered.
	 *
	 * @param {lunr.PipelineFunction} existingFn - A function that already exists in the pipeline.
	 * @param {lunr.PipelineFunction} newFn - The new function to add to the pipeline.
	 */
	public void after(PipelineFunction existingFn, PipelineFunction newFn) {
	  warnIfFunctionNotRegistered(newFn);

	  int pos = _stack.indexOf(existingFn);
	  
	  if (pos == -1) {
	    throw new IllegalStateException("Cannot find existingFn");
	  }

	  pos = pos + 1;
	  
	  this._stack.add(pos, newFn);
	}

	/**
	 * Adds a single function before a function that already exists in the
	 * pipeline.
	 *
	 * Logs a warning if the function has not been registered.
	 *
	 * @param {lunr.PipelineFunction} existingFn - A function that already exists in the pipeline.
	 * @param {lunr.PipelineFunction} newFn - The new function to add to the pipeline.
	 */
	public void before(PipelineFunction existingFn, PipelineFunction newFn) {
	  warnIfFunctionNotRegistered(newFn);

	  int pos = _stack.indexOf(existingFn);
	  
	  if (pos == -1) {
	    throw new IllegalStateException("Cannot find existingFn");
	  }

	  _stack.add(pos, newFn);
	}

	/**
	 * Removes a function from the pipeline.
	 *
	 * @param {lunr.PipelineFunction} fn The function to remove from the pipeline.
	 */
	public void remove(PipelineFunction fn) {
	  int pos = _stack.indexOf(fn);
	  
	  if (pos == -1) {
	    return;
	  }

	  _stack.remove(pos);
	}

	/**
	 * Runs the current list of functions that make up the pipeline against the
	 * passed tokens.
	 *
	 * @param {Token[]} tokens The tokens to run through the pipeline.
	 * @returns {Array}
	 */
	public List<Token> run(List<Token> tokens) {
	  int stackLength = _stack.size();

	  for (int i = 0; i < stackLength; i++) {
	    PipelineFunction fn = _stack.get(i);
	    
	    List<Token> memo = new ArrayList<>();

	    int tokenSize = tokens.size();
	    
	    for (int j = 0; j < tokenSize; j++) {
	      Object result = fn.run(tokens.get(j), j, tokens);

	      if (result == null || "".equals(result))
	    	  continue;

	      if (result instanceof List) {
	    	List<Token> returnedTokens = (List<Token>)result;
	    	
	    	for (Token token: returnedTokens) {
	    		memo.add(token);
	    	}
	      } else {
	        memo.add((Token)result);
	      }
	    }

	    tokens = memo;
	  }

	  return tokens;
	}

	/**
	 * Convenience method for passing a string through a pipeline and getting
	 * strings out. This method takes care of wrapping the passed string in a
	 * token and mapping the resulting tokens back to strings.
	 *
	 * @param {string} str - The string to pass through the pipeline.
	 * @param {?object} metadata - Optional metadata to associate with the token
	 * passed to the pipeline.
	 * @returns {string[]}
	 */
	public List<String> runString(String str, Map<String, Object> metadata) {
	  Token token = new Token (str, metadata);

	  List<Token> tokens = run(Collections.singletonList(token));;
	  
	  List<String> strings = new ArrayList<>(tokens.size());
	  
	  for (Token t: tokens)
		  strings.add(t.str);
	  
	  return strings;
	}

	/**
	 * Resets the pipeline by removing any existing processors.
	 *
	 */
	public void reset() {
	  _stack.clear();
	}

	/**
	 * Returns a representation of the pipeline ready for serialisation.
	 *
	 * Logs a warning if the function has not been registered.
	 *
	 * @returns {Array}
	 */
	public List<String> toJSON() {
	  List<String> labels = new ArrayList<>(_stack.size());
	  
	  _stack.stream().map(functionLabels::get).forEach(labels::add);
	  
	  return labels;
	}

}
