package de.spinfo.bkis.sutsiparse;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.bkis.jaseval.JASEVAL;
import de.bkis.jaseval.SVDocument;
import de.spinfo.bkis.sutsiparse.io.IO;

public class Main {
	
	private static JASEVAL jaseval = new JASEVAL();
	private static int processUnits;
	
	//PATTERNS
	private static final String P_REMOVE1  = "\\s\\([abcde]\\)";
	private static final String P_REMOVE2  = "[:≠]";
	private static final String P_REMOVE_POST = "(\\*|\\,(?=$)|unpers\\.|impars\\.|\\s\\(f\\)|\\s\\(m\\)|\\s\\(n\\))";
	private static final String P_PARANT  =  "[\\(\\)]";
	private static final String P_INSERT   = "≤";
	private static final String P_SEC_FORM = " % ";
	private static final String P_GENUS = 	"\\s[fmn]\\.(\\s\\([fmn]\\.\\))?(?=(\\s|$))";
	private static final String P_GRAM = 	"\\s(conj|interj|interrog|indef|präp|prep|konj|pl|adj|adv|tr|int|tr\\/int|refl|pers|pron|poss|art|def)\\.(?=(\\s|$))";
	private static final String P_SEM = 	"\\s\\(?(col|num|sl|vulg|fam|form|hum|pej|poet|milit|fig|bot|polit|sp).*?\\)?(?=(\\s|$))";
	private static final String P_SUBSEM = 	"\\s\\([^-][^\\(]{2,}\\)(?=(\\s|$))";
	private static final String P_VARIANT = "(\\[|\\().*?(\\]|\\))";
	private static final String P_SUFFIX = "\\s-\\p{L}+(?=\\b)";
	
	//private static final String P_SEP = 	"Ω\\s";
	//private static final String P_R_ENTRY = ".*(?=Ω)";
	//private static final String P_D_ENTRY = "(?<=Ω).*";
	
	
	/*		
	 * 		MARKERS:
	 * 
	 * 		Ω	beginn Übersetzung
	 * 		^	Ende des Eintrags
	 * 		≤	Wort hier nochmal einfügen
	 * 		%	andere (eigene) Schreibweise
	 * 
	 * 		REPLACEMENTS:
	 * 		”	-
	 * 		å	a
	 * 		™	e
	 * 		¡	i
	 * 		ø	o
	 * 		°	u
	 * 		ß	s
	 * 		&lt;[0-9]&gt;	entfernen
	 * 		\\[[0-9]\\]		entfernen
	 * 		
	 */

