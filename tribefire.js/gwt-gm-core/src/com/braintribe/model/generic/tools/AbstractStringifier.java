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
package com.braintribe.model.generic.tools;

import com.braintribe.model.generic.GenericEntity;

/**
 * @author peter.gazdik
 */
public abstract class AbstractStringifier {

	protected StringBuilder builder;
	protected String prefix;
	protected String tab;

	protected boolean usedPrefix = false;

	protected AbstractStringifier() {
		this(new StringBuilder(), "", "\t");
	}

	public AbstractStringifier(AbstractStringifier parent) {
		this(parent.builder, parent.prefix, parent.tab);
	}

	public AbstractStringifier(StringBuilder builder, String prefix, String tab) {
		this.builder = builder;
		this.prefix = prefix;
		this.tab = tab;
	}

	protected void levelUp() {
		prefix += "\t";
	}

	protected void levelDown() {
		prefix = prefix.isEmpty() ? prefix : prefix.substring(tab.length());
	}

	protected String getFullClassName(Object object) {
		if (object instanceof GenericEntity)
			return ((GenericEntity) object).entityType().getTypeSignature();
		else
			return object.getClass().getName();
	}

	protected String getSimpleClassName(Object object) {
		if (object instanceof GenericEntity)
			return ((GenericEntity) object).entityType().getShortName();
		else
			return getSimpleName(object.getClass().getName());
	}

	/** Class.getSimpleName is not supported in GWT 2.4! */
	protected String getSimpleName(String name) {
		int index = name.lastIndexOf('.');
		return name.substring(index + 1);
	}

	protected void print(String s) {
		if (usedPrefix) {
			builder.append(s);
		} else {
			builder.append(prefix + s);
			usedPrefix = true;
		}
	}

	protected void println(String s) {
		if (usedPrefix) {
			builder.append(s + "\n");
			usedPrefix = false;
		} else {
			builder.append(prefix + s + "\n");
		}
	}
}
