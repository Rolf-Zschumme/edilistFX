package de.vbl.ediliste.controller;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.util.converter.NumberStringConverter;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;

import de.vbl.ediliste.model.EdiEintrag;

public class NeuerEdiEintragController {
	private static final String PERSISTENCE_UNIT_NAME = "EdiListFX";
    
    @FXML
    private TextField tfEdiNr;

    @FXML
    private TextField tfKurzbez;

    private Stage aktStage;
	private EntityManager em;
	private IntegerProperty ediNr = new SimpleIntegerProperty();
	private StringProperty kurzbez = new SimpleStringProperty();
	
    public void setStage(Stage temp) {
    	aktStage = temp;
    }
	
    /* ------------------------------------------------------------------------
     * initialize() is the controllers "main"-method 
     * it is called after loading "....fxml" 
     * ----------------------------------------------------------------------*/
    @FXML
    void initialize() {
    	checkFieldFromView();
        setupEntityManager();
        ediNr.setValue(getHighestEdiNr()+1);
        setupBindings();
    }
    
    private void setupBindings() {
    	tfEdiNr.textProperty().bindBidirectional(ediNr,new NumberStringConverter());
    	tfKurzbez.textProperty().bindBidirectional(kurzbez);
	}
    	
    private void setupEntityManager() {
    	EntityManagerFactory factory = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME);
    	em = factory.createEntityManager();
    }
    
    @FXML
    void okPressed(ActionEvent event) {
    	System.out.println("OK");
    	aktStage.close();
    }

    @FXML
    void escapePressed(ActionEvent event) {
    	System.out.println("Abbruch");
    	aktStage.close();
    }
    
    
    
    
/* *****************************************************************************
 * 
 * ****************************************************************************/
    void storeEdiEintrag() {

    	em.getTransaction().begin();
    	
    	EdiEintrag ediEintrag = new EdiEintrag();
    	ediEintrag.setEdiNr(getHighestEdiNr()+1);
    	
      	em.persist(ediEintrag);
    	em.getTransaction().commit();
    	
    }
    
    private Integer getHighestEdiNr() {
    	Query query = em.createQuery("SELECT e.id, e.ediNr, e.kurzBez FROM EdiEintrag e ORDER BY e.ediNr");
    	Integer max = 0;
    	for (Object zeile  : query.getResultList()) {
    		Object[] obj = (Object[]) zeile;
			max = (Integer) obj[1]; 
    	}	
    	return max;
    }
    
    private void checkFieldFromView() {
        assert tfEdiNr != null : "fx:id=\"tfEdiNr\" was not injected: check your FXML file 'NeuerEdiEintrag.fxml'.";
        assert tfKurzbez != null : "fx:id=\"tfKurzbez\" was not injected: check your FXML file 'NeuerEdiEintrag.fxml'.";
	}
}