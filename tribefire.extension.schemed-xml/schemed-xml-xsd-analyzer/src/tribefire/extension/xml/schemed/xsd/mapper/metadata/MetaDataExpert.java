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
package tribefire.extension.xml.schemed.xsd.mapper.metadata;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import com.braintribe.model.meta.data.MetaData;
import com.braintribe.model.meta.data.constraint.Max;
import com.braintribe.model.meta.data.constraint.Min;
import com.braintribe.model.meta.data.display.formatting.FractionDigitsFormatting;
import com.braintribe.model.meta.data.display.formatting.TotalDigitsFormatting;
import com.braintribe.model.meta.data.display.formatting.WhitespaceFormatting;
import com.braintribe.model.meta.data.display.formatting.WhitespacePolicy;

import tribefire.extension.xml.schemed.model.xsd.SimpleTypeRestriction;
import tribefire.extension.xml.schemed.model.xsd.restrictions.FractionDigits;
import tribefire.extension.xml.schemed.model.xsd.restrictions.Length;
import tribefire.extension.xml.schemed.model.xsd.restrictions.MaxExclusive;
import tribefire.extension.xml.schemed.model.xsd.restrictions.MaxInclusive;
import tribefire.extension.xml.schemed.model.xsd.restrictions.MaxLength;
import tribefire.extension.xml.schemed.model.xsd.restrictions.MinExclusive;
import tribefire.extension.xml.schemed.model.xsd.restrictions.MinInclusive;
import tribefire.extension.xml.schemed.model.xsd.restrictions.MinLength;
import tribefire.extension.xml.schemed.model.xsd.restrictions.Pattern;
import tribefire.extension.xml.schemed.model.xsd.restrictions.TotalDigits;
import tribefire.extension.xml.schemed.model.xsd.restrictions.Whitespace;

/**
 * lill' helper to generate {@link MetaData} out of the XSD restrictions
 * @author pit
 *
 */
public class MetaDataExpert {
	
	private static Object createMatchingNumericType( String value) {
		if (value.contains( ".")) {
			return Double.parseDouble(value);
		}
		else {
			return Integer.parseInt(value);
		}
	}

	/**
	 * @param minInclusive - am {@link MinInclusive}
	 * @return - a {@link Min} set to be inclusive
	 */
	public static MetaData createMetaDataForMinInclusive( MinInclusive minInclusive) {
		Min min = Min.T.create();
		min.setExclusive(false);
		min.setLimit( createMatchingNumericType( minInclusive.getValue()));
		return min;
	}
	
	/**
	 * @param minExclusive - a {@link MinExclusive}
	 * @return - a {@link Min} set to be exclusive
	 */
	public static MetaData createMetaDataForMinExclusive( MinExclusive minExclusive) {
		Min min = Min.T.create();
		min.setExclusive( true);
		min.setLimit( createMatchingNumericType(minExclusive.getValue()));
		return min;
	}
	
	/**
	 * @param maxInclusive - a {@link MaxInclusive}
	 * @return - a {@link Max} set to inclusive
	 */
	public static MetaData createMetaDataForMaxInclusive( MaxInclusive maxInclusive) {
		Max min = Max.T.create();
		min.setExclusive(false);
		min.setLimit( createMatchingNumericType(maxInclusive.getValue()));
		return min;
	}
	/**
	 * @param maxExclusive - a {@link MaxExclusive}
	 * @return - a {@link Max} set to be exclusive
	 */
	public static MetaData createMetaDataForMaxExclusive( MaxExclusive maxExclusive) {
		Max min = Max.T.create();
		min.setExclusive( true);
		min.setLimit( createMatchingNumericType(maxExclusive.getValue()));
		return min;
	}

	/**
	 * @param length - a {@link MaxLength}
	 * @return - a {@link com.braintribe.model.meta.data.constraint.MaxLength}
	 */
	public static MetaData createMetaDataForMaxLength( MaxLength length) {
		com.braintribe.model.meta.data.constraint.MaxLength min = com.braintribe.model.meta.data.constraint.MaxLength.T.create();		
		min.setLength( Long.parseLong(length.getValue()));
		return min;
	}  
	/**
	 * @param length - a {@link MinLength}
	 * @return - a {@link com.braintribe.model.meta.data.constraint.MinLength}
	 */
	public static MetaData createMetaDataForMinLength( MinLength length) {
		com.braintribe.model.meta.data.constraint.MinLength min = com.braintribe.model.meta.data.constraint.MinLength.T.create();		
		min.setLength( Long.parseLong(length.getValue()));
		return min;
	} 
	
