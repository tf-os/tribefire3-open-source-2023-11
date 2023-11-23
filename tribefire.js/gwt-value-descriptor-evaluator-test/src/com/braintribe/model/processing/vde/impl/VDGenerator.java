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
package com.braintribe.model.processing.vde.impl;

import com.braintribe.model.bvd.cast.DecimalCast;
import com.braintribe.model.bvd.cast.DoubleCast;
import com.braintribe.model.bvd.cast.FloatCast;
import com.braintribe.model.bvd.cast.IntegerCast;
import com.braintribe.model.bvd.cast.LongCast;
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
import com.braintribe.model.bvd.string.Concatenation;
import com.braintribe.model.bvd.string.Localize;
import com.braintribe.model.bvd.string.Lower;
import com.braintribe.model.bvd.string.SubString;
import com.braintribe.model.bvd.string.Upper;
import com.braintribe.model.bvd.time.Now;
import com.braintribe.model.generic.value.EnumReference;
import com.braintribe.model.generic.value.Escape;
import com.braintribe.model.generic.value.PersistentEntityReference;
import com.braintribe.model.generic.value.PreliminaryEntityReference;
import com.braintribe.model.generic.value.Variable;
import com.braintribe.model.processing.vde.builder.api.VdBuilder;
import com.braintribe.model.processing.vde.evaluator.VDE;

public class VDGenerator {

	public static VdBuilder $ = VDE.builder();

	public DecimalCast decimalCast() {
		return $.decimalCast(null);
	}

	public DoubleCast doubleCast() {
		return $.doubleCast(null);
	}

	public FloatCast floatCast() {
		return $.floatCast(null);
	}

	public IntegerCast integerCast() {
		return $.integerCast(null);
	}

	public LongCast longCast() {
		return $.longCast(null);
	}

	public CurrentLocale currentLocale() {
		return $.currentLocale();
	}

	public UserName userName() {
		return $.userName();
	}

	public ToBoolean toBoolean() {
		return $.toBoolean(null, null);
	}

	public ToDate toDate() {
		return $.toDate(null, null);
	}

	public ToDecimal toDecimal() {
		return $.toDecimal(null, null);
	}

	public ToDouble toDouble() {
		return $.toDouble(null, null);
	}

	public ToEnum toEnum() {
		return $.toEnum(null, null);
	}

	public ToFloat toFloat() {
		return $.toFloat(null, null);
	}

	public ToInteger toInteger() {
		return $.toInteger(null, null);
	}

	public ToLong toLong() {
		return $.toLong(null, null);
	}

	public ToString ToString() {
		return $.toString(null, null);
	}

	public ToSet toSet() {
		return $.toSet(null);
	}

	public ToList toList() {
		return $.toList(null);
	}

	public ToReference toReference() {
		return $.toReference(null);
	}

	public RemoveNulls removeNulls() {
		return $.removeNulls(null);
	}

	public Conjunction conjunction() {
		return $.conjunction((Object[]) null);
	}

	public Disjunction disjunction() {
		return $.disjunction((Object[]) null);
	}

	public Negation negation() {
		return $.negation(null);
	}

	public Add add() {
		return $.add((Object[]) null);
	}

	public Avg avg() {
		return $.avg((Object[]) null);
	}

	public Divide divide() {
		return $.divide((Object[]) null);
	}

	public Max max() {
		return $.max((Object[]) null);
	}

	public Min min() {
		return $.min((Object[]) null);
	}

	public Multiply multiply() {
		return $.multiply((Object[]) null);
	}

	public Subtract subtract() {
		return $.subtract((Object[]) null);
	}

	public Ceil ceil() {
		return $.ceil(null, null);
	}

	public Floor floor() {
		return $.floor(null, null);
	}

	public Round round() {
		return $.round(null, null);
	}

	public PropertyPath propertyPath() {
		return $.propertyPath(null, null);
	}

	public Equal equal() {
		return $.equal(null, null);
	}

	public Greater greater() {
		return $.greater(null, null);
	}

	public GreaterOrEqual greaterOrEqual() {
		return $.greaterOrEqual(null, null);
	}

	public Ilike ilike() {
		return $.ilike(null, null);
	}

	public In in() {
		return $.in(null, null);
	}

	public Less less() {
		return $.less(null, null);
	}

	public LessOrEqual lessOrEqual() {
		return $.lessOrEqual(null, null);
	}

	public Like like() {
		return $.like(null, null);
	}

	public NotEqual notEqual() {
		return $.notEqual(null, null);
	}

	public Concatenation concatenation() {
		return $.concatenation((Object[]) null);
	}

	public Localize localize() {
		return $.localize(null, null);
	}

	public Lower lower() {
		return $.lower(null);
	}

	public Lower lower(Object operand) {
		return $.lower(operand);
	}

	public SubString substring() {
		return $.substring(null, null, null);
	}

	public Upper upper() {
		return $.upper(null);
	}

	public Now now() {
		return $.now();
	}

	public Query query() {
		return $.query(null, null);
	}

	public EnumReference enumReference() {
		return $.enumReference(null, null);
	}

	public Escape escape() {
		return $.escape(null);
	}

	public Escape escape(Object valueDescriptor) {
		return $.escape(valueDescriptor);
	}
	
	public PersistentEntityReference persistentEntityReference() {
		return $.persistentEntityReference(null, null);
	}

	public PreliminaryEntityReference preliminaryEntityReference() {
		return $.preliminaryEntityReference(null, null);
	}

	public Variable variable() {
		return $.variable(null, null, null, null);
	}

	public Assignable assignable() {
		return $.assignable(null, null);
	}

	public InstanceOf instanceOf() {
		return $.instanceOf(null, null);
	}

	public ModelPath modelPathFirstElement() {
		return $.modelPath(ModelPathElementAddressing.first);
	}

	public ModelPath modelPathFirstElementOffset(int offset) {
		return $.modelPath(ModelPathElementAddressing.first, offset, false);
	}

	public ModelPath modelPathLastElement() {
		return $.modelPath(ModelPathElementAddressing.last);
	}

	public ModelPath modelPathLastElementOffset(int offset) {
		return $.modelPath(ModelPathElementAddressing.last, offset, false);
	}

	public ModelPath modelPathFull() {
		return $.modelPath(ModelPathElementAddressing.full);
	}

	public ModelPath modelPathFirstElementForSelection() {
		return $.modelPath(ModelPathElementAddressing.first, 0, true);
	}

	public ModelPath modelPathLastElementForSelection() {
		return $.modelPath(ModelPathElementAddressing.last, 0, true);
	}

	public ModelPath modelPathFullForSelection() {
		return $.modelPath(ModelPathElementAddressing.full, 0, true);
	}

}
