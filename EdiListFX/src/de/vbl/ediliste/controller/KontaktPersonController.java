package de.vbl.ediliste.controller;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.controlsfx.control.action.Action;
import org.controlsfx.dialog.Dialog;
import org.controlsfx.dialog.Dialogs;

import de.vbl.ediliste.model.EdiKomponente;
import de.vbl.ediliste.model.EdiPartner;
import de.vbl.ediliste.model.EdiSystem;
import de.vbl.ediliste.model.KontaktPerson;

public class KontaktPersonController {
	private static final Logger logger = LogManager.getLogger(KontaktPersonController.class.getName());
	private static Stage primaryStage = null;
	private static EdiMainController mainCtr;
	private static EntityManager entityManager;
	private final ObjectProperty<KontaktPerson> kontaktPerson;
	private final ObservableList<EdiKomponente> ediKomponentenList = FXCollections.observableArrayList();
	private KontaktPerson aktKontaktPerson = null;
	
	private BooleanProperty dataIsChanged = new SimpleBooleanProperty(false);
	
	@FXML private ResourceBundle resources;
    @FXML private URL location;

	@FXML private TextField tfNummer; 
	@FXML private TextField tfNachname; 
	@FXML private TextField tfVorname; 
    @FXML private TextField tfAbteilung; 
    @FXML private TextField tfMailadresse; 
    @FXML private TextField tfTelefon;
    
    @FXML private TableView<EdiKomponente> tvKomponenten;
    @FXML private TableColumn<EdiKomponente, String> tcPartnerName;
    @FXML private TableColumn<EdiKomponente, String> tcSystemName;
    @FXML private TableColumn<EdiKomponente, String> tcKomponentenName;
    
    @FXML private Button btnSpeichern;
    @FXML private Button btnLoeschen;
    
    public KontaktPersonController() {
    	this.kontaktPerson = new SimpleObjectProperty<>(this, "kontaktPerson", null);
    }

	public static void setParent(EdiMainController mainController) {
		logger.entry();
		KontaktPersonController.mainCtr = mainController;
		KontaktPersonController.primaryStage = EdiMainController.getStage();
		KontaktPersonController.entityManager = mainController.getEntityManager();
		logger.exit();
	}

