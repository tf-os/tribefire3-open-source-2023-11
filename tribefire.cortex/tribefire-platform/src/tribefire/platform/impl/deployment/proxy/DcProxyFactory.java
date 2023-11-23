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
package tribefire.platform.impl.deployment.proxy;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.braintribe.asm.ClassWriter;
import com.braintribe.asm.FieldVisitor;
import com.braintribe.asm.MethodVisitor;
import com.braintribe.asm.Opcodes;
import com.braintribe.asm.Type;
import com.braintribe.exception.UncheckedReflectiveOperationException;
import com.braintribe.model.deployment.Deployable;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.processing.deployment.api.ConfigurableDcProxyDelegation;
import com.braintribe.model.processing.deployment.api.DcProxy;
import com.braintribe.model.processing.deployment.api.DeployRegistry;
import com.braintribe.model.processing.deployment.api.DeploymentException;
import com.braintribe.model.service.api.InstanceId;
import com.braintribe.utils.ReflectionTools;

/**
 * @author dirk.scheffler
 */
public class DcProxyFactory implements Opcodes {

	private static class ProxyKey extends HashSet<Class<?>> {
		private static final long serialVersionUID = -1139692114689364283L;
		public ProxyKey(Collection<Class<?>> interfaces) {
			super(interfaces);
		}
	}
	
	private static Map<ProxyKey, MethodHandle> proxyFactories = new ConcurrentHashMap<>();
	private static final Type dcProxyDelegationType = Type.getType(DcProxyDelegationImpl.class);
	private static final Type configurableDelegateManagerType = Type.getType(ConfigurableDcProxyDelegation.class);
	private static Map<Class<?>, Integer> primitiveReturnOpcodes = new IdentityHashMap<>();
	private static Map<Class<?>, Integer> primitiveLoads = new IdentityHashMap<>();

	private static final String DELEGATE_MANAGER_FIELD = "delegateManager";
	
	static {
		primitiveReturnOpcodes.put(void.class, RETURN);
		primitiveReturnOpcodes.put(byte.class, IRETURN);
		primitiveReturnOpcodes.put(short.class, IRETURN);
		primitiveReturnOpcodes.put(int.class, IRETURN);
		primitiveReturnOpcodes.put(long.class, LRETURN);
		primitiveReturnOpcodes.put(float.class, FRETURN);
		primitiveReturnOpcodes.put(double.class, DRETURN);
		primitiveReturnOpcodes.put(boolean.class, IRETURN);
		primitiveReturnOpcodes.put(char.class, IRETURN);
		
		primitiveLoads.put(byte.class, ILOAD);
		primitiveLoads.put(short.class, ILOAD);
		primitiveLoads.put(int.class, ILOAD);
		primitiveLoads.put(long.class, LLOAD);
		primitiveLoads.put(float.class, FLOAD);
		primitiveLoads.put(double.class, DLOAD);
		primitiveLoads.put(boolean.class, ILOAD);
		primitiveLoads.put(char.class, ILOAD);
	}
	
	private static String delimitJniName(String name) {
		return "L" + name + ";";
	}

	private static String jniName(Class<?> clazz) {
		return clazz.getName().replace('.', '/');
	}

	public static DcProxy forInterfaces(Class<?>[] interfaces, String externalId, EntityType<? extends Deployable> componentType,
			DeployRegistry deployRegistry, InstanceId processingInstanceId) {
		return forInterfaces(Arrays.asList(interfaces), externalId, componentType, deployRegistry, processingInstanceId);
	}
	
	public static DcProxy forInterfaces(Collection<Class<?>> interfaces, String externalId, EntityType<? extends Deployable> componentType,
			DeployRegistry deployRegistry, InstanceId processingInstanceId) {

		requireNonNull(interfaces, "interfaces must not be null");
		requireNonNull(externalId, "externalId must not be null");
		requireNonNull(componentType, "componentType must not be null");
		requireNonNull(processingInstanceId, "processingInstanceId must not be null");
		requireNonNull(deployRegistry, "deployRegistry must not be null");
		
		try {
			MethodHandle proxyFactory = null;
			
			ProxyKey key = new ProxyKey(interfaces);
			proxyFactory = proxyFactories.computeIfAbsent(key, k -> buildProxyFactory(k, componentType));
			
			DcProxy proxy = (DcProxy) proxyFactory.invoke();
			DcProxyDelegationImpl delegateManager = (DcProxyDelegationImpl) DcProxy.getConfigurableDelegateManager(proxy);  
			delegateManager.setComponentType(componentType);
			delegateManager.setExternalId(externalId);
			delegateManager.setDeployRegistry(deployRegistry);
			delegateManager.setProcessingInstanceId(processingInstanceId);
			delegateManager.postConstruct();

			return proxy;
		} catch (Throwable e) {
			throw new DeploymentException("Error while creating eager component  proxy for interface interfaces " + interfaces
					+ " and externalId [" + externalId + "] and componentType [" + componentType.getTypeSignature() + "]"
					+ (e.getMessage() != null ? ": " + e.getMessage() : ""), e);
		}
	}

