package de.vbl.im.controller;

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
import javafx.scene.control.ChoiceBox;
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

import de.vbl.im.model.Ansprechpartner;
import de.vbl.im.model.InKomponente;
import de.vbl.im.model.InPartner;
import de.vbl.im.model.InSystem;
import de.vbl.im.model.InSzenario;

public class AnsprechpartnerController {
	private static final Logger logger = LogManager.getLogger(AnsprechpartnerController.class.getName());
	private static Stage primaryStage = null;
	private static IMController mainCtr;
	private static EntityManager entityManager;
	private final ObjectProperty<Ansprechpartner> ansprechpartner;
	private final ObservableList<InKomponente> inKomponentenList = FXCollections.observableArrayList();
	private final ObservableList<InSzenario> inSzenarioList = FXCollections.observableArrayList();
	private Ansprechpartner aktAnsprechpartner = null;
	
	private BooleanProperty dataIsChanged = new SimpleBooleanProperty(false);
	
	@FXML private ResourceBundle resources;
    @FXML private URL location;

	@FXML private TextField tfNummer;
	@FXML private ChoiceBox<String> m_Art;
	@FXML private TextField tfNachname; 
	@FXML private TextField tfVorname; 
    @FXML private TextField tfAbteilung; 
    @FXML private TextField tfMailadresse; 
    @FXML private TextField tfTelefon;
    
    @FXML private TableView<InKomponente> tvKomponenten;
    @FXML private TableColumn<InKomponente, String> tcPartnerName;
    @FXML private TableColumn<InKomponente, String> tcSystemName;
    @FXML private TableColumn<InKomponente, String> tcKomponentenName;
    
    @FXML private TableView<InSzenario> tvInSzenario;
    @FXML private TableColumn<InSzenario, String> tcInSzenario;
    @FXML private TableColumn<InSzenario, String> tcIsNr;
    
    @FXML private Button btnSpeichern;
    @FXML private Button btnLoeschen;
    
    public AnsprechpartnerController() {
    	this.ansprechpartner = new SimpleObjectProperty<>(this, "ansprechpartner", null);
    }

	public void setParent(IMController managerController) {
		logger.entry();
		AnsprechpartnerController.mainCtr = managerController;
		AnsprechpartnerController.primaryStage = IMController.getStage();
		AnsprechpartnerController.entityManager = managerController.getEntityManager();
		logger.exit();
	}

