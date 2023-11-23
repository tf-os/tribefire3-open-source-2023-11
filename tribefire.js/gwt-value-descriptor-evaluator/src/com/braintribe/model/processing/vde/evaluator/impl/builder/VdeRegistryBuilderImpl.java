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
package com.braintribe.model.processing.vde.evaluator.impl.builder;

import java.util.HashMap;
import java.util.Map;

import com.braintribe.gm.model.svd.EvaluateRequest;
import com.braintribe.model.bvd.cast.DecimalCast;
import com.braintribe.model.bvd.cast.DoubleCast;
import com.braintribe.model.bvd.cast.FloatCast;
import com.braintribe.model.bvd.cast.IntegerCast;
import com.braintribe.model.bvd.cast.LongCast;
import com.braintribe.model.bvd.conditional.Coalesce;
import com.braintribe.model.bvd.conditional.If;
import com.braintribe.model.bvd.context.CurrentLocale;
import com.braintribe.model.bvd.context.ModelPath;
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
import com.braintribe.model.bvd.predicate.IsNull;
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
import com.braintribe.model.generic.value.Evaluate;
import com.braintribe.model.generic.value.PersistentEntityReference;
import com.braintribe.model.generic.value.PreliminaryEntityReference;
import com.braintribe.model.generic.value.ValueDescriptor;
import com.braintribe.model.generic.value.Variable;
import com.braintribe.model.processing.vde.evaluator.api.ValueDescriptorEvaluator;
import com.braintribe.model.processing.vde.evaluator.api.VdeRegistry;
import com.braintribe.model.processing.vde.evaluator.api.builder.VdeRegistryBuilder;
import com.braintribe.model.processing.vde.evaluator.impl.VdeRegistryImpl;
import com.braintribe.model.processing.vde.evaluator.impl.bvd.cast.DecimalCastVde;
import com.braintribe.model.processing.vde.evaluator.impl.bvd.cast.DoubleCastVde;
import com.braintribe.model.processing.vde.evaluator.impl.bvd.cast.FloatCastVde;
import com.braintribe.model.processing.vde.evaluator.impl.bvd.cast.IntegerCastVde;
import com.braintribe.model.processing.vde.evaluator.impl.bvd.cast.LongCastVde;
import com.braintribe.model.processing.vde.evaluator.impl.bvd.collection.RemoveNullsVde;
import com.braintribe.model.processing.vde.evaluator.impl.bvd.conditional.CoalesceVde;
import com.braintribe.model.processing.vde.evaluator.impl.bvd.conditional.IfVde;
import com.braintribe.model.processing.vde.evaluator.impl.bvd.context.CurrentLocaleVde;
import com.braintribe.model.processing.vde.evaluator.impl.bvd.context.ModelPathVde;
import com.braintribe.model.processing.vde.evaluator.impl.bvd.context.UserNameVde;
import com.braintribe.model.processing.vde.evaluator.impl.bvd.convert.ToBooleanVde;
import com.braintribe.model.processing.vde.evaluator.impl.bvd.convert.ToCollectionVde;
import com.braintribe.model.processing.vde.evaluator.impl.bvd.convert.ToDateVde;
import com.braintribe.model.processing.vde.evaluator.impl.bvd.convert.ToDecimalVde;
import com.braintribe.model.processing.vde.evaluator.impl.bvd.convert.ToDoubleVde;
import com.braintribe.model.processing.vde.evaluator.impl.bvd.convert.ToEnumVde;
import com.braintribe.model.processing.vde.evaluator.impl.bvd.convert.ToFloatVde;
import com.braintribe.model.processing.vde.evaluator.impl.bvd.convert.ToIntegerVde;
import com.braintribe.model.processing.vde.evaluator.impl.bvd.convert.ToLongVde;
import com.braintribe.model.processing.vde.evaluator.impl.bvd.convert.ToReferenceVde;
import com.braintribe.model.processing.vde.evaluator.impl.bvd.convert.ToStringVde;
import com.braintribe.model.processing.vde.evaluator.impl.bvd.logic.ConjunctionVde;
import com.braintribe.model.processing.vde.evaluator.impl.bvd.logic.DisjunctionVde;
import com.braintribe.model.processing.vde.evaluator.impl.bvd.logic.NegationVde;
import com.braintribe.model.processing.vde.evaluator.impl.bvd.math.AddVde;
import com.braintribe.model.processing.vde.evaluator.impl.bvd.math.AvgVde;
import com.braintribe.model.processing.vde.evaluator.impl.bvd.math.CeilVde;
import com.braintribe.model.processing.vde.evaluator.impl.bvd.math.DivideVde;
import com.braintribe.model.processing.vde.evaluator.impl.bvd.math.FloorVde;
import com.braintribe.model.processing.vde.evaluator.impl.bvd.math.MaxVde;
import com.braintribe.model.processing.vde.evaluator.impl.bvd.math.MinVde;
import com.braintribe.model.processing.vde.evaluator.impl.bvd.math.MultiplyVde;
import com.braintribe.model.processing.vde.evaluator.impl.bvd.math.RoundVde;
import com.braintribe.model.processing.vde.evaluator.impl.bvd.math.SubtractVde;
import com.braintribe.model.processing.vde.evaluator.impl.bvd.navigation.PropertyPathVde;
import com.braintribe.model.processing.vde.evaluator.impl.bvd.predicate.AssignableVde;
import com.braintribe.model.processing.vde.evaluator.impl.bvd.predicate.EqualVde;
import com.braintribe.model.processing.vde.evaluator.impl.bvd.predicate.GreaterOrEqualVde;
import com.braintribe.model.processing.vde.evaluator.impl.bvd.predicate.GreaterVde;
import com.braintribe.model.processing.vde.evaluator.impl.bvd.predicate.IlikeVde;
import com.braintribe.model.processing.vde.evaluator.impl.bvd.predicate.InVde;
import com.braintribe.model.processing.vde.evaluator.impl.bvd.predicate.InstanceOfVde;
import com.braintribe.model.processing.vde.evaluator.impl.bvd.predicate.IsNullVde;
import com.braintribe.model.processing.vde.evaluator.impl.bvd.predicate.LessOrEqualVde;
import com.braintribe.model.processing.vde.evaluator.impl.bvd.predicate.LessVde;
import com.braintribe.model.processing.vde.evaluator.impl.bvd.predicate.LikeVde;
import com.braintribe.model.processing.vde.evaluator.impl.bvd.predicate.NotEqualVde;
import com.braintribe.model.processing.vde.evaluator.impl.bvd.string.ConcatenationVde;
import com.braintribe.model.processing.vde.evaluator.impl.bvd.string.LocalizeVde;
import com.braintribe.model.processing.vde.evaluator.impl.bvd.string.LowerVde;
import com.braintribe.model.processing.vde.evaluator.impl.bvd.string.SubStringVde;
import com.braintribe.model.processing.vde.evaluator.impl.bvd.string.UpperVde;
import com.braintribe.model.processing.vde.evaluator.impl.bvd.time.NowVde;
import com.braintribe.model.processing.vde.evaluator.impl.query.QueryVde;
import com.braintribe.model.processing.vde.evaluator.impl.root.EntityReferenceVde;
import com.braintribe.model.processing.vde.evaluator.impl.root.EnumReferenceVde;
import com.braintribe.model.processing.vde.evaluator.impl.root.EscapeVde;
import com.braintribe.model.processing.vde.evaluator.impl.root.EvaluateVde;
import com.braintribe.model.processing.vde.evaluator.impl.root.VariableVde;
import com.braintribe.model.processing.vde.evaluator.impl.service.EvaluateRequestVde;

