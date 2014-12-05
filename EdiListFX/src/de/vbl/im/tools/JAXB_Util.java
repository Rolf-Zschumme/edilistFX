package de.vbl.im.tools;

import java.io.File;

import javafx.collections.ObservableList;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.controlsfx.dialog.Dialogs;

import de.vbl.im.model.InPartner;

public class JAXB_Util {
	
	/**
	 * Loads person data from the specified file. The current person data will
	 * be replaced.
	 * 
	 * @param file
	 */
	static public void loadPersonDataFromFile(File file, ObservableList<InPartner> inPartnerList) {
	    try {
	        JAXBContext context = JAXBContext
	                .newInstance(InPartnerListWrapper.class);
	        Unmarshaller um = context.createUnmarshaller();

	        // Reading XML from the file and UNmarshalling.
	        InPartnerListWrapper wrapper = (InPartnerListWrapper) um.unmarshal(file);

	        inPartnerList.clear();
	        inPartnerList.addAll(wrapper.getInPartnerList());

	        // Save the file path to the registry.
//	        setPersonFilePath(file);

	    } catch (Exception e) { // catches ANY exception
	        Dialogs.create()
	                .title("Error")
	                .masthead("Could not load data from file:\n" + file.getPath())
	                .showException(e);
	    }
	}

	/**
	 * Saves the current person data to the specified file.
	 * 
	 * @param file
	 */
	static public void savePersonDataToFile(File file, ObservableList<InPartner> inPartnerList) {
	    try {
	        JAXBContext context = JAXBContext
	                .newInstance(InPartnerListWrapper.class);
	        Marshaller m = context.createMarshaller();
	        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

	        // Wrapping inPartnerList.
	        InPartnerListWrapper wrapper = new InPartnerListWrapper();
	        wrapper.setInPartnerList(inPartnerList);

	        // MArshalling and saving XML to the file.
	        m.marshal(wrapper, file);

	        // Save the file path to the registry.
//	        setPersonFilePath(file);
	    } catch (Exception e) { // catches ANY exception
	    	System.out.println(e);
	    }
	}
}
