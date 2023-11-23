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
package tribefire.cortex.manipulation.conversion.beans;

import com.braintribe.model.generic.value.PreliminaryEntityReference;

public class NewBean extends EntityBean<PreliminaryEntityReference> {

	public boolean isManaged = false;

	public NewBean(BeanRegistry beanRegistry, PreliminaryEntityReference ref) {
		super(beanRegistry, ref, null);
	}

	@Override
	protected String beanInstanceForSetterInvocation() {
		return "bean";
	}

	@Override
	public void writeYourDeclaration() {
		sb = new StringBuilder();

		String typeName = typeNameIn(ctx.spaceWriter);

		// @Manged
		if (isManaged)
			sb.append("\t@Managed\n");
		else
			sb.append("\t// Managed\n");

		// public MyType myType1() {
		sb.append("\tprivate ");
		sb.append(typeName);
		sb.append(" ");
		sb.append(beanName);
		sb.append("() {\n");

		// MyType bean = create(MyType.T, "globalId");
		sb.append("\t\t");
		sb.append(typeName);
		sb.append(" ");
		sb.append(beanInstanceForSetterInvocation());
		sb.append(" = session().createRaw(");
		sb.append(typeName);
		sb.append(".T, \"");
		sb.append(globalId);
		sb.append("\");\n");

		writePropertyChanges();

		// return bean;;
		sb.append("\t\treturn bean;\n");

		// } // (end of bean method)
		sb.append("\t}\n\n");

		ctx.spaceWriter.addMethod(sb.toString());
	}

}
