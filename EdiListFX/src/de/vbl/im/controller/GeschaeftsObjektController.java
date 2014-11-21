package de.vbl.im.controller;

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

import de.vbl.im.model.Integration;
import de.vbl.im.model.InEmpfaenger;
import de.vbl.im.model.InPartner;
import de.vbl.im.model.GeschaeftsObjekt;

public class GeschaeftsObjektController {
	private static final Logger logger = LogManager.getLogger(GeschaeftsObjektController.class.getName());
	private static Stage primaryStage = null;
	private static IMController mainCtr;
	private static EntityManager entityManager;
	
	private final ObjectProperty<GeschaeftsObjekt> geschaeftsObjekt;
	private final ObservableSet<Integration> integrationSet;      // all assigned integrations
	private final IntegerProperty inSystemAnzahl; 
	private GeschaeftsObjekt aktGeschaeftsObjekt = null;
	
	private BooleanProperty dataIsChanged = new SimpleBooleanProperty(false);
	
	@FXML private ResourceBundle resources;
    @FXML private URL location;
    @FXML private TextField tfBezeichnung;
    @FXML private TextArea taBeschreibung;
    @FXML private TableView<InEmpfaenger> tvVerwendungen;
    @FXML private TableColumn<InEmpfaenger, String> tcInNr;
    @FXML private TableColumn<InEmpfaenger, String> tcInSzenario;
    @FXML private TableColumn<InEmpfaenger, String> tcKonfiguration;
    @FXML private TableColumn<InEmpfaenger, String> tcEmpfaenger;
    @FXML private TableColumn<InEmpfaenger, String> tcSender;
    @FXML private TableColumn<InEmpfaenger, String> tcGeschaeftsobjekt;
    @FXML private TableColumn<InEmpfaenger, String> tcDatumAb;
    @FXML private TableColumn<InEmpfaenger, String> tcDatumBis;
    
    @FXML private Button btnSpeichern;
    @FXML private Button btnLoeschen;
    
    public GeschaeftsObjektController() {
    	this.geschaeftsObjekt = new SimpleObjectProperty<>(this, "geschaeftsObjekt", null);
    	this.integrationSet = FXCollections.observableSet();
    	this.inSystemAnzahl = new SimpleIntegerProperty(0);
    }

	public static void setParent(IMController managerController) {
		logger.entry(primaryStage);
		GeschaeftsObjektController.mainCtr = managerController;
		GeschaeftsObjektController.primaryStage = IMController.getStage();
		GeschaeftsObjektController.entityManager = managerController.getEntityManager();
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
				log("ChangeListener<InPartner>",
					((oldGeschaeftsObjekt==null) ? "null" : oldGeschaeftsObjekt.getName() + " -> " 
				  + ((newGeschaeftsObjekt==null) ? "null" : newGeschaeftsObjekt.getName() )));
				if (oldGeschaeftsObjekt != null && newGeschaeftsObjekt == null) {
					integrationSet.clear();
					tfBezeichnung.setText("");
					taBeschreibung.setText("");
					inSystemAnzahl.unbind();
				}
				if (newGeschaeftsObjekt != null) {
					aktGeschaeftsObjekt = newGeschaeftsObjekt;
					readEmpfaengerListeforGeschaeftsObjekt(newGeschaeftsObjekt);
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
		btnLoeschen.disableProperty().bind(Bindings.lessThan(0, inSystemAnzahl));
//		btnLoeschen.disableProperty().bind(Bindings.not(Bindings.greaterThanOrEqual(0, Bindings.size(integrationSet))));

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
		
		tcInNr.setCellValueFactory(cellData -> Bindings.format(Integration.FORMAT_INNR, 
												cellData.getValue().getIntegration().inNrProperty()));
		tcInSzenario.setCellValueFactory(cell -> cell.getValue().getIntegration().inSzenarioNameProperty());
		tcKonfiguration.setCellValueFactory(cell -> cell.getValue().getIntegration().konfigurationNameProperty());
		tcSender.setCellValueFactory(cellData -> cellData.getValue().getIntegration().getInKomponente().fullnameProperty());
		tcEmpfaenger.setCellValueFactory(cellData -> cellData.getValue().getKomponente().fullnameProperty());
		tcDatumAb.setCellValueFactory(cellData -> cellData.getValue().getIntegration().seitDatumProperty());
		tcDatumBis.setCellValueFactory(cellData -> cellData.getValue().getIntegration().bisDatumProperty());
		
		// TODO: zum Absprung bei Select einer anderen Integration in der Sub-Tabelle
		tvVerwendungen.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<InEmpfaenger>() {
			@Override
			public void changed (ObservableValue<? extends InEmpfaenger> ov, InEmpfaenger oldValue, InEmpfaenger newValue) {
				log("tvVerwendungen.select.changed" ,"newValue" + newValue);
			}
		});
		logger.exit();
	}

