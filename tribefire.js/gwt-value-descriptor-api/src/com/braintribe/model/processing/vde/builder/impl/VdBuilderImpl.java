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
package com.braintribe.model.processing.vde.builder.impl;

import java.util.ArrayList;
import java.util.Arrays;
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
import com.braintribe.model.generic.value.Variable;
import com.braintribe.model.processing.vde.builder.api.VdBuilder;
import com.braintribe.model.time.DateOffset;
import com.braintribe.model.time.DateOffsetUnit;
import com.braintribe.model.time.TimeSpan;
import com.braintribe.model.time.TimeUnit;
import com.braintribe.model.time.TimeZoneOffset;

public class VdBuilderImpl implements VdBuilder {

	@Override
	public DecimalCast decimalCast(Object operand) {
		DecimalCast vd = DecimalCast.T.create();
		vd.setOperand(operand);
		return vd;
	}

	@Override
	public DoubleCast doubleCast(Object operand) {
		DoubleCast vd = DoubleCast.T.create();
		vd.setOperand(operand);
		return vd;
	}

	@Override
	public FloatCast floatCast(Object operand) {
		FloatCast vd = FloatCast.T.create();
		vd.setOperand(operand);
		return vd;
	}

	@Override
	public IntegerCast integerCast(Object operand) {
		IntegerCast vd = IntegerCast.T.create();
		vd.setOperand(operand);
		return vd;
	}

	@Override
	public LongCast longCast(Object operand) {
		LongCast vd = LongCast.T.create();
		vd.setOperand(operand);
		return vd;
	}

	@Override
	public CurrentLocale currentLocale() {
		CurrentLocale vd = CurrentLocale.T.create();
		return vd;
	}

	@Override
	public UserName userName() {
		UserName vd = UserName.T.create();
		return vd;
	}

	@Override
	public ModelPath modelPath(ModelPathElementAddressing elementAddressing) {
		ModelPath vd = ModelPath.T.create();
		vd.setAddressing(elementAddressing);
		return vd;
	}

	@Override
	public ModelPath modelPath(ModelPathElementAddressing elementAddressing, int offset, boolean useSelection) {
		ModelPath vd = ModelPath.T.create();
		vd.setAddressing(elementAddressing);
		vd.setOffset(offset);
		vd.setUseSelection(useSelection);
		return vd;
	}

	@Override
	public ToBoolean toBoolean(Object operand, Object format) {
		ToBoolean vd = ToBoolean.T.create();
		vd.setOperand(operand);
		vd.setFormat(format);
		return vd;
	}

	@Override
	public ToDate toDate(Object operand, Object format) {
		ToDate vd = ToDate.T.create();
		vd.setOperand(operand);
		vd.setFormat(format);
		return vd;
	}

	@Override
	public ToDecimal toDecimal(Object operand, Object format) {
		ToDecimal vd = ToDecimal.T.create();
		vd.setOperand(operand);
		vd.setFormat(format);
		return vd;
	}

	@Override
	public ToDouble toDouble(Object operand, Object format) {
		ToDouble vd = ToDouble.T.create();
		vd.setOperand(operand);
		vd.setFormat(format);
		return vd;
	}

	@Override
	public ToEnum toEnum(Object operand, String typeSignature) {
		ToEnum vd = ToEnum.T.create();
		vd.setOperand(operand);
		vd.setTypeSignature(typeSignature);
		return vd;
	}

	@Override
	public ToFloat toFloat(Object operand, Object format) {
		ToFloat vd = ToFloat.T.create();
		vd.setOperand(operand);
		vd.setFormat(format);
		return vd;
	}

	@Override
	public ToInteger toInteger(Object operand, Object format) {
		ToInteger vd = ToInteger.T.create();
		vd.setOperand(operand);
		vd.setFormat(format);
		return vd;
	}

	@Override
	public ToLong toLong(Object operand, Object format) {
		ToLong vd = ToLong.T.create();
		vd.setOperand(operand);
		vd.setFormat(format);
		return vd;
	}