public class VdeRegistryBuilderImpl implements VdeRegistryBuilder {

	private VdeRegistry registry = null;

	private final static Map<Class<? extends ValueDescriptor>, ValueDescriptorEvaluator<?>> DEFAULT_CONCRETE_EXPERTS;

	// setting up the list of default experts
	static {
		DEFAULT_CONCRETE_EXPERTS = new HashMap<Class<? extends ValueDescriptor>, ValueDescriptorEvaluator<?>>();

		// bvd
		// cast
		DEFAULT_CONCRETE_EXPERTS.put(DecimalCast.class, new DecimalCastVde());
		DEFAULT_CONCRETE_EXPERTS.put(DoubleCast.class, new DoubleCastVde());
		DEFAULT_CONCRETE_EXPERTS.put(FloatCast.class, new FloatCastVde());
		DEFAULT_CONCRETE_EXPERTS.put(IntegerCast.class, new IntegerCastVde());
		DEFAULT_CONCRETE_EXPERTS.put(LongCast.class, new LongCastVde());
		// context
		DEFAULT_CONCRETE_EXPERTS.put(CurrentLocale.class, new CurrentLocaleVde());
		DEFAULT_CONCRETE_EXPERTS.put(UserName.class, new UserNameVde());
		DEFAULT_CONCRETE_EXPERTS.put(ModelPath.class, new ModelPathVde());
		// convert
		DEFAULT_CONCRETE_EXPERTS.put(ToBoolean.class, new ToBooleanVde());
		DEFAULT_CONCRETE_EXPERTS.put(ToDate.class, new ToDateVde());
		DEFAULT_CONCRETE_EXPERTS.put(ToDecimal.class, new ToDecimalVde());
		DEFAULT_CONCRETE_EXPERTS.put(ToDouble.class, new ToDoubleVde());
		DEFAULT_CONCRETE_EXPERTS.put(ToEnum.class, new ToEnumVde());
		DEFAULT_CONCRETE_EXPERTS.put(ToFloat.class, new ToFloatVde());
		DEFAULT_CONCRETE_EXPERTS.put(ToInteger.class, new ToIntegerVde());
		DEFAULT_CONCRETE_EXPERTS.put(ToLong.class, new ToLongVde());
		DEFAULT_CONCRETE_EXPERTS.put(ToString.class, new ToStringVde());
		DEFAULT_CONCRETE_EXPERTS.put(ToSet.class, new ToCollectionVde<ToSet>());
		DEFAULT_CONCRETE_EXPERTS.put(ToList.class, new ToCollectionVde<ToList>());
		DEFAULT_CONCRETE_EXPERTS.put(ToReference.class, new ToReferenceVde());
		// collection
		DEFAULT_CONCRETE_EXPERTS.put(RemoveNulls.class, new RemoveNullsVde());
		// conditional
		DEFAULT_CONCRETE_EXPERTS.put(Coalesce.class, new CoalesceVde());
		DEFAULT_CONCRETE_EXPERTS.put(If.class, new IfVde());
		// logic
		DEFAULT_CONCRETE_EXPERTS.put(Conjunction.class, new ConjunctionVde());
		DEFAULT_CONCRETE_EXPERTS.put(Disjunction.class, new DisjunctionVde());
		DEFAULT_CONCRETE_EXPERTS.put(Negation.class, new NegationVde());
		// math
		DEFAULT_CONCRETE_EXPERTS.put(Add.class, new AddVde());
		DEFAULT_CONCRETE_EXPERTS.put(Subtract.class, new SubtractVde());
		DEFAULT_CONCRETE_EXPERTS.put(Multiply.class, new MultiplyVde());
		DEFAULT_CONCRETE_EXPERTS.put(Divide.class, new DivideVde());
		DEFAULT_CONCRETE_EXPERTS.put(Floor.class, new FloorVde());
		DEFAULT_CONCRETE_EXPERTS.put(Ceil.class, new CeilVde());
		DEFAULT_CONCRETE_EXPERTS.put(Round.class, new RoundVde());
		DEFAULT_CONCRETE_EXPERTS.put(Max.class, new MaxVde());
		DEFAULT_CONCRETE_EXPERTS.put(Min.class, new MinVde());
		DEFAULT_CONCRETE_EXPERTS.put(Avg.class, new AvgVde());
		// navigation
		DEFAULT_CONCRETE_EXPERTS.put(PropertyPath.class, new PropertyPathVde());
		// predicate
		DEFAULT_CONCRETE_EXPERTS.put(Equal.class, new EqualVde());
		DEFAULT_CONCRETE_EXPERTS.put(Greater.class, new GreaterVde());
		DEFAULT_CONCRETE_EXPERTS.put(GreaterOrEqual.class, new GreaterOrEqualVde());
		DEFAULT_CONCRETE_EXPERTS.put(Less.class, new LessVde());
		DEFAULT_CONCRETE_EXPERTS.put(LessOrEqual.class, new LessOrEqualVde());
		DEFAULT_CONCRETE_EXPERTS.put(NotEqual.class, new NotEqualVde());
		DEFAULT_CONCRETE_EXPERTS.put(Like.class, new LikeVde());
		DEFAULT_CONCRETE_EXPERTS.put(Ilike.class, new IlikeVde());
		DEFAULT_CONCRETE_EXPERTS.put(In.class, new InVde());
		DEFAULT_CONCRETE_EXPERTS.put(Assignable.class, new AssignableVde());
		DEFAULT_CONCRETE_EXPERTS.put(InstanceOf.class, new InstanceOfVde());
		DEFAULT_CONCRETE_EXPERTS.put(IsNull.class, new IsNullVde());
		// string
		DEFAULT_CONCRETE_EXPERTS.put(Concatenation.class, new ConcatenationVde());
		DEFAULT_CONCRETE_EXPERTS.put(Localize.class, new LocalizeVde());
		DEFAULT_CONCRETE_EXPERTS.put(Lower.class, new LowerVde());
		DEFAULT_CONCRETE_EXPERTS.put(SubString.class, new SubStringVde());
		DEFAULT_CONCRETE_EXPERTS.put(Upper.class, new UpperVde());
		// time
		DEFAULT_CONCRETE_EXPERTS.put(Now.class, new NowVde());

		// root (non-bvd)
		DEFAULT_CONCRETE_EXPERTS.put(PreliminaryEntityReference.class, EntityReferenceVde.getInstance());
		DEFAULT_CONCRETE_EXPERTS.put(PersistentEntityReference.class, EntityReferenceVde.getInstance());
		DEFAULT_CONCRETE_EXPERTS.put(Variable.class, new VariableVde());
		DEFAULT_CONCRETE_EXPERTS.put(Escape.class, new EscapeVde());
		DEFAULT_CONCRETE_EXPERTS.put(Evaluate.class, new EvaluateVde());
		DEFAULT_CONCRETE_EXPERTS.put(EnumReference.class, new EnumReferenceVde());

		// query
		DEFAULT_CONCRETE_EXPERTS.put(Query.class, QueryVde.getInstance());

		// service
		DEFAULT_CONCRETE_EXPERTS.put(EvaluateRequest.class, EvaluateRequestVde.getInstance());

	}

