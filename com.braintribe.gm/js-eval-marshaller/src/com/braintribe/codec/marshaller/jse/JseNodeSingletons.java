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
package com.braintribe.codec.marshaller.jse;

import com.braintribe.codec.marshaller.jse.tree.JseComment;
import com.braintribe.codec.marshaller.jse.tree.JseNode;
import com.braintribe.codec.marshaller.jse.tree.value.JseFalse;
import com.braintribe.codec.marshaller.jse.tree.value.JseNull;
import com.braintribe.codec.marshaller.jse.tree.value.JseTrue;
import com.braintribe.codec.marshaller.jse.tree.JseTmpVar;

public abstract class JseNodeSingletons {
	public static final JseNode tempVar = new JseTmpVar();
	public static final JseNode nullValue = new JseNull();
	public static final JseNode trueValue = new JseTrue();
	public static final JseNode falseValue = new JseFalse();
	public static final JseNode beginTypesComment = new JseComment("BEGIN_TYPES"); 
	public static final JseNode endTypesComment = new JseComment("END_TYPES"); 
}
