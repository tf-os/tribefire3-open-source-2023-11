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
package com.braintribe.model.processing.xmi.converter.wire.space;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Stream;

import com.braintribe.model.processing.xmi.converter.XmiToMetaModelCodec;
import com.braintribe.model.processing.xmi.converter.experts.ClasspathResourceStringProvider;
import com.braintribe.model.processing.xmi.converter.wire.contract.XmiConverterContract;
import com.braintribe.web.velocity.renderer.VelocityTemplateRenderer;
import com.braintribe.wire.api.annotation.Managed;

/**
 * @author pit
 *
 */
@Managed
public class XmiConverterSpace implements XmiConverterContract {
	
	@Managed(com.braintribe.wire.api.annotation.Scope.prototype)
	private Supplier<String> templateSupplier(String key) {
		ClasspathResourceStringProvider bean = new ClasspathResourceStringProvider(key);
		return bean;
	}

	@Managed
	private VelocityTemplateRenderer renderer() {
		VelocityTemplateRenderer bean = new VelocityTemplateRenderer();
		Map<String, Supplier<String>> map = new HashMap<>();
		
		Stream.of(
			"main",
			"model",
			"package",
			"class",
			"enum",
			"attribute",
			"association",
			"associationClass",
			"generalization",
			"tagReference",
			"stereotype",
			"tagDefinition",
			"simpletypes",
			"namespaceOwned",
			"node"
		).forEach(n -> map.put(n, templateSupplier("templates/" + n + ".template.vm")));
		
		bean.setKeyToProviderMap( map);
		return bean;
	}
	
	@Managed
	public XmiToMetaModelCodec xmiConverter() {
		XmiToMetaModelCodec bean = new XmiToMetaModelCodec();
		bean.setRenderer(renderer());
		return bean;
	}
	
}
