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

import java.util.function.Function;

import com.braintribe.codec.Codec;
import com.google.gwt.user.client.rpc.AsyncCallback;

public abstract class LoaderChain<T> implements Loader<T> {
	public static <T> LoaderChainImpl<T, T, FragmentChainType<T>> beginFragment() {
		FragmentChainType<T> fragmentChainType = new FragmentChainType<T>();
		return new LoaderChainImpl<T, T, FragmentChainType<T>>(fragmentChainType, fragmentChainType);
	}

	public static <T> LoaderChainImpl<T, T, RootChainType<T>> begin(Loader<T> loader) {
		return new LoaderChainImpl<T, T, RootChainType<T>>(new RootChainType<T>(), loader);
	}

	public static <T> LoaderChainImpl<ResultBundle, ResultBundle, RootChainType<ResultBundle>> begin(final String key, final Loader<T> loader) {
		Loader<ResultBundle> firstLoader = new Loader<ResultBundle>() {
			@Override
			public void load(final AsyncCallback<ResultBundle> callback) {
				loader.load(new AsyncCallback<T>() {
					@Override
					public void onSuccess(T result) {
						ResultBundle resultBundle = new ResultBundle();
						resultBundle.set(key, result);
						callback.onSuccess(resultBundle);
					}
					
					@Override
					public void onFailure(Throwable caught) {
						callback.onFailure(caught);
					}
				});
			}
		};
		return new LoaderChainImpl<ResultBundle, ResultBundle, RootChainType<ResultBundle>>(new RootChainType<ResultBundle>(), firstLoader);
	}

	public abstract <N> LoaderChain<N> append(ChainedLoader<T, N> successor);
	public abstract LoaderChain<ResultBundle> append(String key, Loader<?> successor);
	
	public abstract <N> LoaderChain<N> decode(final Codec<? extends N, ? super T> codec);
	public abstract <N> LoaderChain<N> encode(final Codec<? super T, ? extends N> codec);
	public abstract <N> LoaderChain<N> transform(final Function<? super T, ? extends N> indexedProvider);
	public abstract LoaderChain<T> process(final Processor<T> processor);
	public abstract LoaderChain<T> throwException(Throwable t);

	public interface Join<L, R> {

		public <O> LoaderChain<O> merge(final Merger<L, R, O> merger);
		
		public LoaderChain<L> mergeLeft(Merger<L, R, L> merger);
		
		public LoaderChain<R> mergeRight(Merger<L, R, R> merger);
		
		public LoaderChain<L> end(); 
	}
	
	public abstract <E> Join<T, E> join(final Loader<E> successor);
	public abstract <E> Join<T, E> join(final ChainedLoader<T, E> successor);
	
	public Future<T> load() {
		Future<T> future = new Future<T>();
		load(future);
		return future;
	}
}
