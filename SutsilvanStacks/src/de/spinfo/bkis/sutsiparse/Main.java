package de.spinfo.bkis.sutsiparse;

import java.io.IOException;

import de.bkis.jaseval.JASEVAL;
import de.bkis.jaseval.SVDocument;
import de.spinfo.bkis.sutsiparse.io.IO;

public class Main {
	
	//PATTERNS
	private static final String P_GENUS = 	"\\s[fmn]\\.(:|\\s\\([fmn]\\.\\))(?=\\s)";
	private static final String P_GRAM = 	"(?<=\\s)(adj|adv|tr|int|tr\\/int|refl|pron|poss)\\.(?=\\s)"; //TODO: unvollst.
	private static final String P_SEM = 	"(?<=\\s)\\((fig|bot|polit).?\\)(?=\\s)"; //TODO: unvollst.
	private static final String P_SUBSEM = 	"(?<=\\s)\\([\\p{L}\\s]+\\)(?=\\s)"; //TODO: ?
	
	/*		
	 * 		MARKERS:
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
//			line = line.replaceAll("\\[[0-9]\\]\\s", "");
//			sb.append(line + "\n");
//		}
//		try {
//			jaseval.writeToFile(sb.toString(), "sutsilvan_data_clean.txt", "UTF-8");
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
		
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
		for (String f : fields) doc.addField(f);
		
		String[] entries = io.readEntryLines("sutsilvan_data.txt");
		
	}

}
