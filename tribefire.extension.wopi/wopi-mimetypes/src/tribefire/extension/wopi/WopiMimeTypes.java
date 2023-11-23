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
package tribefire.extension.wopi;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Supported WOPI mimetypes - synched: 2019-11-29 <br/>
 * Based on environments <a href= "https://wopi.readthedocs.io/en/latest/build_test_ship/environments.html">WOPI -
 * Office for the web environments</a>: <br/>
 * <a href= "https://onenote.officeapps.live.com/hosting/discovery">WOPI production environment</a>
 * 
 *
 */
public class WopiMimeTypes {

	// -----------------------------------------------------------------------
	// EXTENSION
	// -----------------------------------------------------------------------

	// ------------------
	// EXCEL
	// ------------------

	public static final String CSV_EXTENSION = "csv";
	public static final String ODS_EXTENSION = "ods";
	public static final String XLS_EXTENSION = "xls";
	public static final String XLSB_EXTENSION = "xlsb";
	public static final String XLSM_EXTENSION = "xlsm";
	public static final String XLSX_EXTENSION = "xlsx";

	// ------------------
	// POWERPOINT
	// ------------------

	public static final String ODP_EXTENSION = "odp";
	public static final String POT_EXTENSION = "pot";
	public static final String POTM_EXTENSION = "potm";
	public static final String POTX_EXTENSION = "potx";
	public static final String PPS_EXTENSION = "pps";
	public static final String PPSM_EXTENSION = "ppsm";
	public static final String PPSX_EXTENSION = "ppsx";
	public static final String PPT_EXTENSION = "ppt";
	public static final String PPTM_EXTENSION = "pptm";
	public static final String PPTX_EXTENSION = "pptx";

	// ------------------
	// VISIO
	// ------------------

	public static final String VSD_EXTENSION = "vsd";
	public static final String VSDM_EXTENSION = "vsdm";
	public static final String VSDX_EXTENSION = "vsdx";

	// ------------------
	// WORD
	// ------------------

	public static final String DOC_EXTENSION = "doc";
	public static final String DOCM_EXTENSION = "docm";
	public static final String DOT_EXTENSION = "dot";
	public static final String DOTM_EXTENSION = "dotm";
	public static final String ODT_EXTENSION = "odt";
	public static final String RTF_EXTENSION = "rtf";
	public static final String DOCX_EXTENSION = "docx";
	public static final String DOTX_EXTENSION = "dotx";

	// -----------------------------------------------------------------------
	// MIMETYPES
	// -----------------------------------------------------------------------

	// ------------------
	// EXCEL
	// ------------------

	public static final String MIMETYPE_CSV = "text/csv";
	public static final String MIMETYPE_ODS = "application/vnd.oasis.opendocument.spreadsheet";
	public static final String MIMETYPE_XLS = "application/vnd.ms-excel";
	public static final String MIMETYPE_XLSB = "application/vnd.ms-excel.sheet.binary.macroenabled.12";
	public static final String MIMETYPE_XLSM = "application/vnd.ms-excel.sheet.macroenabled.12";
	public static final String MIMETYPE_XLSX = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

	// ------------------
	// POWERPOINT
	// ------------------

	public static final String MIMETYPE_ODP = "application/vnd.oasis.opendocument.presentation";
	public static final String MIMETYPE_POT = "application/vnd.ms-powerpoint";
	public static final String MIMETYPE_POTM = "application/vnd.ms-powerpoint.template.macroenabled.12";
	public static final String MIMETYPE_POTX = "application/vnd.openxmlformats-officedocument.presentationml.template";
	public static final String MIMETYPE_PPS = "application/vnd.ms-powerpoint";
	public static final String MIMETYPE_PPSM = "application/vnd.ms-powerpoint.slideshow.macroenabled.12";
	public static final String MIMETYPE_PPSX = "application/vnd.openxmlformats-officedocument.presentationml.slideshow";
	public static final String MIMETYPE_PPT = "application/vnd.ms-powerpoint";
	public static final String MIMETYPE_PPTM = "application/vnd.ms-powerpoint.presentation.macroenabled.12";
	public static final String MIMETYPE_PPTX = "application/vnd.openxmlformats-officedocument.presentationml.presentation";

	// ------------------
	// VISIO
	// ------------------

	public static final String MIMETYPE_VSD = "application/vnd.visio";
	public static final String MIMETYPE_VSDM = "application/vnd.ms-visio.drawing.macroenabled.12";
	public static final String MIMETYPE_VSDX = "application/vnd.ms-visio.drawing";

