package de.vbl.ediliste.tools;

import java.util.ArrayList;
import java.util.Collection;

import javafx.stage.Stage;
import javafx.stage.StageStyle;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.controlsfx.dialog.Dialog.Actions;

import de.vbl.ediliste.controller.EdiEintragController;
import de.vbl.ediliste.controller.EdiMainController;
import de.vbl.ediliste.controller.subs.KontaktPersonAuswaehlenController;
import de.vbl.ediliste.model.DokuLink;
import de.vbl.ediliste.model.Repository;


public class TestRepositoryFind {
	private static final String PERSISTENCE_UNIT_NAME = "EdiListFX";
	private static EntityManager em = null;
	
	public static void main(String[] args) {
		EntityManagerFactory factory = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME);
		em = factory.createEntityManager();
		
//		Collection<DokuLink> dokuLinkList = null; 
//		try {
//			Repository repository = new Repository("QS-Akte", em);
//			System.out.println("Repository-Location: " + repository.getLocation());
//			
////			dokuLinkList = repository.findEntries("0237", Repository.DokuArt.xSPEZ);
////			dokuLinkList = repository.findEntries("0111", Repository.DokuArt.FSPEZ);
//			dokuLinkList = repository.findEntries("TSpez_0120", Repository.DokuArt.xSPEZ);
//			
//			for (DokuLink dok : dokuLinkList) {
//				String status = dok.getStatus().toString(); while (status.length() < 14) status += " ";
//				String d_name = dok.getName()  .toString();	while (d_name.length() < 68) d_name += " "; 
//				System.out.println(dok.getRevision() + " " + status + d_name + dok.getPfad());
//			}
//			System.out.println("fertig");
//			
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		testKontaktPerson();
	}
	
	
//	static private void testKontaktPerson() {
//		EdiMainController mainCtr = new EdiMainController();
//    	Stage dialog = new Stage(StageStyle.UTILITY);
//    	KontaktPersonAuswaehlenController controller = mainCtr.loadKontaktPersonAuswahl(dialog);
//    	if (controller != null) {
//    		dialog.showAndWait();
//    		if (controller.getResponse() == Actions.OK) {
//    			System.out.println("ok");
////    			kontaktpersonList.add(controller.getKontaktperson());
//    		}
//    	}
//	}
	
//	private void 
	
	
}