	@FXML
	public void initialize() {
		checkFieldsFromView();
		
		kontaktPerson.addListener(new ChangeListener<KontaktPerson>() {
			@Override
			public void changed(ObservableValue<? extends KontaktPerson> ov,
					KontaktPerson oldPerson, KontaktPerson newPerson) {
				logger.trace(((oldPerson==null) ? "null" : oldPerson.getNachname()) + " -> " 
						   + ((newPerson==null) ? "null" : newPerson.getNachname()) );
				btnLoeschen.disableProperty().unbind();
				if (oldPerson != null) {
					ediKomponentenList.clear();
				}
				tfNachname.setText("");
				tfVorname.setText("");
				if (newPerson != null) {
					aktKontaktPerson = newPerson;
					logger.trace("newPerson.Name="+ newPerson.getNachname());
					readEdiKomponentenListeforPerson(newPerson);
					tfNummer.setText(newPerson.getNummer());
					if (newPerson.getNachname() == null) newPerson.setNachname("");
					tfNachname.setText(newPerson.getNachname());
					if (newPerson.getVorname() == null) newPerson.setVorname("");
					tfVorname.setText(newPerson.getVorname());
					tfAbteilung.setText(newPerson.getAbteilungSafe());
					if (newPerson.getMail() == null) newPerson.setMail("");
					tfMailadresse.setText(newPerson.getMail());
					if (newPerson.getTelefon() == null) newPerson.setTelefon("");
					tfTelefon.setText(newPerson.getTelefon());
					btnLoeschen.disableProperty().bind(Bindings.lessThan(0,Bindings.size(ediKomponentenList)));
				}
				dataIsChanged.set(false);
			}
		});
		
		btnSpeichern.disableProperty().bind(Bindings.not(dataIsChanged));

		tfNachname.textProperty().addListener((observable, oldValue, newValue)  -> {
			if (aktKontaktPerson != null) {
				String msg = "";
				if (aktKontaktPerson.getNachname().equals(newValue) == false) {
					msg = checkKontaktPersonName(newValue, tfVorname.getText(), tfAbteilung.getText());
					dataIsChanged.set(true);
				} else {	
					dataIsChanged.set(!checkForChangesAndSave(Checkmode.ONLY_CHECK));
				}
				mainCtr.setErrorText(msg);			
			}
		}); 

		tfVorname.textProperty().addListener((observable, oldValue, newValue) -> {
			if (aktKontaktPerson != null) {
				String msg = "";
				if (newValue.equals(aktKontaktPerson.getVorname()) == false) {
					msg = checkKontaktPersonName(tfNachname.getText(), newValue, tfAbteilung.getText());
					dataIsChanged.set(true);
				} else {	
					dataIsChanged.set(!checkForChangesAndSave(Checkmode.ONLY_CHECK));
				}
				mainCtr.setErrorText(msg);			
			}
		});
		
		tfAbteilung.textProperty().addListener((observable, oldValue, newValue) -> {
			if (aktKontaktPerson != null) {
				String msg = "";
				if (newValue.equals(aktKontaktPerson.getAbteilungSafe()) == false) {
					msg = checkKontaktPersonName(tfNachname.getText(), tfVorname.getText(), newValue);
					dataIsChanged.set(true);
				} else {	
					dataIsChanged.set(!checkForChangesAndSave(Checkmode.ONLY_CHECK));
				}
				mainCtr.setErrorText(msg);			
			}
		});
		
		tfMailadresse.textProperty().addListener((observable, oldValue, newValue) -> {
			if (aktKontaktPerson != null) {
				String msg = "";
				if (newValue.equals(aktKontaktPerson.getMail()) == false) {
					dataIsChanged.set(true);
				} else {	
					dataIsChanged.set(!checkForChangesAndSave(Checkmode.ONLY_CHECK));
				}
				mainCtr.setErrorText(msg);			
			}
		});
		
		tfTelefon.textProperty().addListener((observable, oldValue, newValue) -> {
			if (aktKontaktPerson != null) {
				String msg = "";
				if (newValue.equals(aktKontaktPerson.getTelefon()) == false) {
					dataIsChanged.set(true);
				} else {	
					dataIsChanged.set(!checkForChangesAndSave(Checkmode.ONLY_CHECK));
				}
				mainCtr.setErrorText(msg);			
			}
		});
		
//	    Setup for Sub-Panel    
		
		tvKomponenten.setItems(ediKomponentenList);

		tcKomponentenName.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
		
		tcSystemName.setCellValueFactory(cellData -> cellData.getValue().getEdiSystem().nameProperty());
		
		tcPartnerName.setCellValueFactory(cellData -> cellData.getValue().getEdiSystem().getEdiPartner().nameProperty());
	}

	@FXML
	void loeschen(ActionEvent event) {
		if (ediKomponentenList.size() > 0) {
			mainCtr.setErrorText("Fehler: Löschung nicht möglich da Eintragungen vorhanden");
			return;
		}	
		String aktName = "Kontakt-Person \"" + aktKontaktPerson.getNachname() + "\"";
		String neuName = aktName;
		if (aktKontaktPerson.getNachname().equals(tfNachname.getText()) == false) {
			neuName = aktName + " / \"" + tfNachname.getText() + "\"";
		}
		Action response = Dialogs.create()
				.owner(primaryStage).title(primaryStage.getTitle())
				.message(neuName +  " wirklich löschen ?")
				.showConfirm();
		if (response == Dialog.Actions.YES) {
			try {
				entityManager.getTransaction().begin();
				entityManager.remove(aktKontaktPerson);
				entityManager.getTransaction().commit();
				aktKontaktPerson = null;
				mainCtr.loadSystemListData();
				mainCtr.setInfoText("Das " + aktName + " wurde erfolgreich gelöscht !");
			} catch (RuntimeException er) {
				Dialogs.create()
					.owner(primaryStage).title(primaryStage.getTitle())
					.masthead("Datenbankfehler")
				    .message("Fehler beim Löschen der Komponente " + aktName)
				    .showException(er);
			}
		}
	}
	
	@FXML
	void speichern(ActionEvent event) {
		checkForChangesAndSave(Checkmode.SAVE_DONT_ASK);
	}
	
