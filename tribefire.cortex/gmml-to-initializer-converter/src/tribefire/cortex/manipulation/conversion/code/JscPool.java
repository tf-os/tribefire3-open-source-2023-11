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
package tribefire.cortex.manipulation.conversion.code;

import static com.braintribe.utils.lcd.CollectionTools2.newMap;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.TimeZone;

import com.braintribe.model.folder.Folder;
import com.braintribe.model.generic.value.type.DynamicallyTypedDescriptor;
import com.braintribe.model.workbench.WorkbenchPerspective;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;
import com.braintribe.wire.api.space.WireSpace;
import com.braintribe.wire.api.util.Lists;
import com.braintribe.wire.api.util.Maps;
import com.braintribe.wire.api.util.Sets;

import tribefire.cortex.initializer.support.impl.lookup.GlobalId;
import tribefire.cortex.initializer.support.impl.lookup.InstanceLookup;
import tribefire.cortex.initializer.support.wire.space.AbstractInitializerSpace;
import tribefire.cortex.sourcewriter.JavaSourceClass;

/**
 * @author peter.gazdik
 */
public class JscPool {

	// We only index acquired Jsc's,
	private final Map<String, JavaSourceClass> fullNameToJsc = newMap();

	// Common
	public final JavaSourceClass dateJsc = createJsc(Date.class);
	public final JavaSourceClass calendarJsc = createJsc(Calendar.class);
	public final JavaSourceClass timeZoneJsc = createJsc(TimeZone.class);

	// For Space
	public final JavaSourceClass managedAnnoJsc = createJsc(Managed.class);
	public final JavaSourceClass importAnnoJsc = createJsc(Import.class);
	public final JavaSourceClass abstractInitializerSpaceJsc = createJsc(AbstractInitializerSpace.class);

	public final JavaSourceClass mapsJsc = createJsc(Maps.class);
	public final JavaSourceClass listsJsc = createJsc(Lists.class);
	public final JavaSourceClass setsJsc = createJsc(Sets.class);

	// For Lookup Contract
	public final JavaSourceClass globalIdAnnoJsc = createJsc(GlobalId.class);
	public final JavaSourceClass instanceLookupAnnoJsc = createJsc(InstanceLookup.class);
	public final JavaSourceClass wireSpaceJsc = createJsc(WireSpace.class);

	// For WB
	public final JavaSourceClass folderJsc = createJsc(Folder.class);
	public final JavaSourceClass wbPerscpectiveJsc = createJsc(WorkbenchPerspective.class);

	
	private JavaSourceClass createJsc(Class<?> clazz) {
		JavaSourceClass jsc = JavaSourceClass.create(clazz);
		fullNameToJsc.put(clazz.getName(), jsc);

		return jsc;
	}

	public JavaSourceClass acquireJsc(DynamicallyTypedDescriptor ref) {
		return acquireJsc(ref.getTypeSignature());
	}

	public JavaSourceClass acquireJsc(Enum<?> enumValue) {
		return acquireJsc(enumValue.getClass().getName());
	}

	public JavaSourceClass acquireJsc(String typeSignature) {
		return fullNameToJsc.computeIfAbsent(typeSignature, ts -> JavaSourceClass.build(ts).isInterface(true).please());
	}

}
