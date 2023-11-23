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
package tribefire.module.wire.contract;

import com.braintribe.wire.api.context.WireContextBuilder;
import com.braintribe.wire.api.module.WireModule;

/**
 * Utility class for TF module {@link WireModule wire modules}.
 * 
 * @see StandardTribefireModuleWireModule
 * 
 * @author peter.gazdik
 */
public class TribefireModules {

	/**
	 * Binds the {@link TribefireModuleContract}'s space, assuming the given {@code moduleSpaceClass} resides directly inside a package called
	 * "space".
	 * <p>
	 * This method actually binds given class directly to the {@link TribefireModuleContract}, and also binds all the contracts for the parent package
	 * of given space class via {@link WireContextBuilder#bindContracts(String)}.
	 * <p>
	 * Note that this method allows the space package to lie anywhere, doesn't have to be the standard {@code tribefire.module.wire.space} package as
	 * described in {@link StandardTribefireModuleWireModule}, although that's gonna be the case most of (if not all) the time.
	 * 
	 * @see StandardTribefireModuleWireModule
	 */
	public static void bindModuleContract(WireContextBuilder<?> contextBuilder, Class<? extends TribefireModuleContract> moduleSpaceClass) {
		contextBuilder.bindContracts(getParentPackageName(moduleSpaceClass));

		contextBuilder.bindContract(TribefireModuleContract.class, moduleSpaceClass);
	}

	private static String getParentPackageName(Class<?> moduleSpaceClass) {
		Package pckg = moduleSpaceClass.getPackage();
		if (pckg == null)
			throw new IllegalArgumentException("Module Space class has no package: " + moduleSpaceClass.getName());

		String pckgName = pckg.getName();
		int i = pckgName.lastIndexOf(".");
		if (i < 0)
			throw unexpectedPackageNameException(moduleSpaceClass);

		String hopefullyTheWordSpace = pckgName.substring(i + 1);
		if (!"space".equals(hopefullyTheWordSpace))
			throw unexpectedPackageNameException(moduleSpaceClass);

		return pckgName.substring(0, i);
	}

	private static IllegalArgumentException unexpectedPackageNameException(Class<?> moduleSpaceClass) {
		return new IllegalArgumentException("Invalid Module Space class package for class: '" + moduleSpaceClass.getName()
				+ "' . Expected structure: '${x.y.z...}.{moduleName}.space'");
	}

}
