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
import com.braintribe.codec.CodecException;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * LoaderChain supports complex and successive loading and decoding from async sources.
 * Thereby it tries to make it very comfortable to build up the chains.
 * @author dirk.scheffler
 */
public class LoaderChainImpl<T, T1, C extends ChainType<T1>> extends LoaderChain<T> {
	protected Loader<T> loader;
	protected C chainType;
	
	protected LoaderChainImpl(C chainType, Loader<T> loader) {
		this.loader = loader;
		this.chainType = chainType;
	}
	
	protected C getChainType() {
		return chainType;
	}
	
	@Override
	public <N> LoaderChainImpl<N, T1, C> append(final ChainedLoader<T, N> successor) {
		return new LoaderChainImpl<N, T1, C>(chainType, new Loader<N>() {
			@Override
			public void load(final AsyncCallback<N> callback) {
				loader.load(new AsyncCallback<T>() {
					@Override
					public void onSuccess(T result) {
						successor.load(result, callback);
					}
					
					@Override
					public void onFailure(Throwable caught) {
						callback.onFailure(caught);
					}
				});
			}
		});
	}
	
	public <N> LoaderChainImpl<N, T1, C> append(final LoaderChainImpl<N, T, FragmentChainType<T>> successor) {
		return append(new ChainedLoader<T, N>() {
			@Override
			public void load(T input, final AsyncCallback<N> callback) {
				successor.load(callback);
				FragmentChainType<T> fragmentChainType = successor.getChainType();
				fragmentChainType.getAsyncCallback().onSuccess(input);
			}
		});
	}
	
	@Override
	public LoaderChainImpl<ResultBundle, T1, C> append(final String key, final Loader<?> successor) {
		final Loader<Object> successorLoader = (Loader<Object>)successor;
		return append(new ChainedLoader<T, ResultBundle>() {
			@Override
			public void load(final T input, final AsyncCallback<ResultBundle> callback) {
				successorLoader.load(new AsyncCallback<Object>() {
					@Override
					public void onSuccess(Object result) {
						ResultBundle resultBundle = null;
						if (input instanceof ResultBundle) {
							resultBundle = (ResultBundle)input;
						}
						else {
							resultBundle = new ResultBundle(input);
						}
						resultBundle.set(key, result);
						callback.onSuccess(resultBundle);
					}
					
					@Override
					public void onFailure(Throwable caught) {
						callback.onFailure(caught);
					}
				});
			}
		});
	}
	
	@Override
	public <N> LoaderChainImpl<N, T1, C> decode(final Codec<? extends N, ? super T> codec) {
		return new LoaderChainImpl<N, T1, C>(chainType, new Loader<N>() {
			@Override
			public void load(final AsyncCallback<N> callback) {
				loader.load(new AsyncCallback<T>() {
					@Override
					public void onSuccess(T result) {
						try {
							callback.onSuccess(codec.decode(result));
						} catch (CodecException e) {
							callback.onFailure(e);
						}
					}
					
					@Override
					public void onFailure(Throwable caught) {
						callback.onFailure(caught);
					}
				});
			}
		});
	}
	
	@Override
	public <N> LoaderChainImpl<N, T1, C> encode(final Codec<? super T, ? extends N> codec) {
		return new LoaderChainImpl<N, T1, C>(chainType, new Loader<N>() {
			@Override
			public void load(final AsyncCallback<N> callback) {
				loader.load(new AsyncCallback<T>() {
					@Override
					public void onSuccess(T result) {
						try {
							callback.onSuccess(codec.encode(result));
						} catch (CodecException e) {
							callback.onFailure(e);
						}
					}
					
					@Override
					public void onFailure(Throwable caught) {
						callback.onFailure(caught);
					}
				});
			}
		});
	}
	
	@Override
	public <N> LoaderChainImpl<N, T1, C> transform(final Function<? super T, ? extends N> indexedProvider) {
		return new LoaderChainImpl<N, T1, C>(chainType, new Loader<N>() {
			@Override
			public void load(final AsyncCallback<N> callback) {
				loader.load(new AsyncCallback<T>() {
					@Override
					public void onSuccess(T result) {
						try {
							callback.onSuccess(indexedProvider.apply(result));
						} catch (RuntimeException e) {
							callback.onFailure(e);
						}
					}
					
					@Override
					public void onFailure(Throwable caught) {
						callback.onFailure(caught);
					}
				});
			}
		});
	}
	
