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
package com.braintribe.model.processing.smart.query.planner.context;

import static com.braintribe.model.processing.query.tools.SourceTypeResolver.resolvePropertyType;
import static com.braintribe.model.processing.smart.query.planner.tools.SmartQueryPlannerTools.enumTypeOrNull;
import static java.util.Collections.emptyMap;

import com.braintribe.model.accessdeployment.smart.meta.conversion.SmartConversion;
import com.braintribe.model.generic.reflection.TypeCode;
import com.braintribe.model.meta.GmEnumType;
import com.braintribe.model.meta.GmLinearCollectionType;
import com.braintribe.model.meta.GmMapType;
import com.braintribe.model.meta.GmProperty;
import com.braintribe.model.meta.GmType;
import com.braintribe.model.processing.query.planner.RuntimeQueryPlannerException;
import com.braintribe.model.processing.query.planner.builder.ValueBuilder;
import com.braintribe.model.processing.query.planner.tools.EntitySignatureTools;
import com.braintribe.model.processing.smart.query.planner.SmartQueryPlannerException;
import com.braintribe.model.processing.smart.query.planner.core.builder.SmartValueBuilder;
import com.braintribe.model.processing.smart.query.planner.graph.EntitySourceNode;
import com.braintribe.model.processing.smart.query.planner.graph.QueryPlanStructure;
import com.braintribe.model.processing.smart.query.planner.graph.SimpleValueNode;
import com.braintribe.model.processing.smart.query.planner.graph.SourceNode;
import com.braintribe.model.processing.smart.query.planner.structure.EntityHierarchyNode;
import com.braintribe.model.processing.smart.query.planner.structure.ModelExpert;
import com.braintribe.model.processing.smart.query.planner.structure.adapter.EntityPropertyMapping.ConstantPropertyWrapper;
import com.braintribe.model.processing.smart.query.planner.structure.adapter.EnumMapping;
import com.braintribe.model.query.Operand;
import com.braintribe.model.query.PropertyOperand;
import com.braintribe.model.query.Source;
import com.braintribe.model.query.functions.EntitySignature;
import com.braintribe.model.query.functions.JoinFunction;
import com.braintribe.model.query.functions.MapKey;
import com.braintribe.model.query.functions.QueryFunction;
import com.braintribe.model.query.functions.aggregate.AggregateFunction;
import com.braintribe.model.query.functions.aggregate.Average;
import com.braintribe.model.query.functions.aggregate.Count;
import com.braintribe.model.query.functions.aggregate.Max;
import com.braintribe.model.query.functions.aggregate.Min;
import com.braintribe.model.query.functions.aggregate.Sum;
import com.braintribe.model.queryplan.value.AggregationFunctionType;
import com.braintribe.model.queryplan.value.Value;
import com.braintribe.model.smartqueryplan.queryfunctions.ResolveDelegateProperty;
import com.braintribe.model.smartqueryplan.queryfunctions.ResolveId;

/**
 * @see #convert(Object)
 */
class SmartOperandConverter {

	private final SmartQueryPlannerContext context;
	private final QueryPlanStructure planStructure;
	private final ModelExpert modelExpert;
	private final AssembleEntityFunctionBuilder assembleEntityFunctionBuilder;

	public SmartOperandConverter(SmartQueryPlannerContext context) {
		this.context = context;
		this.planStructure = context.planStructure();
		this.modelExpert = context.modelExpert();
		this.assembleEntityFunctionBuilder = new AssembleEntityFunctionBuilder(this);
	}