	public boolean checkForChangesAndAskForSave() {
		return checkForChangesAndSave(Checkmode.ASK_FOR_UPDATE);
	}

	private static enum Checkmode { ONLY_CHECK, ASK_FOR_UPDATE, SAVE_DONT_ASK };
	
	private boolean checkForChangesAndSave(Checkmode checkmode) {
		logger.trace("aktSystem=" + (aktKontaktPerson==null ? "null" : aktKontaktPerson.getVorname() + " " + aktKontaktPerson.getNachname()));
		if (aktKontaktPerson == null ) {
			return true;
		}
		String orgNachname  = aktKontaktPerson.getNachname() == null ? "" : aktKontaktPerson.getNachname();
		String newNachname  = tfNachname.getText()           == null ? "" : tfNachname.getText() ;
		String orgVorname   = aktKontaktPerson.getVorname()  == null ? "" : aktKontaktPerson.getVorname();
		String newVorname   = tfVorname.getText()            == null ? "" : tfVorname.getText();
		String orgAbteilung = aktKontaktPerson.getAbteilungSafe();
		String newAbteilung = tfAbteilung.getText()          == null ? "" : tfAbteilung.getText();
		String orgMail      = aktKontaktPerson.getMail()     == null ? "" : aktKontaktPerson.getMail();
		String newMail      = tfMailadresse.getText()        == null ? "" : tfMailadresse.getText();
		String orgTelefon   = aktKontaktPerson.getTelefon()  == null ? "" : aktKontaktPerson.getTelefon();
		String newTelefon   = tfTelefon.getText()            == null ? "" : tfTelefon.getText();
		
		if (orgNachname.equals(newNachname)   &&
			orgVorname.equals(newVorname)     &&
			orgAbteilung.equals(newAbteilung) &&
			orgMail.equals(newMail)           &&
			orgTelefon.equals(newTelefon)        ) 
		{
			logger.trace("Vorname, Nachname, Abteilung, Mail und Telefon unveraendert");
		} else {
			if (checkmode == Checkmode.ONLY_CHECK) {
				return false;
			}
			if (checkmode == Checkmode.ASK_FOR_UPDATE) {
				Action response = Dialogs.create()
						.owner(primaryStage).title(primaryStage.getTitle())
						.actions(Dialog.Actions.YES, Dialog.Actions.NO, Dialog.Actions.CANCEL)
						.message("Sollen die Änderungen an dem Kontakt " + orgNachname + " gespeichert werden ?")
						.showConfirm();
				if (response == Dialog.Actions.CANCEL) {
					return false;
				}
				if (response == Dialog.Actions.NO) {
					aktKontaktPerson = null;
					return true;
				}
			}
			if (!orgNachname.equals(newNachname)   ||
				!orgVorname.equals(newVorname)     ||
				!orgAbteilung.equals(newAbteilung)   )    
			{
				String msg = checkKontaktPersonName(newNachname, newVorname, newAbteilung);
				if (msg != null) {
					mainCtr.setErrorText(msg);
					tfNachname.requestFocus();
					return false;
				}
			}
			logger.info("Aenderung erkannt -> update");
			entityManager.getTransaction().begin();
			aktKontaktPerson.setNachname(newNachname);
			aktKontaktPerson.setVorname(newVorname);
			aktKontaktPerson.setAbteilung(newAbteilung);
			entityManager.getTransaction().commit();
			readEdiKomponentenListeforPerson(aktKontaktPerson);
			mainCtr.setInfoText("Die Daten der Kontaktperson wurde gespeichert");
		}
		return true;
	}
	
	private String checkKontaktPersonName(String nachname, String vorname, String abteilung) {
		if ("".equals(nachname)) {
			return "Ein Nachname ist erforderlich";
		}
		TypedQuery<KontaktPerson> tq = entityManager.createQuery(
				"SELECT k FROM KontaktPerson k WHERE LOWER(k.nachname) = LOWER(:n)",KontaktPerson.class);
		tq.setParameter("n", nachname);
		List<KontaktPerson> kontaktPersonList = tq.getResultList();
		for (KontaktPerson k : kontaktPersonList) {
			if (k.getVorname().equalsIgnoreCase(vorname) && 
				k.getAbteilungSafe().equalsIgnoreCase(abteilung) ) {
				if (vorname.length() > 0)   
					vorname += " ";
				if (abteilung.length() > 0) 
					abteilung = " bei " + abteilung;
				return "Es gibt schon einen \"" + vorname + k.getNachname() + "\"" + abteilung;
			}
		}
		return null;
	}

