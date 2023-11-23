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
package edu.ycp.cs.dh.acegwt.client.ace;

/**
 * Enumeration for ACE editor themes.
 * Note that the corresponding .js file must be loaded
 * before a theme can be set.
 */
public enum AceEditorTheme {
	AMBIANCE("ambiance"),
	CHAOS("chaos"),
	CHROME("chrome"),
	CLOUD9_DAY("cloud9_day"),
	CLOUD9_NIGHT("cloud9_night"),
	CLOUD9_NIGHT_LOW_COLOR("cloud9_night_low_color"),
	CLOUDS("clouds"),
	CLOUDS_MIDNIGHT("clouds_midnight"),
	COBALT("cobalt"),
	CRIMSON_EDITOR("crimson_editor"),
	DAWN("dawn"),
	DREAMWEAVER("dreamweaver"),
	ECLIPSE("eclipse"),
	GITHUB("github"),
	IDLE_FINGERS("idle_fingers"),
	KATZENMILCH("katzenmilch"),
	KR_THEME("kr_theme"),
	KR("kr"),
	KUROIR("kuroir"),
	MERBIVORE("merbivore"),
	MERBIVORE_SOFT("merbivore_soft"),
	MONO_INDUSTRIAL("mono_industrial"),
	MONOKAI("monokai"),
	PASTEL_ON_DARK("pastel_on_dark"),
	SOLARIZED_DARK("solarized_dark"),
	SOLARIZED_LIGHT("solarized_light"),
	TERMINAL("terminal"),
	TEXTMATE("textmate"),
	TOMORROW_NIGHT_BLUE("tomorrow_night_blue"),
	TOMORROW_NIGHT_BRIGHT("tomorrow_night_bright"),
	TOMORROW_NIGHT_EIGHTIES("tomorrow_night_eighties"),
	TOMORROW_NIGHT("tomorrow_night"),
	TOMORROW("tomorrow"),
	TWILIGHT("twilight"),
	VIBRANT_INK("vibrant_ink"),
	XCODE("xcode");
	
	private final String name;
	
	private AceEditorTheme(String name) {
		this.name = name;
	}
	
	/**
	 * @return the theme name (e.g., "eclipse")
	 */
	public String getName() {
		return name;
	}
}