	@FXML
	public void initialize() {
		checkFieldsFromView();
		
		ansprechpartner.addListener(new ChangeListener<Ansprechpartner>() {
			@Override
			public void changed(ObservableValue<? extends Ansprechpartner> ov,
					Ansprechpartner oldPerson, Ansprechpartner newPerson) {
				logger.trace(((oldPerson==null) ? "null" : oldPerson.getNachname()) + " -> " 
						   + ((newPerson==null) ? "null" : newPerson.getNachname()) );
				btnLoeschen.disableProperty().unbind();
				if (oldPerson != null) {
					inKomponentenList.clear();
					inSzenarioList.clear();
				}
				tfNachname.setText("");
				tfVorname.setText("");
				if (newPerson != null) {
					aktAnsprechpartner = newPerson;
					logger.trace("newPerson.Name="+ newPerson.getNachname());
					readInKomponentenListeforPerson();
					readInSzeanrioListeforPerson();
					tfNummer.setText(newPerson.getNummer());
					m_Art.getSelectionModel().select(newPerson.getArtLong());
					if (newPerson.getNachname() == null) newPerson.setNachname("");
					tfNachname.setText(newPerson.getNachname());
					if (newPerson.getVorname() == null) newPerson.setVorname("");
					tfVorname.setText(newPerson.getVorname());
					tfAbteilung.setText(newPerson.getAbteilungSafe());
					if (newPerson.getMail() == null) newPerson.setMail("");
					tfMailadresse.setText(newPerson.getMail());
					if (newPerson.getTelefon() == null) newPerson.setTelefon("");
					tfTelefon.setText(newPerson.getTelefon());
//					BooleanBinding hasKomponent = Bindings.lessThan(0,Bindings.size(inKomponentenList));
//					BooleanBinding hasSzenario  = Bindings.lessThan(0,Bindings.size(inSzenarioList));
//					btnLoeschen.disableProperty().bind(hasKomponent.or(hasSzenario)); 
					btnLoeschen.disableProperty().bind(Bindings.lessThan(0,Bindings.size(inKomponentenList))
												   .or(Bindings.lessThan(0,Bindings.size(inSzenarioList))));
				}
				dataIsChanged.set(false);
			}
		});
		
		m_Art.getItems().addAll(Ansprechpartner.valuesOfArt);
		m_Art.valueProperty().addListener( (observable, oldValue, newValue) -> {
			dataIsChanged.set(!checkForChangesAndSave(Checkmode.ONLY_CHECK));
		});
		
		btnSpeichern.disableProperty().bind(Bindings.not(dataIsChanged));

		tfNachname.textProperty().addListener((observable, oldValue, newValue)  -> {
			if (aktAnsprechpartner != null) {
				String msg = "";
				if (aktAnsprechpartner.getNachname().equals(newValue) == false) {
					msg = checkAnsprechpartnerName(newValue, tfVorname.getText(), tfAbteilung.getText());
					dataIsChanged.set(true);
				} else {	
					dataIsChanged.set(!checkForChangesAndSave(Checkmode.ONLY_CHECK));
				}
				mainCtr.setErrorText(msg);			
			}
		}); 

		tfVorname.textProperty().addListener((observable, oldValue, newValue) -> {
			if (aktAnsprechpartner != null) {
				String msg = "";
				if (newValue.equals(aktAnsprechpartner.getVorname()) == false) {
					msg = checkAnsprechpartnerName(tfNachname.getText(), newValue, tfAbteilung.getText());
					dataIsChanged.set(true);
				} else {	
					dataIsChanged.set(!checkForChangesAndSave(Checkmode.ONLY_CHECK));
				}
				mainCtr.setErrorText(msg);			
			}
		});
		
		tfAbteilung.textProperty().addListener((observable, oldValue, newValue) -> {
			if (aktAnsprechpartner != null) {
				String msg = "";
				if (newValue.equals(aktAnsprechpartner.getAbteilungSafe()) == false) {
					msg = checkAnsprechpartnerName(tfNachname.getText(), tfVorname.getText(), newValue);
					dataIsChanged.set(true);
				} else {	
					dataIsChanged.set(!checkForChangesAndSave(Checkmode.ONLY_CHECK));
				}
				mainCtr.setErrorText(msg);			
			}
		});
		
		tfMailadresse.textProperty().addListener((observable, oldValue, newValue) -> {
			if (aktAnsprechpartner != null) {
				String msg = "";
				if (newValue.equals(aktAnsprechpartner.getMail()) == false) {
					dataIsChanged.set(true);
				} else {	
					dataIsChanged.set(!checkForChangesAndSave(Checkmode.ONLY_CHECK));
				}
				mainCtr.setErrorText(msg);			
			}
		});
		
		tfTelefon.textProperty().addListener((observable, oldValue, newValue) -> {
			if (aktAnsprechpartner != null) {
				String msg = "";
				if (newValue.equals(aktAnsprechpartner.getTelefon()) == false) {
					dataIsChanged.set(true);
				} else {	
					dataIsChanged.set(!checkForChangesAndSave(Checkmode.ONLY_CHECK));
				}
				mainCtr.setErrorText(msg);			
			}
		});
		
//	    Setup for Sub-Panels    
		
		tvKomponenten.setItems(inKomponentenList);
		tcKomponentenName.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
		tcSystemName.setCellValueFactory(cellData -> cellData.getValue().getInSystem().nameProperty());
		tcPartnerName.setCellValueFactory(cellData -> cellData.getValue().getInSystem().getinPartner().nameProperty());
		
		tvInSzenario.setItems(inSzenarioList);
		tcIsNr.setCellValueFactory(cellData -> Bindings.format(InSzenario.FORMAT_ISNR, 
												     cellData.getValue().getIsNr()));
		tcInSzenario.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
	}

