package de.vbl.ediliste.controller;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;
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
import de.vbl.ediliste.model.EdiPartner;

public class EdiPartnerController {
	private static final Logger logger = LogManager.getLogger(EdiPartnerController.class.getName());
	private static Stage primaryStage = null;
	private static EdiMainController mainCtr;
	private static EntityManager entityManager;
	private final ObjectProperty<EdiPartner> ediPartner;
	private final ObservableSet<EdiEintrag> ediEintragsSet;      // all assigned EDI-Entities
	private final IntegerProperty ediSystemAnzahl; 
	
	private EdiPartner aktPartner = null;
	
    private BooleanProperty dataIsChanged = new SimpleBooleanProperty(false);
	
	@FXML private ResourceBundle resources;
    @FXML private URL location;
    @FXML private TextField tfBezeichnung;
    @FXML private TextArea taBeschreibung;
    @FXML private TableView<EdiEmpfaenger> tvVerwendungen;
    @FXML private TableColumn<EdiEmpfaenger, String> tcEmpfaenger;
    @FXML private TableColumn<EdiEmpfaenger, String> tcEdiNr;
    @FXML private TableColumn<EdiEmpfaenger, String> tcSender;
    @FXML private TableColumn<EdiEmpfaenger, String> tcGeschaeftsobjekt;
    @FXML private TableColumn<EdiEmpfaenger, String> tcDatumAb;
    @FXML private TableColumn<EdiEmpfaenger, String> tcDatumBis;
    
    @FXML private Button btnSpeichern;
    @FXML private Button btnLoeschen;
    
    public EdiPartnerController() {
    	this.ediPartner = new SimpleObjectProperty<>(this, "ediPartner", null);
    	this.ediEintragsSet = FXCollections.observableSet();
    	this.ediSystemAnzahl = new SimpleIntegerProperty(0);
    }

	public static void start(Stage 			   primaryStage, 
							 EdiMainController mainController, 
							 EntityManager     entityManager) {
		logger.entry(primaryStage);
		EdiPartnerController.primaryStage = primaryStage;
		EdiPartnerController.mainCtr = mainController;
		EdiPartnerController.entityManager = entityManager;
		logger.exit();
	}

