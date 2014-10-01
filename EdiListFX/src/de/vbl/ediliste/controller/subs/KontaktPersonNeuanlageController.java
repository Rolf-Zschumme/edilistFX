package de.vbl.ediliste.controller.subs;

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

import de.vbl.ediliste.controller.EdiMainController;
import de.vbl.ediliste.model.KontaktPerson;

public class KontaktPersonNeuanlageController implements Initializable {
	private static final Logger logger = LogManager.getLogger(KontaktPersonNeuanlageController.class.getName());

	private static Stage primaryStage = null;
    private static String applName = null;
	private static EdiMainController mainController;
    private static EntityManager entityManager = null;
	/**
	 * injection from 'KontaktPersonNeuanlage.fxml'
	 */
	@FXML private TextField tfNachname; 
	@FXML private TextField tfNummer; 
	@FXML private TextField tfVorname; 
    @FXML private TextField tfAbteilung; 
    @FXML private TextField tfMailadresse; 
    @FXML private TextField tfTelefon;
    @FXML private Label     lbHinweis;
    @FXML private Button    btnOK; 

    private KontaktPerson kontaktperson = new KontaktPerson();
    private BooleanProperty nachnameFilled = new SimpleBooleanProperty(false);
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
    	logger.info("init");
    	kontaktperson.setVorname("");
    	
    	tfNachname.textProperty().addListener((ov, oldValue, newValue) ->  {
    		kontaktperson.setNachname(newValue.trim());
    		nachnameFilled.set(newValue.trim().length() > 0);
    	}); 
    	
    	tfVorname.textProperty().addListener((ov, oldValue, newValue) -> {
    		kontaktperson.setVorname(newValue.trim());
    	});
    	
    	tfAbteilung.textProperty().addListener((ov, oldValue, newValue) -> {
    		kontaktperson.setAbteilung(newValue);
    	});
    	
    	tfTelefon.textProperty().addListener((ov, oldValue, newValue) -> {
    		kontaktperson.setTelefon(newValue.trim());
    	});
    	
    	tfMailadresse.textProperty().addListener((ov, oldValue, newValue) -> {
    		kontaktperson.setMail(newValue.trim());
    	});
    	
    	btnOK.disableProperty().bind(Bindings.not(nachnameFilled));
    }

    public void start(Stage primaryStage, EdiMainController mainController, EntityManager entityManager) {

    	KontaktPersonNeuanlageController.primaryStage = primaryStage;
    	KontaktPersonNeuanlageController.entityManager = entityManager;
    	KontaktPersonNeuanlageController.mainController = mainController;
		applName = primaryStage.getTitle();
	}


    
    @FXML
    void okPressed(ActionEvent event) {
    	if (applName == null && primaryStage == null && mainController == null && entityManager == null) {
    		;
    	}
    	if (saveInput() == true) {
    		close(event);
    	}
    }

	@FXML
    void escapePressed(ActionEvent event) {
		kontaktperson = null;
    	close(event);
    }
    
    private void close(ActionEvent event) {
    	Node source = (Node) event.getSource();
    	Stage stage = (Stage) source.getScene().getWindow();
    	stage.close();
    }
    
    private boolean saveInput() {
    	String ret = checkKontakt(kontaktperson);
    	if (ret != null) {
    		lbHinweis.textProperty().set(ret);
    		return false;
    	}
		entityManager.getTransaction().begin();
		entityManager.persist(kontaktperson);
		entityManager.getTransaction().commit();
		
		logger.info("Neuer Kontakt (" + kontaktperson.getId() + ") erfolgreich angelegt");
    	return true;
    }

	private String checkKontakt(KontaktPerson kp) {
		if ("".equals(kp.getNachname())) {
			return "Ein Nachname ist erforderlich";
		}
		TypedQuery<KontaktPerson> tq = entityManager.createQuery(
				"SELECT k FROM KontaktPerson k WHERE LOWER(k.nachname) = LOWER(:n)",KontaktPerson.class);
		tq.setParameter("n", kp.getNachname());
		List<KontaktPerson> kontaktPersonList = tq.getResultList();
		for (KontaktPerson k : kontaktPersonList) {
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

	public KontaktPerson getKontaktperson() {
		return kontaktperson;
	}
}