	@Override
	public ToString toString(Object operand, Object format) {
		ToString vd = ToString.T.create();
		vd.setOperand(operand);
		vd.setFormat(format);
		return vd;
	}

	@Override
	public ToSet toSet(Object operand) {
		ToSet vd = ToSet.T.create();
		vd.setOperand(operand);
		return vd;
	}

	@Override
	public ToList toList(Object operand) {
		ToList vd = ToList.T.create();
		vd.setOperand(operand);
		return vd;
	}

	@Override
	public ToReference toReference(Object operand) {
		ToReference vd = ToReference.T.create();
		vd.setOperand(operand);
		return vd;
	}
	
	@Override
	public RemoveNulls removeNulls(Object collection) {
		RemoveNulls vd = RemoveNulls.T.create();
		vd.setCollection(collection);
		return vd;
	}
	
	@Override
	public Coalesce coalesce(Object operand, Object replacement) {
		Coalesce vd = Coalesce.T.create();
		vd.setOperand(operand);
		vd.setReplacement(replacement);
		return vd;
	}

	@Override
	public If _if(Object predicate, Object then, Object _else) {
		If vd = If.T.create();
		vd.setPredicate(predicate);
		vd.setThen(then);
		vd.setElse(_else);
		return vd;
	}

	@Override
	public Conjunction conjunction(Object... operands) {
		Conjunction vd = Conjunction.T.create();
		if (operands != null) {
			vd.setOperands(new ArrayList<Object>(Arrays.asList(operands)));
		}
		return vd;
	}

	@Override
	public Conjunction conjunction(List<Object> operandsList) {
		Conjunction vd = Conjunction.T.create();
		vd.setOperands(operandsList);
		return vd;
	}

	@Override
	public Disjunction disjunction(Object... operands) {
		Disjunction vd = Disjunction.T.create();
		if (operands != null) {
			vd.setOperands(new ArrayList<Object>(Arrays.asList(operands)));
		}
		return vd;
	}

	@Override
	public Disjunction disjunction(List<Object> operandsList) {
		Disjunction vd = Disjunction.T.create();
		vd.setOperands(operandsList);
		return vd;
	}

	@Override
	public Negation negation(Object operand) {
		Negation vd = Negation.T.create();
		vd.setOperand(operand);
		return vd;
	}

	@Override
	public Add add(Object... operands) {
		Add vd = Add.T.create();
		if (operands != null) {
			vd.setOperands(new ArrayList<Object>(Arrays.asList(operands)));
		}
		return vd;
	}

	@Override
	public Add add(List<Object> operandsList) {
		Add vd = Add.T.create();
		vd.setOperands(operandsList);
		return vd;
	}

	@Override
	public Avg avg(Object... operands) {
		Avg vd = Avg.T.create();
		if (operands != null) {
			vd.setOperands(new ArrayList<Object>(Arrays.asList(operands)));
		}
		return vd;
	}

	@Override
	public Avg avg(List<Object> operandsList) {
		Avg vd = Avg.T.create();
		vd.setOperands(operandsList);
		return vd;
	}

	@Override
	public Divide divide(Object... operands) {
		Divide vd = Divide.T.create();
		if (operands != null) {
			vd.setOperands(new ArrayList<Object>(Arrays.asList(operands)));
		}
		return vd;
	}

	@Override
	public Divide divide(List<Object> operandsList) {
		Divide vd = Divide.T.create();
		vd.setOperands(operandsList);
		return vd;
	}

	@Override
	public Max max(Object... operands) {
		Max vd = Max.T.create();
		if (operands != null) {
			vd.setOperands(new ArrayList<Object>(Arrays.asList(operands)));
		}
		return vd;
	}

	@Override
	public Max max(List<Object> operandsList) {
		Max vd = Max.T.create();
		vd.setOperands(operandsList);
		return vd;
	}

	@Override
	public Min min(Object... operands) {
		Min vd = Min.T.create();
		if (operands != null) {
			vd.setOperands(new ArrayList<Object>(Arrays.asList(operands)));
		}
		return vd;
	}

