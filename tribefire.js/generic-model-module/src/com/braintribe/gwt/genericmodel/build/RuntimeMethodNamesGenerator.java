// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.gwt.genericmodel.build;

import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import com.braintribe.gwt.genericmodel.client.itw.MethodIdentification;
import com.google.gwt.core.ext.Generator;
import com.google.gwt.core.ext.GeneratorContext;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.core.ext.typeinfo.JClassType;
import com.google.gwt.core.ext.typeinfo.JMethod;
import com.google.gwt.core.ext.typeinfo.JPrimitiveType;
import com.google.gwt.core.ext.typeinfo.JType;
import com.google.gwt.core.ext.typeinfo.NotFoundException;
import com.google.gwt.core.ext.typeinfo.TypeOracle;

public class RuntimeMethodNamesGenerator extends Generator {

	private VelocityEngine velocityEngine;
	private JClassType stringType;

	public VelocityEngine getVelocityEngine() throws Exception {
		if (velocityEngine == null) {
			Properties velocityProperties = new Properties();
			InputStream in = getClass().getResourceAsStream("velocity.properties");

			try {
				velocityProperties.load(in);
			} finally {
				in.close();
			}

			VelocityEngine engine = new VelocityEngine();
			engine.init(velocityProperties);

			velocityEngine = new VelocityEngine(velocityProperties);
		}

		return velocityEngine;
	}

	@Override
	public String generate(TreeLogger logger, GeneratorContext context, String typeName) throws UnableToCompleteException {

		try {
			TypeOracle typeOracle = context.getTypeOracle();
			JClassType rmnClass = typeOracle.getType(typeName);

			this.stringType = typeOracle.getType(String.class.getName());

			String packageName = rmnClass.getPackage().getName();
			String className = "RuntimeMethodNamesImpl";

			List<MethodDesc> methodDescs = getMethodDescs(rmnClass, context);
			
			PrintWriter printWriter = context.tryCreate(logger, packageName, className);
			if (printWriter != null) {
				VelocityContext velocityContext = new VelocityContext();
				velocityContext.put("package", packageName);
				velocityContext.put("className", className);
				velocityContext.put("methodDescs", methodDescs);
				velocityContext.put("scriptMode", context.isProdMode());

				VelocityEngine engine = getVelocityEngine();
				engine.mergeTemplate("/com/braintribe/gwt/genericmodel/build/RuntimeMethodNames.java.vm", "ISO-8859-1", velocityContext,
						printWriter);
				context.commit(logger, printWriter);
			}

			return packageName + "." + className;

		} catch (UnableToCompleteException e) {
			throw e;

		} catch (Exception e) {
			UnableToCompleteException exception = new UnableToCompleteException();
			exception.initCause(e);
			throw exception;
		}
	}

	private List<MethodDesc> getMethodDescs(JClassType rmnClass, GeneratorContext context) throws NotFoundException {
		List<MethodDesc> result = new ArrayList<MethodDesc>();

		for (JMethod m: rmnClass.getMethods()) {
			MethodIdentification mi = m.getAnnotation(MethodIdentification.class);
			
			validateMethod(m, mi);

			MethodDesc md = new MethodDesc();
			md.methodName = m.getName();
			md.declarationClassName = mi.declarationClass().getName();
			md.declaredMethodName = mi.name();
			md.declaredMethodParamsJsniSignature = createParamSignatures(mi.parameterTypes(), context);
			
			result.add(md);
		}

		return result;
	}

	private void validateMethod(JMethod m, MethodIdentification mi) {
		if (m.getReturnType() != stringType || m.getParameters().length > 0) {
			throw new RuntimeException("Unsupportade method signature of method: " + m);
		}

		if (mi == null) {
			throw new RuntimeException("Missing 'MethodIndentification' annotation of method: " + m);
		}
	}

	private String createParamSignatures(Class<?>[] parameterTypes, GeneratorContext context) throws NotFoundException {
		TypeOracle typeOracle = context.getTypeOracle();

		StringBuilder sb = new StringBuilder();

		for (Class<?> clazz: parameterTypes) {
			JType jType;

			if (clazz.isPrimitive()) {
				jType = JPrimitiveType.parse(clazz.getName());
			} else {
				jType = typeOracle.getType(clazz.getName());
			}

			sb.append(jType.getJNISignature());
		}

		return sb.toString();
	}

	public static class MethodDesc {
		public String declaredMethodParamsJsniSignature;
		public String declaredMethodName;
		public String declarationClassName;
		public String methodName;

		public String getDeclaredMethodParamsJsniSignature() {
			return declaredMethodParamsJsniSignature;
		}

		public String getDeclaredMethodName() {
			return declaredMethodName;
		}

		public String getDeclarationClassName() {
			return declarationClassName;
		}

		public String getMethodName() {
			return methodName;
		}
	}

}
