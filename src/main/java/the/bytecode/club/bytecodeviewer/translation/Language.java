package the.bytecode.club.bytecodeviewer.translation;

import com.google.gson.reflect.TypeToken;
import org.apache.commons.collections4.map.HashedMap;
import org.apache.commons.collections4.map.LinkedMap;
import the.bytecode.club.bytecodeviewer.BytecodeViewer;
import the.bytecode.club.bytecodeviewer.Resources;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;

/***************************************************************************
 * Bytecode Viewer (BCV) - Java & Android Reverse Engineering Suite        *
 * Copyright (C) 2014 Kalen 'Konloch' Kinloch - http://bytecodeviewer.com  *
 *                                                                         *
 * This program is free software: you can redistribute it and/or modify    *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation, either version 3 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 *   This program is distributed in the hope that it will be useful,       *
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of        *
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the         *
 *   GNU General Public License for more details.                          *
 *                                                                         *
 *   You should have received a copy of the GNU General Public License     *
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>. *
 ***************************************************************************/

/**
 * All of the supported languages
 *
 * TODO: Hindi, Bengali, Korean, Thai & Javanese need fonts to be supplied for them to show.
 *  The default font should be saved so it can be restored for latin-character based languages
 *
 * @author Konloch
 * @since 6/28/2021
 */
public enum Language
{
	ARABIC("/translations/arabic.json", "عربى", "English", "ar"),
	ENGLISH("/translations/english.json", "English", "English", "en"),
	FARSI("/translations/farsi.json", "فارسی ", "English", "fa"),
	FINNISH("/translations/finnish.json", "Suomen Kieli", "English", "fi"),
	FRENCH("/translations/french.json", "Français", "English", "fr"),
	GERMAN("/translations/german.json", "Deutsch", "German", "de"),
	GEORGIAN("/translations/georgian.json", "ქართული ენა", "English", "ka"),
	GREEK("/translations/greek.json", "ελληνικά", "English", "el"),
	HAUSA("/translations/hausa.json", "Hausa", "English", "ha"),
	HEBREW("/translations/hebrew.json", "עִבְרִית\u200E", "English", "iw", "he"),
	//HINDI("/translations/hindi.json", "हिंदी", "English", "hi"),
	//BENGALI("/translations/bengali.json", "বাংলা", "English", "bn"),
	HUNGARIAN("/translations/hungarian.json", "Magyar Nyelv", "English", "hu"),
	INDONESIAN("/translations/indonesian.json", "bahasa Indonesia", "English", "id"),
	ITALIAN("/translations/italian.json", "Italiano", "English", "it"),
	JAPANESE("/translations/japanese.json", "日本語", "English", "ja"),
	//JAVANESE("/translations/javanese.json", "ꦧꦱꦗꦮ", "English", "jw", "jv"),
	//KOREAN("/translations/korean.json", "Korean", "English", "ko"),
	MALAY("/translations/malay.json", "Bahasa Melayu", "English", "ms"),
	MANDARIN("/translations/mandarin.json", "普通话", "Mandarin", "zh-CN", "zh_cn", "zh"),
	NEDERLANDS("/translations/nederlands.json", "Nederlands", "English", "nl"), //dutch
	NORWEGIAN("/translations/norwegian.json", "Norsk", "English", "no"),
	POLISH("/translations/polish.json", "Polski", "English", "pl"),
	PORTUGUESE("/translations/portuguese.json", "Português", "English", "pt"),
	RUSSIAN("/translations/russian.json", "русский", "English", "ru"),
	SPANISH("/translations/spanish.json", "Español", "English", "es"),
	SWAHILI("/translations/swahili.json", "Kiswahili", "English", "sw"),
	SWEDISH("/translations/swedish.json", "svenska", "English", "sv"),
	//THAI("/translations/thai.json", "ภาษาไทย", "English", "th"),
	TURKISH("/translations/turkish.json", "Türkçe", "English", "tr"),
	UKRAINIAN("/translations/ukrainian.json", "украї́нська мо́ва", "English", "uk"),
	VIETNAMESE("/translations/vietnamese.json", "Tiếng Việt", "English", "vi"),
	;
	
	private static final HashedMap<String, Language> languageCodeLookup;
	
	static
	{
		languageCodeLookup = new HashedMap<>();
		for(Language l : values())
			l.languageCode.forEach((langCode)->
					languageCodeLookup.put(langCode, l));
	}
	
	private final String resourcePath;
	private final String readableName;
	private final String htmlIdentifier;
	private final LinkedHashSet<String> languageCode;
	
	Language(String resourcePath, String readableName, String htmlIdentifier, String... languageCodes)
	{
		this.resourcePath = resourcePath;
		this.readableName = readableName;
		this.htmlIdentifier = htmlIdentifier.toLowerCase();
		this.languageCode = new LinkedHashSet<>(Arrays.asList(languageCodes));
	}
	
	public void loadLanguage() throws IOException
	{
		printMissingLanguageKeys();
		
		HashMap<String, String> translationMap = BytecodeViewer.gson.fromJson(
				Resources.loadResourceAsString(resourcePath),
				new TypeToken<HashMap<String, String>>(){}.getType());
		
		for(Translation translation : Translation.values())
		{
			TranslatedComponentReference text = translation.getTranslatedComponentReference();
			
			//skip translating if the language config is missing the translation key
			if(!translationMap.containsKey(text.key))
			{
				System.err.println("MISSING TRANSLATION KEY: " + text.key);
				continue;
			}
			
			//update translation text value
			text.value = translationMap.get(text.key);
			
			//translate constant strings
			try {
				TranslatedStrings str = TranslatedStrings.valueOf(text.key);
				str.setText(text.value);
			} catch (IllegalArgumentException e) { }
			
			//check if translation key has been assigned to a component,
			//on fail print an error alerting the devs
			if(translation.getTranslatedComponentReference().runOnUpdate.isEmpty())
					//&& TranslatedStrings.nameSet.contains(translation.name()))
			{
				System.err.println("Translation Reference " + translation.name() + " is missing component attachment, skipping...");
				continue;
			}
			
			//trigger translation event
			translation.getTranslatedComponentReference().translate();
		}
	}
	
	//TODO
	// When adding new Translation Components:
	// 1) start by adding the strings into the english.json
	// 2) run this function to get the keys and add them into the Translation.java enum
	// 3) replace the swing component (MainViewerGUI) with a translated component
	//    and reference the correct translation key
	// 4) add the translation key to the rest of the translation files
	public void printMissingLanguageKeys() throws IOException
	{
		if(this != ENGLISH)
			return;
		
		LinkedMap<String, String> translationMap = BytecodeViewer.gson.fromJson(
				Resources.loadResourceAsString(resourcePath),
				new TypeToken<LinkedMap<String, String>>(){}.getType());
		
		HashSet<String> existingKeys = new HashSet<>();
		for(Translation t : Translation.values())
			existingKeys.add(t.name());
		
		for(String key : translationMap.keySet())
			if(!existingKeys.contains(key))
				System.err.println(key + ",");
	}
	
	public String getResourcePath()
	{
		return resourcePath;
	}
	
	public HashSet<String> getLanguageCode()
	{
		return languageCode;
	}
	
	public String getReadableName()
	{
		return readableName;
	}
	
	public String getHTMLPath(String identifier)
	{
		return "translations/html/" + identifier + "." + htmlIdentifier +  ".html";
	}
	
	public static HashedMap<String, Language> getLanguageCodeLookup()
	{
		return languageCodeLookup;
	}
}
