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
/*
 * Copyright 2007 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.google.gwt.dev.js;

import com.google.gwt.dev.cfg.ConfigurationProperties;
import com.google.gwt.dev.js.ast.JsName;
import com.google.gwt.dev.js.ast.JsProgram;
import com.google.gwt.dev.js.ast.JsScope;

/**
 * A namer that uses long, fully qualified names for maximum unambiguous
 * debuggability.
 */
public class JsVerboseNamer extends JsNamer {

  public static void exec(JsProgram program, ConfigurationProperties config) throws IllegalNameException {
    new JsVerboseNamer(program, config).execImpl();
  }

  public JsVerboseNamer(JsProgram program, ConfigurationProperties config) {
    super(program, config);
  }

  @Override
  protected void reset() {
    // Nothing to do.
  }

  @Override
  protected void visit(JsScope scope) {
    // Visit children.
    for (JsScope child : scope.getChildren()) {
      visit(child);
    }

    boolean ensureFirstCharNotLower = NamerTools.ensureFirstCharNotLower();

    // Visit all my idents.
    for (JsName name : scope.getAllNames()) {
      if (!referenced.contains(name)) {
        // Don't allocate idents for non-referenced names.
        continue;
      }

      if (!name.isObfuscatable()) {
        // Unobfuscatable names become themselves.
    	//gwt 2.8.0 has a problem when using generic along with method reference. Fixing it here.
        name.setShortIdent(NamerTools.fixGenericIssue(name.getIdent()));
        continue;
      }

      //gwt 2.8.0 has a problem when using generic along with method reference. Fixing it here.
      String fullIdent = NamerTools.fixGenericIssue(name.getIdent());
      
      if (ensureFirstCharNotLower) {
      	fullIdent = NamerTools.ensureFirstCharIsNotValidForPropertyNames(fullIdent);
      }
      
      // Fixes package-info.java classes.
      fullIdent = fullIdent.replace("-", "_");
      if (!isLegal(fullIdent)) {
        String checkIdent;
        for (int i = 0; true; ++i) {
          checkIdent = fullIdent + "_" + i;
          if (isLegal(checkIdent)) {
            break;
          }
        }
        name.setShortIdent(checkIdent);
      } else {
        // set each name's short ident to its full ident
        name.setShortIdent(fullIdent);
      }
    }
  }

  protected boolean isLegal(String newIdent) {
    return reserved.isAvailable(newIdent);
  }
}
