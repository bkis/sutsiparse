package de.spinfo.bkis.sutsiparse;

import de.bkis.jaseval.JASEVAL;
import de.bkis.jaseval.SVDocument;
import de.spinfo.bkis.sutsiparse.io.IO;

public class Main {
	
	

	public static void main(String[] args) {
		IO io = new IO();
		JASEVAL jaseval = new JASEVAL();
		
		// extrahierte hypercard-daten (nur reintext)
		//in eine gemeinsame datei schreiben
		//io.convertToTxt("datenXML", "sutsilvan_data.txt");
		
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
