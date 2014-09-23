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
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
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
import de.vbl.ediliste.model.GeschaeftsObjekt;

public class GeschaeftsObjektController {
	private static final Logger logger = LogManager.getLogger(GeschaeftsObjektController.class.getName());
	private static Stage primaryStage = null;
	private static EdiMainController mainCtr;
	private static EntityManager entityManager;
	
	private final ObjectProperty<GeschaeftsObjekt> geschaeftsObjekt;
	private final ObservableSet<EdiEintrag> ediEintragsSet;      // all assigned EDI-Entities
	private final IntegerProperty ediSystemAnzahl; 
	private GeschaeftsObjekt aktGeschaeftsObjekt = null;
	
	private BooleanProperty dataIsChanged = new SimpleBooleanProperty(false);
	
	@FXML private ResourceBundle resources;
    @FXML private URL location;
    @FXML private TextField tfBezeichnung;
    @FXML private TextArea taBeschreibung;
    @FXML private TableView<EdiEmpfaenger> tvVerwendungen;
    @FXML private TableColumn<EdiEmpfaenger, String> tcEdiNr;
    @FXML private TableColumn<EdiEmpfaenger, String> tcIntegration;
    @FXML private TableColumn<EdiEmpfaenger, String> tcKonfiguration;
    @FXML private TableColumn<EdiEmpfaenger, String> tcEmpfaenger;
    @FXML private TableColumn<EdiEmpfaenger, String> tcSender;
    @FXML private TableColumn<EdiEmpfaenger, String> tcGeschaeftsobjekt;
    @FXML private TableColumn<EdiEmpfaenger, String> tcDatumAb;
    @FXML private TableColumn<EdiEmpfaenger, String> tcDatumBis;
    
    @FXML private Button btnSpeichern;
    @FXML private Button btnLoeschen;
    
    public GeschaeftsObjektController() {
    	this.geschaeftsObjekt = new SimpleObjectProperty<>(this, "geschaeftsObjekt", null);
    	this.ediEintragsSet = FXCollections.observableSet();
    	this.ediSystemAnzahl = new SimpleIntegerProperty(0);
    }

	public static void start(Stage 			   primaryStage, 
							 EdiMainController mainController, 
							 EntityManager     entityManager) {
		logger.entry(primaryStage);
		GeschaeftsObjektController.primaryStage = primaryStage;
		GeschaeftsObjektController.mainCtr = mainController;
		GeschaeftsObjektController.entityManager = entityManager;
		logger.exit();
	}

