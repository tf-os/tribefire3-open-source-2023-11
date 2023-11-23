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
package com.braintribe.model.processing.xmi.converter;

/**
 * @author pit
 *
 */
public class Tokens {
	public static final String TOKEN_STEREOTYPE_IDPROPERTY = "IdProperty";
	public static final String TOKEN_STEREOTYPE_ROOT_TYPE = "RootType";
	public static final String TOKEN_STEREOTYPE_PLAIN = "Plain";
	public static final String TOKEN_STEREOTYPE_DISCARD = "Discard";
	public static final String TOKEN_STEREOTYPE_NONNULLABLE = "NonNullable";

	public static final String TOKEN_SUPPORTEDVERSION = "1.2";

	public static final String TOKEN_GE_TYPESIGNATURE = "com.braintribe.model.generic.GenericEntity";

	public static final String TOKEN_PATH_MODEL = "XMI.content/.*Model";

	public static final String TOKEN_TAG_ARTIFACTBINDING = "DeclaringModel";
	public static final String TOKEN_TAG_MODEL_DEPENDENCIES = "ModelDependencies";
	
	public static final String TOKEN_TAG_DOCUMENTATION_SEE = "see";
	public static final String TOKEN_TAG_DOCUMENTATION_SINCE = "since";
	public static final String TOKEN_TAG_DOCUMENTATION = "documentation";
	public static final String TOKEN_TAG_DEPRECATED = "deprecated";
	

	public static final String TOKEN_TAG_GENERALIZATION_PRIORITY = "GeneralizationPriority";
	public static final String TOKEN_TAG_GLOBALID = "GlobalId";

}