	/**
	 * @param length - a fixed {@link Length}
	 * @return - a {@link Collection} of a {@link com.braintribe.model.meta.data.constraint.MinLength} and a {@link com.braintribe.model.meta.data.constraint.MaxLength}
	 */
	public static Collection<MetaData> createMetaDataForLength( Length length) {
		com.braintribe.model.meta.data.constraint.MaxLength max = com.braintribe.model.meta.data.constraint.MaxLength.T.create();		
		max.setLength( Long.parseLong(length.getValue()));
		com.braintribe.model.meta.data.constraint.MinLength min = com.braintribe.model.meta.data.constraint.MinLength.T.create();		
		min.setLength( Long.parseLong(length.getValue()));
		
		return Arrays.asList( min, max);
	}
	
	/**
	 * @param pattern - a {@link Pattern}
	 * @return - a {@link com.braintribe.model.meta.data.constraint.Pattern}
	 */
	public static MetaData createMetaDataforPattern( Pattern pattern) {		
		return createMetaDataforPattern(pattern.getValue());
	}
	
	public static MetaData createMetaDataforPattern( String pattern) {
		com.braintribe.model.meta.data.constraint.Pattern patternMd = com.braintribe.model.meta.data.constraint.Pattern.T.create();
		patternMd.setExpression( pattern);
		return patternMd;
	}
	
	/**
	 * @param totalDigits - a {@link TotalDigits}
	 * @return - a {@link TotalDigitsFormatting}
	 */
	public static MetaData createMetaDataForTotalDigits( TotalDigits totalDigits) {
		TotalDigitsFormatting totalDigitsFormatting =TotalDigitsFormatting.T.create();
		totalDigitsFormatting.setDigits( Integer.parseInt( totalDigits.getValue()));
		return totalDigitsFormatting;
	}
	
	/**
	 * @param fractionDigits - a {@link FractionDigits}
	 * @return - a {@link FractionDigitsFormatting}
	 */
	public static MetaData createMetaDataForFractionDigits( FractionDigits fractionDigits) {
		FractionDigitsFormatting fractionDigitsFormatting = FractionDigitsFormatting.T.create();
		fractionDigitsFormatting.setDigits( Integer.parseInt( fractionDigits.getValue()));
		return fractionDigitsFormatting;
	}
	
	/**
	 * 
	 * @param whitespace - a {@link Whitespace}
	 * @return - a {@link WhitespaceFormatting}
	 */
	public static MetaData createMetaDataForWhitespace( Whitespace whitespace) {
		WhitespaceFormatting whitespaceFormatting = WhitespaceFormatting.T.create();
		whitespaceFormatting.setPolicy( WhitespacePolicy.valueOf( whitespace.getValue().toString()));
		return whitespaceFormatting;
	}
	
	public static Collection<MetaData> createMetaDataForSimpleTypeRestriction( SimpleTypeRestriction restriction) {
		List<MetaData> mds = new ArrayList<>();
		
		MinExclusive minE = restriction.getMinExclusive();
		if (minE != null)
			mds.add( createMetaDataForMinExclusive(minE));
		
		MinInclusive minI = restriction.getMinInclusive();
		if (minI != null)
			mds.add( createMetaDataForMinInclusive(minI));
		
		MaxExclusive maxE = restriction.getMaxExclusive();
		if (maxE != null)
			mds.add( createMetaDataForMaxExclusive(maxE));		
		
		MaxInclusive maxI = restriction.getMaxInclusive();
		if (maxI != null)
			mds.add( createMetaDataForMaxInclusive(maxI));

		MinLength minLength = restriction.getMinLength();
		if (minLength != null) {
			mds.add( createMetaDataForMinLength(minLength));
		}
		MaxLength maxLength = restriction.getMaxLength();
		if (maxLength != null) {
			mds.add( createMetaDataForMaxLength(maxLength));
		}	
		Length length = restriction.getLength();
		if (length != null) {
			mds.addAll( createMetaDataForLength(length));
		}
		
		Pattern pattern = restriction.getPattern();
		if (pattern != null) {
			mds.add( createMetaDataforPattern(pattern));
		}
		
		FractionDigits dig = restriction.getFractionDigits();
		if (dig != null) {
			mds.add( createMetaDataForFractionDigits(dig));
		}
		
		TotalDigits tdig = restriction.getTotalDigits();
		if (tdig != null) {
			mds.add( createMetaDataForTotalDigits( tdig));
		}
		
		Whitespace ws = restriction.getWhitespace();
		if (ws != null) {
			mds.add( createMetaDataForWhitespace(ws)); 
		}
		
		return mds;
	}
}