	@FXML
	void loeschen(ActionEvent event) {
		if (inKomponentenList.size() > 0) {
			mainCtr.setErrorText("Fehler: L�schung nicht m�glich da Eintragungen vorhanden");
			return;
		}	
		if (inSzenarioList.size() > 0) {
			mainCtr.setErrorText("Fehler: L�schung nicht m�glich da Eintragungen vorhanden");
			return;
		}	
		String aktName = "Ansprechpartner \"" + aktAnsprechpartner.getNachname() + "\"";
		String neuName = aktName;
		if (aktAnsprechpartner.getNachname().equals(tfNachname.getText()) == false) {
			neuName = aktName + " / \"" + tfNachname.getText() + "\"";
		}
		Action response = Dialogs.create()
				.owner(primaryStage).title(primaryStage.getTitle())
				.message(neuName +  " wirklich l�schen ?")
				.showConfirm();
		if (response == Dialog.Actions.YES) {
			try {
				entityManager.getTransaction().begin();
				entityManager.remove(aktAnsprechpartner);
				entityManager.getTransaction().commit();
				aktAnsprechpartner = null;
				mainCtr.loadAnsprechpartnerListData();
				mainCtr.setInfoText("Das " + aktName + " wurde erfolgreich gel�scht !");
			} catch (RuntimeException er) {
				Dialogs.create()
					.owner(primaryStage).title(primaryStage.getTitle())
					.masthead("Datenbankfehler")
				    .message("Fehler beim L�schen der Komponente " + aktName)
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
		logger.trace("aktSystem=" + (aktAnsprechpartner==null ? "null" : aktAnsprechpartner.getVorname() + " " + aktAnsprechpartner.getNachname()));
		if (aktAnsprechpartner == null ) {
			return true;
		}
		String orgArt       = aktAnsprechpartner.getArtLong();
		String newArt       = m_Art.getValue();
		String orgNachname  = aktAnsprechpartner.getNachname() == null ? "" : aktAnsprechpartner.getNachname();
		String newNachname  = tfNachname.getText()           == null ? "" : tfNachname.getText() ;
		String orgVorname   = aktAnsprechpartner.getVorname()  == null ? "" : aktAnsprechpartner.getVorname();
		String newVorname   = tfVorname.getText()            == null ? "" : tfVorname.getText();
		String orgAbteilung = aktAnsprechpartner.getAbteilungSafe();
		String newAbteilung = tfAbteilung.getText()          == null ? "" : tfAbteilung.getText();
		String orgMail      = aktAnsprechpartner.getMail()     == null ? "" : aktAnsprechpartner.getMail();
		String newMail      = tfMailadresse.getText()        == null ? "" : tfMailadresse.getText();
		String orgTelefon   = aktAnsprechpartner.getTelefon()  == null ? "" : aktAnsprechpartner.getTelefon();
		String newTelefon   = tfTelefon.getText()            == null ? "" : tfTelefon.getText();
		
		if (orgArt.equals(newArt)             &&
			orgNachname.equals(newNachname)   &&
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
						.message("Sollen die �nderungen an dem Kontakt " + orgNachname + " gespeichert werden ?")
						.showConfirm();
				if (response == Dialog.Actions.CANCEL) {
					return false;
				}
				if (response == Dialog.Actions.NO) {
					aktAnsprechpartner = null;
					return true;
				}
			}
			if (!orgNachname.equals(newNachname)   ||
				!orgVorname.equals(newVorname)     ||
				!orgAbteilung.equals(newAbteilung)   )    
			{
				String msg = checkAnsprechpartnerName(newNachname, newVorname, newAbteilung);
				if (msg != null) {
					mainCtr.setErrorText(msg);
					tfNachname.requestFocus();
					return false;
				}
			}
			logger.info("Update Ansprechpartner " + newNachname + ", " + newVorname);
			try {
				entityManager.getTransaction().begin();
				aktAnsprechpartner.setArt(newArt);
				aktAnsprechpartner.setNachname(newNachname);
				aktAnsprechpartner.setVorname(newVorname);
				aktAnsprechpartner.setAbteilung(newAbteilung);
				aktAnsprechpartner.setTelefon(newTelefon);
				aktAnsprechpartner.setMail(newMail);
				entityManager.getTransaction().commit();
				mainCtr.setInfoText("Die Daten des Ansprechpartners '"+newNachname+"' wurden gespeichert");
				dataIsChanged.set(false);
	    	} catch (RuntimeException e) {
	    		logger.error("Message:"+ e.getMessage(),e);
				Dialogs.create().owner(primaryStage)
					.title(primaryStage.getTitle())
					.masthead("FEHLER")
					.message("Fehler beim Speichern der Daten des Ansprechpartners:\n" + e.getMessage())
					.showException(e);
	    	}
			readInKomponentenListeforPerson();
			readInSzeanrioListeforPerson();
		}
		return true;
	}
	
	private String checkAnsprechpartnerName(String nachname, String vorname, String abteilung) {
		if ("".equals(nachname)) {
			return "Ein Nachname ist erforderlich";
		}
		TypedQuery<Ansprechpartner> tq = entityManager.createQuery(
				"SELECT k FROM Ansprechpartner k WHERE LOWER(k.nachname) = LOWER(:n)",Ansprechpartner.class);
		tq.setParameter("n", nachname);
		List<Ansprechpartner> ansprechpartnerList = tq.getResultList();
		for (Ansprechpartner k : ansprechpartnerList) {
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

	protected void readInKomponentenListeforPerson() {
		inKomponentenList.clear();
		TypedQuery<InKomponente> tqK = entityManager.createQuery(
				"SELECT k FROM InKomponente k", InKomponente.class);
		List<InKomponente> resultList = tqK.getResultList();
		for(InKomponente k : resultList ) {
			if (k.getAnsprechpartner().size() > 0 &&  
				k.getAnsprechpartner().contains(aktAnsprechpartner) ) {
				inKomponentenList.add(k);
			}
		}
		TypedQuery<InSystem> tqS = entityManager.createQuery(
				"SELECT s FROM InSystem s", InSystem.class);
		List<InSystem> inSystemList = tqS.getResultList();
		for(InSystem s : inSystemList ) {
			if (s.getAnsprechpartner().size() > 0 &&  
				s.getAnsprechpartner().contains(aktAnsprechpartner) ) {
				InPartner tmpInPartner = new InPartner(s.getinPartner().getName());
				InSystem tmpInSystem = new InSystem(s.getName(), tmpInPartner);
				inKomponentenList.add(new InKomponente("-", tmpInSystem));
			}
		}
		TypedQuery<InPartner> tqP = entityManager.createQuery(
				"SELECT p FROM InPartner p", InPartner.class);
		List<InPartner> inPartnerList = tqP.getResultList();
		for(InPartner p : inPartnerList ) {
			if (p.getAnsprechpartner().size() > 0 &&  
				p.getAnsprechpartner().contains(aktAnsprechpartner) ) {
				InPartner tmpinPartner = new InPartner(p.getName());
				InSystem tmpInSystem = new InSystem("-", tmpinPartner);
				inKomponentenList.add(new InKomponente("-", tmpInSystem));
			}
		}
		logger.trace("fuer "+ aktAnsprechpartner.getNachname() + " " + 
			inKomponentenList.size() + " Komponenten gefunden");
	}

	protected void readInSzeanrioListeforPerson() {
		inSzenarioList.clear();
		TypedQuery<InSzenario> tqK = entityManager.createQuery(
				"SELECT i FROM InSzenario i", InSzenario.class);
		List<InSzenario> resultList = tqK.getResultList();
		for(InSzenario k : resultList ) {
			if (k.getAnsprechpartner().size() > 0 &&  
				k.getAnsprechpartner().contains(aktAnsprechpartner) ) {
				inSzenarioList.add(k);
			}
		}
	}
	
	public final ObjectProperty<Ansprechpartner> ansprechpartnerProperty() {
		return ansprechpartner;
	}
	
	public final Ansprechpartner getAnsprechpartner() {
		return ansprechpartner.get() ;
	}
	
	public final void setInSystem(Ansprechpartner ansprechpartner) {
		this.ansprechpartner.set(ansprechpartner);
	}
    
    void checkFieldsFromView() {
    	assert tfNummer      	 != null : "fx:id=\"tfNummer\" was not injected: check your FXML file 'Ansprechpartner.fxml'.";
    	assert tfNachname    	 != null : "fx:id=\"tfNachname\" was not injected: check your FXML file 'Ansprechpartner.fxml'.";
    	assert tfVorname     	 != null : "fx:id=\"tfVorname\" was not injected: check your FXML file 'Ansprechpartner.fxml'.";
    	assert tfAbteilung   	 != null : "fx:id=\"tfAbteilung\" was not injected: check your FXML file 'Ansprechpartner.fxml'.";
        assert tfMailadresse	 != null : "fx:id=\"tfMailadresse\" was not injected: check your FXML file 'Ansprechpartner.fxml'.";
        assert tfTelefon		 != null : "fx:id=\"tfTelefon\" was not injected: check your FXML file 'Ansprechpartner.fxml'.";
        assert tvKomponenten	 != null : "fx:id=\"tvKomponenten\" was not injected: check your FXML file 'Ansprechpartner.fxml'.";
        assert tcPartnerName     != null : "fx:id=\"tcPartnerName\" was not injected: check your FXML file 'Ansprechpartner.fxml'.";
        assert tcSystemName      != null : "fx:id=\"tcSystemName\" was not injected: check your FXML file 'Ansprechpartner.fxml'.";
        assert tcKomponentenName != null : "fx:id=\"tcKomponentenName\" was not injected: check your FXML file 'Ansprechpartner.fxml'.";

        assert btnSpeichern  	 != null : "fx:id=\"btnSpeichern\" was not injected: check your FXML file 'Ansprechpartner.fxml'.";
        assert btnLoeschen   	 != null : "fx:id=\"btnLoeschen\" was not injected: check your FXML file 'Ansprechpartner.fxml'.";
        assert tvInSzenario  	 != null : "fx:id=\"tvInSzenario\" was not injected: check your FXML file 'Ansprechpartner.fxml'.";
        assert tcInSzenario 	 != null : "fx:id=\"tcInSzenario\" was not injected: check your FXML file 'Ansprechpartner.fxml'.";
        assert tcIsNr 			 != null : "fx:id=\"tcIsNr\" was not injected: check your FXML file 'Ansprechpartner.fxml'.";
    }
}
