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

/**
 * @author peter.gazdik
 */
public class BasicStringifier extends AbstractStringifier {

	public BasicStringifier() {
		this(new StringBuilder(), "", "\t");
	}

	public BasicStringifier(AbstractStringifier parent) {
		super(parent);
	}

	public BasicStringifier(StringBuilder builder, String prefix, String tab) {
		super(builder, prefix, tab);
	}

	public BasicStringifier(Appendable builder, String prefix, String tab) {
		super(builder, prefix, tab);
	}

	public Appendable appendable() {
		return builder;
	}

	@Override
	public void levelUp() {
		super.levelUp();
	}

	@Override
	public void levelDown() {
		super.levelDown();
	}

	@Override
	public String getFullClassName(Object object) {
		return super.getFullClassName(object);
	}

	@Override
	public String getSimpleClassName(Object object) {
		return super.getSimpleClassName(object);
	}

	@Override
	public String getSimpleName(String name) {
		return super.getSimpleName(name);
	}

	@Override
	public void print(String s) {
		super.print(s);
	}

	@Override
	public void println() {
		super.println();
	}

	@Override
	public void println(String s) {
		super.println(s);

	}

}