	public static void main(String[] args) {
		IO io = new IO();
		
		// extrahierte hypercard-daten (nur reintext)
		//in eine gemeinsame datei schreiben
		//io.convertToTxt("datenXML", "sutsilvan_data.txt");
		
		
//		// CLEANUP
//		String[] entries = io.readEntryLines("sutsilvan_data.txt");
//		StringBuilder sb = new StringBuilder();
//		for (String line : entries){
//			line = line.replaceAll("”", "-");
//			line = line.replaceAll("å", "a");
//			line = line.replaceAll("™", "e");
//			line = line.replaceAll("¡", "i");
//			line = line.replaceAll("ø", "o");
//			line = line.replaceAll("°", "u");
//			line = line.replaceAll("&lt;[0-9]&gt;", "");
//			line = line.replaceAll("\\[[0-9]\\]\\s?", "");
//			line = line.replaceAll("\\s\\^", "");
//			sb.append(line + "\n");
//		}
//		try {
//			jaseval.writeToFile(sb.toString(), "sutsilvan_data_clean.txt", "UTF-8");
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
		//prepare document
		System.out.println("[STATUS]\tpreparing new document...");
		SVDocument doc = new SVDocument();
		String[] fields = {"RecID", "DStichwort", "DGenus", "DGrammatik", "RStichwort",
				"RGenus", "RGrammatik", "RFlex", "DStatus", "DStichwort_sort", "RStatus",
				"RStichwort_sort", "DSemind", "RSemind", "DSubsemantik", "RSubsemantik",
				"DSemantik", "RSemantik", "Bearbeitungshinweis", "RGrammatik_Kategorie",
				"REinschränkung", "Geprüft", "varSuchwort", "varSuchart", "varSuchfehler",
				"varAnführungszeichen", "varSuchergebnis", "varLayout", "varDSort", "varRSort",
				"DxAusgabe", "RxAusgabe", "xtemp_Achtung_Bearbeitung", "varMailtext", "xvarMailtext",
				"XTempStichwort", "XTempGrammatik", "varCopiar", "Remartgas", "maalr_timestamp",
				"Bearbeitungshinweis2", "xvarMail", "xDiever", "vVersiun", "redirect_a", "redirect_b"};
		String header = "";
		for (String field : fields) header += field + "\t";
		header.trim();
		doc.setHeader(header, "\t");
		
		//LOAD ABBREVIATIONS LISTS
		System.out.println("[STATUS]\tloading abbreviations lists...");
		SVDocument abbrD = null;
		SVDocument abbrR = null;
		SVDocument abbrLemma = null;
		try {
			abbrD = jaseval.readSVFile("abbrev_sutsilvan_d.csv", ";", false);
			abbrR = jaseval.readSVFile("abbrev_sutsilvan_r.csv", ";", false);
			abbrLemma = jaseval.readSVFile("abbrev_lemma.csv", ";", false);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		//process entries
		System.out.println("[STATUS]\tstarting entry processing...");
		String[] entries = io.readEntryLines("sutsilvan_data_clean.txt");
		String[] raw;
		int id = 0;
		for (String s : entries){
			raw = s.split(" Ω ");
			String[] entry = getEmptyEntry(doc.getHeader().length);
			entry = processEntry(raw[0], entry, doc, "R");
			entry = processEntry(raw[1], entry, doc, "D");
			entry = processRedirects(entry, doc);
			entry = replaceAbbreviations(entry, doc, abbrD, abbrR);
			entry = replaceLemmaAbbreviations(entry, doc, abbrLemma);
			
			// " ' "-entfernen
			if (entry[doc.getFieldIndex("RStichwort")].contains(" '"))
				entry[doc.getFieldIndex("RStichwort")] = entry[doc.getFieldIndex("RStichwort")].split(" '")[0];
			if (entry[doc.getFieldIndex("DStichwort")].contains(" '"))
				entry[doc.getFieldIndex("DStichwort")] = entry[doc.getFieldIndex("DStichwort")].split(" '")[0];
			
			entry[0] = "" + id++;
			doc.addEntry(entry);
			displayProcess(id, entries.length, false);
		}
		displayProcess(id, entries.length, true);
		
		//write to file
		try {
			jaseval.writeSV(doc, "\t", "data_st.tab", "UTF-8");
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	private static String[] processEntry(String raw, String[] entry, SVDocument doc, String headerPrefix){
		int stichw = doc.getFieldIndex(headerPrefix + "Stichwort");
		
		//cleanup
		raw = raw.replaceAll(P_REMOVE1, "");
		raw = raw.replaceAll(P_REMOVE2, "");
		
		//REPLACEMENTS
		if (headerPrefix.equals("R"))
			raw = raw.replaceAll("ß", "s");
		raw = raw.replaceAll("(?<=\\w)\\s\\,\\s", ", "); // " , " durch ", " ersetzen
		
		//GENUS
		entry = processField(entry, P_GENUS, raw, doc.getFieldIndex(headerPrefix + "Genus"));
		entry[doc.getFieldIndex(headerPrefix + "Genus")]
				= entry[doc.getFieldIndex(headerPrefix + "Genus")].replaceAll("(?<=m)\\.\\s(?=f)", ",");
		entry[doc.getFieldIndex(headerPrefix + "Genus")]
				= entry[doc.getFieldIndex(headerPrefix + "Genus")].replaceAll("\\s\\(", "(");
		entry[doc.getFieldIndex(headerPrefix + "Genus")]
				= entry[doc.getFieldIndex(headerPrefix + "Genus")].replaceAll("\\.", "");
		raw = raw.replaceAll(P_GENUS, "");
		
		//GRAM
		entry = processField(entry, P_GRAM, raw, doc.getFieldIndex(headerPrefix + "Grammatik"));
		entry[doc.getFieldIndex(headerPrefix + "Grammatik")]
				= entry[doc.getFieldIndex(headerPrefix + "Grammatik")].replaceAll("\\.", "");
		raw = raw.replaceAll(P_GRAM, "");
		
		//SEM
		entry = processField(entry, P_SEM, raw, doc.getFieldIndex(headerPrefix + "Semantik"));
		raw = raw.replaceAll(P_SEM, "");
		entry[doc.getFieldIndex(headerPrefix + "Semantik")]
				= entry[doc.getFieldIndex(headerPrefix + "Semantik")].replaceAll(P_PARANT, "");
		
		
		//SUBSEM
		entry = processField(entry, P_SUBSEM, raw, doc.getFieldIndex(headerPrefix + "Subsemantik"));
		raw = raw.replaceAll(P_SUBSEM, "");
		entry[doc.getFieldIndex(headerPrefix + "Subsemantik")]
				= entry[doc.getFieldIndex(headerPrefix + "Subsemantik")].replaceAll(P_PARANT, "");
		
		//STICHWORT
		entry[stichw] = raw;
		
		//INSERTIONS
		if (entry[stichw].contains(P_INSERT)){
			//int marker = entry[stichw].indexOf(P_INSERT);
			String ins = entry[stichw].split(" ")[0];
			entry[stichw] = entry[stichw].replace(P_INSERT, ins).trim();
			entry[stichw] = entry[stichw].substring(ins.length(), entry[stichw].length());
			//move possible suffix behind insertion
			String suff = getMatch(entry[stichw],P_SUFFIX);
			if (suff.length() > 0){
				entry[stichw] = entry[stichw].replace(suff, "").trim();
				entry[stichw] = entry[stichw].substring(0,
						entry[stichw].indexOf(ins) + ins.length()) + suff
						+ entry[stichw].substring(entry[stichw].indexOf(ins) + ins.length());
			}
		}
		
		//MEHRERE SCHREIBWEISEN (MARKIERT MIT %)
		if (entry[stichw].contains(P_SEC_FORM)){
			entry[stichw] = entry[stichw].substring(entry[stichw].indexOf(P_SEC_FORM) + P_SEC_FORM.length());
		}
		
		//CLEAN STICHWORT
		entry[stichw]
				= entry[stichw].replaceAll(P_REMOVE_POST, "");
		
		//REMOVE GENUS OF ENTRIES CONTAINING ! OR ?
		if (entry[stichw].contains("!")
				|| entry[stichw].contains("?")){
			entry[doc.getFieldIndex(headerPrefix + "Genus")] = "";
		}
		
		//MOVE VARIANTS TO END OF ENTRY
		String variant = getMatch(entry[stichw], P_VARIANT);
		if (variant.length() > 0){
			entry[stichw] = entry[stichw].replaceAll(",\\s\\[", " ["); //PRE-VARIANTS CLEANUP
			entry[stichw] = entry[stichw].replace(variant, "").trim();
			entry[stichw] += " " + variant.trim();
		}
		
		//MOVE LEFTOVER "col" DATE TO SEMANTICS FIELD
		String col = getMatch(entry[stichw], "\\[\\p{L}+\\]\\)");
		if (col.length() > 0){
			entry[stichw] = entry[stichw].replace(col, "").trim();
			entry[doc.getFieldIndex(headerPrefix + "Semantik")] += " " + col.replaceAll("[\\(\\)]", "");
		}
		
		//CORRECT GENUS FORMAT
		entry[doc.getFieldIndex(headerPrefix + "Genus")]
				= entry[doc.getFieldIndex(headerPrefix + "Genus")].replaceFirst("m,f", "m(f)");
		entry[doc.getFieldIndex(headerPrefix + "Genus")]
				= entry[doc.getFieldIndex(headerPrefix + "Genus")].replaceFirst(" n", ",n");
		
		// "pp." -> RFlex: Angaben mit "pp."
//		String rflex = getMatch(entry[doc.getFieldIndex("RSubsemantik")], "pp\\.\\s\\p{L}+");
//		if (rflex.length() > 0){
//			entry[doc.getFieldIndex("RFlex")] = rflex.replace("pp.", "partizip perfect:");
//			entry[doc.getFieldIndex("RSubsemantik")]
//					= entry[doc.getFieldIndex("RSubsemantik")].replace(rflex, "").trim();
//		}
		
		
		// "pp." -> RFlex: Angaben ohne "pp.", aber mit "int" / "tr" / "tr/int"
//		if (headerPrefix.equals("R")){
//			if (entry[doc.getFieldIndex(headerPrefix + "Grammatik")].matches(".*(tr|int|tr/int).*")){
//				String tr = getMatch(entry[doc.getFieldIndex(headerPrefix + "Subsemantik")], "\\b\\p{L}+\\b");
//				if (tr.length() > 0){
//					entry[doc.getFieldIndex(headerPrefix + "Subsemantik")] = entry[doc.getFieldIndex(headerPrefix + "Subsemantik")].replace(tr, "").trim();
//					entry[doc.getFieldIndex("RFlex")] = tr.replaceAll(P_PARANT, "");
//				}
//			}
//		}
				
				

		
		//// GENERAL CLEANUP ////
		
		//REMOVE UNWANTED SQUARE BRACKETS
		if (entry[stichw].startsWith("[")
				&& entry[stichw].endsWith("]")){
			entry[stichw].replaceAll("[\\[\\]]", "");
		}
		
		//REPLACE ", -" WITH " -"
		entry[stichw] = entry[stichw].replace(", -", " -");
		
		//REMOVE COMMA AT BEGINING OF ENTRY
		entry[stichw] = entry[stichw].replaceAll("^,\\s", "");
		
		//REMOVE DOUBLE SPACES
		entry[stichw] = entry[stichw].replaceAll("\\s\\s", " ");
		
		//REMOVE EXCESS SYMBOLS
		entry[stichw] = entry[stichw].replaceAll("\\'\\s", "");
		
		//REMOVE TRAILING COMMAS
		entry[stichw] = entry[stichw].replaceAll(",\\B", "").trim();
		
		
		return entry;
	}
	
	private static String[] replaceLemmaAbbreviations(String[] entry, SVDocument doc, SVDocument abbrLemma){
		for (int i = 0; i < entry.length; i++) {
			while (abbrLemma.hasNextEntry()){
				String[] e = abbrLemma.nextEntry();
				entry[i] = entry[i].replaceAll("(?<=(\\s|\\A))" + e[0] + "\\.?(?=(\\s|\\z))", e[1]);
			}
		}
		return entry;
	}
	
	private static String[] processField(String[] entry, String patternString, String raw, int fieldIndex){
		Pattern pattern = Pattern.compile(patternString);
		Matcher matcher = pattern.matcher(raw);
		while (matcher.find())
			entry[fieldIndex] +=
			(entry[fieldIndex].length() > 0 ? " " : "") + matcher.group().trim();
		return entry;
	}
	
	private static String getMatch(String string, String patternString){
		Pattern pattern = Pattern.compile(patternString);
		Matcher matcher = pattern.matcher(string);
		String toReturn = "";
		if (matcher.find()){
			toReturn = matcher.group();
		}
		return toReturn;
	}
	
	private static String[] processRedirects(String[] entry, SVDocument doc){
		//CF-REDIRECTS
		if (entry[doc.getFieldIndex("RStichwort")].contains("cf.")){
			//D formatieren (cf. an den Anfang, eckige Klammern weg)
			entry[doc.getFieldIndex("DStichwort")]
					= "cf. " + entry[doc.getFieldIndex("DStichwort")].replaceAll("ß", "s");
			entry[doc.getFieldIndex("RStichwort")]
					= entry[doc.getFieldIndex("RStichwort")]
							.replaceAll(" cf.", "");
			entry[doc.getFieldIndex("RStichwort")]
					= entry[doc.getFieldIndex("RStichwort")]
							.replaceAll("[\\[\\]]", "");
			//Verweis setzen
			entry[doc.getFieldIndex("redirect_a")]
					= "searchPhrase=" + entry[doc.getFieldIndex("DStichwort")].substring(4);
		}
		
		return entry;
	}
	
	
	private static String[] replaceAbbreviations(String[] entry, SVDocument doc, SVDocument abbrD, SVDocument abbrR){
		Set<Integer> indicesD = new HashSet<Integer>(Arrays.asList(new Integer[]{
				//doc.getFieldIndex("DGrammatik"),
				doc.getFieldIndex("DSemantik"),
				doc.getFieldIndex("DSubsemantik"),
				doc.getFieldIndex("DGrammatik")}));
		
		Set<Integer> indicesR = new HashSet<Integer>(Arrays.asList(new Integer[]{
				//doc.getFieldIndex("RGrammatik"),
				doc.getFieldIndex("RSemantik"),
				doc.getFieldIndex("RSubsemantik"),
				doc.getFieldIndex("RGrammatik")}));
		
		//replace abbreviations
		for (int i = 0; i < entry.length; i++) {
			if (indicesD.contains(i)){
				while (abbrD.hasNextEntry()){
					String[] e = abbrD.nextEntry();
					entry[i] = entry[i].replaceAll("(?<=(\\b|\\A))" + e[0] + "\\.?(?=(\\P{L}|\\z))", e[1]);
				}
			}
			if (indicesR.contains(i)){
				while (abbrR.hasNextEntry()){
					String[] e = abbrR.nextEntry();
					entry[i] = entry[i].replaceAll("(?<=(\\b|\\A))" + e[0] + "\\.?(?=(\\P{L}|\\z))", e[1]);
				}
			}
		}
		
		return entry;
	}
	
	
	private static String[] getEmptyEntry(int length){
		String[] entry = new String[length];
		for (int i = 0; i < entry.length; i++) entry[i] = "";
		return entry;
	}
	
	private static void displayProcess(long step, long of, boolean reset){
		if (++processUnits >= 1000 || reset){
			processUnits = 0;
			System.out.print("\t" + step + " / " + of + "\n");
		} else {
			System.out.print(processUnits % 10 == 0 ? "#" : "");
		}
	}

}