	/**
	 * Functionality for {@link SmartQueryPlannerContext#convertOperand(Object)}
	 */
	public Value convert(Object operand) {
		// check OperandConverter for analogy
		if (!(operand instanceof Operand) || context.isEvaluationExclude(operand))
			return ValueBuilder.staticValue(operand);

		if (context.isUnmappedSourceRelatedOperand((Operand) operand))
			return ValueBuilder.staticValue(null);

		if (operand instanceof PropertyOperand) {
			PropertyOperand po = (PropertyOperand) operand;

			Source source = po.getSource();
			String propertyName = po.getPropertyName();

			if (propertyName == null)
				return convert(source);

			EntitySourceNode sourceNode = planStructure.getSourceNode(source);

			if (!sourceNode.isSmartPropertyMapped(propertyName))
				return ValueBuilder.staticValue(null);

			if (resolvePropertyType(source, propertyName, true).getTypeCode() == TypeCode.entityType) {
				EntitySourceNode joinNode = sourceNode.getEntityJoin(propertyName);
				return convert(joinNode.getSource());
			}

			return convertSmartProperty(sourceNode, propertyName);

		} else if (operand instanceof JoinFunction) {
			return convertJoinFunction((JoinFunction) operand);

		} else if (operand instanceof AggregateFunction) {
			// Is this expected here?
			return convertAggregateFunction((AggregateFunction) operand);

		} else if (operand instanceof QueryFunction) {
			return convertQueryFunction((QueryFunction) operand);

		} else if (operand instanceof Source) {
			return convert((Source) operand);

		} else {
			throw new RuntimeQueryPlannerException("Unsupported operand: " + operand + " of type: " + operand.getClass().getName());
		}
	}

	/** Also used by {@link AssembleEntityFunctionBuilder} */
	Value convertSmartProperty(EntitySourceNode sourceNode, String smartProperty) {
		if (sourceNode.isSelectedVirtualProperty(smartProperty))
			return buildVirtualValue(sourceNode, smartProperty);
		else
			return convertMappedSmartProperty(sourceNode, smartProperty);
	}

	private Value buildVirtualValue(EntitySourceNode sourceNode, String smartProperty) {
		EntityHierarchyNode rootNode = sourceNode.resolveHierarchyRootedAtThis();

		if (rootNode.getSubNodes().isEmpty() || isConstantValueStatic(sourceNode, smartProperty)) {
			ConstantPropertyWrapper cpw = (ConstantPropertyWrapper) sourceNode.resolveSmartPropertyMapping(smartProperty);
			return ValueBuilder.staticValue(cpw.getConstantValue());
		}

		return ValueBuilder.queryFunctionNoMappings(ConstantPropertyTools.buildDiscriminatorValue(sourceNode, smartProperty));
	}

	private boolean isConstantValueStatic(EntitySourceNode sourceNode, String smartProperty) {
		return sourceNode.resolveConstantPropertyMapping(smartProperty).isStatic;
	}

	private Value convertMappedSmartProperty(EntitySourceNode sourceNode, String smartProperty) {
		int propertyPosition = sourceNode.getSimplePropertyPosition(smartProperty);
		SmartConversion conversion = sourceNode.findSmartPropertyConversion(smartProperty);

		GmProperty gmProperty = modelExpert.getGmProperty(sourceNode.getSmartGmType(), smartProperty);

		return buildValue(propertyPosition, conversion, enumTypeOrNull(gmProperty.getType()));
	}

	private Value convertAggregateFunction(AggregateFunction operand) {
		Value convertedOperand = convert(operand.getOperand());
		AggregationFunctionType type;

		if (operand instanceof Count) {
			Count count = (Count) operand;
			type = count.getDistinct() ? AggregationFunctionType.countDistinct : AggregationFunctionType.count;

		} else if (operand instanceof Sum) {
			type = AggregationFunctionType.sum;

		} else if (operand instanceof Min) {
			type = AggregationFunctionType.min;

		} else if (operand instanceof Max) {
			type = AggregationFunctionType.max;

		} else if (operand instanceof Average) {
			type = AggregationFunctionType.avg;

		} else {
			throw new RuntimeQueryPlannerException("Unsupported aggregateFunction: " + operand + " of type: " + operand.getClass().getName());
		}

		return ValueBuilder.aggregateFunction(convertedOperand, type);
	}

