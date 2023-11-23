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
		DecimalCast vd = $.decimalCast(null);
		return vd;
	}

	public DoubleCast doubleCast() {
		DoubleCast vd = $.doubleCast(null);
		return vd;
	}

	public FloatCast floatCast() {
		FloatCast vd = $.floatCast(null);
		return vd;
	}

	public IntegerCast integerCast() {
		IntegerCast vd = $.integerCast(null);
		return vd;
	}

	public LongCast longCast() {
		LongCast vd = $.longCast(null);
		return vd;
	}

	public CurrentLocale currentLocale() {
		CurrentLocale vd = $.currentLocale();
		return vd;
	}

	public UserName userName() {
		UserName vd = $.userName();
		return vd;
	}

	public ToBoolean toBoolean() {
		ToBoolean vd = $.toBoolean(null, null);
		return vd;
	}

	public ToDate toDate() {
		ToDate vd = $.toDate(null, null);
		return vd;
	}

	public ToDecimal toDecimal() {
		ToDecimal vd = $.toDecimal(null, null);
		return vd;
	}

	public ToDouble toDouble() {
		ToDouble vd = $.toDouble(null, null);
		return vd;
	}

	public ToEnum toEnum() {
		ToEnum vd = $.toEnum(null, null);
		return vd;
	}

	public ToFloat toFloat() {
		ToFloat vd = $.toFloat(null, null);
		return vd;
	}

	public ToInteger toInteger() {
		ToInteger vd = $.toInteger(null, null);
		return vd;
	}

	public ToLong toLong() {
		ToLong vd = $.toLong(null, null);
		return vd;
	}

	public ToString ToString() {
		ToString vd = $.toString(null, null);
		return vd;
	}

	public ToSet toSet() {
		ToSet vd = $.toSet(null);
		return vd;
	}

	public ToList toList() {
		ToList vd = $.toList(null);
		return vd;
	}

	public ToReference toReference() {
		ToReference vd = $.toReference(null);
		return vd;
	}

	public RemoveNulls removeNulls() {
		RemoveNulls vd = $.removeNulls(null);
		return vd;
	}
	
	
	public Conjunction conjunction() {
		Conjunction vd = $.conjunction((Object []) null);
		return vd;
	}

	public Disjunction disjunction() {
		Disjunction vd = $.disjunction((Object []) null);
		return vd;
	}

	public Negation negation() {
		Negation vd = $.negation(null);
		return vd;
	}

	public Add add() {
		Add vd = $.add((Object []) null);
		return vd;
	}

	public Avg avg() {
		Avg vd = $.avg((Object []) null);
		return vd;
	}

	public Divide divide() {
		Divide vd = $.divide((Object []) null);
		return vd;
	}

	public Max max() {
		Max vd = $.max((Object []) null);
		return vd;
	}

	public Min min() {
		Min vd = $.min((Object []) null);
		return vd;
	}

	public Multiply multiply() {
		Multiply vd = $.multiply((Object []) null);
		return vd;
	}

	public Subtract subtract() {
		Subtract vd = $.subtract((Object []) null);
		return vd;
	}

	public Ceil ceil() {
		Ceil vd = $.ceil(null, null);
		return vd;
	}

	public Floor floor() {
		Floor vd = $.floor(null, null);
		return vd;
	}

	public Round round() {
		Round vd = $.round(null, null);
		return vd;
	}

	public PropertyPath propertyPath() {
		PropertyPath vd = $.propertyPath(null, null);
		return vd;
	}

	public Equal equal() {
		Equal vd = $.equal(null, null);
		return vd;
	}

	public Greater greater() {
		Greater vd = $.greater(null, null);
		return vd;
	}

	public GreaterOrEqual greaterOrEqual() {
		GreaterOrEqual vd = $.greaterOrEqual(null, null);
		return vd;
	}

	public Ilike ilike() {
		Ilike vd = $.ilike(null, null);
		return vd;
	}

	public In in() {
		In vd = $.in(null, null);
		return vd;
	}

	public Less less() {
		Less vd = $.less(null, null);
		return vd;
	}

	public LessOrEqual lessOrEqual() {
		LessOrEqual vd = $.lessOrEqual(null, null);
		return vd;
	}

	public Like like() {
		Like vd = $.like(null, null);
		return vd;
	}

	public NotEqual notEqual() {
		NotEqual vd = $.notEqual(null, null);
		return vd;
	}

	public Concatenation concatenation() {
		Concatenation vd = $.concatenation((Object []) null);
		return vd;
	}

	public Localize localize() {
		Localize vd = $.localize(null, null);
		return vd;
	}

	public Lower lower() {
		Lower vd = $.lower(null);
		return vd;
	}

	public SubString substring() {
		SubString vd = $.substring(null, null, null);
		return vd;
	}

	public Upper upper() {
		Upper vd = $.upper(null);
		return vd;
	}

	public Now now() {
		Now vd = $.now();
		return vd;
	}
	
	public Query query() {
		Query vd = $.query(null,null);
		return vd;
	}

	public EnumReference enumReference() {
		EnumReference vd = $.enumReference(null, null);
		return vd;
	}

	public Escape escape() {
		Escape vd = $.escape(null);
		return vd;
	}

	public PersistentEntityReference persistentEntityReference() {
		PersistentEntityReference vd = $.persistentEntityReference(null, null);
		return vd;
	}

	public PreliminaryEntityReference preliminaryEntityReference() {
		PreliminaryEntityReference vd = $.preliminaryEntityReference(null, null);
		return vd;
	}

	public Variable variable() {
		Variable vd = $.variable(null, null, null, null);
		return vd;
	}

	public Assignable assignable() {
		Assignable vd = $.assignable(null, null);
		return vd;
	}

	public InstanceOf instanceOf() {
		InstanceOf vd = $.instanceOf(null, null);
		return vd;
	}
	
	
	public ModelPath modelPathFirstElement() {
		ModelPath vd = $.modelPath(ModelPathElementAddressing.first);
		return vd;
	}

	public ModelPath modelPathFirstElementOffset(int offset) {
		ModelPath vd = $.modelPath(ModelPathElementAddressing.first, offset, false);
		return vd;
	}

	public ModelPath modelPathLastElement() {
		ModelPath vd = $.modelPath(ModelPathElementAddressing.last);
		return vd;
	}

	public ModelPath modelPathLastElementOffset(int offset) {
		ModelPath vd = $.modelPath(ModelPathElementAddressing.last, offset, false);
		return vd;
	}

	public ModelPath modelPathFull() {
		ModelPath vd = $.modelPath(ModelPathElementAddressing.full);
		return vd;
	}

	public ModelPath modelPathFirstElementForSelection() {
		ModelPath vd = $.modelPath(ModelPathElementAddressing.first, 0, true);
		return vd;
	}

	public ModelPath modelPathLastElementForSelection() {
		ModelPath vd = $.modelPath(ModelPathElementAddressing.last, 0, true);
		return vd;
	}

	public ModelPath modelPathFullForSelection() {
		ModelPath vd = $.modelPath(ModelPathElementAddressing.full, 0, true);
		return vd;
	}

}
