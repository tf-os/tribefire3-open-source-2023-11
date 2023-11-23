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
package com.braintribe.model.processing.shiro.util;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.Subject;

import com.braintribe.utils.DateTools;
import com.braintribe.utils.StringTools;

public class ShiroTools {
	public static void printUser(String source) {
		StringBuilder sb = new StringBuilder(source+" ("+DateTools.getCurrentDateString()+")\n\n");
		
		Subject user = SecurityUtils.getSubject();
		if (user != null) {
			
			Session session = user.getSession();
			sb.append("Session: "+session+"\n");
			sb.append("User authenticated: "+user.isAuthenticated()+"\n");
			
			//if (user.isAuthenticated()) {
				PrincipalCollection principals = user.getPrincipals();
				if (principals != null && !principals.isEmpty()) {
					principals.forEach(p -> {
						sb.append("Principal: "+p+" ("+p.getClass()+")\n");
					});
				}
//			} else {
//				sb.append("not authenticated\n");
//			}
			sb.append(user.toString()+"\n");
		} else {
			sb.append("no user\n");
		}
		System.out.println(StringTools.asciiBoxMessage(sb.toString(), -1));
	}
}
