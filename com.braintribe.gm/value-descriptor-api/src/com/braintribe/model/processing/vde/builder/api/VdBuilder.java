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
package com.braintribe.model.processing.vde.builder.api;

import java.util.List;

import com.braintribe.model.bvd.cast.DecimalCast;
import com.braintribe.model.bvd.cast.DoubleCast;
import com.braintribe.model.bvd.cast.FloatCast;
import com.braintribe.model.bvd.cast.IntegerCast;
import com.braintribe.model.bvd.cast.LongCast;
import com.braintribe.model.bvd.conditional.Coalesce;
import com.braintribe.model.bvd.conditional.If;
import com.braintribe.model.bvd.context.CurrentLocale;
import com.braintribe.model.bvd.context.ModelPath;
import com.braintribe.model.bvd.context.ModelPathElementAddressing;
import com.braintribe.model.bvd.context.UserName;
import com.braintribe.model.bvd.convert.ToBoolean;
import com.braintribe.model.bvd.convert.ToDate;
import com.braintribe.model.bvd.convert.ToDecimal;
import com.braintribe.model.bvd.convert.ToDouble;
import com.braintribe.model.bvd.convert.ToEnum;
import com.braintribe.model.bvd.convert.ToFloat;
import com.braintribe.model.bvd.convert.ToInteger;
import com.braintribe.model.bvd.convert.ToList;
import com.braintribe.model.bvd.convert.ToLong;
import com.braintribe.model.bvd.convert.ToReference;
import com.braintribe.model.bvd.convert.ToSet;
import com.braintribe.model.bvd.convert.ToString;
import com.braintribe.model.bvd.convert.collection.RemoveNulls;
import com.braintribe.model.bvd.logic.Conjunction;
import com.braintribe.model.bvd.logic.Disjunction;
import com.braintribe.model.bvd.logic.Negation;
import com.braintribe.model.bvd.math.Add;
import com.braintribe.model.bvd.math.Avg;
import com.braintribe.model.bvd.math.Ceil;
import com.braintribe.model.bvd.math.Divide;
import com.braintribe.model.bvd.math.Floor;
import com.braintribe.model.bvd.math.Max;
import com.braintribe.model.bvd.math.Min;
import com.braintribe.model.bvd.math.Multiply;
import com.braintribe.model.bvd.math.Round;
import com.braintribe.model.bvd.math.Subtract;
import com.braintribe.model.bvd.navigation.PropertyPath;
import com.braintribe.model.bvd.predicate.Assignable;
import com.braintribe.model.bvd.predicate.Equal;
import com.braintribe.model.bvd.predicate.Greater;
import com.braintribe.model.bvd.predicate.GreaterOrEqual;
import com.braintribe.model.bvd.predicate.Ilike;
import com.braintribe.model.bvd.predicate.In;
import com.braintribe.model.bvd.predicate.InstanceOf;
import com.braintribe.model.bvd.predicate.Less;
import com.braintribe.model.bvd.predicate.LessOrEqual;
import com.braintribe.model.bvd.predicate.Like;
import com.braintribe.model.bvd.predicate.NotEqual;
import com.braintribe.model.bvd.query.Query;
import com.braintribe.model.bvd.query.ResultConvenience;
import com.braintribe.model.bvd.string.Concatenation;
import com.braintribe.model.bvd.string.Localize;
import com.braintribe.model.bvd.string.Lower;
import com.braintribe.model.bvd.string.SubString;
import com.braintribe.model.bvd.string.Upper;
import com.braintribe.model.bvd.time.Now;
import com.braintribe.model.generic.i18n.LocalizedString;
import com.braintribe.model.generic.value.EnumReference;
import com.braintribe.model.generic.value.Escape;
import com.braintribe.model.generic.value.Evaluate;
import com.braintribe.model.generic.value.PersistentEntityReference;
import com.braintribe.model.generic.value.PreliminaryEntityReference;
import com.braintribe.model.generic.value.ValueDescriptor;
import com.braintribe.model.generic.value.Variable;
import com.braintribe.model.time.DateOffset;
import com.braintribe.model.time.DateOffsetUnit;
import com.braintribe.model.time.TimeSpan;
import com.braintribe.model.time.TimeUnit;
import com.braintribe.model.time.TimeZoneOffset;

/**
 * This is a builder for all standard {@link ValueDescriptor}s. Standard means
 * either defined in ValueDescriptorModel or in BasicValueDescriptor Model
 *
 */