	@Override
	public Min min(List<Object> operandsList) {
		Min vd = Min.T.create();
		vd.setOperands(operandsList);
		return vd;
	}

	@Override
	public Multiply multiply(Object... operands) {
		Multiply vd = Multiply.T.create();
		if (operands != null) {
			vd.setOperands(new ArrayList<Object>(Arrays.asList(operands)));
		}
		return vd;
	}

	@Override
	public Multiply multiply(List<Object> operandsList) {
		Multiply vd = Multiply.T.create();
		vd.setOperands(operandsList);
		return vd;
	}

	@Override
	public Subtract subtract(Object... operands) {
		Subtract vd = Subtract.T.create();
		if (operands != null) {
			vd.setOperands(new ArrayList<Object>(Arrays.asList(operands)));
		}
		return vd;
	}

	@Override
	public Subtract subtract(List<Object> operandsList) {
		Subtract vd = Subtract.T.create();
		vd.setOperands(operandsList);
		return vd;
	}

	@Override
	public Ceil ceil(Object value, Object precision) {
		Ceil vd = Ceil.T.create();
		vd.setValue(value);
		vd.setPrecision(precision);
		return vd;
	}

	@Override
	public Floor floor(Object value, Object precision) {
		Floor vd = Floor.T.create();
		vd.setValue(value);
		vd.setPrecision(precision);
		return vd;
	}

	@Override
	public Round round(Object value, Object precision) {
		Round vd = Round.T.create();
		vd.setValue(value);
		vd.setPrecision(precision);
		return vd;
	}

	@Override
	public PropertyPath propertyPath(String propertyPath, Object entity) {
		PropertyPath vd = PropertyPath.T.create();
		vd.setPropertyPath(propertyPath);
		vd.setEntity(entity);
		return vd;
	}

	@Override
	public Equal equal(Object leftOperand, Object rightOperand) {
		Equal vd = Equal.T.create();
		vd.setLeftOperand(leftOperand);
		vd.setRightOperand(rightOperand);
		return vd;
	}

	@Override
	public Greater greater(Object leftOperand, Object rightOperand) {
		Greater vd = Greater.T.create();
		vd.setLeftOperand(leftOperand);
		vd.setRightOperand(rightOperand);
		return vd;
	}

	@Override
	public GreaterOrEqual greaterOrEqual(Object leftOperand, Object rightOperand) {
		GreaterOrEqual vd = GreaterOrEqual.T.create();
		vd.setLeftOperand(leftOperand);
		vd.setRightOperand(rightOperand);
		return vd;
	}

	@Override
	public Ilike ilike(Object leftOperand, Object rightOperand) {
		Ilike vd = Ilike.T.create();
		vd.setLeftOperand(leftOperand);
		vd.setRightOperand(rightOperand);
		return vd;
	}

	@Override
	public In in(Object leftOperand, Object rightOperand) {
		In vd = In.T.create();
		vd.setLeftOperand(leftOperand);
		vd.setRightOperand(rightOperand);
		return vd;
	}

	@Override
	public Less less(Object leftOperand, Object rightOperand) {
		Less vd = Less.T.create();
		vd.setLeftOperand(leftOperand);
		vd.setRightOperand(rightOperand);
		return vd;
	}

	@Override
	public LessOrEqual lessOrEqual(Object leftOperand, Object rightOperand) {
		LessOrEqual vd = LessOrEqual.T.create();
		vd.setLeftOperand(leftOperand);
		vd.setRightOperand(rightOperand);
		return vd;
	}

	@Override
	public Like like(Object leftOperand, Object rightOperand) {
		Like vd = Like.T.create();
		vd.setLeftOperand(leftOperand);
		vd.setRightOperand(rightOperand);
		return vd;
	}

	@Override
	public NotEqual notEqual(Object leftOperand, Object rightOperand) {
		NotEqual vd = NotEqual.T.create();
		vd.setLeftOperand(leftOperand);
		vd.setRightOperand(rightOperand);
		return vd;
	}

	@Override
	public Assignable assignable(Object leftOperand, Object rightOperand) {
		Assignable vd = Assignable.T.create();
		vd.setLeftOperand(leftOperand);
		vd.setRightOperand(rightOperand);
		return vd;
	}