	private Value convertQueryFunction(QueryFunction operand) {
		if (operand instanceof EntitySignature) {
			String staticSignature = EntitySignatureTools.getStaticSignature((EntitySignature) operand);

			if (staticSignature != null)
				return ValueBuilder.staticValue(staticSignature);

		} else if (operand instanceof ResolveId) {
			ResolveId ri = (ResolveId) operand;
			EntitySourceNode sourceNode = planStructure.getSourceNode(ri.getSource());
			int entityIdPosition = sourceNode.getEntityIdPosition();
			return ValueBuilder.tupleComponent(entityIdPosition);

		} else if (operand instanceof ResolveDelegateProperty) {
			ResolveDelegateProperty rdp = (ResolveDelegateProperty) operand;
			EntitySourceNode sourceNode = planStructure.getSourceNode(rdp.getSource());
			int delegatePropertyPosition = sourceNode.getSimpleDelegatePropertyPosition(rdp.getDelegateProperty());

			GmProperty delegateGmProperty = sourceNode.getDelegateGmProperty(rdp.getDelegateProperty());
			GmEnumType gmEnumType = enumTypeOrNull(delegateGmProperty.getType());
			SmartConversion conversion = gmEnumType == null ? null : SmartValueBuilder.identityEnumToStringConversion(gmEnumType);

			return buildValue(delegatePropertyPosition, conversion, gmEnumType);
		}

		return ValueBuilder.queryFunction(operand, context.getFunctionOperandMappings(operand));
	}

	private Value convert(Source source) {
		SourceNode sourceNode = planStructure.getSourceNode(source);

		if (sourceNode instanceof EntitySourceNode) {
			return convert((EntitySourceNode) sourceNode);

		} else {
			SimpleValueNode svn = (SimpleValueNode) sourceNode;
			GmEnumType gmEnumType = resolveSmartGmEnumTypeAsCollectionParameter(svn, false);
			return buildValue(svn.getValuePosition(), svn.findSmartConversion(), gmEnumType);
		}
	}

	private Value convertJoinFunction(JoinFunction operand) {
		SourceNode sourceNode = planStructure.getSourceNode(operand.getJoin());

		GmEnumType gmEnumType = null;

		if (operand instanceof MapKey) {
			EntitySourceNode mapKeyNode = sourceNode.getMapKeyNode();

			if (mapKeyNode != null) {
				return convert(mapKeyNode);

			} else {
				gmEnumType = resolveSmartGmEnumTypeAsCollectionParameter(sourceNode, true);
			}
		}

		return buildValue(sourceNode.getJoinFunctionPosition(), null, gmEnumType);
	}

	private GmEnumType resolveSmartGmEnumTypeAsCollectionParameter(SourceNode collectionSourceNode, boolean mapKey) {
		return enumTypeOrNull(resolveSmartGmTypeAsCollectionParameter(collectionSourceNode, mapKey));
	}

	private GmType resolveSmartGmTypeAsCollectionParameter(SourceNode collectionSourceNode, boolean mapKey) {
		GmProperty smartJoinP = collectionSourceNode.getSmartJoinGmProperty();
		GmType type = smartJoinP.getType();

		if (mapKey) {
			if (type instanceof GmMapType)
				return ((GmMapType) type).getKeyType();

		} else {
			if (type instanceof GmLinearCollectionType)
				return ((GmLinearCollectionType) type).getElementType();
			else if (type instanceof GmMapType)
				return ((GmMapType) type).getValueType();
		}

		String expectedType = mapKey ? "Map<?, ?>" : "Collection<?>";
		throw new SmartQueryPlannerException("Wrong property type. " + expectedType + " expected, but: " + type + "found. Property: "
				+ smartJoinP.getDeclaringType().getTypeSignature() + "." + smartJoinP);
	}

	private Value convert(EntitySourceNode sourceNode) {
		return ValueBuilder.queryFunction(assembleEntityFunctionBuilder.build(sourceNode), emptyMap());
	}

	private Value buildValue(int position, SmartConversion conversion, GmEnumType gmEnumType) {
		Value value = ValueBuilder.tupleComponent(position);

		if (conversion != null) {
			value = SmartValueBuilder.convertedValue(value, conversion);

		} else if (gmEnumType != null) {
			/* The EnumMapping is only considered iff there is no conversion for the property. */
			EnumMapping enumMapping = modelExpert.resolveEnumMapping(gmEnumType);

			value = SmartValueBuilder.convertedValue(value, enumMapping.getConversion());
		}

		return value;
	}

}
