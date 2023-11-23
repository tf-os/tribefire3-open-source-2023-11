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
package tribefire.cortex.initializer.support.impl.lookup;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.NoSuchElementException;
import java.util.function.Function;
import java.util.function.Supplier;

import com.braintribe.model.generic.GenericEntity;

/**
 * <p>
 * This invocation handler takes care of providing lookups on managed instances within a wire contract.
 * 
 */
public class ManagedInstancesLookups implements InvocationHandler {

	private static final String SUFFIX_CONTRACT = "Contract";
	private static final String SUFFIX_SPACE = "Space";
	
	private static final String PREFIX_GLOBAL_ID = "wire://";
	
	Function<String, GenericEntity> instanceLookup;
	private Supplier<String> initializerIdSupplier;
	private Class<?> iface;
	private InstanceLookup instanceLookupAnno;

	public ManagedInstancesLookups(Supplier<String> initializerIdSupplier, Class<?> iface, Function<String, GenericEntity> instanceLookup, InstanceLookup instanceLookupAnno) {
		this.initializerIdSupplier = initializerIdSupplier;
		this.iface = iface;
		this.instanceLookup = instanceLookup;
		this.instanceLookupAnno = instanceLookupAnno;
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T create(Class<T> iface, InstanceLookup instanceLookupAnno, Supplier<String> initializerIdSupplier, Function<String, GenericEntity> instanceLookup) {
		ManagedInstancesLookups handler = new ManagedInstancesLookups(initializerIdSupplier, iface, instanceLookup, instanceLookupAnno);
		return (T)Proxy.newProxyInstance(iface.getClassLoader(), new Class<?>[] {iface}, handler);
	}
	
	@Override
	public Object invoke(Object classLoader, Method method, Object[] args) throws Throwable {
		if (method.getDeclaringClass() == Object.class)
			return method.invoke(this, args);
		
		String globalId = getGlobalId(method);
		
		GenericEntity instance = instanceLookup.apply(globalId);
		
		if (instance == null)
			throw new NoSuchElementException("Could not find entity with globalId: " + globalId);
		
		return instance;
	}

	private String getGlobalId(Method method) {
		GlobalId globalIdAnnotation = method.getAnnotation(GlobalId.class);

		if (globalIdAnnotation != null) {
			if (!instanceLookupAnno.globalIdPrefix().isEmpty()) 
				return instanceLookupAnno.globalIdPrefix() + globalIdAnnotation.value();
				
			return globalIdAnnotation.value();
		}
		
		String contractName = iface.getSimpleName();
		String spaceName = getSpaceNameFor(contractName);
		
		String initializerId = initializerIdSupplier.get();
		
		StringBuilder builder = new StringBuilder();
		builder.append(PREFIX_GLOBAL_ID);
		builder.append(initializerId);
		builder.append('/');
		builder.append(spaceName);
		builder.append('/');
		builder.append(method.getName());
		
		return builder.toString();
	}

	private String getSpaceNameFor(String contractName) {
		StringBuilder builder = new StringBuilder();
		builder.append(contractName.substring(0, contractName.length() - SUFFIX_CONTRACT.length()));
		builder.append(SUFFIX_SPACE);
		return builder.toString();
	}

}