	private static MethodHandle buildProxyFactory(Collection<Class<?>> interfaces, EntityType<? extends Deployable> componentType) {
		Class<?> superClass = null;
		for (Class<?> interfaceClass: interfaces) {
			requireNonNull(interfaceClass, () -> "One of given interfaces is null. Component type: " + componentType.getTypeSignature());

			if (!interfaceClass.isInterface())
				if (superClass == null)
					superClass = interfaceClass;
				else
					throw new IllegalArgumentException("Multiple classes found among interfaces of component type: "
							+ componentType.getTypeSignature() + ". Interfaces: " + interfaces);
		}

		if (superClass != null)
			verifyHasNoArgConstructor(superClass);
		else
			superClass = Object.class;

		try {
			MethodHandle proxyFactory;
			Class<?> proxyClass = buildProxyClass(superClass, interfaces);
			proxyFactory = MethodHandles.lookup().unreflectConstructor(proxyClass.getConstructor());
			
			return proxyFactory;
		} catch (ReflectiveOperationException e) {
			throw new UncheckedReflectiveOperationException("error while building proxy factory", e);
		}
	}
	
	private static void verifyHasNoArgConstructor(Class<?> clazz) {
		try {
			clazz.getConstructor();
		} catch (NoSuchMethodException e) {
			throw new IllegalArgumentException("Cannot build proxy as class does not have a public zero argument constructor: " + clazz.getName());
		}
	}

	public static <I> I forInterface(Class<I> interfaceClass, String externalId, EntityType<? extends Deployable> componentKind, DeployRegistry deployRegistry, InstanceId instanceId) {
		return (I)forInterfaces(Collections.singleton(interfaceClass), externalId, componentKind, deployRegistry, instanceId);
	}

