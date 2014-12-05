package de.vbl.im.tools;

import java.io.File;
//import java.util.ArrayList;

//import de.vbl.im.model.*;

import javafx.stage.Stage;

//import org.controlsfx.dialog.Dialogs;

public class XStreamUtil {
	/**
	 * Loads person data from the specified file. The current person data will
	 * be replaced.
	 * 
	 * @param file
	 */
//	@SuppressWarnings("unchecked")
	public void loadPersonDataFromFile(File file, Stage primaryStage) {
//	  XStream xstream = new XStream();
//	  xstream.alias("partner", InPartner.class);
//
//	  try {
//	    String xml = FileUtil.readFile(file);
//
//	    ArrayList<InPartner> personList = (ArrayList<InPartner>) xstream.fromXML(xml);
//
//	    personList.clear();
//	    personList.addAll(personList);
//
//	    setPersonFilePath(file);
//	  } catch (Exception e) { // catches ANY exception
//	    Dialogs.showErrorDialog(primaryStage,
//	        "Could not load data from file:\n" + file.getPath(),
//	        "Could not load data", "Error", e);
//	  }
	}

	/**
	 * Saves the current person data to the specified file.
	 * 
	 * @param file
	 */
	public void savePersonDataToFile(File file) {
//	  XStream xstream = new XStream();
//	  xstream.alias("person", InPartner.class);
//
//	  // Convert ObservableList to a normal ArrayList
//	  ArrayList<Person> personList = new ArrayList<>(personList);
//
//	  String xml = xstream.toXML(personList);
//	  try {
//	    FileUtil.saveFile(xml, file);
//
//	    setPersonFilePath(file);
//	  } catch (Exception e) { // catches ANY exception
//	    Dialogs.showErrorDialog(primaryStage,
//	        "Could not save data to file:\n" + file.getPath(),
//	        "Could not save data", "Error", e);
//	  }
	}
}
