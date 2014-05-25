package de.vbl.ediliste.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.util.converter.NumberStringConverter;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;

import org.controlsfx.dialog.Dialog;

import de.vbl.ediliste.model.EdiEintrag;

public class NeuerEdiEintragController {
	private static final String PERSISTENCE_UNIT_NAME = "EdiListFX";
    
    @FXML private TextField tfEdiNr;
//  @FXML private TextField tfKurzbez;
    @FXML private Label fehlertext;
    
	private EntityManager em;
	private Dialog.Actions response = Dialog.Actions.CANCEL;
	private EdiEintrag ediEintrag;

	public Dialog.Actions getResponse() {
		return response; 
	}
	public EdiEintrag getNewEdiEintrag () {
		return ediEintrag;
	}
	/* ------------------------------------------------------------------------
     * initialize() is the controllers "main"-method 
     * it is called after loading "....fxml" 
     * ----------------------------------------------------------------------*/
    @FXML
    void initialize() {
    	checkFieldFromView();
        setupEntityManager();
        
        ediEintrag = new EdiEintrag();        
        ediEintrag.setEdiNr(getHighestEdiNr()+1);
        
        setupBindings();
    }
    
    private void setupBindings() {
    	tfEdiNr.textProperty().bindBidirectional(ediEintrag.ediNrProperty(),new NumberStringConverter());
	}
    	
    private void setupEntityManager() {
    	EntityManagerFactory factory = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME);
    	em = factory.createEntityManager();
    }
    
    @FXML
    void okPressed(ActionEvent event) {
    	int ediNr = ediEintrag.getEdiNr();
    	if (ediNr < 1) {
    		fehlertext.setText("Ein Zahl gr��er Null eingeben");
    	}
    	else if (isEdiNrUsed(ediNr)) {
    		fehlertext.setText("Die Nummer " + ediNr + " ist bereits vergeben - bitte �ndern");
    	}
    	else {
    		em.getTransaction().begin();
    		em.persist(ediEintrag);
    		em.getTransaction().commit();
    		response = Dialog.Actions.OK;
    		unbind();
    		close(event);
    	}
    }

    @FXML
    void escapePressed(ActionEvent event) {
		unbind();
    	ediEintrag = null;
    	close(event);
    }
    
    private void unbind() {
    	tfEdiNr.textProperty().unbindBidirectional(ediEintrag.ediNrProperty());
    }
    
    private void close(ActionEvent event) {
    	Node source = (Node) event.getSource();
    	Stage stage = (Stage) source.getScene().getWindow();
    	stage.close();
    }

	/* *****************************************************************************
	 * 
	 * ****************************************************************************/
    private boolean isEdiNrUsed(int nr) {
    	Query query = em.createQuery("SELECT e.ediNr FROM EdiEintrag e");
    	for (Object zeile  : query.getResultList()) {
    		Object obj = (Object) zeile;
    		int aktnr = (Integer) obj;
			if (aktnr == nr)
				return true;
    	}	
    	return false;
    }
    
    private Integer getHighestEdiNr() {
    	Query query = em.createQuery("SELECT e.ediNr FROM EdiEintrag e ORDER BY e.ediNr");
    	Integer max = 0;
    	for (Object zeile  : query.getResultList()) {
    		Object obj = (Object) zeile;
			max = (Integer) obj; 
    	}	
    	return max;
    }
    
    private void checkFieldFromView() {
        assert tfEdiNr != null : "fx:id=\"tfEdiNr\" was not injected: check your FXML file 'NeuerEdiEintrag.fxml'.";
        assert fehlertext != null : "fx:id=\"fehlertext\" was not injected: check your FXML file 'NeuerEdiEintrag.fxml'.";
	}
}