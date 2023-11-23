//// ============================================================================
//// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
//// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
//// It is strictly forbidden to copy, modify, distribute or use this code without written permission
//// To this file the Braintribe License Agreement applies.
//// ============================================================================
//
//
//package com.braintribe.gwt.gmrpc.base.build;
//
//import java.io.InputStream;
//import java.io.PrintWriter;
//import java.math.BigDecimal;
//import java.util.ArrayList;
//import java.util.Date;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.Properties;
//import java.util.Set;
//
//import org.apache.velocity.VelocityContext;
//import org.apache.velocity.app.VelocityEngine;
//
//import com.braintribe.model.generic.GenericEntity;
//import com.google.gwt.core.ext.Generator;
//import com.google.gwt.core.ext.GeneratorContext;
//import com.google.gwt.core.ext.TreeLogger;
//import com.google.gwt.core.ext.TreeLogger.Type;
//import com.google.gwt.core.ext.UnableToCompleteException;
//import com.google.gwt.core.ext.typeinfo.JClassType;
//import com.google.gwt.core.ext.typeinfo.JConstructor;
//import com.google.gwt.core.ext.typeinfo.JMethod;
//import com.google.gwt.core.ext.typeinfo.JParameter;
//import com.google.gwt.core.ext.typeinfo.JParameterizedType;
//import com.google.gwt.core.ext.typeinfo.JType;
//import com.google.gwt.core.ext.typeinfo.NotFoundException;
//import com.google.gwt.core.ext.typeinfo.TypeOracle;
//
//public class GmRpcServiceGenerator extends Generator {
//	
//	private static Map<String, String> typeSignatureTranslation = new HashMap<>() ;
//	private static Map<String, String> collectionTypeSignatureTranslation = new HashMap<>() ;
//	
//	static {
//		// base type
//		registerTypeSignatureTranslation(Object.class, "object");
//		
//		// primitive based simple types
//		registerTypeSignatureTranslation(boolean.class, "boolean");
//		registerTypeSignatureTranslation(int.class, "integer");
//		registerTypeSignatureTranslation(long.class, "long");
//		registerTypeSignatureTranslation(float.class, "float");
//		registerTypeSignatureTranslation(double.class, "double");
//		
//		// object based simple types
//		registerTypeSignatureTranslation(Boolean.class, "boolean");
//		registerTypeSignatureTranslation(Integer.class, "integer");
//		registerTypeSignatureTranslation(Long.class, "long");
//		registerTypeSignatureTranslation(Float.class, "float");
//		registerTypeSignatureTranslation(Short.class, "short");
//		registerTypeSignatureTranslation(Double.class, "double");
//		registerTypeSignatureTranslation(String.class, "string");
//		registerTypeSignatureTranslation(Date.class, "date");
//		registerTypeSignatureTranslation(BigDecimal.class, "decimal");
//		
//		// collection types
//		registerCollectionTypeSignatureTranslation(List.class, "list");
//		registerCollectionTypeSignatureTranslation(Set.class, "set");
//		registerCollectionTypeSignatureTranslation(Map.class, "map");
//	}
//	
//	static void registerTypeSignatureTranslation(Class<?> clazz, String gmTypeSignature) {
//		typeSignatureTranslation.put(clazz.getCanonicalName(), gmTypeSignature);
//		
//	}
//	
//	static void registerCollectionTypeSignatureTranslation(Class<?> clazz, String gmTypeSignature) {
//		collectionTypeSignatureTranslation.put(clazz.getCanonicalName(), gmTypeSignature);
//		
//	}
//	
//	private VelocityEngine velocityEngine;
//	
//	public VelocityEngine getVelocityEngine() throws Exception {
//		if (velocityEngine == null) {
//			Properties velocityProperties = new Properties();
//			InputStream in = getClass().getResourceAsStream("velocity.properties");
//			
//			try {
//				velocityProperties.load(in);
//			}
//			finally {
//				in.close();
//			}
//			
//			VelocityEngine engine = new VelocityEngine();
//			engine.init(velocityProperties);
//
//			velocityEngine = new VelocityEngine(velocityProperties);
//		}
//
//		return velocityEngine;
//	}
//
//	
//	@Override
//	public String generate(TreeLogger logger, GeneratorContext context,
//			String typeName) throws UnableToCompleteException {
//		
//		try {
//			EssentialTypesContext essentialTypesContext;
//			try {
//				essentialTypesContext = new EssentialTypesContext(context.getTypeOracle());
//			} catch (NotFoundException e) {
//				logger.log(Type.ERROR, "could not find essential GM types");
//				UnableToCompleteException ex = new UnableToCompleteException();
//				ex.initCause(e);
//				throw ex;
//			}
//			
//			TypeOracle typeOracle = context.getTypeOracle();
//			JClassType gmRpcServiceType = typeOracle.getType(typeName);
//			
//			JClassType serviceType = getServiceInterface(logger, gmRpcServiceType);
//			
//			String packageName = serviceType.getPackage().getName();
//			String gmRpcServiceClassName = serviceType.getSimpleSourceName() + "_" + GmRpcService.class.getSimpleName();
//			String serviceProxyClassName = serviceType.getSimpleSourceName() + "_" + "Proxy";
//			String serviceClassName = serviceType.getQualifiedSourceName();
//			
//			PrintWriter printWriter = null;
//	        
//        	printWriter = context.tryCreate(logger, packageName, serviceProxyClassName); 
//	        if (printWriter != null) {
//	        	List<ServiceMethodDescriptor> methodDescriptors = getMethodDescriptors(logger, essentialTypesContext, serviceType);
//	        	List<ExceptionDescriptor> exceptions = getExceptionDescriptors(methodDescriptors, essentialTypesContext);
//		
//				VelocityContext velocityContext = new VelocityContext();
//				velocityContext.put("package", packageName);
//				velocityContext.put("className", serviceProxyClassName);
//				velocityContext.put("serviceInterfaceClassName", serviceType.getSimpleSourceName());
//				velocityContext.put("methods", methodDescriptors);
//				velocityContext.put("exceptions", exceptions);
//		
//				VelocityEngine engine = getVelocityEngine();
//		    	engine.mergeTemplate("/com/braintribe/gwt/gmrpc/base/build/ServiceProxy.java.vm", "ISO-8859-1", velocityContext, printWriter);
//		    	context.commit(logger, printWriter);
//	        }
//	        
//	        printWriter = context.tryCreate(logger, packageName, gmRpcServiceClassName); 
//	        if (printWriter != null) {
//	        	
//	        	
//	        	VelocityContext velocityContext = new VelocityContext();
//	        	velocityContext.put("package", packageName);
//	        	velocityContext.put("className", gmRpcServiceClassName);
//	        	velocityContext.put("serviceProxyClassName", serviceProxyClassName);
//	        	velocityContext.put("serviceClassName", serviceClassName);
//	        	
//	        	VelocityEngine engine = getVelocityEngine();
//	        	engine.mergeTemplate("/com/braintribe/gwt/gmrpc/base/build/GmRpcService.java.vm", "ISO-8859-1", velocityContext, printWriter);
//	        	context.commit(logger, printWriter);
//	        }	
//			
//			
//			return packageName + "." + gmRpcServiceClassName;
//	    }
//		catch (UnableToCompleteException e) {
//			throw e;
//		}
//		catch (Exception e) {
//			UnableToCompleteException exception = new UnableToCompleteException();
//			exception.initCause(e);
//			throw exception;
//		}
//	}
//	
//	private List<ExceptionDescriptor> getExceptionDescriptors(List<ServiceMethodDescriptor> methodDescriptors, EssentialTypesContext essentialTypesContext) {
//		List<ExceptionDescriptor> exceptionDescriptors = new ArrayList<>();
//		
//		for (ServiceMethodDescriptor methodDescriptor: methodDescriptors) {
//			JMethod method = methodDescriptor.getMethod();
//			for (JClassType exceptionType: method.getThrows()) {
//				if (exceptionType.isAbstract())
//					continue;
//				
//				// search constructor with message parameter
//				JConstructor constructor = exceptionType.findConstructor(new JType[]{essentialTypesContext.stringType});
//				
//				if (constructor != null && constructor.isPublic()) {
//					ExceptionDescriptor exceptionDescriptor = new ExceptionDescriptor(exceptionType.getQualifiedSourceName(), true);
//					exceptionDescriptors.add(exceptionDescriptor);
//					continue;
//				}
//
//				// fallback search for constructor with no parameter
//				constructor = exceptionType.findConstructor(new JType[]{});
//				
//				if (constructor != null && constructor.isPublic()) {
//					ExceptionDescriptor exceptionDescriptor = new ExceptionDescriptor(exceptionType.getQualifiedSourceName(), false);
//					exceptionDescriptors.add(exceptionDescriptor);
//					continue;
//				}
//			}
//		}
//		
//		return exceptionDescriptors;
//	}
//
//	private static class Counter {
//		public int count = 0;
//	}
//        
//    protected List<ServiceMethodDescriptor> getMethodDescriptors(TreeLogger logger, EssentialTypesContext context, JClassType serviceType) throws UnableToCompleteException {
//    	Map<String, Counter> methodNameMap = new HashMap<>();
//    	List<ServiceMethodDescriptor> serviceMethodDescriptors = new ArrayList<>();
//    	for (JMethod method: serviceType.getInheritableMethods()) {
//			if (method.isPublic()) {
//				String methodName = method.getName();
//				
//				Counter counter = methodNameMap.get(methodName); 
//				
//				if (counter == null) {
//					counter = new Counter();
//					methodNameMap.put(methodName, counter);
//				}
//				else {
//					counter.count++;
//				}
//				
//				JParameter parameters[] = method.getParameters();
//				List<ParameterDescriptor> parameterDescriptors = new ArrayList<>(parameters.length);
//				
//				for (JParameter parameter: parameters) {
//					parameterDescriptors.add(getParameterDescriptor(logger, context, parameter));
//				}
//				
//				String uniqueMethodName = methodName;
//				if (counter.count > 0) 
//					uniqueMethodName += counter.count;
//						
//				
//				TypeDescriptor typeDescriptor = getTypeDescriptor(logger, context, method.getReturnType());
//				
//				ServiceMethodDescriptor serviceMethodDescriptor = new ServiceMethodDescriptor(method, uniqueMethodName, typeDescriptor, parameterDescriptors);
//				serviceMethodDescriptors.add(serviceMethodDescriptor);
//			}
//    	}
//    	
//    	return serviceMethodDescriptors;
//    }
//    
//    protected ParameterDescriptor getParameterDescriptor(TreeLogger logger, EssentialTypesContext context, JParameter parameter) throws UnableToCompleteException {
//    	JType type = parameter.getType();
//    	
//    	ParameterDescriptor pd = new ParameterDescriptor(
//    			parameter.getName(), 
//    			type.isPrimitive() != null, 
//    			getGmTypeSignature(logger, context, type, false), 
//    			type.getParameterizedQualifiedSourceName());
//    	
//    	return pd;
//    }
//    
//    protected TypeDescriptor getTypeDescriptor(TreeLogger logger, EssentialTypesContext context, JType type) throws UnableToCompleteException {
//    	TypeDescriptor td = new TypeDescriptor(
//    			type.isPrimitive() != null, 
//    			type.getQualifiedSourceName().equals("void")? null: getGmTypeSignature(logger, context, type, false), 
//    			type.getParameterizedQualifiedSourceName());
//    	
//    	return td;
//    }
//        
//    protected JClassType getServiceInterface(TreeLogger logger, JClassType gmRpcServiceType) throws UnableToCompleteException {
//    	gmRpcServiceType = gmRpcServiceType.getImplementedInterfaces()[0];
//    	JParameterizedType parameterizedType = gmRpcServiceType.isParameterized();
//    	
//    	if (parameterizedType != null) {
//    		JClassType typeArg = parameterizedType.getTypeArgs()[0];
//    		
//    		if (typeArg.isInterface() != null) {
//    			return typeArg;
//    		}
//    		else {
//    			logger.log(Type.ERROR, "the following type has an invalid type parameter (must be an interface): " + typeArg.getQualifiedSourceName());
//    			throw new UnableToCompleteException();
//    		}
//    	}
//    	else {
//    		logger.log(Type.ERROR, "the following type is not a valid GmRpcService interface derivation " + gmRpcServiceType.getQualifiedSourceName());
//    		throw new UnableToCompleteException();
//    	}
//    }
//	
//	protected String getGmTypeSignature(TreeLogger logger, EssentialTypesContext context, JType type, boolean nested) throws UnableToCompleteException {
//		// check for GE or enum
//		if (type.isClassOrInterface() != null && (context.genericEntityType.isAssignableFrom(type.isClassOrInterface()) || type.isEnum() != null)) {
//			return type.getQualifiedSourceName();
//		}
//		// check for collection types
//		else if (type.isParameterized() != null) {
//			if (nested) {
//				logger.log(Type.ERROR, "invalid nested collection type used in service method signature: " + type);
//				throw new UnableToCompleteException();
//			}
//			JParameterizedType parameterizedType = type.isParameterized();
//			
//			JClassType rawType = parameterizedType.getRawType();
//			
//			String rawTypeSignature = collectionTypeSignatureTranslation.get(rawType.getQualifiedSourceName());
//			
//			if (rawTypeSignature == null) {
//				logger.log(Type.ERROR, "invalid type used in service method signature: " + type);
//				throw new UnableToCompleteException();
//			}
//				
//			StringBuilder builder = new StringBuilder();
//			builder.append(rawTypeSignature);
//			builder.append('<');
//			JClassType typeArgs[] = parameterizedType.getTypeArgs();
//			for (int i = 0; i <typeArgs.length; i++) {
//				JClassType parameterClassType = typeArgs[i];
//				String signature = getGmTypeSignature(logger, context, parameterClassType, true);
//				if (i > 0)
//					builder.append(',');
//				
//				builder.append(signature);
//			}
//			
//			builder.append('>');
//			
//			return builder.toString();
//		}
//		else {
//			String signature = typeSignatureTranslation.get(type.getQualifiedSourceName());
//			
//			if (signature != null)
//				return signature;
//			
//			logger.log(Type.ERROR, "invalid type used in service method signature: " + type);
//			throw new UnableToCompleteException();
//		}
//	}
//
//	public static class EssentialTypesContext {
//		
//		public JClassType genericEntityType;
//		public JClassType listType;
//		public JClassType setType;
//		public JClassType mapType;
//		public JClassType stringType;
//		public TypeOracle typeOracle;
//		
//		public EssentialTypesContext(TypeOracle typeOracle) throws NotFoundException {
//			genericEntityType = typeOracle.getType(GenericEntity.class.getName());
//			listType = typeOracle.getType(List.class.getName());
//			setType = typeOracle.getType(Set.class.getName());
//			mapType = typeOracle.getType(Map.class.getName());
//			stringType = typeOracle.getType(String.class.getName());
//			this.typeOracle = typeOracle;
//		}
//	}
//}
