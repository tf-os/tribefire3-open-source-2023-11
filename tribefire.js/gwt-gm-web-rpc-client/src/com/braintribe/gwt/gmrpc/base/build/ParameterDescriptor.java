// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================


package com.braintribe.gwt.gmrpc.base.build;

public class ParameterDescriptor extends TypeDescriptor {
	private String parameterName;

	public ParameterDescriptor(String name, boolean simple, String gmSignature,
			String javaSignature) {
		super(simple, gmSignature, javaSignature);
		this.parameterName = name;
	}
	
	public String getParameterName() {
		return parameterName;
	}
}
