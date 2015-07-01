package de.spinfo.bkis.sutsiparse;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.bkis.jaseval.JASEVAL;
import de.bkis.jaseval.SVDocument;
import de.spinfo.bkis.sutsiparse.io.IO;

public class Main {
	
	//PATTERNS
	private static final String P_GENUS = 	"(?<=\\s)[fmn]\\.(\\s\\([fmn]\\.\\))?(?=(:|\\s|$))";
	private static final String P_GRAM = 	"(?<=\\s)(adj|adv|tr|int|tr\\/int|refl|pron|poss)\\.(?=(\\s|$))"; //TODO: unvollst.
	private static final String P_SEM = 	"(?<=\\s\\()(fig|bot|polit).?(?=\\)(\\s|$))"; //TODO: unvollst.
	private static final String P_SUBSEM = 	"(?<=\\s\\()[\\p{L}\\s]+(?=\\)(\\s|$))"; //TODO: ?
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
	 * 		&lt;[0-9]&gt;	entfernen
	 * 		\\[[0-9]\\]		entfernen
	 * 		
	 */

	public static void main(String[] args) {
		IO io = new IO();
		JASEVAL jaseval = new JASEVAL();
		
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
		
		//process entries
		String[] entries = io.readEntryLines("sutsilvan_data_clean.txt");
		String[] raw;
		for (String s : entries){
			raw = s.split(" Ω ");
			String[] entry = getEmptyEntry(doc.getHeader().length);
			entry = processEntry(raw[0], entry, doc, "R");
			entry = processEntry(raw[1], entry, doc, "D");
			doc.addEntry(entry);
		}
		
		//write to file
		try {
			jaseval.writeSV(doc, "\t", "data_sm.tab", "UTF-8");
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	private static String[] processEntry(String raw, String[] entry, SVDocument doc, String headerPrefix){
		Pattern pattern;
		Matcher matcher;
		
		//GENUS
		pattern = Pattern.compile(P_GENUS);
		matcher = pattern.matcher(raw);
		if (matcher.find()) entry[doc.getFieldIndex(headerPrefix + "Genus")] = matcher.group();
		raw.replaceAll(P_GENUS, "");
		
		//GRAM
		pattern = Pattern.compile(P_GRAM);
		matcher = pattern.matcher(raw);
		if (matcher.find()) entry[doc.getFieldIndex(headerPrefix + "Grammatik")] = matcher.group();
		raw.replaceAll(P_GRAM, "");
		
		//SEM
		pattern = Pattern.compile(P_SEM);
		matcher = pattern.matcher(raw);
		if (matcher.find()) entry[doc.getFieldIndex(headerPrefix + "Semantik")] = matcher.group();
		raw.replaceAll(P_SEM, "");
		
		//SUBSEM
		pattern = Pattern.compile(P_SUBSEM);
		matcher = pattern.matcher(raw);
		if (matcher.find()) entry[doc.getFieldIndex(headerPrefix + "Subsemantik")] = matcher.group();
		raw.replaceAll(P_SUBSEM, "");
		
		//STICHWORT
		//entry[doc.getFieldIndex(headerPrefix + "Stichwort")] = raw;
		
		return entry;
	}
	
	private static String[] getEmptyEntry(int length){
		String[] entry = new String[length];
		for (int i = 0; i < entry.length; i++) entry[i] = "";
		return entry;
	}

}