	@FXML
	void loeschen(ActionEvent event) {
		if (integrationSet.size() > 0) {
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
			readEmpfaengerListeforGeschaeftsObjekt(aktGeschaeftsObjekt);
			mainCtr.setInfoText("Der Partner \"" + aktGeschaeftsObjekt.getName() + "\" wurde gespeichert");
		}
		return true;
	}
	
	private String checkGeschaeftsObjektName(String newName) {
		if ("".equals(newName)) {
			return "Eine Bezeichnung ist erforderlich";
		}
		TypedQuery<InPartner> tq = entityManager.createQuery(
				"SELECT p FROM InPartner p WHERE LOWER(p.name) = LOWER(:n)",InPartner.class);
		tq.setParameter("n", newName);
		List<InPartner> partnerList = tq.getResultList();
		for (InPartner p : partnerList ) {
			if (p.getId() != aktGeschaeftsObjekt.getId()) {
				if (p.getName().equalsIgnoreCase(newName)) {
					return "Ein anderer Partner heiﬂt bereits so!";
				}
			}
		}
		return null;
	}

	private void readEmpfaengerListeforGeschaeftsObjekt( GeschaeftsObjekt geschaeftsObjekt) {
		tvVerwendungen.getItems().clear();
		ObservableList<InEmpfaenger> empfaengerList = FXCollections.observableArrayList();
		integrationSet.clear(); 
		/* read all InEmpfaenger with given GeschaeftsObjekt 
		 */
		TypedQuery<InEmpfaenger> tqE = entityManager.createQuery(
			"SELECT e FROM InEmpfaenger e WHERE e.geschaeftsObjekt = :g", InEmpfaenger.class);
		tqE.setParameter("g", geschaeftsObjekt);
//		inKomponenteList.addAll(tqE.getResultList());
		for(InEmpfaenger e : tqE.getResultList() ) {
			logger.info("add Empfaenger mit " + e.getGeschaeftsObjekt().getName());
			empfaengerList.add(e);
			integrationSet.add(e.getIntegration());
		}
		tvVerwendungen.setItems(empfaengerList);
		logger.trace("size="+ integrationSet.size());
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
    	assert tcInNr != null : "fx:id=\"tcInNr\" was not injected: check your FXML file 'GeschaeftsObjekt.fxml'.";
    	assert tcSender != null : "fx:id=\"tcSender\" was not injected: check your FXML file 'GeschaeftsObjekt.fxml'.";
        assert tcEmpfaenger != null : "fx:id=\"tcEmpfaenger\" was not injected: check your FXML file 'GeschaeftsObjekt.fxml'.";
        assert tcInSzenario != null : "fx:id=\"tcInSzenario\" was not injected: check your FXML file 'GeschaeftsObjekt.fxml'.";
        assert tcKonfiguration != null : "fx:id=\"tcKonfiguration\" was not injected: check your FXML file 'GeschaeftsObjekt.fxml'.";
        assert tcDatumAb != null : "fx:id=\"tcDatumAb\" was not injected: check your FXML file 'GeschaeftsObjekt.fxml'.";
        assert tcDatumBis != null : "fx:id=\"tcDatumBis\" was not injected: check your FXML file 'GeschaeftsObjekt.fxml'.";
        assert tvVerwendungen != null : "fx:id=\"tvVerwendungen\" was not injected: check your FXML file 'GeschaeftsObjekt.fxml'.";
        assert btnLoeschen != null : "fx:id=\"btnLoeschen\" was not injected: check your FXML file 'GeschaeftsObjekt.fxml'.";
        assert btnSpeichern != null : "fx:id=\"btnSpeichern\" was not injected: check your FXML file 'GeschaeftsObjekt.fxml'.";
    }
    
}
