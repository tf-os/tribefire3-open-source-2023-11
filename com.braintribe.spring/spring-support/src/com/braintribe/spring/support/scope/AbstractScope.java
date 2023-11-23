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
package com.braintribe.spring.support.scope;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.config.Scope;

/**
 * 
 * @author dirk.scheffler
 *
 */
public class AbstractScope<S> implements Scope {
  private ThreadLocal<Stack<S>> tlScopeStack = new ThreadLocal<Stack<S>>();
  private Map<S, Map<String, ScopeObjectEntry>> scopes = new HashMap<S, Map<String,ScopeObjectEntry>>(); 
  
  private Stack<S> getScopeStack() {
    Stack<S> stack = tlScopeStack.get();
    if (stack == null) {
      stack = new Stack<S>();
      tlScopeStack.set(stack);
    }
    return stack;
  }
  
  public void pushScope(S scope) {
    getScopeStack().push(scope);
  }
  
  public void popScope() {
    getScopeStack().pop();
  }
  
  public S getCurrentScope() {
    Stack<S> stack = getScopeStack();
    if (stack.isEmpty()) return null;
    else return stack.peek();
  }
  
  public void endScope(S s) {
    Map<String, ScopeObjectEntry> instances = scopes.remove(s);
    if (instances != null) {
      for (ScopeObjectEntry entry: instances.values()) {
        Runnable destructionCallback = entry.getDestructionCallback();
        if (destructionCallback != null)
          destructionCallback.run();
      }
    }
  }
  
  private synchronized Map<String, ScopeObjectEntry> aquireInstances(S scope) {
    Map<String, ScopeObjectEntry> instances = scopes.get(scope);
    if (instances == null) {
      instances = new HashMap<String, ScopeObjectEntry>();
      scopes.put(scope, instances);
    }
    return instances;
  }
  
  private synchronized ScopeObjectEntry aquireEntry(S scope, String name) {
    Map<String, ScopeObjectEntry> instances = aquireInstances(scope);
    
    ScopeObjectEntry entry = instances.get(name);
    if (entry == null) {
      entry = new ScopeObjectEntry();
      instances.put(name, entry);
    }
    
    return entry;
  }
  
  @SuppressWarnings("rawtypes")
public Object get(String name, ObjectFactory factory) {
    ScopeObjectEntry entry = aquireEntry(getCurrentScope(), name);
    
    if (!entry.isObjectInitialized()) {
      entry.setObject(factory.getObject());
    }
    return entry.getObject();
  }

  public String getConversationId() {
	 return null;
  }

  public void registerDestructionCallback(String name, Runnable destructionCallback) {
    aquireEntry(getCurrentScope(), name).setDestructionCallback(destructionCallback);
  }

  public Object remove(String name) {
    throw new UnsupportedOperationException();
  }

	public Object resolveContextualObject(String key) {
		// TODO Auto-generated method stub
		return null;
	}
  
  
  
}