	// ------------------
	// WORD
	// ------------------

	public static final String MIMETYPE_DOC = "application/msword";
	public static final String MIMETYPE_DOCM = "application/vnd.ms-word.document.macroenabled.12";
	public static final String MIMETYPE_DOCX = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
	public static final String MIMETYPE_DOT = "application/msword";
	public static final String MIMETYPE_DOTM = "application/vnd.ms-word.template.macroenabled.12";
	public static final String MIMETYPE_DOTX = "application/vnd.openxmlformats-officedocument.wordprocessingml.template";
	public static final String MIMETYPE_ODT = "application/vnd.oasis.opendocument.text";
	public static final String MIMETYPE_RTF = "application/rtf";

	// -----------------------------------------------------------------------
	// WOPI - VIEW
	// -----------------------------------------------------------------------

	//@formatter:off
	public static final Set<String> VIEW_MIMETYPES_EXCEL = asSet(
			MIMETYPE_CSV, 
			MIMETYPE_ODS, 
			MIMETYPE_XLS, 
			MIMETYPE_XLSB, 
			MIMETYPE_XLSM,
			MIMETYPE_XLSX
		);
	public static final Set<String> VIEW_MIMETYPES_POWERPOINT = asSet(
			MIMETYPE_ODP, 
			MIMETYPE_POT, 
			MIMETYPE_POTM, 
			MIMETYPE_POTX, 
			MIMETYPE_PPS,
			MIMETYPE_PPSM, 
			MIMETYPE_PPSX, 
			MIMETYPE_PPT, 
			MIMETYPE_PPTM, 
			MIMETYPE_PPTX
		);
	public static final Set<String> VIEW_MIMETYPES_VISIO = asSet(
			MIMETYPE_VSD, 
			MIMETYPE_VSDM, 
			MIMETYPE_VSDX
		);
	public static final Set<String> VIEW_MIMETYPES_WORD = asSet(
			MIMETYPE_DOC, 
			MIMETYPE_DOCM, 
			MIMETYPE_DOCX, 
			MIMETYPE_DOT, 
			MIMETYPE_DOTM,
			MIMETYPE_DOTX, 
			MIMETYPE_ODT, 
			MIMETYPE_RTF
		);
	//@formatter:on

	// -----------------------------------------------------------------------
	// WOPI - EDIT
	// -----------------------------------------------------------------------

	//@formatter:off
	public static final Set<String> EDIT_MIMETYPES_EXCEL = asSet(
			MIMETYPE_ODS, 
			MIMETYPE_XLSB, 
			MIMETYPE_XLSM, 
			MIMETYPE_XLSX
		);
	public static final Set<String> EDIT_MIMETYPES_POWERPOINT = asSet(
			MIMETYPE_ODP, 
			MIMETYPE_PPSX, 
			MIMETYPE_PPTX
		);
	public static final Set<String> EDIT_MIMETYPES_VISIO = asSet(
			MIMETYPE_VSDX
		);
	public static final Set<String> EDIT_MIMETYPES_WORD = asSet(
			MIMETYPE_DOCM, 
			MIMETYPE_DOCX, 
			MIMETYPE_ODT
		);
	//@formatter:on

	// -----------------------------------------------------------------------
	// WOPI - TEST
	// -----------------------------------------------------------------------

	public static final String TEST_MIMETYPE = "text/plain";
	public static final String TEST_EXTENSION = "wopitest";

	// -----------------------------------------------------------------------
	// MIMETYPE GROUPS
	// -----------------------------------------------------------------------

	//@formatter:off
	/**
	 * {@link Set} of mimetypes supporting <b>read and write</b>
	 */
	public static final Set<String> SUPPORTED_EDIT_MIMETYPES = Stream.of(
			EDIT_MIMETYPES_EXCEL, 
			EDIT_MIMETYPES_POWERPOINT,
			EDIT_MIMETYPES_VISIO, 
			EDIT_MIMETYPES_WORD 
		).flatMap(Set::stream).collect(Collectors.toSet());
	//@formatter:on

	//@formatter:off
	/**
	 * {@link Set} of mimetypes supporting <b>read only</b>
	 */
	public static final Set<String> SUPPORTED_READ_MIMETYPES = Stream.of(
			VIEW_MIMETYPES_EXCEL,
			VIEW_MIMETYPES_POWERPOINT,
			VIEW_MIMETYPES_VISIO,
			VIEW_MIMETYPES_WORD
		).flatMap(Set::stream).collect(Collectors.toSet());
	//@formatter:on