public interface VdBuilder{

	//TimeModel
	//TODO check with dirk if they will be moved
	DateOffset dateOffset(int value, DateOffsetUnit unit);
	TimeZoneOffset timeZoneOffset(int minutes);
	TimeSpan timeSpan(double value, TimeUnit unit);
	
	//VD
	EnumReference enumReference(String typeSignature, String constant);
	Escape escape(Object valueDescriptor);
	Evaluate evaluate(Object valueDescriptor);
	PersistentEntityReference persistentEntityReference(String typeSignature, Object id);
	PreliminaryEntityReference preliminaryEntityReference(String typeSignature, Object id);
	Variable variable(String name, LocalizedString localizedName, LocalizedString description,Object defaultValue);
	
	//BVD
	// cast
	DecimalCast decimalCast(Object operand);
	DoubleCast doubleCast(Object operand);
	FloatCast floatCast(Object operand);
	IntegerCast integerCast(Object operand);
	LongCast longCast(Object operand);
	
	//context
	CurrentLocale currentLocale();
	UserName userName();
	ModelPath modelPath(ModelPathElementAddressing elementAddressing);
	ModelPath modelPath(ModelPathElementAddressing elementAddressing, int offset, boolean useSelection);
	
	//convert
	ToBoolean toBoolean(Object operand, Object format);
	ToDate toDate(Object operand, Object format);
	ToDecimal toDecimal(Object operand, Object format);
	ToDouble toDouble(Object operand, Object format);
	ToEnum toEnum(Object operand, String typeSignature);
	ToFloat toFloat(Object operand, Object format);
	ToInteger toInteger(Object operand, Object format);
	ToLong toLong(Object operand, Object format);
	ToString toString(Object operand, Object format);
	ToSet toSet(Object operand);
	ToList toList(Object operand);
	ToReference toReference(Object operand);
	
	//collection
	RemoveNulls removeNulls(Object collection);

	//conditional
	Coalesce coalesce(Object operand, Object replacement);
	If _if(Object predicate, Object then, Object _else);

//	// logic
	Conjunction conjunction(Object ... operands);
	Conjunction conjunction(List<Object> operandsList);
	Disjunction disjunction(Object ... operands);
	Disjunction disjunction(List<Object> operandsList);
	Negation negation(Object operand);
	
	// math
	// 		arithmetic
	Add add(Object ... operands);
	Add add(List<Object> operandsList);
	Avg avg(Object ... operands);
	Avg avg(List<Object> operandsList);
	Divide divide(Object ... operands);
	Divide divide(List<Object> operandsList);
	Max max(Object ... operands);
	Max max(List<Object> operandsList);
	Min min(Object ... operands);
	Min min(List<Object> operandsList);
	Multiply multiply(Object ... operands);
	Multiply multiply(List<Object> operandsList);
	Subtract subtract(Object ... operands);
	Subtract subtract(List<Object> operandsList);
	// 		approximate	
	Ceil ceil(Object value, Object precision);
	Floor floor(Object value, Object precision);
	Round round(Object value, Object precision);
	
	//navigation
	PropertyPath propertyPath(String propertyPath, Object entity);
	
	// predicate
	Equal equal(Object leftOperand, Object rightOperand);
	Greater greater(Object leftOperand, Object rightOperand);
	GreaterOrEqual greaterOrEqual(Object leftOperand, Object rightOperand);
	Ilike ilike(Object leftOperand, Object rightOperand);
	In in(Object leftOperand, Object rightOperand);
	Less less(Object leftOperand, Object rightOperand);
	LessOrEqual lessOrEqual(Object leftOperand, Object rightOperand);
	Like like(Object leftOperand, Object rightOperand);
	NotEqual notEqual(Object leftOperand, Object rightOperand);
	Assignable assignable(Object leftOperand, Object rightOperand);
	InstanceOf instanceOf(Object leftOperand, Object rightOperand);
	
	//string
	Concatenation concatenation(Object ... operands);
	Concatenation concatenation(List<Object> operandsList);
	Localize localize(Object localizedString, Object locale);
	Lower lower(Object operand);
	SubString substring(Object operand, Integer startIndex, Integer endIndex);
	Upper upper(Object operand);
	
	//time
	Now now();
	
	//query
	Query query(Object query, ResultConvenience queryConvenience);
}
