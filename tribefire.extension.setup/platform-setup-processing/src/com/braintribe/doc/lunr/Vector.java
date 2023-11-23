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
package com.braintribe.doc.lunr;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;

public class Vector {
	private List<Number> elements;
	private double _magnitude;

	/**
	 * A vector is used to construct the vector space of documents and queries. These vectors support operations to
	 * determine the similarity between two documents or a document and a query.
	 *
	 * Normally no parameters are required for initializing a vector, but in the case of loading a previously dumped
	 * vector the raw elements can be provided to the constructor.
	 *
	 * For performance reasons vectors are implemented with a flat array, where an elements index is immediately
	 * followed by its value. E.g. [index, value, index, value]. This allows the underlying array to be as sparse as
	 * possible and still offer decent performance when being used for vector calculations.
	 *
	 * @constructor
	 * @param {Number[]}
	 *            [elements] - The flat list of element index and element value pairs.
	 */

	public Vector(List<Number> elements) {
		this._magnitude = 0;
		this.elements = elements != null ? elements : new ArrayList<>();
	}
	
	public Vector() {
		this._magnitude = 0;
		this.elements = new ArrayList<>();
	}

	/**
	 * Inserts an element at an index within the vector.
	 *
	 * Does not allow duplicates, will throw an error if there is already an entry for this index.
	 *
	 * @param {Number}
	 *            insertIdx - The index at which the element should be inserted.
	 * @param {Number}
	 *            val - The value to be inserted into the vector.
	 */
	public void insert(int insertIdx, double val) {
		upsert(insertIdx, val, DUPLICATE_INDEX);
	}

	private static BiFunction<Number, Number, Number> DUPLICATE_INDEX = (oldValue, newValue) -> {
		throw new IllegalStateException("duplicate index");
	};

	/**
	 * Inserts or updates an existing index within the vector.
	 *
	 * @param {Number}
	 *            insertIdx - The index at which the element should be inserted.
	 * @param {Number}
	 *            val - The value to be inserted into the vector.
	 * @param {function}
	 *            fn - A function that is called for updates, the existing value and the requested value are passed as
	 *            arguments
	 */
	public void upsert(int insertIdx, Number val, BiFunction<Number, Number, Number> fn) {
		_magnitude = 0;

		int position = this.positionForIndex(insertIdx);

		if (getInt(position) == insertIdx) {
			Number oldVal = elements.get(position + 1);
			elements.set(position + 1, fn.apply(oldVal, val));
		} else {
			// equivalent: LunrTools.splice(elements, position, 0, insertIdx, val);
			elements.addAll(position, Arrays.asList(insertIdx, val));
		}
	}
	
	private int getInt(int pos) {
		if (pos > (elements.size() - 1))
			return -1;
		
		Number number = elements.get(pos);
		return number != null? number.intValue(): -1;
	}
	
	 

	/**
	 * Calculates the position within the vector to insert a given index.
	 *
	 * This is used internally by insert and upsert. If there are duplicate indexes then the position is returned as if
	 * the value for that index were to be updated, but it is the callers responsibility to check whether there is a
	 * duplicate at that index
	 *
	 * @param {Number}
	 *            insertIdx - The index at which the element should be inserted.
	 * @returns {Number}
	 */
	int positionForIndex(int index) {
		// For an empty vector the tuple can be inserted at the beginning
		int size = this.elements.size();
		if (size == 0) {
			return 0;
		}

		int start = 0;
		int end = size / 2;
		int sliceLength = end - start;
		int pivotPoint = sliceLength / 2;
		int pivotIndex = getInt(pivotPoint * 2);

		while (sliceLength > 1) {
			if (pivotIndex < index) {
				start = pivotPoint;
			}

			if (pivotIndex > index) {
				end = pivotPoint;
			}

			if (pivotIndex == index) {
				break;
			}

			sliceLength = end - start;
			pivotPoint = start + sliceLength / 2;
			pivotIndex = getInt(pivotPoint * 2);
		}

		if (pivotIndex == index) {
			return pivotPoint * 2;
		} else if (pivotIndex > index) {
			return pivotPoint * 2;
		} else /* if (pivotIndex < index) */ {
			return (pivotPoint + 1) * 2;
		}
	}

	/**
	 * Calculates the magnitude of this vector.
	 *
	 * @returns {Number}
	 */
	public double magnitude() {
		if (_magnitude != 0)
			return _magnitude;

		int sumOfSquares = 0, elementsLength = elements.size();

		for (int i = 1; i < elementsLength; i += 2) {
			double val = elements.get(i).doubleValue();
			sumOfSquares += val * val;
		}

		return _magnitude = Math.sqrt(sumOfSquares);
	}

	/**
	 * Calculates the dot product of this vector and another vector.
	 *
	 * @param {lunr.Vector}
	 *            otherVector - The vector to compute the dot product with.
	 * @returns {Number}
	 */
	public double dot(Vector otherVector) {
		double dotProduct = 0;
		List<Number> a = elements, b = otherVector.elements;
		int aLen = a.size(), bLen = b.size();
		double aVal = 0, bVal = 0;
		int i = 0, j = 0;

		while (i < aLen && j < bLen) {
			aVal = a.get(i).doubleValue();
			bVal = b.get(j).doubleValue();

			if (aVal < bVal) {
				i += 2;
			} else if (aVal > bVal) {
				j += 2;
			} else if (aVal == bVal) {
				dotProduct += a.get(i + 1).doubleValue() * b.get(j + 1).doubleValue();
				i += 2;
				j += 2;
			}
		}

		return dotProduct;
	}

	/**
	 * Calculates the similarity between this vector and another vector.
	 *
	 * @param {lunr.Vector}
	 *            otherVector - The other vector to calculate the similarity with.
	 * @returns {Number}
	 */
	public double similarity(Vector otherVector) {
		return dot(otherVector) / this.magnitude();
	}

	/**
	 * Converts the vector to an array of the elements within the vector.
	 *
	 * @returns {Number[]}
	 */

	public List<Number> toArray() {
		int size = elements.size();
		List<Number> output = new ArrayList<>(size / 2);

		for (int i = 1, j = 0; i < size; i += 2, j++) {
			output.add(elements.get(i));
		}

		return output;
	};

	/**
	 * A JSON serializable representation of the vector.
	 *
	 * @returns {Number[]}
	 */
	public List<Number> toJSON() {
		return elements;
	}

}
