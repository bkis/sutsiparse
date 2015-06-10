package de.spinfo.bkis.sutsiparse.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;


public class IO {
	
	public void convertToTxt(String stackXMLDirPath, String targetPath){
		List<File> files = new ArrayList<File>();
		files = getFiles(files, new File("datenXML").toPath(), "^card_.*");
		System.out.println("Files found: " + files.size());
		StringBuilder sb = new StringBuilder();
		
		System.out.print("Reading files... ");
		for (File f : files){
			String[] entryLines = getEntryContent(f.getPath());
			for (String s : entryLines){
				sb.append(s + "\n");
			}
		}
		System.out.println("OK");
		
		System.out.print("Writing target file... ");
		try {
			FileWriter fw = new FileWriter(new File(targetPath));
			fw.write(sb.toString());
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("OK");
	}
	
	private String[] getEntryContent(String xmlPath){
		File f = new File(xmlPath);
		Scanner scanner = null;
		StringBuilder sb = new StringBuilder();
		
		try {
			scanner = new Scanner(f);
			//scanner.useDelimiter("\\<[\\/?]text\\>");
			while (scanner.hasNextLine()){
				String s = scanner.nextLine();
				sb.append(s + "\n");
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		scanner.close();
		
		String startPattern = "<id>692</id>\n\t\t<text>";
		int start = sb.toString().indexOf(startPattern) + startPattern.length();
		return sb.toString().subSequence(start, sb.toString().indexOf("</text>", start)).toString().split("\n");
	}
	
	private List<File> getFiles(List<File> files, Path dir, String namePattern) {
	    try(DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
	        for (Path path : stream) {
	            if(path.toFile().isDirectory()) {
	                getFiles(files, path, namePattern);
	            } else if (path.getFileName().toString().matches(namePattern)){
	            	files.add(path.toFile());
	                System.out.println(path.getFileName());
	            }
	        }
	    } catch(IOException e) {
	        e.printStackTrace();
	    }
	    return files;
	} 

}
