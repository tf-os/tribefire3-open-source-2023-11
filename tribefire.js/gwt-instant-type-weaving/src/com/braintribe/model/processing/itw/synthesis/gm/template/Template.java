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
package com.braintribe.model.processing.itw.synthesis.gm.template;

import static com.braintribe.utils.lcd.CollectionTools2.last;
import static com.braintribe.utils.lcd.CollectionTools2.newList;

import java.util.List;
import java.util.Map;

import com.braintribe.asm.MethodVisitor;
import com.braintribe.model.processing.itw.synthesis.gm.GenericModelTypeSynthesis;
import com.braintribe.model.processing.itw.synthesis.gm.experts.template.MagicVariableNames;
import com.braintribe.model.processing.itw.synthesis.gm.experts.template.MagicVariableNames.MagicVarKind;
import com.braintribe.model.processing.itw.synthesis.java.JavaTypeSynthesisException;
import com.braintribe.model.weaving.ProtoGmEntityType;
import com.braintribe.model.weaving.ProtoGmProperty;
import com.braintribe.model.weaving.ProtoGmType;
import com.braintribe.model.weaving.info.ProtoGmPropertyInfo;

public class Template {

	private final TemplateNode rootNode;

	public Template(TemplateNode rootNode) {
		this.rootNode = rootNode;
	}

	public void merge(MethodVisitor mv, VariableResolver variableResolver) {
		rootNode.merge(mv, variableResolver);
	}

	public static Template parse(GenericModelTypeSynthesis gmts, ProtoGmEntityType gmEntityType, String template) {
		Sequence sequence = new Sequence();

		int index = 0;

		while (index < template.length()) {
			int start = findNextPropertyStart(template, index);
			if (start != -1) {
				sequence.add(new StaticText(template.substring(index, start).replace("$${", "${")));
				index = start;
				int end = template.indexOf('}', start);
				if (end != -1) {
					String variableOrPropertyChain = template.substring(start + 2, end);

					sequence.add(toTemplateNode(gmts, gmEntityType, variableOrPropertyChain));
					index = end + 1;
				} else {
					sequence.add(new StaticText(template.substring(index).replace("$${", "${")));
					break;
				}
			} else {
				// end here
				sequence.add(new StaticText(template.substring(index).replace("$${", "${")));
				break;
			}
		}

		return new Template(sequence);
	}

	protected static int findNextPropertyStart(String str, int start) {
		int index = start;
		while (index < str.length()) {
			index = str.indexOf('$', index);
			if (index == -1)
				return -1;
			int previousIndex = index - 1;
			int nextIndex = index + 1;
			if ((previousIndex < start || str.charAt(previousIndex) != '$') && nextIndex < str.length() && str.charAt(nextIndex) == '{') {

				return index;
			}
			index++;
		}

		return -1;
	}

	private static TemplateNode toTemplateNode(GenericModelTypeSynthesis gmts, ProtoGmEntityType gmEntityType, String variable) {
		if (isMagicVariable(variable))
			return new Variable(variable, null);

		List<ProtoGmProperty> propertyChain = resolveNonOverlayProperties(gmts, gmEntityType, variable);
		if (propertyChain != null)
			return new Variable(variable, propertyChain);

		return new StaticText("${" + variable + "}");
	}

	private static List<ProtoGmProperty> resolveNonOverlayProperties(GenericModelTypeSynthesis gmts, ProtoGmEntityType gmEntityType,
			String variable) {
		String propertyNameChain[] = variable.split("\\.");
		ProtoGmType propertyType = gmEntityType; // type of 'this' - our property chain is this.propX.propY...

		List<ProtoGmProperty> result = newList();

		for (String propertyName : propertyNameChain) {
			ProtoGmProperty nonOverlayProperty = resolveNonOverlayProperty(gmts, (ProtoGmEntityType) propertyType, propertyName);
			if (nonOverlayProperty == null)
				return null;

			propertyType = nonOverlayProperty.getType();

			result.add(nonOverlayProperty);
		}

		return result;
	}

	private static ProtoGmProperty resolveNonOverlayProperty(GenericModelTypeSynthesis gmts, ProtoGmEntityType ownerType, String propertyName) {
		Map<String, ProtoGmPropertyInfo[]> mergedProtoGmProperties = mergedPropertiesFor(gmts, ownerType);
		ProtoGmPropertyInfo[] propertyLineage = mergedProtoGmProperties.get(propertyName);

		return propertyLineage == null ? null : (ProtoGmProperty) last(propertyLineage);
	}

	private static Map<String, ProtoGmPropertyInfo[]> mergedPropertiesFor(GenericModelTypeSynthesis gmts, ProtoGmType propertyType) {
		try {
			return gmts.getMergedPropertiesFromTypeHierarchy((ProtoGmEntityType) propertyType);

		} catch (JavaTypeSynthesisException e) {
			throw new RuntimeException("Error while getting merged properties for: " + propertyType.getTypeSignature(), e);
		}
	}

	private static boolean isMagicVariable(String variable) {
		return MagicVariableNames.getMagicVarKind(variable) != MagicVarKind.none;
	}

}