	@Override
	public LoaderChainImpl<T, T1, C> throwException(Throwable t) {
		Future<T> future = new Future<T>();
		future.onFailure(t);
		return new LoaderChainImpl<T, T1, C>(chainType, future);
	}
	
	@Override
	public LoaderChainImpl<T, T1, C> process(final Processor<T> processor) {
		return new LoaderChainImpl<T, T1, C>(chainType, new Loader<T>() {
			@Override
			public void load(final AsyncCallback<T> callback) {
				loader.load(new AsyncCallback<T>() {
					@Override
					public void onSuccess(T result) {
						try {
							processor.process(result);
							callback.onSuccess(result);
						} catch (Exception e) {
							callback.onFailure(e);
						}
					}
					
					@Override
					public void onFailure(Throwable caught) {
						callback.onFailure(caught);
					}
				});
			}
		});
	}
	
	public LoaderChainImpl<Void, T1, C> voidResult() {
		return new LoaderChainImpl<Void, T1, C>(chainType, new Loader<Void>() {
			@Override
			public void load(final AsyncCallback<Void> callback) {
				loader.load(new AsyncCallback<T>() {
					@Override
					public void onSuccess(T result) {
						callback.onSuccess(null);
					}
					
					@Override
					public void onFailure(Throwable caught) {
						callback.onFailure(caught);
					}
				});
			}
		});
	}
	
	@Override
	public void load(AsyncCallback<T> asyncCallback) {
		loader.load(asyncCallback);
	}
	
	public class JoinImpl<L, R> implements Join<L, R>{
		private Loader<L> predeccessor;
		private ChainedLoader<L, R> successor;
		
		public JoinImpl(Loader<L> predeccessor, ChainedLoader<L, R> successor) {
			super();
			this.predeccessor = predeccessor;
			this.successor = successor;
		}
		
		public JoinImpl(Loader<L> predeccessor, final Loader<R> successorLoader) {
			super();
			this.predeccessor = predeccessor;
			this.successor = new ChainedLoader<L, R>() {
				@Override
				public void load(L input, AsyncCallback<R> callback) {
					successorLoader.load(callback);
				}
			};
		}
		
		@Override
		public LoaderChainImpl<L, T1, C> end() {
			return new LoaderChainImpl<L, T1, C>(chainType, new Loader<L>() {
				@Override
				public void load(final AsyncCallback<L> asyncCallback) {
					predeccessor.load(new AsyncCallback<L>() {
						@Override
						public void onSuccess(final L leftResult) {
							successor.load(leftResult, new AsyncCallback<R>() {
								@Override
								public void onSuccess(R rightResult) {
									try {
										asyncCallback.onSuccess(leftResult);
									}
									catch (Exception e) {
										asyncCallback.onFailure(e);
									}
								}
								
								@Override
								public void onFailure(Throwable caught) {
									asyncCallback.onFailure(caught);
								}
							});
						}
						
						@Override
						public void onFailure(Throwable caught) {
							asyncCallback.onFailure(caught);
						}
					});
				}
			});
		}

		@Override
		public <O> LoaderChainImpl<O, T1, C> merge(final Merger<L, R, O> merger) {
			return new LoaderChainImpl<O, T1, C>(chainType, new Loader<O>() {
				@Override
				public void load(final AsyncCallback<O> asyncCallback) {
					predeccessor.load(new AsyncCallback<L>() {
						@Override
						public void onSuccess(final L leftResult) {
							successor.load(leftResult, new AsyncCallback<R>() {
								@Override
								public void onSuccess(R rightResult) {
									try {
										O result = merger.merge(leftResult, rightResult);
										asyncCallback.onSuccess(result);
									}
									catch (Exception e) {
										asyncCallback.onFailure(e);
									}
								}
								
								@Override
								public void onFailure(Throwable caught) {
									asyncCallback.onFailure(caught);
								}
							});
						}
						
						@Override
						public void onFailure(Throwable caught) {
							asyncCallback.onFailure(caught);
						}
					});
				}
			});
		}
		
		@Override
		public LoaderChainImpl<L, T1, C> mergeLeft(Merger<L, R, L> merger) {
			return merge(merger);
		}
		
		@Override
		public LoaderChainImpl<R, T1, C> mergeRight(Merger<L, R, R> merger) {
			return merge(merger);
		}
	}
	
	@Override
	public <E> JoinImpl<T, E> join(final Loader<E> successor) {
		return new JoinImpl<T, E>(loader, successor); 
	}
	
	@Override
	public <E> JoinImpl<T, E> join(final ChainedLoader<T, E> successor) {
		return new JoinImpl<T, E>(loader, successor);
	}
}
