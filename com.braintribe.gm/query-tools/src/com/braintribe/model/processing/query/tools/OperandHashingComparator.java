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
package com.braintribe.model.processing.query.tools;

import com.braintribe.cc.lcd.HashingComparator;
import com.braintribe.model.query.From;
import com.braintribe.model.query.Join;
import com.braintribe.model.query.Operand;
import com.braintribe.model.query.PropertyOperand;
import com.braintribe.model.query.Source;
import com.braintribe.model.query.functions.EntitySignature;
import com.braintribe.model.query.functions.JoinFunction;
import com.braintribe.model.query.functions.Localize;
import com.braintribe.model.query.functions.QueryFunction;
import com.braintribe.model.query.functions.aggregate.AggregateFunction;

/**
 * This comparator considers two operands equal if they denote the same path starting with a {@link From} following with properties, and the same
 * functions are applied. E.g, there are many ways how to represent the path {@code Person.company.address.street}, with 1, 2 or 3 joins, but this
 * comparator would consider all of them being equal.
 *
 * We use this for example when resolving information related to the source path, e.g. when determining whether or not a given source is mapped to
 * persistence.
 * 
 * @author peter.gazdik
 */
public class OperandHashingComparator implements HashingComparator<Operand> {

	public static final OperandHashingComparator INSTANCE = new OperandHashingComparator();

	private OperandHashingComparator() {
	}

	@Override
	public boolean compare(Operand o1, Operand o2) {
		if (o1 == o2)
			return true;

		if (o1 == null || o2 == null)
			return false;

		return buildPath(o1).equals(buildPath(o2));
	}

	@Override
	public int computeHash(Operand o) {
		return o == null ? 0 : buildPath(o).hashCode();
	}

	public static String buildPath(Operand o) {
		if (o instanceof PropertyOperand)
			return buildPropertyOperandPath((PropertyOperand) o);

		if (o instanceof Source)
			return buildSourcePath((Source) o);

		if (o instanceof QueryFunction)
			return buildQueryFunctionPath((QueryFunction) o);

		throw new IllegalArgumentException(
				"Operand not supported: " + o + ". This code is still kind of experimental, maybe something is missing. Sorry.");
	}

	private static String buildQueryFunctionPath(QueryFunction o) {
		if (o instanceof AggregateFunction)
			return buildSingleOperandFunction(o, ((AggregateFunction) o).getOperand());

		if (o instanceof EntitySignature)
			return buildSingleOperandFunction(o, ((EntitySignature) o).getOperand());

		if (o instanceof Localize)
			return buildSingleOperandFunction(o, ((Localize) o).getLocalizedStringOperand());

		if (o instanceof JoinFunction)
			return buildSingleOperandFunction(o, ((JoinFunction) o).getJoin());

		throw new IllegalArgumentException(
				"Operand not supported: " + o + ". This code is still kind of experimental, maybe something is missing. Sorry.");
	}

	private static String buildPropertyOperandPath(PropertyOperand po) {
		String sourcePath = buildSourcePath(po.getSource());

		String name = po.getPropertyName();
		if (name == null)
			return sourcePath;
		else
			return sourcePath + "." + name;
	}

	public static String buildSourcePath(Source source) {
		if (source instanceof From)
			return ((From) source).getEntityTypeSignature();

		Join join = (Join) source;
		return buildPath(join.getSource()) + "." + join.getProperty();
	}

	private static String buildSingleOperandFunction(QueryFunction qf, Object operand) {
		if (!(operand instanceof Operand))
			throw new IllegalArgumentException("QueryFunction for a constant value (rather than Operand) not supported. Actual function: " + qf);

		return qf.entityType().getShortName() + "(" + buildPath((Operand) operand) + ")";
	}

}
