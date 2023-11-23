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
/**

<h2>Important Unchecked Exceptions</h2>

<ul>
<li>{@link java.lang.NullPointerException}
<li>{@link java.lang.IllegalArgumentException}
<li>{@link java.lang.IllegalStateException}
<li>{@link java.lang.UnsupportedOperationException}
<li>{@link java.util.NoSuchElementException}
<li>{@link java.io.UncheckedIOException}
<li>{@link com.braintribe.exception.CommunicationException}
<li>{@link com.braintribe.exception.AuthorizationException}
<li>{@link com.braintribe.exception.CanceledException}
<li>{@link com.braintribe.exception.GenericServiceException}

java.lang.IllegalArgumentException: 4056: you gave me wrong arguments
</ul>

<h2>Important Checked Exceptions</h2>
<ul>
<li>{@link java.lang.InterruptedException}
</ul>

<p>
The commonly used strategy of exception handling has the disadvantage that it always wraps exceptions in order to add contextual information from
higher call sites. It also often forces to wrap in order to pass on checked exceptions that are not announced by the throws clause of the catching method.
Such repeated wrappings lead to obscure stacktraces which treat the important root cause with less priority than all its wrappings. This is especially
annoying when trying to react on specific exceptions because they could be hidden by wrappings and therefore require extra scanning in the chain of causes.
Also the rendering confuses with truncated stack frames in order to avoid the inherent redundancy. 
This redundancy is not only useless but also costly as each new constructed wrapper exception involves a determination of the stack
frames which is an expensive reflective operation.   

<p>
In order to solve all the issues that are made up by the common strategy we introduce a new strategy that is supported by this class.

<p>
The new strategy avoids unnecessary wrappings by using dynamically acquired suppressed exceptions to store additional contextual information
from higher call sites in method {@link com.braintribe.exception.Exceptions#contextualize(Throwable, String)}. Wrapping exceptions should only be done if really needed in case
of undeclared checked exceptions by using one of the following methods:
<ul>
 <li>{@link com.braintribe.exception.Exceptions#unchecked(Throwable, String)}
 <li>{@link com.braintribe.exception.Exceptions#unchecked(Throwable, String, java.util.function.BiFunction)}
 <li>{@link com.braintribe.exception.Exceptions#uncheckedAndContextualize(Throwable, String, java.util.function.Function)}
</ul>

<p>
This class offers stringification of exceptions in a different way than {@link java.lang.Throwable#printStackTrace()}. It only renders the root cause
and its stack frames and joins contextual information from wrappers and informations from suppressed exceptions on those:
<ul>
 <li>{@link com.braintribe.exception.Exceptions#stringify(Throwable)}
 <li>{@link com.braintribe.exception.Exceptions#stringify(Throwable, java.lang.Appendable)}
</ul>

<h2>Examples</h2>
<pre>
</pre>

 */
package com.braintribe.exception;
