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

import java.io.IOException;

import com.braintribe.exception.Exceptions;
import com.braintribe.model.generic.GenericEntity;

/**
 * @author peter.gazdik
 */
public abstract class AbstractStringifier {

	protected Appendable builder;
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

	public AbstractStringifier(Appendable builder, String prefix, String tab) {
		this.builder = builder;
		this.prefix = prefix;
		this.tab = tab;
	}

	protected void levelUp() {
		prefix += tab;
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

	protected void println(String s1, String s2, String... strings) {
		print(s1, s2, strings);
		println();
	}

	protected void println() {
		append("\n");
		usedPrefix = false;
	}

	protected void println(String s) {
		print(s);
		println();
	}

	protected void print(String s1, String s2, String... strings) {
		print(s1);
		print(s2);
		if (strings != null)
			for (String s : strings)
				print(s);
	}

	protected void print(String s) {
		if (usedPrefix) {
			append(s);
		} else {
			append(prefix + s);
			usedPrefix = true;
		}
	}

	private void append(String s) {
		try {
			builder.append(s);
		} catch (IOException e) {
			throw Exceptions.unchecked(e);
		}
	}

}
