package de.vbl.im.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.util.converter.NumberStringConverter;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.controlsfx.dialog.Dialog;

import de.vbl.im.model.EdiEintrag;

public class NeuerEdiEintragController {
	private static final Logger logger = LogManager.getLogger(NeuerEdiEintragController.class.getName()); 
    @FXML private TextField tfEdiNr;
    @FXML private Label fehlertext;
    
	private EntityManager entityManager;
	private Dialog.Actions response = Dialog.Actions.CANCEL;
	private EdiEintrag ediEintrag;

	public void setEntityManager(EntityManager entityManager) {
		logger.info("entered");
		this.entityManager = entityManager;
	}
	
	public Dialog.Actions getResponse() {
		return response; 
	}
	public EdiEintrag getNewEdiEintrag () {
		return ediEintrag;
	}

	public void start() {
		logger.info("entered");
		ediEintrag.setEdiNr(getHighestEdiNr()+1);
	}
	
	/* ------------------------------------------------------------------------
     * initialize() is the controllers "main"-method 
     * it is called after loading "....fxml" 
     * ----------------------------------------------------------------------*/
    @FXML
    void initialize() {
		logger.info("entered");
    	checkFieldFromView();
    	ediEintrag = new EdiEintrag();        
        setupBindings();
    }
    
    private void setupBindings() {
    	tfEdiNr.textProperty().bindBidirectional(ediEintrag.ediNrProperty(),new NumberStringConverter());
	}
    	
    @FXML
    void okPressed(ActionEvent event) {
    	int ediNr = ediEintrag.getEdiNr();
    	if (ediNr < 1) {
    		fehlertext.setText("Ein Zahl größer Null eingeben");
    	}
    	else if (isEdiNrUsed(ediNr)) {
    		fehlertext.setText("Die Nummer " + ediNr + " ist bereits vergeben - bitte ändern");
    	}
    	else {
    		entityManager.getTransaction().begin();
    		ediEintrag.setBezeichnung("(EDI-Nummer Reserviert)");
    		entityManager.persist(ediEintrag);
    		entityManager.getTransaction().commit();
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
    	Query query = entityManager.createQuery("SELECT e.ediNr FROM EdiEintrag e");
    	for (Object zeile  : query.getResultList()) {
    		Object obj = (Object) zeile;
    		int aktnr = (Integer) obj;
			if (aktnr == nr)
				return true;
    	}	
    	return false;
    }
    
    private Integer getHighestEdiNr() {
		logger.info("entered");
    	Query query = entityManager.createQuery("SELECT e.ediNr FROM EdiEintrag e ORDER BY e.ediNr");
    	Integer max = 0;
    	for (Object zeile  : query.getResultList()) {
    		Object obj = (Object) zeile;
			max = (Integer) obj; 
    	}	
    	return max;
    }
    
    private void checkFieldFromView() {
		logger.info("entered");
        assert tfEdiNr != null : "fx:id=\"tfEdiNr\" was not injected: check your FXML file 'NeuerEdiEintrag.fxml'.";
        assert fehlertext != null : "fx:id=\"fehlertext\" was not injected: check your FXML file 'NeuerEdiEintrag.fxml'.";
	}
}