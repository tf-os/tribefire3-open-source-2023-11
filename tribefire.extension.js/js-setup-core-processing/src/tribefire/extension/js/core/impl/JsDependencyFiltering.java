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
package tribefire.extension.js.core.impl;

import java.util.Optional;

import com.braintribe.model.artifact.Dependency;
import com.braintribe.model.artifact.Property;
import com.braintribe.model.artifact.Solution;

public interface JsDependencyFiltering {
	static boolean filter(Solution depender, Dependency dependency) {
		String scope = Optional.ofNullable(dependency.getScope()).orElse("compile");
		
		switch (scope) {
		case "provided":
		case "test":
			return false;
		default:
			break;
		}
		
		if (dependency.getOptional())
			return false;
		
		String type = Optional.ofNullable(dependency.getType()).orElse("jar");
		String classifier = dependency.getClassifier();
		
		if (classifier != null)
			return false;
		
		switch (type) {
		case "pom":
		case "jar":
			break;
		default:
			return false;
		}
		
		// js tagged if depender solution is jsinterop marked
		boolean jsinterop = false;
		
		for (Property property: depender.getProperties()) {
			if (property.getName().equals("jsinterop") && Boolean.TRUE.toString().equals(property.getValue())) {
				jsinterop = true;
				break;
			}
		}
		
		return !jsinterop || dependency.getTags().contains("js");
	}
}
