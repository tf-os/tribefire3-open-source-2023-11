// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================


package com.braintribe.gwt.gmrpc.base.build;

public class TypeDescriptor {
	private boolean primitive;
	private String gmSignature;
	private String javaSignature;
	
	public TypeDescriptor(boolean simple, String gmSignature,
			String javaSignature) {
		super();
		this.primitive = simple;
		this.gmSignature = gmSignature;
		this.javaSignature = javaSignature;
	}

	public boolean isPrimitive() {
		return primitive;
	}

	public String getGmSignature() {
		return gmSignature;
	}

	public String getJavaSignature() {
		return javaSignature;
	}
}