	public VdeRegistryBuilderImpl() {
		registry = new VdeRegistryImpl();
	}

	@Override
	public VdeRegistry defaultSetup() {
		registry.resetRegistry();
		registry.setConcreteExperts(DEFAULT_CONCRETE_EXPERTS);

		return registry;
	}

	@Override
	public VdeRegistryBuilder addRegistry(VdeRegistry otherRegistry) {
		registry.loadOtherRegistry(otherRegistry);
		return this;
	}

	@Override
	public VdeRegistryBuilder loadDefaultSetup() {
		VdeRegistry tempRegistry = new VdeRegistryImpl();
		tempRegistry.setConcreteExperts(DEFAULT_CONCRETE_EXPERTS);

		return addRegistry(tempRegistry);
	}

	@Override
	public <D extends ValueDescriptor> VdeRegistryBuilder withConcreteExpert(Class<D> vdType, ValueDescriptorEvaluator<? super D> vdEvaluator) {
		registry.putConcreteExpert(vdType, vdEvaluator);
		return this;
	}

	@Override
	public <D extends ValueDescriptor> VdeRegistryBuilder withAbstractExpert(Class<D> vdType, ValueDescriptorEvaluator<? super D> vdEvaluator) {
		registry.putAbstractExpert(vdType, vdEvaluator);
		return this;
	}

	@Override
	public VdeRegistry done() {
		return this.registry;
	}

	@Override
	public VdeRegistryBuilder removeAbstractExpert(Class<? extends ValueDescriptor> valueDescriptorClass) {
		registry.removeAbstractExpert(valueDescriptorClass);
		return this;
	}

	@Override
	public VdeRegistryBuilder removeConcreteExpert(Class<? extends ValueDescriptor> valueDescriptorClass) {
		registry.removeConcreteExpert(valueDescriptorClass);
		return this;
	}

}
