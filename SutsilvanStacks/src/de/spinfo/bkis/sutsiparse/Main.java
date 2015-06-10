package de.spinfo.bkis.sutsiparse;

import de.spinfo.bkis.sutsiparse.io.IO;

public class Main {

	public static void main(String[] args) {
		IO io = new IO();
		io.convertToTxt("datenXML", "sutsilvan_data.txt");
	}

}
