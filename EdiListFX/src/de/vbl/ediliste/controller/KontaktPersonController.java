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
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.controlsfx.control.action.Action;
import org.controlsfx.dialog.Dialog;
import org.controlsfx.dialog.Dialogs;

import de.vbl.ediliste.model.EdiEintrag;
import de.vbl.ediliste.model.EdiEmpfaenger;
import de.vbl.ediliste.model.EdiKomponente;
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

	@FXML private TextField tfNachname; 
	@FXML private TextField tfNummer; 
	@FXML private TextField tfVorname; 
    @FXML private TextField tfAbteilung; 
    @FXML private TextField tfMailadresse; 
    @FXML private TextField tfTelefon;
    
    @FXML private TableView<EdiKomponente> tvSysteme;
    @FXML private TableColumn<EdiKomponente, String> tcPartnerName;
    @FXML private TableColumn<EdiKomponente, String> tcSystemName;
    @FXML private TableColumn<EdiKomponente, String> tcKomponentenName;
    
    @FXML private Button btnSpeichern;
    @FXML private Button btnLoeschen;
    
    public KontaktPersonController() {
    	this.kontaktPerson = new SimpleObjectProperty<>(this, "kontaktPerson", null);
    }

	public static void start(Stage 			   primaryStage, 
							 EdiMainController mainController, 
							 EntityManager     entityManager) {
		KontaktPersonController.primaryStage = primaryStage;
		KontaktPersonController.mainCtr = mainController;
		KontaktPersonController.entityManager = entityManager;
	}

	@FXML
	public void initialize() {
		checkFieldsFromView();
		
		kontaktPerson.addListener(new ChangeListener<KontaktPerson>() {
			@Override
			public void changed(ObservableValue<? extends KontaktPerson> ov,
					KontaktPerson oldPerson, KontaktPerson newPerson) {
				logger.info(((oldPerson==null) ? "null" : oldPerson.getNachname()) + " -> " 
						  + ((newPerson==null) ? "null" : newPerson.getNachname()) );
				btnLoeschen.disableProperty().unbind();
				if (oldPerson != null) {
					ediKomponentenList.clear();
				}
				tfNachname.setText("");
				tfVorname.setText("");
				if (newPerson != null) {
					aktKontaktPerson = newPerson;
					logger.info("newPerson.Name="+ newPerson.getNachname());
					readEdiKomponentenListeforPerson(newPerson);
					tfBezeichnung.setText(newSystem.getName());
					if (newSystem.getBeschreibung() == null) {
						newSystem.setBeschreibung("");
					}
					taBeschreibung.setText(newSystem.getBeschreibung());
					btnLoeschen.disableProperty().bind(Bindings.lessThan(0, aktKontaktPerson.anzKomponentenProperty()));
				}
				dataIsChanged.set(false);
			}
		});
		
		btnSpeichern.disableProperty().bind(Bindings.not(dataIsChanged));

		tfBezeichnung.textProperty().addListener((observable, oldValue, newValue)  -> {
			if (aktKontaktPerson == null) {
				logger.error("aktSystem==null in Listener for tfBezeichnung");
			}
			else if (aktKontaktPerson.getName() == null) {
				logger.error("aktSystem.getName()==null in Listener for tfBezeichnung");
			} else {	
				String msg = "";
				if (aktKontaktPerson.getName().equals(newValue) == false) {
					msg = checkSystemName(newValue);
					dataIsChanged.set(true);
				} else {	
					dataIsChanged.set(!checkForChangesAndSave(Checkmode.ONLY_CHECK));
				}
				mainCtr.setErrorText(msg);			
			}
		}); 

		taBeschreibung.textProperty().addListener((observable, oldValue, newValue) -> {
			if (newValue.equals(aktKontaktPerson.getBeschreibung()) == false) {
				dataIsChanged.set(true);
			} else {	
				dataIsChanged.set(!checkForChangesAndSave(Checkmode.ONLY_CHECK));
			}
		});
		
//	    Setup for Sub-Panel    
		
		tvVerwendungen.setItems(ediKomponentenList);
		tcEdiNr.setCellValueFactory(cellData -> 
					Bindings.format(EdiEintrag.FORMAT_EDINR, cellData.getValue().getEdiNrProperty()));
		
		tcSender.setCellValueFactory(cellData -> cellData.getValue().senderNameProperty());
		tcSender.setCellFactory(column -> {
			return new TableCell<EdiEmpfaenger, String>() {
				@Override
				protected void updateItem (String sender, boolean empty) {
					super.updateItem(sender, empty);
					if (sender == null || empty) 
						setText(null); 
					else {
						setText(sender);
						FontWeight fw = sender.startsWith(aktFullName) ? FontWeight.BOLD : FontWeight.NORMAL;
						setFont(Font.font(null, fw, getFont().getSize()));
					}
				}
			};
		});
		
		tcEmpfaenger.setCellValueFactory(cellData -> cellData.getValue().empfaengerNameProperty());
		tcEmpfaenger.setCellFactory(column -> {
			return new TableCell<EdiEmpfaenger, String>() {
				@Override
				protected void updateItem (String empf, boolean empty) {
					super.updateItem(empf, empty);
					if (empf == null || empty) 
						setText(null); 
					else {
						setText(empf);
						FontWeight fw = empf.startsWith(aktFullName) ? FontWeight.BOLD : FontWeight.NORMAL;
						setFont(Font.font(null, fw, getFont().getSize()));
					}
				}
			};
		});
		tcGeschaeftsobjekt.setCellValueFactory(cellData -> cellData.getValue().geschaeftsObjektNameProperty());
		tcDatumAb.setCellValueFactory(cellData -> cellData.getValue().getEdiEintrag().seitDatumProperty());
		tcDatumBis.setCellValueFactory(cellData -> cellData.getValue().getEdiEintrag().bisDatumProperty());
	}

	@FXML
	void loeschen(ActionEvent event) {
		if (ediKomponentenList.size() > 0) {
			mainCtr.setErrorText("Fehler: Komponente wird verwendet");
			return;
		}	
		String aktName = "System \"" + aktKontaktPerson.getName() + "\"";
		String neuName = aktName;
		if (aktKontaktPerson.getName().equals(tfBezeichnung.getText()) == false) {
			neuName = aktName + " / \"" + tfBezeichnung.getText() + "\"";
		}
		Action response = Dialogs.create()
				.owner(primaryStage).title(primaryStage.getTitle())
				.message(neuName + " des Partners " + " \"" + 
				aktKontaktPerson.getEdiPartner().getName() + "\" wirklich löschen ?")
				.showConfirm();
		if (response == Dialog.Actions.YES) {
			try {
				aktKontaktPerson.getEdiPartner().getEdiSystem().remove(aktKontaktPerson);
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
		logger.info("aktSystem=" + (aktKontaktPerson==null ? "null" : aktKontaktPerson.getFullname()));
		if (aktKontaktPerson == null ) {
			return true;
		}
		String orgName = aktKontaktPerson.getName();
		String newName = tfBezeichnung.getText();
		String orgBeschreibung = aktKontaktPerson.getBeschreibung()==null ? "" : aktKontaktPerson.getBeschreibung();
		String newBeschreibung = taBeschreibung.getText()==null ? "" : taBeschreibung.getText();
		
		if (orgName.equals(newName) &&
			orgBeschreibung.equals(newBeschreibung) ) {
			logger.info("Name und Bezeichnung unveraendert");
		} else {
			if (checkmode == Checkmode.ONLY_CHECK) {
				return false;
			}
			if (checkmode == Checkmode.ASK_FOR_UPDATE) {
				Action response = Dialogs.create()
						.owner(primaryStage).title(primaryStage.getTitle())
						.actions(Dialog.Actions.YES, Dialog.Actions.NO, Dialog.Actions.CANCEL)
						.message("Sollen die Änderungen an dem System " + orgName + " gespeichert werden ?")
						.showConfirm();
				if (response == Dialog.Actions.CANCEL) {
					return false;
				}
				if (response == Dialog.Actions.NO) {
					aktKontaktPerson = null;
					return true;
				}
			}
			String msg = checkSystemName(newName);
			if (msg != null) {
				mainCtr.setErrorText(msg);
				tfBezeichnung.requestFocus();
				return false;
			}
			logger.info("Aenderung erkannt -> update");
			entityManager.getTransaction().begin();
			aktKontaktPerson.setName(newName);
			aktKontaktPerson.setBeschreibung(newBeschreibung);
			entityManager.getTransaction().commit();
			readEdiKomponentenListeforPerson(aktKontaktPerson);
			mainCtr.setInfoText("Das System " + orgName + " wurde gespeichert");
		}
		return true;
	}
	
	private String checkSystemName(String newName) {
		if ("".equals(newName)) {
			return "Eine Bezeichnung ist erforderlich";
		}
		TypedQuery<EdiSystem> tq = entityManager.createQuery(
				"SELECT s FROM EdiSystem s WHERE LOWER(s.name) = LOWER(:n)",EdiSystem.class);
		tq.setParameter("n", newName);
		List<EdiSystem> systemList = tq.getResultList();
		for (EdiSystem s : systemList ) {
			if (s.getId() != aktKontaktPerson.getId() &&
				s.getEdiPartner().getId() == aktKontaktPerson.getEdiPartner().getId())  {
				if (s.getName().equalsIgnoreCase(newName)) {
					return "Eine anderes System des Partners \"" +
							aktKontaktPerson.getEdiPartner().getName() + "\" heißt bereits so!";
				}
			}
		}
		return null;
	}

	private void readEdiKomponentenListeforPerson( KontaktPerson selKontaktPerson) {
		ediKomponentenList.clear();
		/* 1. lese alle EdiEintraege mit Sender = selekierter Komponente 
		 * 		-> zeige jeweils alle zugehoerigen Empfaenger, falls kein Empfaenger vorhanden dummy erzeugen
		*/
		TypedQuery<EdiEintrag> tqS = entityManager.createQuery(
				"SELECT e FROM EdiEintrag e WHERE e.ediKomponente.ediSystem = :s", EdiEintrag.class);
		tqS.setParameter("s", selKontaktPerson);
		List<EdiEintrag> ediList = tqS.getResultList();
		for(EdiEintrag e : ediList ) {
			if (e.getEdiEmpfaenger().size() > 0)
				ediKomponentenList.addAll(e.getEdiEmpfaenger());
			else {
				EdiEmpfaenger tmpE = new EdiEmpfaenger();
				tmpE.setEdiEintrag(e);
				ediKomponentenList.addAll(tmpE);
			}
		}
		logger.info("fuer "+ selKontaktPerson.getName() + " " + 
			ediList.size() + " EDI-Eintraege" + " mit insgesamt " + 
			ediKomponentenList.size() + " Empfaenger gelesen");
		
		/* 2. lese alle Empfaenger mit Empfaenger = selektierte Komponente 
		 *    -> zeige alle Empfaenger  
		 */
		
		TypedQuery<EdiEmpfaenger> tqE = entityManager.createQuery(
				"SELECT e FROM EdiEmpfaenger e WHERE e.komponente.ediSystem = :s", EdiEmpfaenger.class);
		tqE.setParameter("s", selKontaktPerson);
		ediKomponentenList.addAll(tqE.getResultList());
		logger.info("fuer " + selKontaktPerson.getName() + " " + 
			tqE.getResultList().size() + " EDI-Empfaenger gelesen");
	}

	public final ObjectProperty<EdiSystem> ediSystemProperty() {
		return kontaktPerson;
	}
	
	public final EdiSystem getEdiSystem() {
		return kontaktPerson.get() ;
	}
	
	public final void setEdiSystem(EdiSystem ediSystem) {
		this.kontaktPerson.set(ediSystem);
	}
    
    void checkFieldsFromView() {
 //   	assert ediSystemPane != null : "fx:id=\"ediSystemPane\" was not injected: check your FXML file 'EdiSystem.fxml'.";
    	assert tfBezeichnung != null : "fx:id=\"tfBezeichnung\" was not injected: check your FXML file 'EdiSystem.fxml'.";
    	assert taBeschreibung != null : "fx:id=\"taBeschreibung\" was not injected: check your FXML file 'EdiSystem.fxml'.";
    	assert tcEdiNr != null : "fx:id=\"tcEdiNr\" was not injected: check your FXML file 'EdiSystem.fxml'.";
    	assert tcSender != null : "fx:id=\"tcSender\" was not injected: check your FXML file 'EdiSystem.fxml'.";
        assert tcEmpfaenger != null : "fx:id=\"tcEmpfaenger\" was not injected: check your FXML file 'EdiSystem.fxml'.";
        assert tcDatumBis != null : "fx:id=\"tcDatumBis\" was not injected: check your FXML file 'EdiSystem.fxml'.";
        assert tvVerwendungen != null : "fx:id=\"tvVerwendungen\" was not injected: check your FXML file 'EdiSystem.fxml'.";
        assert btnSpeichern != null : "fx:id=\"btnSpeichern\" was not injected: check your FXML file 'EdiSystem.fxml'.";
        assert btnLoeschen != null : "fx:id=\"btnLoeschen\" was not injected: check your FXML file 'EdiSystem.fxml'.";
    }
    
}
