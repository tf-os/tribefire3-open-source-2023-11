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
package com.braintribe.model.processing.query.stringifier.experts;

import java.util.Collection;
import java.util.function.Function;

import com.braintribe.model.processing.query.api.stringifier.QueryStringifierRuntimeException;
import com.braintribe.model.processing.query.api.stringifier.experts.Stringifier;
import com.braintribe.model.processing.query.api.stringifier.experts.paramter.FunctionParameterProvider;
import com.braintribe.model.processing.query.api.stringifier.experts.paramter.MultipleParameterProvider;
import com.braintribe.model.processing.query.api.stringifier.experts.paramter.SingleParameterProvider;
import com.braintribe.model.processing.query.stringifier.BasicQueryStringifierContext;

public class FunctionStringifier<F extends Object> implements Stringifier<F, BasicQueryStringifierContext> {
	private Function<F,String> functionNameSupplier;
	private FunctionParameterProvider parameterProvider;

	public FunctionStringifier() {
		// Nothing
	}

	public FunctionStringifier(String functionName, FunctionParameterProvider parameterProvider) {
		this();
		setFunctionName(functionName);
		setOperandProvider(parameterProvider);
	}

	public FunctionStringifier(Function<F,String> functionNameSupplier, FunctionParameterProvider parameterProvider) {
		setFunctionNameSupplier(functionNameSupplier);
		setOperandProvider(parameterProvider);
	}
	
	public void setFunctionName(String functionName) {
		this.functionNameSupplier = (v)->functionName;
	}

	public void setFunctionNameSupplier(Function<F, String> functionNameSupplier) {
		this.functionNameSupplier = functionNameSupplier;
	}
	
	public void setOperandProvider(FunctionParameterProvider parameterProvider) {
		this.parameterProvider = parameterProvider;
	}

	@Override
	public String stringify(F function, BasicQueryStringifierContext context) throws QueryStringifierRuntimeException {
		StringBuilder queryString = new StringBuilder();

		queryString.append(this.functionNameSupplier.apply(function));
		queryString.append("(");

		if (this.parameterProvider != null) {
			if (this.parameterProvider instanceof MultipleParameterProvider<?>) {
				MultipleParameterProvider<F> multipleProvider = (MultipleParameterProvider<F>) this.parameterProvider;

				Collection<?> parameters = multipleProvider.provideParameters(function);
				
				if (parameters.size() == 1) {
					appendSingleParameter(parameters.iterator().next(), context, queryString);
				} else {
					boolean first = true;
					for (Object parameter : parameters) {
						if (!first) {
							queryString.append(", ");
						}
						
						queryString.append(context.stringify(parameter));
						first = false;
					}
				}
			} else if (this.parameterProvider instanceof SingleParameterProvider<?>) {
				SingleParameterProvider<F> singleProvider = (SingleParameterProvider<F>) this.parameterProvider;
				appendSingleParameter(singleProvider.provideParameter(function), context, queryString);
			} else {
				throw new QueryStringifierRuntimeException("Unsuppored parameter provider: " + this.parameterProvider);
			}

		}

		queryString.append(")");
		return queryString.toString();
	}

	private void appendSingleParameter(Object singleParameter, BasicQueryStringifierContext context, StringBuilder queryString) {
		if (singleParameter instanceof Collection<?>) {
			appendSingleCollection(context, queryString, singleParameter);
		} else {
			queryString.append(context.stringify(singleParameter));
		}
	}

	private void appendSingleCollection(BasicQueryStringifierContext context, StringBuilder queryString, Object singleParameter) {
		Collection<?> collectionParameter = (Collection<?>) singleParameter;
		if (collectionParameter.size() > 0) {
			boolean first = true;
			for (final Object item : collectionParameter) {
				if (first == false) {
					// Add operand splitter
					queryString.append(", ");
				}

				// Add stringified operand
				queryString.append(context.stringify(item));
				first = false;
			}
		} else {
			queryString.append("()");
		}
	}
}
