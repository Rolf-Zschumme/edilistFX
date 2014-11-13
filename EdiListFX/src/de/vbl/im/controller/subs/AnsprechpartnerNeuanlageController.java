package de.vbl.im.controller.subs;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.controlsfx.dialog.Dialog.Actions;

import de.vbl.im.controller.IMController;
import de.vbl.im.model.Ansprechpartner;

public class AnsprechpartnerNeuanlageController implements Initializable {
	private static final Logger logger = LogManager.getLogger(AnsprechpartnerNeuanlageController.class.getName());

	private static Stage primaryStage = null;
    private static String applName = null;
	private static IMController managerController;
    private static EntityManager entityManager = null;
	/**
	 * injection from 'AnsprechpartnerNeuanlage.fxml'
	 */
	@FXML private TextField tfNachname; 
	@FXML private TextField tfNummer; 
	@FXML private TextField tfVorname; 
    @FXML private TextField tfAbteilung; 
    @FXML private TextField tfMailadresse; 
    @FXML private TextField tfTelefon;
    @FXML private Label     lbHinweis;
    @FXML private Button    btnOK; 

    private Actions retAction = Actions.CLOSE;
    private Ansprechpartner ansprechpartner = new Ansprechpartner();
    private BooleanProperty nachnameFilled = new SimpleBooleanProperty(false);
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
    	logger.info("init");
    	
        try {
			assert tfNachname != null : "fx:id=\"tfNachname\" was not injected: check your FXML file 'AnsprechpartnerNeuanlage.fxml'.";
			assert tfNummer != null : "fx:id=\"tfNummer\" was not injected: check your FXML file 'AnsprechpartnerNeuanlage.fxml'.";
			assert tfVorname != null : "fx:id=\"tfVorname\" was not injected: check your FXML file 'AnsprechpartnerNeuanlage.fxml'.";
			assert tfAbteilung != null : "fx:id=\"tfAbteilung\" was not injected: check your FXML file 'AnsprechpartnerNeuanlage.fxml'.";
			assert tfMailadresse != null : "fx:id=\"tfMailadresse\" was not injected: check your FXML file 'AnsprechpartnerNeuanlage.fxml'.";
			assert tfTelefon != null : "fx:id=\"tfTelefon\" was not injected: check your FXML file 'AnsprechpartnerNeuanlage.fxml'.";
			assert btnOK != null : "fx:id=\"btnOK\" was not injected: check your FXML file 'AnsprechpartnerNeuanlage.fxml'.";
		} catch (AssertionError e) {
			logger.error(e.getMessage(), e);
		}
    	
    	ansprechpartner.setVorname("");
    	
    	tfNachname.textProperty().addListener((ov, oldValue, newValue) ->  {
    		ansprechpartner.setNachname(newValue.trim());
    		nachnameFilled.set(newValue.trim().length() > 0);
    	}); 
    	
    	tfVorname.textProperty().addListener((ov, oldValue, newValue) -> {
    		ansprechpartner.setVorname(newValue.trim());
    	});
    	
    	tfAbteilung.textProperty().addListener((ov, oldValue, newValue) -> {
    		ansprechpartner.setAbteilung(newValue.trim());
    	});
    	
    	tfTelefon.textProperty().addListener((ov, oldValue, newValue) -> {
    		ansprechpartner.setTelefon(newValue.trim());
    	});
    	
    	tfMailadresse.textProperty().addListener((ov, oldValue, newValue) -> {
    		ansprechpartner.setMail(newValue.trim());
    	});
    	
    	btnOK.disableProperty().bind(Bindings.not(nachnameFilled));
    }

    public void start(Stage primaryStage, IMController managerController, EntityManager entityManager) {

    	AnsprechpartnerNeuanlageController.primaryStage = primaryStage;
    	AnsprechpartnerNeuanlageController.entityManager = entityManager;
    	AnsprechpartnerNeuanlageController.managerController = managerController;
		applName = primaryStage.getTitle();
	}

	public Actions getResponse () {
		return retAction;
	}

	public Ansprechpartner getAnsprechpartner() {
		return ansprechpartner;
	}
    
    @FXML
    private void okPressed(ActionEvent event) {
    	if (applName == null && primaryStage == null && managerController == null ) {
    		;
    	}
    	if (saveInput() == true) {
    		retAction = Actions.OK;
    		close(event);
    	}
    }

	@FXML
    private void escapePressed(ActionEvent event) {
		retAction = Actions.CANCEL;
    	close(event);
    }
    
    private void close(ActionEvent event) {
    	Node source = (Node) event.getSource();
    	Stage stage = (Stage) source.getScene().getWindow();
    	stage.close();
    }
    
    private boolean saveInput() {
    	String ret = checkKontakt(ansprechpartner);
    	if (ret != null) {
    		lbHinweis.textProperty().set(ret);
    		return false;
    	}
		entityManager.getTransaction().begin();
		entityManager.persist(ansprechpartner);
		entityManager.getTransaction().commit();
		
		logger.info("Neuer Kontakt (" + ansprechpartner.getId() + ") erfolgreich angelegt");
    	return true;
    }

	private String checkKontakt(Ansprechpartner kp) {
		if ("".equals(kp.getNachname())) {
			return "Ein Nachname ist erforderlich";
		}
		TypedQuery<Ansprechpartner> tq = entityManager.createQuery(
				"SELECT k FROM Ansprechpartner k WHERE LOWER(k.nachname) = LOWER(:n)",Ansprechpartner.class);
		tq.setParameter("n", kp.getNachname());
		List<Ansprechpartner> ansprechpartnerList = tq.getResultList();
		for (Ansprechpartner k : ansprechpartnerList) {
			String vorname = k.vornameProperty().getValueSafe(); 
			String abteilung = k.getAbteilungSafe();
			if (kp.getVorname().equalsIgnoreCase(vorname) && 
				kp.getAbteilungSafe().equalsIgnoreCase(abteilung) ) {
				if (vorname.length() > 0)   
					vorname += " ";
				if (abteilung.length() > 0) 
					abteilung = " bei " + abteilung;
				return "Es gibt schon einen \"" + vorname + k.getNachname() + "\"" + abteilung;
			}
		}
		return null;
	}
}
