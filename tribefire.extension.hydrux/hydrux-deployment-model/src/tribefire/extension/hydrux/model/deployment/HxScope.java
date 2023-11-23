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
package tribefire.extension.hydrux.model.deployment;

import java.util.List;

import com.braintribe.model.descriptive.HasName;
import com.braintribe.model.generic.annotation.SelectiveInformation;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

import tribefire.extension.hydrux.model.deployment.domain.HxDomainSupplier;
import tribefire.extension.hydrux.model.deployment.session.HxSession;

/**
 * Represents a logical space of {@link HxComponent components} within which a given denotation instance denotes the same implementation instance (I
 * promise it gets clearer).
 * 
 * <h3>Motivation</h3>
 * 
 * Imagine we want to have a button in our app which creates a new tab. We could want one of the two:
 * <p>
 * <ul>
 * <li>all the components within the tab are independent from the rest of the application, i.e. if we do some changes within that tab, we cannot save
 * them by clicking "save" in a different tab.
 * <li>some component are shared across all the tabs, i.e. it is the same instance everywhere.
 * </ul>
 * 
 * How do we configure one or the other?
 * 
 * <h3>Solution</h3>
 * 
 * Let's first have a look at the final solutions, and then explain how it works.
 *
 * <h4>Independent tabs</h4>
 * 
 * In this case, we have a <tt>FrameView</tt> with a "new tab" button, and the actual tabs, denoted by <tt>TabView</tt>. Consider following
 * configuration:
 * 
 * <pre>
 * HxApplication
 *   rootScope: HxScope(root)
 *   view: FrameView
 *     scope: null
 *     tab: TabView
 *       scope: HxScope(tab)
 * </pre>
 * 
 * <p>
 * If we had three tabs open, these would be the scopes of our components (numbers distinguish different instances of the same type):
 * 
 * <pre>
 * frameView: hxScope(root)
 * tabView1: hxScope(tab)1
 * tabView2: hxScope(tab)2
 * tabView3: hxScope(tab)3
 * </pre>
 * 
 * Q: Why is the frameView's scope the root scope?<br>
 * A: As the denotation's scope is <tt>null</tt>, the scope is inherited from the context that is resolving it. Since that view is configured as
 * <tt>HxApplication.view</tt>, it's the application that is resolving it, thus it's scope is inherited.
 * <p>
 * Q: What is <tt>tabView1</tt>, <tt>tabView2</tt>...?<br>
 * A: Those are different instances of an actual <tt>TabView</tt> implementation. Every time our <tt>frameView</tt> implementation resolves the same
 * <tt>TabView</tt> denotation, with the same <tt>HxScope</tt> denotation, a new <tt>IHxScope</tt> is created and a new <tt>tabView</tt>
 * implementation instance is created.
 * <p>
 * Q: Why always a new instance?<br>
 * A: The application keeps track of a stack of scopes. Since you are in the <tt>frameView</tt>, only the <tt>hxScope(root)</tt> is on the stack. Now
 * when you try to resolve the <tt>TabView</tt> instance, it sees a scope that is not yet on the stack - <tt>HxScope(tab)</tt>. Thus it creates a new
 * instance of this scope, and uses that new scope to resolve the <tt>TabView</tt>. And the new scope is of course empty, so a new <tt>tabView</tt>
 * implementation is created.
 * <p>
 * Q: So now, for each tab, the scope stack contains the root scope and the tab scope, but for each tab it is a different tab scope?<br>
 * A: Exactly.
 * <p>
 * Q: And that means?<br>
 * A: That means if the tab referenced a component with <tt>HxScope(root)</tt>, every single tab instance gets the exact same implementation instance
 * of this component. See also the next example.
 * 
 * <h4>Tabs sharing a component</h4>
 * 
 * In second case, we want the tabs to have a common component instance. So for example, if they are referencing an {@link HxSession}, it would be the
 * exact same implementation instance in all tabs. This would be achieved with this configuration:
 * 
 * <pre>
 * HxApplication
 *   rootScope: HxScope(root)
 *   view: FrameView
 *     scope: null
 *     tab: TabView
 *       scope: HxScope(tab)
 *       session: HxSession
 *         scope: HxScope(root)
 * </pre>
 * 
 * <p>
 * 
 * If we had three tabs open, these would be the scopes of our components:
 * 
 * <pre>
 * frameView: hxScope(root)
 * tabView1: hxScope(tab)1
 * tabView2: hxScope(tab)2
 * tabView3: hxScope(tab)3
 * 
 * tabView1.session: hxScope(root)
 * tabView2.session: hxScope(root)
 * tabView3.session: hxScope(root)
 * </pre>
 * 
 * Q: OK, I get it.<br>
 * A: Just to be sure, each tab is a new instance, just like before. But when these instances try to resolve their dependency - the
 * <tt>HxSession</tt>, that session's scope (<tt>HxScope(root)</tt>) is resolved. And because that already exists on the scope stack (root scope is
 * always on the stack), we do not get a new scope instance, but an existing one. So when the first tab resolves the <tt>HxSession</tt> against the
 * root scope, a new session implementation instance is created and cached. When the other two tabs do the resolution, the same instance is already
 * found and returned. Thus we have achieved that all our tabs share the same session.
 * <p>
 * 
 * Hopefully it's clear now how this scopes are intended to be used.
 * 
 * @author peter.gazdik
 */
@SelectiveInformation("HxScope[${name}]")
public interface HxScope extends HasName {

	EntityType<HxScope> T = EntityTypes.T(HxScope.class);

	/** This has no technical purpose, just makes the configuration easier to understand. */
	@Override
	String getName();

	HxDomainSupplier getDefaultDomain();
	void setDefaultDomain(HxDomainSupplier defaultDomain);

	/** These components are initialized (resolve) automatically when the scope is resolved. */
	List<HxController> getControllers();
	void setControllers(List<HxController> controllers);

}
