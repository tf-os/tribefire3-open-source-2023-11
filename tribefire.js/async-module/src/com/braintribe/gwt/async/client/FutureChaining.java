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
package com.braintribe.gwt.async.client;

import com.braintribe.common.lcd.function.CheckedBiConsumer;
import com.braintribe.common.lcd.function.CheckedConsumer;
import com.braintribe.common.lcd.function.CheckedFunction;
import com.braintribe.common.lcd.function.CheckedRunnable;
import com.braintribe.common.lcd.function.CheckedSupplier;

/**
 * @author peter.gazdik
 */
/* package */ interface FutureChaining {

	// ######################################################
	// ## . . . . . . . . . Chain starts . . . . . . . . . ##
	// ######################################################

	static <T> Future<T> fromSupplier(CheckedSupplier<? extends T, Throwable> supplier) {
		Future<T> result = new Future<>();

		try {
			result.onSuccess(supplier.get());
		} catch (Throwable e) {
			result.onFailure(e);
		}

		return result;
	}

	static <T> Future<T> fromAsyncCallback(CheckedConsumer<? super Future<T>, ? extends Throwable> consumer) {
		Future<T> result = new Future<>();

		try {
			consumer.accept(result);
		} catch (Throwable e) {
			result.onFailure(e);
		}

		return result;
	}

	// ######################################################
	// ## . . . . . . . . Chain follow-ups . . . . . . . . ##
	// ######################################################

	// onFailure just delegates
	static <T> Future<T> andThen(Future<T> f, CheckedConsumer<? super T, Throwable> consumer) {
		return andThenMap(f, t -> {
			consumer.accept(t);
			return t;
		});
	}

	static <X, T> Future<X> andThenNotify(Future<T> f, CheckedBiConsumer<? super T, ? super Future<X>, ? extends Throwable> consumer) {
		Future<X> result = new Future<>();

		f.get(value -> {
			try {
				consumer.accept(value, result);
			} catch (Throwable e) {
				result.onFailure(e);
				return;
			}

		}, result::onFailure);

		return result;
	}

	// onFailure just delegates
	static <T, X> Future<X> andThenMap(Future<T> f, CheckedFunction<? super T, ? extends X, Throwable> mappingFunction) {
		Future<X> result = new Future<>();

		f.get(value -> {
			X mapped;
			try {
				mapped = mappingFunction.apply(value);
			} catch (Throwable e) {
				result.onFailure(e);
				return;
			}
			result.onSuccess(mapped);

		}, result::onFailure);

		return result;
	}

	// onFailure just delegates
	static <T, X> Future<X> andThenMapAsync(Future<T> f, CheckedFunction<? super T, ? extends Future<? extends X>, Throwable> mappingFunction) {
		Future<X> result = new Future<>();

		f.get(value -> {
			Future<? extends X> futureX;
			try {
				futureX = mappingFunction.apply(value);
			} catch (Throwable e) {
				result.onFailure(e);
				return;
			}

			futureX.get(result);

		}, result::onFailure);

		return result;
	}

	// onSuccess does nothing
	static <T> Future<T> onError(Future<T> f, CheckedConsumer<? super Throwable, Throwable> consumer) {
		Future<T> result = new Future<>();

		f.get(result::onSuccess, error -> {
			try {
				consumer.accept(error);
			} catch (Throwable e1) {
				error.addSuppressed(e1);
			}
			result.onFailure(error);
		});

		return result;
	}

	static <T> Future<T> contextualizeError(Future<T> f, CheckedFunction<? super Throwable, ? extends Throwable, Throwable> contextualizer) {
		Future<T> result = new Future<>();

		f.get(result::onSuccess, error -> {
			Throwable errorToPass;
			try {
				errorToPass = contextualizer.apply(error);

			} catch (Throwable e1) {
				error.addSuppressed(e1);
				errorToPass = error;
			}
			result.onFailure(errorToPass);
		});

		return result;
	}

	static <T> Future<T> catchError(Future<T> f, CheckedFunction<? super Throwable, ? extends T, Throwable> valueProvider) {
		Future<T> result = new Future<>();

		f.get(result::onSuccess, error -> {
			T newValue;
			try {
				newValue = valueProvider.apply(error);
			} catch (Throwable e) {
				error.addSuppressed(e);
				result.onFailure(e);
				return;
			}
			result.onSuccess(newValue);
		});

		return result;
	}

	// onSucess/onFailure call this function (the other param being null), and failure is of course also delegated
	static <T> Future<T> andThenOrOnError(Future<T> f, CheckedBiConsumer<? super T, ? super Throwable, Throwable> consumer) {
		Future<T> result = new Future<>();

		f.get(t -> {
			try {
				consumer.accept(t, null);
			} catch (Throwable e) {
				result.onFailure(e);
				return;
			}
			result.onSuccess(t);

		}, error -> {
			try {
				consumer.accept(null, error);
			} catch (Throwable e) {
				error.addSuppressed(e);
			}
			result.onFailure(error);
		});

		return result;
	}

	static <T> Future<T> andFinally(Future<T> f, CheckedRunnable<Throwable> runnable) {
		Future<T> result = new Future<>();

		f.get(t -> {
			try {
				runnable.run();
			} catch (Throwable e) {
				result.onFailure(e);
				return;
			}
			result.onSuccess(t);

		}, error -> {
			try {
				runnable.run();
			} catch (Throwable e) {
				error.addSuppressed(e);
			}
			result.onFailure(error);
		});

		return result;
	}

}
