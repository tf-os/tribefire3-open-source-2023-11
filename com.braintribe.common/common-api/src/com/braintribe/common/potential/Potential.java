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
package com.braintribe.common.potential;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * @author pit / dirk
 *
 * @param <T> - the expected type of the Potenial
 * @param <R> - the Reason as object
 */
public class Potential<T, R> implements Supplier<T> {
	
	private T value;
	private R whyEmpty;
	private Function<R, String> formatter;

	private Potential() {
	}
	
	public static <T, R> Potential<T, R> empty(R whyEmpty) {
		Potential<T, R> p = new Potential<>();
		p.whyEmpty = Objects.requireNonNull(whyEmpty, "whyEmpty must not be null");
		return p;
	}
	
	public static <T, R> Potential<T, R> empty(R whyEmpty, Function<R, String> formatter) {
		Potential<T, R> p = new Potential<>();
		p.whyEmpty = Objects.requireNonNull(whyEmpty, "whyEmpty must not be null");
		p.formatter = formatter;
		return p;
	}
	
	public static <T, R> Potential<T, R> fill(T value) {
		Potential<T, R> p = new Potential<>();
		p.value = Objects.requireNonNull(value, "value must not be null");
		return p;
	}
	
	public static <T, R> Potential<T, R> fillNullable(T value) {
		Potential<T, R> p = new Potential<>();
		p.value = value;
		return p;
	}

	public boolean isFilled() {
		return whyEmpty == null;
	}
	
	public boolean isEmpty() {
		return whyEmpty != null;
	}
	
	@Override
	public T get() {
		if (whyEmpty != null) {
			throw new NoSuchElementException(formatter != null? formatter.apply(whyEmpty): whyEmpty.toString());
		}
		
		return value;
	}

	public T orElse(T other) {
		return whyEmpty == null? value: other;
	}
	
	public T orElseGet(Supplier<? extends T> other) {
		return whyEmpty == null? value: other.get();
	}
	
    public <X extends Throwable> T orElseThrow(Supplier<? extends X> exceptionSupplier) throws X {
    	if (whyEmpty == null)
    		return value;
    	
    	throw exceptionSupplier.get();
    }
    
    public void ifPresent(Consumer<? super T> consumer) {
        if (whyEmpty == null)
            consumer.accept(value);
    }
    
    public R whyEmpty() {
    	return whyEmpty;
    }
    
    public Optional<T> asOptional() {
    	return Optional.ofNullable(value);
    }
}