	private void readEdiKomponentenListeforPerson( KontaktPerson selKontaktPerson) {
		ediKomponentenList.clear();
		TypedQuery<EdiKomponente> tqK = entityManager.createQuery(
				"SELECT k FROM EdiKomponente k", EdiKomponente.class);
		List<EdiKomponente> ediList = tqK.getResultList();
		for(EdiKomponente k : ediList ) {
			if (k.getKontaktPerson().size() > 0 &&  
				k.getKontaktPerson().contains(selKontaktPerson) ) {
				ediKomponentenList.add(k);
			}
		}
		TypedQuery<EdiSystem> tqS = entityManager.createQuery(
				"SELECT s FROM EdiSystem s", EdiSystem.class);
		List<EdiSystem> ediSystemList = tqS.getResultList();
		for(EdiSystem s : ediSystemList ) {
			if (s.getKontaktPerson().size() > 0 &&  
				s.getKontaktPerson().contains(selKontaktPerson) ) {
				EdiPartner tmpEdiPartner = new EdiPartner(s.getEdiPartner().getName());
				EdiSystem tmpEdiSystem = new EdiSystem(s.getName(), tmpEdiPartner);
				ediKomponentenList.add(new EdiKomponente("-", tmpEdiSystem));
			}
		}
		TypedQuery<EdiPartner> tqP = entityManager.createQuery(
				"SELECT p FROM EdiPartner p", EdiPartner.class);
		List<EdiPartner> ediPartnerList = tqP.getResultList();
		for(EdiPartner p : ediPartnerList ) {
			if (p.getKontaktPerson().size() > 0 &&  
				p.getKontaktPerson().contains(selKontaktPerson) ) {
				EdiPartner tmpEdiPartner = new EdiPartner(p.getName());
				EdiSystem tmpEdiSystem = new EdiSystem("-", tmpEdiPartner);
				ediKomponentenList.add(new EdiKomponente("-", tmpEdiSystem));
			}
		}
		logger.trace("fuer "+ selKontaktPerson.getNachname() + " " + 
			ediKomponentenList.size() + " Komponenten gefunden");
	}

	public final ObjectProperty<KontaktPerson> kontaktPersonProperty() {
		return kontaktPerson;
	}
	
	public final KontaktPerson getKontaktPerson() {
		return kontaktPerson.get() ;
	}
	
	public final void setEdiSystem(KontaktPerson kontaktPerson) {
		this.kontaktPerson.set(kontaktPerson);
	}
    
    void checkFieldsFromView() {
    	assert tfNummer      != null : "fx:id=\"tfNummer\" was not injected: check your FXML file 'EdiSystem.fxml'.";
    	assert tfNachname    != null : "fx:id=\"tfNachname\" was not injected: check your FXML file 'EdiSystem.fxml'.";
    	assert tfVorname     != null : "fx:id=\"tfVorname\" was not injected: check your FXML file 'EdiSystem.fxml'.";
    	assert tfAbteilung   != null : "fx:id=\"tfAbteilung\" was not injected: check your FXML file 'EdiSystem.fxml'.";
        assert tfMailadresse != null : "fx:id=\"tfMailadresse\" was not injected: check your FXML file 'EdiSystem.fxml'.";
        assert tfTelefon     != null : "fx:id=\"tfTelefon\" was not injected: check your FXML file 'EdiSystem.fxml'.";
        assert tvKomponenten != null : "fx:id=\"tvKomponenten\" was not injected: check your FXML file 'EdiSystem.fxml'.";
        assert btnSpeichern  != null : "fx:id=\"btnSpeichern\" was not injected: check your FXML file 'EdiSystem.fxml'.";
        assert btnLoeschen   != null : "fx:id=\"btnLoeschen\" was not injected: check your FXML file 'EdiSystem.fxml'.";
    }
}