	@Override
	public InstanceOf instanceOf(Object leftOperand, Object rightOperand) {
		InstanceOf vd = InstanceOf.T.create();
		vd.setLeftOperand(leftOperand);
		vd.setRightOperand(rightOperand);
		return vd;
	}

	@Override
	public Concatenation concatenation(Object... operands) {
		Concatenation vd = Concatenation.T.create();
		if (operands != null) {
			vd.setOperands(new ArrayList<Object>(Arrays.asList(operands)));
		}
		return vd;
	}

	@Override
	public Concatenation concatenation(List<Object> operandsList) {
		Concatenation vd = Concatenation.T.create();
		vd.setOperands(operandsList);
		return vd;
	}

	@Override
	public Localize localize(Object localizedString, Object locale) {
		Localize vd = Localize.T.create();
		vd.setLocalizedString(localizedString);
		vd.setLocale(locale);
		return vd;
	}

	@Override
	public Lower lower(Object operand) {
		Lower vd = Lower.T.create();
		vd.setOperand(operand);
		return vd;
	}

	@Override
	public SubString substring(Object operand, Integer startIndex, Integer endIndex) {
		SubString vd = SubString.T.create();
		vd.setOperand(operand);
		vd.setStartIndex(startIndex);
		vd.setEndIndex(endIndex);
		return vd;
	}

	@Override
	public Upper upper(Object operand) {
		Upper vd = Upper.T.create();
		vd.setOperand(operand);
		return vd;
	}

	@Override
	public Now now() {
		Now vd = Now.T.create();
		return vd;
	}

	@Override
	public Query query(Object query, ResultConvenience resultConvenience) {
		Query vd = Query.T.create();
		vd.setQuery(query);
		vd.setResultConvenience(resultConvenience);
		return vd;
	}
	
	@Override
	public EnumReference enumReference(String typeSignature, String constant) {
		EnumReference vd = EnumReference.T.create();
		vd.setTypeSignature(typeSignature);
		vd.setConstant(constant);
		return vd;
	}

	@Override
	public Escape escape(Object valueDescriptor) {
		Escape vd = Escape.T.create();
		vd.setValue(valueDescriptor);
		return vd;
	}

	@Override
	public Evaluate evaluate(Object valueDescriptor) {
		Evaluate vd = Evaluate.T.create();
		vd.setValue(valueDescriptor);
		return vd;
	}

	@Override
	public PersistentEntityReference persistentEntityReference(String typeSignature, Object id) {
		PersistentEntityReference vd = PersistentEntityReference.T.create();
		vd.setRefId(id);
		vd.setTypeSignature(typeSignature);
		return vd;
	}

	@Override
	public PreliminaryEntityReference preliminaryEntityReference(String typeSignature, Object id) {
		PreliminaryEntityReference vd = PreliminaryEntityReference.T.create();
		vd.setRefId(id);
		vd.setTypeSignature(typeSignature);
		return vd;
	}

	@Override
	@SuppressWarnings("deprecation")
	public Variable variable(String name, LocalizedString localizedName, LocalizedString description, Object defaultValue) {
		Variable vd = Variable.T.create();
		vd.setName(name);
		vd.setLocalizedName(localizedName);
		vd.setDescription(description);
		vd.setDefaultValue(defaultValue);
		return vd;
	}

	@Override
	public DateOffset dateOffset(int value, DateOffsetUnit unit) {
		DateOffset offset = DateOffset.T.create();
		offset.setValue(value);
		offset.setOffset(unit);
		return offset;
	}

	@Override
	public TimeZoneOffset timeZoneOffset(int minutes) {
		TimeZoneOffset offset = TimeZoneOffset.T.create();
		offset.setMinutes(minutes);
		return offset;
	}

	@Override
	public TimeSpan timeSpan(double value, TimeUnit unit) {
		TimeSpan span = TimeSpan.T.create();
		span.setUnit(unit);
		span.setValue(value);
		return span;
	}

}