	private static Class<?> buildProxyClass(Class<?> superClass, Collection<Class<?>> interfaces) {
		ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
		MethodVisitor mv;
		FieldVisitor fv;
		
		String simpleName = interfaces.stream() //
				.map(Class::getSimpleName) //
				.sorted() //
				.collect(Collectors.joining("-")) //
				+ "-" + UUID.randomUUID().toString();

		String className = "tribefire.proxy.deploy." + simpleName;
		String classInternalName = "tribefire/proxy/deploy/" + simpleName;
		
		String[] ifaceNames = Stream.concat(interfaces.stream(), Stream.of(DcProxy.class)) //
				.filter(Class::isInterface) //
				.map(DcProxyFactory::jniName) //
				.toArray(String[]::new);
		
		cw.visit(V1_7, ACC_PUBLIC + ACC_SUPER, classInternalName, null, Type.getType(superClass).getInternalName() , ifaceNames);

		{
			mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
			mv.visitCode();
			mv.visitVarInsn(ALOAD, 0);
			mv.visitMethodInsn(INVOKESPECIAL, Type.getType(superClass).getInternalName(), "<init>", "()V", false);
			
			// proxy = new DeploymentProxyDelegateManager();
			mv.visitVarInsn(ALOAD, 0);
			mv.visitTypeInsn(NEW, dcProxyDelegationType.getInternalName());
			mv.visitInsn(DUP);
			mv.visitMethodInsn(INVOKESPECIAL, dcProxyDelegationType.getInternalName(), "<init>", "()V", false);
			mv.visitFieldInsn(PUTFIELD, classInternalName, DELEGATE_MANAGER_FIELD, dcProxyDelegationType.getDescriptor());

			mv.visitInsn(RETURN);
			mv.visitMaxs(1, 1);
			mv.visitEnd();
		}

		{
			fv = cw.visitField(ACC_PUBLIC | ACC_FINAL, DELEGATE_MANAGER_FIELD, dcProxyDelegationType.getDescriptor(), null, null);
			fv.visitEnd();
		}		

		{
			mv = cw.visitMethod(ACC_PUBLIC, "$_delegatorAligator", "()" + configurableDelegateManagerType.getDescriptor(), null, null);
			mv.visitCode();
			mv.visitVarInsn(ALOAD, 0);
			mv.visitFieldInsn(GETFIELD, classInternalName, DELEGATE_MANAGER_FIELD, dcProxyDelegationType.getDescriptor());
			mv.visitInsn(ARETURN);
			mv.visitMaxs(1, 1);
			mv.visitEnd();
		}
	
		Set<String> processedSignatures = new HashSet<>();
		for (Class<?> interfaceClass: interfaces) {
			for (Method method: interfaceClass.getMethods()) {
				if (ReflectionTools.hasModifiers(method, Modifier.FINAL))
					continue;

				String methodName = method.getName();
				StringBuilder signature = new StringBuilder();
				signature.append('(');
				Class<?>[] parameterTypes = method.getParameterTypes();
				for (Class<?> parameterType: parameterTypes) {
					signature.append(signatureName(parameterType));
				}
				signature.append(')');
				signature.append(signatureName(method.getReturnType()));
				
				String methodSignature = signature.toString();
				String namedMethodSignature = methodName + methodSignature;
				
				if (!processedSignatures.add(namedMethodSignature))
					continue;
				
				Class<?>[] exceptionTypes = method.getExceptionTypes();
				String exceptions[] = new String[exceptionTypes.length];
				
				for (int i = 0; i < exceptionTypes.length; i++) {
					exceptions[i] = jniName(exceptionTypes[i]);
				}
				
				String plainJniName = jniName(interfaceClass);
				
				mv = cw.visitMethod(ACC_PUBLIC,  methodName, methodSignature, null, exceptions);
				mv.visitCode();
				mv.visitVarInsn(ALOAD, 0);
				mv.visitFieldInsn(GETFIELD, classInternalName, DELEGATE_MANAGER_FIELD, dcProxyDelegationType.getDescriptor());
				mv.visitMethodInsn(INVOKEVIRTUAL, dcProxyDelegationType.getInternalName(), "getDelegate", "()Ljava/lang/Object;", false);
				mv.visitTypeInsn(CHECKCAST, plainJniName);
				
				for (int i = 0; i < parameterTypes.length; i++) {
					Class<?> parameterType = parameterTypes[i];
					int loadOpcode = parameterType.isPrimitive()? primitiveLoads.get(parameterType): ALOAD;
					mv.visitVarInsn(loadOpcode, i + 1);
				}
				
				if (interfaceClass.isInterface())
					mv.visitMethodInsn(INVOKEINTERFACE, plainJniName, methodName, signature.toString(), true);
				else
					mv.visitMethodInsn(INVOKEVIRTUAL, plainJniName, methodName, signature.toString(), false);
	
				int returnOpcodeForType = getReturnOpcodeForType(method.getReturnType());
				mv.visitInsn(returnOpcodeForType);
	
				mv.visitMaxs(0, 0);
				
				mv.visitEnd();
			}
		}
		cw.visitEnd();

		byte[] classData = cw.toByteArray();
		
		ClassLoader parentClassLoader = findDeepestClassloader(interfaces);
		DelegatorProxyClassLoader classLoader = new DelegatorProxyClassLoader(parentClassLoader);
		
		Class<?> deployedClass = classLoader.deploy(className, classData);
		return deployedClass;
	}

	private static ClassLoader findDeepestClassloader(Collection<Class<?>> interfaces) {
		ClassLoader cl = DcProxyFactory.class.getClassLoader();
		for (Class<?> iface: interfaces) {
			ClassLoader ifaceCl = iface.getClassLoader();
			
			if (ifaceCl == null)
				continue;
			
			if (hasTransitiveParent(ifaceCl, cl))
				cl = ifaceCl;
		}

		return cl;
	}
	
	private static boolean hasTransitiveParent(ClassLoader cl, ClassLoader parent) {
		ClassLoader p = cl.getParent();
		if (p == null)
			return false;
		
		if (p == parent)
			return true;
		
		return hasTransitiveParent(p, parent);
	}
	
	private static int getReturnOpcodeForType(Class<?> returnType) {
		if (returnType.isPrimitive())
			return primitiveReturnOpcodes.get(returnType);
		else
			return ARETURN;
	}
	
	public static String signatureName(Class<?> parameterType) {
		if (parameterType.isArray()) {
			return jniName(parameterType);
		}
		else if (parameterType.isPrimitive()) {
			if (parameterType == void.class)
				return "V";
			else
				return Array.newInstance(parameterType, 0).getClass().getName().substring(1);
		}
		else {
			return delimitJniName(jniName(parameterType));
		}
	}
	
	private static class DelegatorProxyClassLoader extends ClassLoader {
		public DelegatorProxyClassLoader(ClassLoader classLoader) {
			super(classLoader);
		}

		public Class<?> deploy(String name, byte[] classData) {
			return defineClass(name, classData, 0, classData.length);
		}
	}
}