	@FXML
	public void initialize() {
		logger.entry();
		checkFieldsFromView();
		
		ediPartner.addListener(new ChangeListener<EdiPartner>() {
			@Override
			public void changed(ObservableValue<? extends EdiPartner> ov,
					EdiPartner oldPartner, EdiPartner newPartner) {
				log("ChangeListener<EdiPartner>",
					((oldPartner==null) ? "null" : oldPartner.getName() + " -> " 
				  + ((newPartner==null) ? "null" : newPartner.getName() )));
				if (oldPartner != null && newPartner == null) {
					ediEintragsSet.clear();
					tfBezeichnung.setText("");
					taBeschreibung.setText("");
					ediSystemAnzahl.unbind();
				}
				if (newPartner != null) {
					aktPartner = newPartner;
					readEdiListeforPartner(newPartner);
					tfBezeichnung.setText(newPartner.getName());
					if (newPartner.getBeschreibung() == null) {
						newPartner.setBeschreibung("");
					}
					taBeschreibung.setText(newPartner.getBeschreibung());
					ediSystemAnzahl.bind(aktPartner.anzSystemeProperty());
				}
				dataIsChanged.set(false);
			}
		});
		
		btnSpeichern.disableProperty().bind(Bindings.not(dataIsChanged));
		btnLoeschen.disableProperty().bind(Bindings.lessThan(0, ediSystemAnzahl));
//		btnLoeschen.disableProperty().bind(Bindings.not(Bindings.greaterThanOrEqual(0, Bindings.size(ediEintragsSet))));

		tfBezeichnung.textProperty().addListener((observable, oldValue, newValue)  -> {
			if (aktPartner.getName().equals(newValue) == false) {
				dataIsChanged.set(true);
			} else {	
				dataIsChanged.set(!checkForChangesWithMode(Checkmode.ONLY_CHECK));
			}
		}); 

		taBeschreibung.textProperty().addListener((observable, oldValue, newValue) -> {
			if (newValue.equals(aktPartner.getBeschreibung()) == false) {
				dataIsChanged.set(true);
			} else {	
				dataIsChanged.set(!checkForChangesWithMode(Checkmode.ONLY_CHECK));
			}
		});
		
//	    Setup for Sub-Panel    
		
		tcEdiNr.setCellValueFactory(cellData -> Bindings.format(EdiEintrag.FORMAT_EDINR, 
												cellData.getValue().getEdiEintrag().ediNrProperty()));

		tcSender.setCellValueFactory(cellData -> cellData.getValue().getEdiEintrag().getEdiKomponente().fullnameProperty());
		
		tcSender.setCellFactory(column -> {
			return new TableCell<EdiEmpfaenger, String>() {
				@Override
				protected void updateItem (String senderName, boolean empty) {
					super.updateItem(senderName, empty);
					if (senderName == null || empty) 
						setText(null); 
					else {
						setText(senderName);
						FontWeight fw = senderName.startsWith(aktPartner.getName()) ? FontWeight.BOLD : FontWeight.NORMAL;
						setFont(Font.font(null, fw, getFont().getSize()));
					}
				}
			};
		});
		
		tcEmpfaenger.setCellValueFactory(cellData -> cellData.getValue().getKomponente().fullnameProperty());

		tcEmpfaenger.setCellFactory(column -> {
			return new TableCell<EdiEmpfaenger, String>() {
				@Override
				protected void updateItem (String empfaengerName, boolean empty) {
					super.updateItem(empfaengerName, empty);
					if (empfaengerName == null || empty) 
						setText(null); 
					else {
						setText(empfaengerName);
						FontWeight fw = empfaengerName.startsWith(aktPartner.getName()) ? FontWeight.BOLD : FontWeight.NORMAL;
						setFont(Font.font(null, fw, getFont().getSize()));
					}
				}
			};
		});
		tcGeschaeftsobjekt.setCellValueFactory(cellData -> cellData.getValue().geschaeftsObjektNameProperty());
		tcDatumAb.setCellValueFactory(cellData -> cellData.getValue().getEdiEintrag().seitDatumProperty());
		tcDatumBis.setCellValueFactory(cellData -> cellData.getValue().getEdiEintrag().bisDatumProperty());
		
		// todo: zum Absprung bei Select eines Edi-Eintrages in der Sub-Tabelle
		tvVerwendungen.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<EdiEmpfaenger>() {
			@Override
			public void changed (ObservableValue<? extends EdiEmpfaenger> ov, EdiEmpfaenger oldValue, EdiEmpfaenger newValue) {
				log("tvVerwendungen.select.changed" ,"newValue" + newValue);
			}
		});
		logger.exit();
	}

	@FXML
	void loeschen(ActionEvent event) {
		if (ediEintragsSet.size() > 0) {
			String msg = "Fehler beim Löschen des Partners \"" + aktPartner.getName() +"\" da er verwendet wird";
			mainCtr.setErrorText(msg);
			logger.warn(msg);
			return; 
		}	
		String partnerName1 = "Partner \"" + aktPartner.getName() + "\"";
		String partnerName2 = partnerName1;
		if (aktPartner.getName().equals(tfBezeichnung.getText()) == false) {
			partnerName2 = partnerName1 + " / \"" + tfBezeichnung.getText() + "\"";
		}
		Action response = Dialogs.create()
				.owner(primaryStage).title(primaryStage.getTitle())
				.message(partnerName2 + " wirklich löschen ?")
				.showConfirm();
		if (response == Dialog.Actions.YES) {
			try {
				entityManager.getTransaction().begin();
				entityManager.remove(aktPartner);
				entityManager.getTransaction().commit();
				aktPartner = null;
				mainCtr.loadPartnerListData();
				mainCtr.setInfoText("Der " + partnerName1 + " wurde erfolgreich gelöscht !");
			} catch (RuntimeException er) {
				String msg = "Fehler beim Löschen der Partners " + partnerName1;
				logger.fatal(msg, er);
				Dialogs.create()
					.owner(primaryStage).title(primaryStage.getTitle())
					.masthead("Datenbankfehler")
				    .message(msg)
				    .showException(er);
			}
		}
	}
	
	@FXML
	void speichern(ActionEvent event) {
		checkForChangesWithMode(Checkmode.SAVE_DONT_ASK);
	}
	
	public boolean checkForChangesAndAskForSave() {
		return checkForChangesWithMode(Checkmode.ASK_FOR_UPDATE);
	}

	private static enum Checkmode { ONLY_CHECK, ASK_FOR_UPDATE, SAVE_DONT_ASK };
	
	private boolean checkForChangesWithMode(Checkmode checkmode) {
		log("checkForChangesWithMode","aktPartner=" + (aktPartner==null ? "null" : aktPartner.getName()));
		if (aktPartner == null ) {
			return true;
		}
		String orgName = aktPartner.getName();
		String newName = tfBezeichnung.getText();
		String orgBeschreibung = aktPartner.getBeschreibung()==null ? "" : aktPartner.getBeschreibung();
		String newBeschreibung = taBeschreibung.getText()==null ? "" : taBeschreibung.getText();

		if (orgName.equals(newName) &&
			orgBeschreibung.equals(newBeschreibung) ) {
			log("checkForChangesWithMode", "Name und Bezeichnung sind unverändert");
		} else {	
			if (checkPartnerName(newName) == false) {
				mainCtr.setErrorText("Ein anderer Partner heißt bereits so!");
				return false;
			}
			if (checkmode == Checkmode.ONLY_CHECK) {
				return false;
			}
			if (checkmode == Checkmode.ASK_FOR_UPDATE) {
				Action response = Dialogs.create()
    				.owner(primaryStage).title(primaryStage.getTitle())
    				.actions(Dialog.Actions.YES, Dialog.Actions.NO, Dialog.Actions.CANCEL)
    				.message("Sollen die Änderungen am Partner " + orgName + " gespeichert werden ?")
    				.showConfirm();
	    		if (response == Dialog.Actions.CANCEL) {
	    			return false;
	    		}
	    		if (response == Dialog.Actions.NO) {
	    			aktPartner = null;
	    			return true;
	    		}
			}	
			log("checkForChangesWithMode","Änderung erkannt -> update");
			entityManager.getTransaction().begin();
			aktPartner.setName(newName);
			aktPartner.setBeschreibung(newBeschreibung);
			entityManager.getTransaction().commit();
			readEdiListeforPartner(aktPartner);
			mainCtr.setInfoText("Der Partner \"" + aktPartner.getName() + "\" wurde gespeichert");
		}
		return true;
	}
	
	private boolean checkPartnerName(String newName) {
		TypedQuery<EdiPartner> tq = entityManager.createQuery(
				"SELECT p FROM EdiPartner p WHERE LOWER(p.name) = LOWER(:n)",EdiPartner.class);
		tq.setParameter("n", newName);
		List<EdiPartner> partnerList = tq.getResultList();
		for (EdiPartner p : partnerList ) {
			if (p.getId() != aktPartner.getId()) {
				if (p.getName().equalsIgnoreCase(newName)) {
					return false;
				}
			}
		}
		return true;
	}

	private void readEdiListeforPartner( EdiPartner newPartner) {
		tvVerwendungen.getItems().clear();
		ObservableList<EdiEmpfaenger> empfaengerList = FXCollections.observableArrayList();
		ediEintragsSet.clear(); 
		/* 1. lese alle EdiEinträge mit Sender = selekierter Partner 
		 * 		-> zeige jeweils alle zugehörigen Empfänger, falls kein Empfänger vorhanden dummy erzeugen
		*/
		TypedQuery<EdiEintrag> tqS = entityManager.createQuery(
				"SELECT e FROM EdiEintrag e WHERE e.ediKomponente.ediSystem.ediPartner = :p", EdiEintrag.class);
		tqS.setParameter("p", newPartner);
		List<EdiEintrag> ediList = tqS.getResultList();
		for(EdiEintrag e : ediList ) {
			ediEintragsSet.add(e);
			if (e.getEdiEmpfaenger().size() > 0) {
				empfaengerList.addAll(e.getEdiEmpfaenger());
//				for(EdiEmpfaenger ee : e.getEdiEmpfaenger() ) ediKomponenteList.add(ee); 
			} else {
				EdiEmpfaenger tmpE = new EdiEmpfaenger();
				tmpE.setEdiEintrag(e);
				empfaengerList.add(tmpE);
			}
		}
//		log("readEdiListeforKomponete", "für "+ selKomponente.getName() + " " + 
//			ediList.size() + " EDI-Einträge" + " mit insgesamt " + 
//			empfaengerList.size() + " Empfänger gelesen (Refresh=" + cache+ ")");
		
		/* 2. lese alle Empfänger mit Empfänger = selektierte Komponente 
		 *    -> zeige alle Empfänger  
		 */
		
		TypedQuery<EdiEmpfaenger> tqE = entityManager.createQuery(
				"SELECT e FROM EdiEmpfaenger e WHERE e.komponente.ediSystem.ediPartner = :p", EdiEmpfaenger.class);
		tqE.setParameter("p", newPartner);
//		ediKomponenteList.addAll(tqE.getResultList());
		for(EdiEmpfaenger e : tqE.getResultList() ) {
			log("readEdiListeforKomponete", "Empfaenger:" + e.getKomponente().getFullname() + " add");
			empfaengerList.add(e);
			ediEintragsSet.add(e.getEdiEintrag());
		}
//		log("readEdiListeforKomponete", "für " + selKomponente.getName() + " " + 
//			tqE.getResultList().size() + " EDI-Empfänger gelesen (Refresh=" + cache+ ")");
		
		tvVerwendungen.setItems(empfaengerList);
//		log("readEdiListeforKomponente","size="+ ediEintragsSet.size());
	}

	public final ObjectProperty<EdiPartner> ediPartnerProperty() {
		return ediPartner;
	}
	
	public final EdiPartner getEdiPartner() {
		return ediPartner.get() ;
	}
	
	public final void setEdiPartner(EdiPartner ediPartner) {
		this.ediPartner.set(ediPartner);
	}
    
	private static void log(String methode, String message) {
		if (message == null || methode == null) {
			String className = EdiPartnerController.class.getName().substring(16);
			System.out.println(className + "." + methode + "(): " + message); 
		}
	}

	void checkFieldsFromView() {
//    	assert ediPartnerPane != null : "fx:id=\"ediPartnerPane\" was not injected: check your FXML file 'EdiPartner.fxml'.";
    	assert tfBezeichnung != null : "fx:id=\"tfBezeichnung\" was not injected: check your FXML file 'EdiPartner.fxml'.";
    	assert taBeschreibung != null : "fx:id=\"taBeschreibung\" was not injected: check your FXML file 'EdiPartner.fxml'.";
    	assert tcEdiNr != null : "fx:id=\"tcEdiNr\" was not injected: check your FXML file 'EdiPartner.fxml'.";
    	assert tcSender != null : "fx:id=\"tcSender\" was not injected: check your FXML file 'EdiPartner.fxml'.";
        assert tcEmpfaenger != null : "fx:id=\"tcEmpfaenger\" was not injected: check your FXML file 'EdiPartner.fxml'.";
        assert tcDatumBis != null : "fx:id=\"tcDatumBis\" was not injected: check your FXML file 'EdiPartner.fxml'.";
        assert tvVerwendungen != null : "fx:id=\"tvVerwendungen\" was not injected: check your FXML file 'EdiPartner.fxml'.";
        assert btnLoeschen != null : "fx:id=\"btnLoeschen\" was not injected: check your FXML file 'EdiPartner.fxml'.";
        assert btnSpeichern != null : "fx:id=\"btnSpeichern\" was not injected: check your FXML file 'EdiPartner.fxml'.";
    }
    
}