	//@formatter:off
	public static final Set<String> SUPPORTED_EXCEL_MIMETYPES = Stream.of(
			VIEW_MIMETYPES_EXCEL,
			EDIT_MIMETYPES_EXCEL
			).flatMap(Set::stream).collect(Collectors.toSet());
	//@formatter:on

	//@formatter:off
	public static final Set<String> SUPPORTED_POWERPOINT_MIMETYPES = Stream.of(
			VIEW_MIMETYPES_POWERPOINT,
			EDIT_MIMETYPES_POWERPOINT
			).flatMap(Set::stream).collect(Collectors.toSet());
	//@formatter:on

	//@formatter:off
	public static final Set<String> SUPPORTED_WORD_MIMETYPES = Stream.of(
			VIEW_MIMETYPES_WORD,
			EDIT_MIMETYPES_WORD
		).flatMap(Set::stream).collect(Collectors.toSet());
	//@formatter:on

	// -----------------------------------------------------------------------
	// WOPI - EXTENSION - MIMETYPE MAPPING
	// -----------------------------------------------------------------------

	/**
	 * Mapping mimetype -> file extension
	 */
	//@formatter:off
	public static final Map<String, String> mimeTypeExtensionMap = asMap(
			// Excel
			MIMETYPE_CSV, CSV_EXTENSION,
			MIMETYPE_ODS, ODS_EXTENSION,
			MIMETYPE_XLS, XLS_EXTENSION,
			MIMETYPE_XLSB, XLSB_EXTENSION,
			MIMETYPE_XLSM, XLSM_EXTENSION,
			MIMETYPE_XLSX, XLSX_EXTENSION,
			
			// PowerPoint
			MIMETYPE_ODP, ODP_EXTENSION,
			MIMETYPE_POT, POT_EXTENSION,
			MIMETYPE_POTM, POTM_EXTENSION,
			MIMETYPE_PPTX, PPTX_EXTENSION,
			MIMETYPE_PPS, PPS_EXTENSION,
			MIMETYPE_PPSM, PPSM_EXTENSION,
			MIMETYPE_PPSX, PPSX_EXTENSION,
			MIMETYPE_PPT, PPT_EXTENSION,
			MIMETYPE_PPTM, PPTM_EXTENSION,
			MIMETYPE_PPTX, PPTX_EXTENSION,
			
			// Visio
			MIMETYPE_VSD, VSD_EXTENSION,
			MIMETYPE_VSDM, VSDM_EXTENSION,
			MIMETYPE_VSDX, VSDX_EXTENSION,
			
			// Word
			MIMETYPE_DOC, DOC_EXTENSION,
			MIMETYPE_DOCM, DOCM_EXTENSION,
			MIMETYPE_DOCX, DOCX_EXTENSION,
			MIMETYPE_DOT, DOT_EXTENSION,
			MIMETYPE_DOTM, DOTM_EXTENSION,
			MIMETYPE_DOTX, DOTX_EXTENSION,
			MIMETYPE_ODT, ODT_EXTENSION,
			MIMETYPE_RTF, RTF_EXTENSION,
			
			// Test
			TEST_MIMETYPE, TEST_EXTENSION
		);
	//@formatter:on

	// -----------------------------------------------------------------------
	// HELPERS - this comes from misc utility classes - this is done to have still Java 8 support
	// -----------------------------------------------------------------------

	@SafeVarargs
	private static <E> HashSet<E> asSet(E... elements) {
		return new HashSet<>(list(elements));
	}

	@SafeVarargs
	private static <E> List<E> list(E... elements) {
		return Arrays.asList(elements);
	}

	public static <K, V> HashMap<K, V> asMap(Object... elements) {
		return (HashMap<K, V>) (Map<?, ?>) putAllToMap(new HashMap<K, V>(), elements);
	}

	private static <K, V> Map<K, V> putAllToMap(Map<K, V> map, final Object... keyAndValuePairs) {
		if (keyAndValuePairs != null) {

			if (!isEven(keyAndValuePairs.length)) {
				throw new IllegalArgumentException("Cannot create map because one value is missing! " + Arrays.asList(keyAndValuePairs));
			}

			for (int i = 0; i < keyAndValuePairs.length - 1; i += 2) {
				map.put((K) keyAndValuePairs[i], (V) keyAndValuePairs[i + 1]);
			}
		}
		return map;
	}

	private static boolean isEven(final long number) {
		return ((number % 2) == 0);
	}
}
