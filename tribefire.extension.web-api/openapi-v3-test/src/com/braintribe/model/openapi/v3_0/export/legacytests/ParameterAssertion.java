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
package com.braintribe.model.openapi.v3_0.export.legacytests;

import static com.braintribe.model.openapi.v3_0.export.legacytests.AbstractOpenapiProcessorTest.getReferenced;
import static com.braintribe.testing.junit.assertions.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.braintribe.model.openapi.v3_0.OpenapiParameter;
import com.braintribe.model.openapi.v3_0.OpenapiSchema;
import com.braintribe.model.openapi.v3_0.OpenapiType;

public abstract class ParameterAssertion<T extends OpenapiParameter> implements Cloneable {
	private final String name;
	private final String in;
	private final boolean required;
	protected Object defaultValue;
	
	public ParameterAssertion(String name, String in, boolean required) {
		super();
		this.name = name;
		this.in = in;
		this.required = required;
	}
	
	public String getName() {
		return name;
	}

	protected abstract void _test(T parameter);
	

	public void test(OpenapiParameter parameter) {
		assertThat(parameter.getName()).as(getName() + " - name").isEqualTo(name);
		assertThat(parameter.getIn()).as(getName() + " - in").isEqualTo(in);
		assertThat(parameter.getRequired()).as(getName() + " - required").isEqualTo(required);
		
		_test((T) parameter);
	}
	
	@Override
	protected ParameterAssertion<T> clone()  {
		try {
			return (ParameterAssertion<T>) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException("Someone removed the Cloneable interface... Put it back please.", e);
		}
	}
	
	protected ParameterAssertion<T> clone(Object newDefault)  {
		ParameterAssertion<T> cloned = clone();
		cloned.defaultValue = newDefault;
		return cloned;
	}
	
	public static class CollectionParameterAssertion extends SimpleParameterAssertion {

		private final OpenapiType elementType;

		public CollectionParameterAssertion(String name, String in, boolean required, OpenapiType elementType) {
			super(name, in, OpenapiType.ARRAY, required, null);
			this.elementType = elementType;
		}
		
		@Override
		protected void _test(OpenapiParameter parameter) {
			super._test(parameter);
			
			OpenapiSchema items = parameter.getSchema().getItems();
			assertThat(items).isNotNull();
			assertThat(items.getType()).isEqualTo(elementType);
		}
		
	}
	
	public static class EnumParameterAssertion extends ParameterAssertion<OpenapiParameter> {

		public EnumParameterAssertion(String name, String in, boolean required, Object defaultValue) {
			super(name, in, required);
			this.defaultValue = defaultValue;
		}

		@Override
		protected void _test(OpenapiParameter parameter) {
			OpenapiSchema propertySchema = parameter.getSchema();
			
			// default value of the property
			assertThat(propertySchema.getDefault()).as(getName() + " - default").isEqualTo(defaultValue);
			
			// type of the schema
			assertThat(propertySchema.getType()).as(getName() + " - type").isEqualTo(OpenapiType.STRING);
		}
		
	}
	
	public static class SimpleParameterAssertion extends ParameterAssertion<OpenapiParameter> {
		private final OpenapiType type;
		
		public SimpleParameterAssertion(String name, String in, OpenapiType type, boolean required, Object defaultValue) {
			super(name, in, required);
			this.type = type;
			this.defaultValue = defaultValue;
		}
		
		@Override
		protected void _test(OpenapiParameter parameter) {
			OpenapiSchema schema = parameter.getSchema();
			if (schema.get$ref() != null) {
				 schema = getReferenced(schema);
				
			} else {
				assertThat(schema.getType()).isNotNull();
			}
			
			assertThat(schema.getDefault()).as(getName() + " - default").isEqualTo(defaultValue);
			assertThat(schema.getType()).as(getName() + " - type").isEqualTo(type);
		}
	}
	
	public interface DefaultEndpoints {
		EnumParameterAssertion identityManagementMode = new EnumParameterAssertion("endpoint.identityManagementMode", "query", false, "auto");
		EnumParameterAssertion prettiness = new EnumParameterAssertion("endpoint.prettiness", "query", false, "mid");
		EnumParameterAssertion typeExplicitness = new EnumParameterAssertion("endpoint.typeExplicitness", "query", false, "auto");
		SimpleParameterAssertion depth = new SimpleParameterAssertion("endpoint.depth", "query", OpenapiType.STRING, false, "3"); 
		SimpleParameterAssertion downloadResource = new SimpleParameterAssertion("endpoint.downloadResource", "query", OpenapiType.BOOLEAN, false, false); 
		SimpleParameterAssertion entityRecurrenceDepth = new SimpleParameterAssertion("endpoint.entityRecurrenceDepth", "query", OpenapiType.INTEGER, false, 0);
		SimpleParameterAssertion projection = new SimpleParameterAssertion("endpoint.projection", "query", OpenapiType.STRING, false, null);
		SimpleParameterAssertion responseContentType = new SimpleParameterAssertion("endpoint.responseContentType", "query",OpenapiType.STRING, false, null);
		SimpleParameterAssertion responseFilename = new SimpleParameterAssertion("endpoint.responseFilename", "query", OpenapiType.STRING, false, null);
		SimpleParameterAssertion saveLocally = new SimpleParameterAssertion("endpoint.saveLocally", "query", OpenapiType.BOOLEAN, false, false); 
		SimpleParameterAssertion stabilizeOrder = new SimpleParameterAssertion("endpoint.stabilizeOrder", "query", OpenapiType.BOOLEAN, false, false); 
		SimpleParameterAssertion inferResponseType = new SimpleParameterAssertion("endpoint.inferResponseType", "query", OpenapiType.BOOLEAN, false, false);
		SimpleParameterAssertion useSessionEvaluation = new SimpleParameterAssertion("endpoint.useSessionEvaluation", "query", OpenapiType.BOOLEAN, false, null);
		SimpleParameterAssertion writeAbsenceInformation = new SimpleParameterAssertion("endpoint.writeAbsenceInformation", "query", OpenapiType.BOOLEAN, false, false); 
		SimpleParameterAssertion writeEmptyProperties = new SimpleParameterAssertion("endpoint.writeEmptyProperties", "query", OpenapiType.BOOLEAN, false, false);
		
		List<ParameterAssertion<?>> all = Arrays.asList(
				depth, 
				downloadResource,
				entityRecurrenceDepth,
				identityManagementMode,
				inferResponseType,
				prettiness,
				projection,
				responseContentType,
				responseFilename,
				saveLocally,
				stabilizeOrder,
				typeExplicitness,
				useSessionEvaluation,
				writeAbsenceInformation,
				writeEmptyProperties);
		
		static List<ParameterAssertion<?>> switchDefault(Map<String,String> newDefaults, List<ParameterAssertion<?>> assertions) {
			List<ParameterAssertion<?>> switched = new ArrayList<>();
			assertions.forEach(a -> {
				ParameterAssertion<?> cloned;
				
				String paramName = a.getName();
				System.out.println(paramName);
				if (newDefaults.containsKey(paramName)) {
					cloned = a.clone(newDefaults.get(paramName));
				} else {
					cloned = a.clone();
				}
				
				switched.add(cloned);
			});
			
			return switched;
		}
	}
}
