package de.vbl.im.controller.subs;

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

import de.vbl.im.model.Integration;

public class NeueIntegrationController {
	private static final Logger logger = LogManager.getLogger(NeueIntegrationController.class.getName()); 
    @FXML private TextField tfInNr;
    @FXML private Label fehlertext;
    
	private EntityManager entityManager;
	private Dialog.Actions response = Dialog.Actions.CANCEL;
	private Integration integration;

	public void setEntityManager(EntityManager entityManager) {
		logger.info("entered");
		this.entityManager = entityManager;
	}
	
	public Dialog.Actions getResponse() {
		return response; 
	}
	public Integration getNewIntegration () {
		return integration;
	}

	public void start() {
		logger.info("entered");
		integration.setInNr(getHighestInNr()+1);
	}
	
	/* ------------------------------------------------------------------------
     * initialize() is the controllers "main"-method 
     * it is called after loading "....fxml" 
     * ----------------------------------------------------------------------*/
    @FXML
    void initialize() {
		logger.info("entered");
    	checkFieldFromView();
    	integration = new Integration();        
        setupBindings();
    }
    
    private void setupBindings() {
    	tfInNr.textProperty().bindBidirectional(integration.inNrProperty(),new NumberStringConverter());
	}
    	
    @FXML
    void okPressed(ActionEvent event) {
    	int inNr = integration.getInNr();
    	if (inNr < 1) {
    		fehlertext.setText("Ein Zahl größer Null eingeben");
    	}
    	else if (isInNrUsed(inNr)) {
    		fehlertext.setText("Die Nummer " + inNr + " ist bereits vergeben - bitte ändern");
    	}
    	else {
    		entityManager.getTransaction().begin();
    		integration.setBezeichnung("(I-Nummer Reserviert)");
    		entityManager.persist(integration);
    		entityManager.getTransaction().commit();
    		response = Dialog.Actions.OK;
    		unbind();
    		close(event);
    	}
    }

    @FXML
    void escapePressed(ActionEvent event) {
		unbind();
    	integration = null;
    	close(event);
    }
    
    private void unbind() {
    	tfInNr.textProperty().unbindBidirectional(integration.inNrProperty());
    }
    
    private void close(ActionEvent event) {
    	Node source = (Node) event.getSource();
    	Stage stage = (Stage) source.getScene().getWindow();
    	stage.close();
    }

	/* *****************************************************************************
	 * 
	 * ****************************************************************************/
    private boolean isInNrUsed(int nr) {
    	Query query = entityManager.createQuery("SELECT e.inNr FROM Integration e");
    	for (Object zeile  : query.getResultList()) {
    		Object obj = (Object) zeile;
    		int aktnr = (Integer) obj;
			if (aktnr == nr)
				return true;
    	}	
    	return false;
    }
    
    private Integer getHighestInNr() {
		logger.info("entered");
    	Query query = entityManager.createQuery("SELECT e.inNr FROM Integration e ORDER BY e.inNr");
    	Integer max = 0;
    	for (Object zeile  : query.getResultList()) {
    		Object obj = (Object) zeile;
			max = (Integer) obj; 
    	}	
    	return max;
    }
    
    private void checkFieldFromView() {
		logger.info("entered");
        assert tfInNr != null : "fx:id=\"tfInNr\" was not injected: check your FXML file 'NeueIntegration.fxml'.";
        assert fehlertext != null : "fx:id=\"fehlertext\" was not injected: check your FXML file 'NeueIntegration.fxml'.";
	}
}