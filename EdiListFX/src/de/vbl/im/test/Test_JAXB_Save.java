package de.vbl.im.test;

import java.io.File;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import de.vbl.im.model.InPartner;
import de.vbl.im.tools.IMconstant;
import de.vbl.im.tools.JAXB_Util;

public class Test_JAXB_Save { // extends Application {

	static EntityManager entityManager = setupDataBase();
//	@Override
//	public void start(Stage primaryStage) {
//		
//	}

	public static void main(String[] args) {
		
		InPartner p = new InPartner();
		p.setId(1);
		p.setName("TestPartnerName");
		ObservableList<InPartner> inPartnerList = FXCollections.observableArrayList();
		inPartnerList.add(p);
		
		TypedQuery<InPartner> tq = entityManager.createQuery(
				"SELECT p FROM InPartner p ORDER BY p.name", InPartner.class);
		List<InPartner> aktuList = tq.getResultList(); 
		inPartnerList.addAll(aktuList);
		
		File file = new File("Test.xml");
		
		JAXB_Util.savePersonDataToFile(file, inPartnerList);
		
		System.out.println("Pfad:" + file.getAbsolutePath());
		System.out.println(file.toString());
		
//		launch(args);
	}

	private static EntityManager setupDataBase() {
		EntityManagerFactory factory = null;
		try 
		{
			factory = Persistence.createEntityManagerFactory(IMconstant.PERSISTENCE_UNIT_NAME);
		} catch (RuntimeException e) {
			System.out.println(e);
		}
		EntityManager em = factory.createEntityManager();
		return em;
	}


}
