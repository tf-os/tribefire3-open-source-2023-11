// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================


package com.braintribe.gwt.genericmodel.build;

import static com.braintribe.utils.lcd.CollectionTools2.newList;

import java.io.InputStream;
import java.io.PrintWriter;
import java.util.List;
import java.util.Properties;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import com.google.gwt.core.ext.Generator;
import com.google.gwt.core.ext.GeneratorContext;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.TreeLogger.Type;
import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.core.ext.typeinfo.JClassType;
import com.google.gwt.core.ext.typeinfo.JMethod;
import com.google.gwt.core.ext.typeinfo.JParameterizedType;
import com.google.gwt.core.ext.typeinfo.TypeOracle;

public class JavascriptMemberReflectionGenerator extends Generator {

	private VelocityEngine velocityEngine;

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
			JClassType javascriptMemberReflectionType = typeOracle.getType(typeName);

			JClassType reflectedType = getReflectedType(logger, javascriptMemberReflectionType);

			String packageName = reflectedType.getPackage().getName();
			String memberReflectionImplName = reflectedType.getSimpleSourceName() + "_JsMemberReflection";

			PrintWriter printWriter = null;

			printWriter = context.tryCreate(logger, packageName, memberReflectionImplName);
			if (printWriter != null) {
				VelocityContext velocityContext = new VelocityContext();
				velocityContext.put("package", packageName);
				velocityContext.put("className", memberReflectionImplName);
				velocityContext.put("reflectedTypeName", reflectedType.getQualifiedSourceName());
				velocityContext.put("jsniMethodSignatures", jsniMethodSignaturesFor(reflectedType));

				VelocityEngine engine = getVelocityEngine();
				engine.mergeTemplate("/com/braintribe/gwt/genericmodel/build/JavascriptMemberReflection.java.vm", "ISO-8859-1", velocityContext, printWriter);
				context.commit(logger, printWriter);
			}

			return packageName + "." + memberReflectionImplName;

		} catch (UnableToCompleteException e) {
			throw e;

		} catch (Exception e) {
			UnableToCompleteException exception = new UnableToCompleteException();
			exception.initCause(e);
			throw exception;
		}
	}

	private List<String> jsniMethodSignaturesFor(JClassType reflectedType) {
		List<String> result = newList();

		for (JMethod method: reflectedType.getOverridableMethods()) {
			result.add(method.getJsniSignature());
		}

		return result;
	}

	protected JClassType getReflectedType(TreeLogger logger, JClassType javascriptMemberReflectionType) throws UnableToCompleteException {
		JClassType reflectedInterfaceType = javascriptMemberReflectionType.getImplementedInterfaces()[0];
		JParameterizedType parameterizedType = reflectedInterfaceType.isParameterized();

		if (parameterizedType != null) {
			JClassType typeArg = parameterizedType.getTypeArgs()[0];

			if (typeArg.isInterface() != null) {
				return typeArg;
			} else {
				logger.log(Type.ERROR,
						"the following type has an invalid type parameter (must be an interface): " + typeArg.getQualifiedSourceName());
				throw new UnableToCompleteException();
			}
		} else {
			logger.log(
					Type.ERROR,
					"the following type is not a valid GmRpcService interface derivation " +
							reflectedInterfaceType.getQualifiedSourceName());
			throw new UnableToCompleteException();
		}
	}

}