	@FXML
	public void initialize() {
		logger.entry();
		checkFieldsFromView();
		
		geschaeftsObjekt.addListener(new ChangeListener<GeschaeftsObjekt>() {
			@Override
			public void changed(ObservableValue<? extends GeschaeftsObjekt> ov,
					GeschaeftsObjekt oldGeschaeftsObjekt, GeschaeftsObjekt newGeschaeftsObjekt) {
				log("ChangeListener<EdiPartner>",
					((oldGeschaeftsObjekt==null) ? "null" : oldGeschaeftsObjekt.getName() + " -> " 
				  + ((newGeschaeftsObjekt==null) ? "null" : newGeschaeftsObjekt.getName() )));
				if (oldGeschaeftsObjekt != null && newGeschaeftsObjekt == null) {
					ediEintragsSet.clear();
					tfBezeichnung.setText("");
					taBeschreibung.setText("");
					ediSystemAnzahl.unbind();
				}
				if (newGeschaeftsObjekt != null) {
					aktGeschaeftsObjekt = newGeschaeftsObjekt;
					readEdiListeforGeschaeftsObjekt(newGeschaeftsObjekt);
					tfBezeichnung.setText(newGeschaeftsObjekt.getName());
					if (newGeschaeftsObjekt.getBeschreibung() == null) {
						newGeschaeftsObjekt.setBeschreibung("");
					}
					taBeschreibung.setText(newGeschaeftsObjekt.getBeschreibung());
				}
				dataIsChanged.set(false);
			}
		});
		
		btnSpeichern.disableProperty().bind(Bindings.not(dataIsChanged));
		btnLoeschen.disableProperty().bind(Bindings.lessThan(0, ediSystemAnzahl));
//		btnLoeschen.disableProperty().bind(Bindings.not(Bindings.greaterThanOrEqual(0, Bindings.size(ediEintragsSet))));

		tfBezeichnung.textProperty().addListener((observable, oldValue, newValue)  -> {
			String msg = "";
			if (aktGeschaeftsObjekt.getName().equals(newValue) == false) {
				msg = checkGeschaeftsObjektName(newValue);
				dataIsChanged.set(true);
			} else {	
				dataIsChanged.set(!checkForChangesWithMode(Checkmode.ONLY_CHECK));
			}
			mainCtr.setErrorText(msg);
		}); 

		taBeschreibung.textProperty().addListener((observable, oldValue, newValue) -> {
			if (newValue.equals(aktGeschaeftsObjekt.getBeschreibung()) == false) {
				dataIsChanged.set(true);
			} else {	
				dataIsChanged.set(!checkForChangesWithMode(Checkmode.ONLY_CHECK));
			}
		});
		
//	    Setup for Sub-Panel    
		
		tcEdiNr.setCellValueFactory(cellData -> Bindings.format(EdiEintrag.FORMAT_EDINR, 
												cellData.getValue().getEdiEintrag().ediNrProperty()));
		tcIntegration.setCellValueFactory(cell -> cell.getValue().getEdiEintrag().intregrationName());
		tcKonfiguration.setCellValueFactory(cell -> cell.getValue().getEdiEintrag().konfigurationName());
		tcSender.setCellValueFactory(cellData -> cellData.getValue().getEdiEintrag().getEdiKomponente().fullnameProperty());
		tcEmpfaenger.setCellValueFactory(cellData -> cellData.getValue().getKomponente().fullnameProperty());
		tcDatumAb.setCellValueFactory(cellData -> cellData.getValue().getEdiEintrag().seitDatumProperty());
		tcDatumBis.setCellValueFactory(cellData -> cellData.getValue().getEdiEintrag().bisDatumProperty());
		
		// TODO: zum Absprung bei Select eines Edi-Eintrages in der Sub-Tabelle
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
			String msg = "Fehler beim Lˆschen des Gesch‰ftsobjektes \"" + 
						  aktGeschaeftsObjekt.getName() + "\" da dieses verwendet wird";
			mainCtr.setErrorText(msg);
			logger.warn(msg);
			return; 
		}	
		String geObName1 = "Gesch‰ftsobjekt-Name \"" + aktGeschaeftsObjekt.getName() + "\"";
		String geObName2 = geObName1;
		if (aktGeschaeftsObjekt.getName().equals(tfBezeichnung.getText()) == false) {
			geObName2 = geObName1 + " / \"" + tfBezeichnung.getText() + "\"";
		}
		Action response = Dialogs.create()
				.owner(primaryStage).title(primaryStage.getTitle())
				.message(geObName2 + " wirklich lˆschen ?")
				.showConfirm();
		if (response == Dialog.Actions.YES) {
			try {
				entityManager.getTransaction().begin();
				entityManager.remove(aktGeschaeftsObjekt);
				entityManager.getTransaction().commit();
				aktGeschaeftsObjekt = null;
				mainCtr.loadGeschaeftobjektListData();
				mainCtr.setInfoText("Der GO-Name " + geObName1 + " wurde erfolgreich gelˆscht !");
			} catch (RuntimeException er) {
				String msg = "Fehler beim Lˆschen der GO-Name " + geObName1;
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
		log("checkForChangesWithMode","aktPartner=" + (aktGeschaeftsObjekt==null ? "null" : aktGeschaeftsObjekt.getName()));
		if (aktGeschaeftsObjekt == null ) {
			return true;
		}
		String orgName = aktGeschaeftsObjekt.getName();
		String newName = tfBezeichnung.getText();
		String orgBeschreibung = aktGeschaeftsObjekt.getBeschreibung()==null ? "" : aktGeschaeftsObjekt.getBeschreibung();
		String newBeschreibung = taBeschreibung.getText()==null ? "" : taBeschreibung.getText();

		if (orgName.equals(newName) &&
			orgBeschreibung.equals(newBeschreibung) ) {
			log("checkForChangesWithMode", "Name und Bezeichnung sind unver‰ndert");
		} else {	
			if (checkmode == Checkmode.ONLY_CHECK) {
				return false;
			}
			if (checkmode == Checkmode.ASK_FOR_UPDATE) {
				Action response = Dialogs.create()
    				.owner(primaryStage).title(primaryStage.getTitle())
    				.actions(Dialog.Actions.YES, Dialog.Actions.NO, Dialog.Actions.CANCEL)
    				.message("Sollen die ƒnderungen am Partner " + orgName + " gespeichert werden ?")
    				.showConfirm();
	    		if (response == Dialog.Actions.CANCEL) {
	    			return false;
	    		}
	    		if (response == Dialog.Actions.NO) {
	    			aktGeschaeftsObjekt = null;
	    			return true;
	    		}
			}	
			String msg = checkGeschaeftsObjektName(newName);
			if (msg != null) {
				mainCtr.setErrorText(msg);
				tfBezeichnung.requestFocus();
				return false;
			}
			log("checkForChangesWithMode","ƒnderung erkannt -> update");
			entityManager.getTransaction().begin();
			aktGeschaeftsObjekt.setName(newName);
			aktGeschaeftsObjekt.setBeschreibung(newBeschreibung);
			entityManager.getTransaction().commit();
			readEdiListeforGeschaeftsObjekt(aktGeschaeftsObjekt);
			mainCtr.setInfoText("Der Partner \"" + aktGeschaeftsObjekt.getName() + "\" wurde gespeichert");
		}
		return true;
	}
	
	private String checkGeschaeftsObjektName(String newName) {
		if ("".equals(newName)) {
			return "Eine Bezeichnung ist erforderlich";
		}
		TypedQuery<EdiPartner> tq = entityManager.createQuery(
				"SELECT p FROM EdiPartner p WHERE LOWER(p.name) = LOWER(:n)",EdiPartner.class);
		tq.setParameter("n", newName);
		List<EdiPartner> partnerList = tq.getResultList();
		for (EdiPartner p : partnerList ) {
			if (p.getId() != aktGeschaeftsObjekt.getId()) {
				if (p.getName().equalsIgnoreCase(newName)) {
					return "Ein anderer Partner heiﬂt bereits so!";
				}
			}
		}
		return null;
	}

	private void readEdiListeforGeschaeftsObjekt( GeschaeftsObjekt geschaeftsObjekt) {
		tvVerwendungen.getItems().clear();
		ObservableList<EdiEmpfaenger> empfaengerList = FXCollections.observableArrayList();
		ediEintragsSet.clear(); 
		/* read all EdiEmpfaenger with given GeschaeftsObjekt 
		 */
		TypedQuery<EdiEmpfaenger> tqE = entityManager.createQuery(
			"SELECT e FROM EdiEmpfaenger e WHERE e.geschaeftsObjekt = :g", EdiEmpfaenger.class);
		tqE.setParameter("g", geschaeftsObjekt);
//		ediKomponenteList.addAll(tqE.getResultList());
		for(EdiEmpfaenger e : tqE.getResultList() ) {
			log("readEdiListeforKomponete", "add Empfaenger mit " + e.getGeschaeftsObjekt().getName());
			empfaengerList.add(e);
			ediEintragsSet.add(e.getEdiEintrag());
		}
		tvVerwendungen.setItems(empfaengerList);
		log("readEdiListeforKomponente","size="+ ediEintragsSet.size());
	}

	public final ObjectProperty<GeschaeftsObjekt> geschaeftsObjektProperty() {
		return geschaeftsObjekt;
	}
	
	public final GeschaeftsObjekt getGeschaeftsObjekt() {
		return geschaeftsObjekt.get() ;
	}
	
	public final void setGeschaeftsObjekt(GeschaeftsObjekt geschaeftsObjekt) {
		this.geschaeftsObjekt.set(geschaeftsObjekt);
	}
    
	private static void log(String methode, String message) {
		if (message == null || methode == null) {
			String className = GeschaeftsObjektController.class.getName().substring(16);
			System.out.println(className + "." + methode + "(): " + message); 
		}
	}

	void checkFieldsFromView() {
    	assert tfBezeichnung != null : "fx:id=\"tfBezeichnung\" was not injected: check your FXML file 'GeschaeftsObjekt.fxml'.";
    	assert taBeschreibung != null : "fx:id=\"taBeschreibung\" was not injected: check your FXML file 'GeschaeftsObjekt.fxml'.";
    	assert tcEdiNr != null : "fx:id=\"tcEdiNr\" was not injected: check your FXML file 'GeschaeftsObjekt.fxml'.";
    	assert tcSender != null : "fx:id=\"tcSender\" was not injected: check your FXML file 'GeschaeftsObjekt.fxml'.";
        assert tcEmpfaenger != null : "fx:id=\"tcEmpfaenger\" was not injected: check your FXML file 'GeschaeftsObjekt.fxml'.";
        assert tcDatumBis != null : "fx:id=\"tcDatumBis\" was not injected: check your FXML file 'GeschaeftsObjekt.fxml'.";
        assert tvVerwendungen != null : "fx:id=\"tvVerwendungen\" was not injected: check your FXML file 'GeschaeftsObjekt.fxml'.";
        assert btnLoeschen != null : "fx:id=\"btnLoeschen\" was not injected: check your FXML file 'GeschaeftsObjekt.fxml'.";
        assert btnSpeichern != null : "fx:id=\"btnSpeichern\" was not injected: check your FXML file 'GeschaeftsObjekt.fxml'.";
    }
    
}
