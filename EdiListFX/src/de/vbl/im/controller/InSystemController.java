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
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.controlsfx.control.action.Action;
import org.controlsfx.dialog.Dialog;
import org.controlsfx.dialog.Dialogs;
import org.controlsfx.dialog.Dialog.Actions;

import de.vbl.im.controller.subs.AnsprechpartnerAuswaehlenController;
import de.vbl.im.model.Integration;
import de.vbl.im.model.InEmpfaenger;
import de.vbl.im.model.InSystem;
import de.vbl.im.model.Ansprechpartner;

public class InSystemController {
	private static final Logger logger = LogManager.getLogger(InSystemController.class.getName());
	private static Stage primaryStage = null;
	private static IMController mainCtr;
	private static EntityManager entityManager;
	private final ObjectProperty<InSystem> inSystem;
	private final ObservableList<InEmpfaenger> inKomponentenList = FXCollections.observableArrayList();
	private final ObservableList<Ansprechpartner> ansprechpartnerList; 
	private InSystem aktSystem = null;
	private static String aktFullName;
	
	private BooleanProperty dataIsChanged = new SimpleBooleanProperty(false);
	
	@FXML private ResourceBundle resources;
    @FXML private URL location;
    @FXML private TextField tfBezeichnung;
    @FXML private TextArea taBeschreibung;
    @FXML private ListView<Ansprechpartner> lvAnsprechpartner;
    @FXML private TableView<InEmpfaenger> tvVerwendungen;
    @FXML private TableColumn<InEmpfaenger, String> tcEmpfaenger;
    @FXML private TableColumn<InEmpfaenger, String> tcInNr;
    @FXML private TableColumn<InEmpfaenger, String> tcSender;
    @FXML private TableColumn<InEmpfaenger, String> tcGeschaeftsobjekt;
    @FXML private TableColumn<InEmpfaenger, String> tcDatumAb;
    @FXML private TableColumn<InEmpfaenger, String> tcDatumBis;
    
    @FXML private Button btnSpeichern;
    @FXML private Button btnLoeschen;
    @FXML private Button btnRemoveAnsprechpartner;
    
    public InSystemController() {
    	this.inSystem = new SimpleObjectProperty<>(this, "inSystem", null);
    	this.ansprechpartnerList = FXCollections.observableArrayList();
    }

	public void setParent(IMController managerController) {
		logger.entry();
		InSystemController.mainCtr = managerController;
		InSystemController.primaryStage = IMController.getStage();
		InSystemController.entityManager = managerController.getEntityManager();
		logger.exit();
	}

	@FXML
	public void initialize() {
		checkFieldsFromView();
		
		inSystem.addListener(new ChangeListener<InSystem>() {
			@Override
			public void changed(ObservableValue<? extends InSystem> ov,
					InSystem oldSystem, InSystem newSystem) {
				logger.info(((oldSystem==null) ? "null" : oldSystem.getFullname()) + " -> " 
						  + ((newSystem==null) ? "null" : newSystem.getFullname()) );
				btnLoeschen.disableProperty().unbind();
				if (oldSystem != null) {
					inKomponentenList.clear();
					ansprechpartnerList.clear();
				}
				tfBezeichnung.setText("");
				taBeschreibung.setText("");
				if (newSystem != null) {
					aktSystem = newSystem;
					logger.info("newSystem.Name="+ newSystem.getName());
					aktFullName = aktSystem.getFullname();
					readTablesForSystem(newSystem);
					tfBezeichnung.setText(newSystem.getName());
					if (newSystem.getBeschreibung() == null) {
						newSystem.setBeschreibung("");
					}
					taBeschreibung.setText(newSystem.getBeschreibung());
					btnLoeschen.disableProperty().bind(Bindings.lessThan(0, aktSystem.anzKomponentenProperty()));
					ansprechpartnerList.addAll(newSystem.getAnsprechpartner());
					btnRemoveAnsprechpartner.disableProperty().bind(lvAnsprechpartner.getSelectionModel().selectedItemProperty().isNull());
				}
				dataIsChanged.set(false);
			}
		});
		
		btnSpeichern.disableProperty().bind(Bindings.not(dataIsChanged));

		tfBezeichnung.textProperty().addListener((observable, oldValue, newValue)  -> {
			if (aktSystem != null) {
				String userMsg = "";
				if (aktSystem.getName().equals(newValue) == false) {
					userMsg = checkSystemName(newValue);
				}
				dataIsChanged.set(!checkForChangesAndSave(Checkmode.ONLY_CHECK));
				mainCtr.setErrorText(userMsg);			
			}
		}); 

		taBeschreibung.textProperty().addListener((observable, oldValue, newValue) -> {
			if (aktSystem != null) {
				String userMsg = "";
				if (newValue.equals(aktSystem.getBeschreibung()) == false) {
					dataIsChanged.set(true);
				} else {	
					dataIsChanged.set(!checkForChangesAndSave(Checkmode.ONLY_CHECK));
				}
				mainCtr.setErrorText(userMsg);			
			}
		});
		
		lvAnsprechpartner.setItems(ansprechpartnerList);
		lvAnsprechpartner.setCellFactory( (list) -> {
			return new ListCell<Ansprechpartner>() {
				@Override
				protected void updateItem(Ansprechpartner k, boolean empty) {
					super.updateItem(k, empty);
					if (k == null || empty) {
						setText(null);
					} else {
						setText(k.getArtNameFirma());
					}
				}
			};
		});
		
//	    Setup for Sub-Panel    
		
		tvVerwendungen.setItems(inKomponentenList);
		tcInNr.setCellValueFactory(cellData -> 
					Bindings.format(Integration.FORMAT_INNR, cellData.getValue().getInNrProperty()));
		
		tcSender.setCellValueFactory(cellData -> cellData.getValue().senderNameProperty());
		tcSender.setCellFactory(column -> {
			return new TableCell<InEmpfaenger, String>() {
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
			return new TableCell<InEmpfaenger, String>() {
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
		tcDatumAb.setCellValueFactory(cellData -> cellData.getValue().getIntegration().seitDatumProperty());
		tcDatumBis.setCellValueFactory(cellData -> cellData.getValue().getIntegration().bisDatumProperty());
	}

	@FXML
	void loeschen(ActionEvent event) {
		if (inKomponentenList.size() > 0) {
			mainCtr.setErrorText("Fehler: Komponente wird verwendet");
			return;
		}	
		String aktName = "System \"" + aktSystem.getName() + "\"";
		String neuName = aktName;
		if (aktSystem.getName().equals(tfBezeichnung.getText()) == false) {
			neuName = aktName + " / \"" + tfBezeichnung.getText() + "\"";
		}
		Action response = Dialogs.create()
				.owner(primaryStage).title(primaryStage.getTitle())
				.message(neuName + " des Partners " + " \"" + 
				aktSystem.getinPartner().getName() + "\" wirklich löschen ?")
				.showConfirm();
		if (response == Dialog.Actions.YES) {
			try {
				aktSystem.getinPartner().getInSystem().remove(aktSystem);
				entityManager.getTransaction().begin();
				entityManager.remove(aktSystem);
				entityManager.getTransaction().commit();
				aktSystem = null;
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
		if (aktSystem == null ) {
			logger.info("aktSystem=NULL?");
			return true;
		}
		String orgName = aktSystem.getName();
		String newName = tfBezeichnung.getText();
		String orgBeschreibung = aktSystem.getBeschreibung()==null ? "" : aktSystem.getBeschreibung();
		String newBeschreibung = taBeschreibung.getText()==null ? "" : taBeschreibung.getText();
		
		if (orgName.equals(newName) &&
			orgBeschreibung.equals(newBeschreibung) && 
			aktSystem.getAnsprechpartner().containsAll(ansprechpartnerList) &&
			ansprechpartnerList.containsAll(aktSystem.getAnsprechpartner())    ) 
		{
			return true;  // no changes -> nothing to do  
		}
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
				aktSystem = null;
				return true;
			}
		}
		String msg = checkSystemName(newName);
		if (msg != null) {
			mainCtr.setErrorText(msg);
			tfBezeichnung.requestFocus();
			return false;
		}
		logger.info("Update System " + newName);
		try {
			entityManager.getTransaction().begin();
			aktSystem.setName(newName);
			aktSystem.setBeschreibung(newBeschreibung);
			boolean kontaktListChanged = aktSystem.getAnsprechpartner().retainAll(ansprechpartnerList);
			for (Ansprechpartner k : ansprechpartnerList) {
				if (aktSystem.getAnsprechpartner().contains(k)== false) {
					aktSystem.getAnsprechpartner().add(k);
					kontaktListChanged = true;
				}
			}
			entityManager.getTransaction().commit();
			if (kontaktListChanged) {
				mainCtr.refreshKontaktReferences();
			}
			mainCtr.setInfoText("Das System " + orgName + " wurde gespeichert");
			dataIsChanged.set(false);
    	} catch (RuntimeException e) {
    		logger.error("Message:"+ e.getMessage(),e);
			Dialogs.create().owner(primaryStage)
				.title(primaryStage.getTitle())
				.masthead("FEHLER")
				.message("Fehler beim Speichern der Systemdaten:\n" + e.getMessage())
				.showException(e);
    	}
		readTablesForSystem(aktSystem);
		return true;
	}
	
	private String checkSystemName(String newName) {
		if ("".equals(newName)) {
			return "Eine Bezeichnung ist erforderlich";
		}
		TypedQuery<InSystem> tq = entityManager.createQuery(
				"SELECT s FROM InSystem s WHERE LOWER(s.name) = LOWER(:n)",InSystem.class);
		tq.setParameter("n", newName);
		List<InSystem> systemList = tq.getResultList();
		for (InSystem s : systemList ) {
			if (s.getId() != aktSystem.getId() &&
				s.getinPartner().getId() == aktSystem.getinPartner().getId())  {
				if (s.getName().equalsIgnoreCase(newName)) {
					return "Eine anderes System des Partners \"" +
							aktSystem.getinPartner().getName() + "\" heißt bereits so!";
				}
			}
		}
		return null;
	}

	private void readTablesForSystem( InSystem selSystem) {
		inKomponentenList.clear();
		TypedQuery<Integration> tqS = entityManager.createQuery(
				"SELECT e FROM Integration e WHERE e.inKomponente.inSystem = :s", Integration.class);
		tqS.setParameter("s", selSystem);
		List<Integration> resultList = tqS.getResultList();
		for(Integration e : resultList ) {
			if (e.getInEmpfaenger().size() > 0)
				inKomponentenList.addAll(e.getInEmpfaenger());
			else {
				InEmpfaenger tmpE = new InEmpfaenger();
				tmpE.setIntegration(e);
				inKomponentenList.addAll(tmpE);
			}
		}
		TypedQuery<InEmpfaenger> tqE = entityManager.createQuery(
				"SELECT e FROM InEmpfaenger e WHERE e.komponente.inSystem = :s", InEmpfaenger.class);
		tqE.setParameter("s", selSystem);
		inKomponentenList.addAll(tqE.getResultList());
	}

    @FXML
    void actionAddAnsprechpartner(ActionEvent event) {
    	Stage dialog = new Stage(StageStyle.UTILITY);
    	AnsprechpartnerAuswaehlenController controller = mainCtr.loadAnsprechpartnerAuswahl(dialog);
    	if (controller != null) {
    		dialog.showAndWait();
    		String userInfo = "Die Ansprechpartner-Auswahl wurde abgebrochen"; 
    		if (controller.getResponse() == Actions.OK) {
    			Ansprechpartner selectedKontakt = controller.getAnsprechpartner();
    			if (ansprechpartnerList.contains(selectedKontakt)) {
    				userInfo = "Der ausgewählte Ansprechpartner ist bereits eingetragen";
    			} else {
    				ansprechpartnerList.add(selectedKontakt);
					dataIsChanged.set(!checkForChangesAndSave(Checkmode.ONLY_CHECK));
    				userInfo = "Der ausgewählte Kontakt wurde ergänzt";
    			}
    		}
    		mainCtr.setInfoText(userInfo);
    	}
    }

    @FXML
    void actionRemoveAnsprechpartner(ActionEvent event) {
    	Ansprechpartner toBeRemoved = lvAnsprechpartner.getSelectionModel().getSelectedItem();
    	logger.info("remove Kontakt " + toBeRemoved.getNachname());
    	ansprechpartnerList.remove(toBeRemoved);
    	mainCtr.setInfoText("Der Ansprechpartner \"" + toBeRemoved.getVorname() + " " + 
    					toBeRemoved.getNachname() + "\" wurde aus dieser Liste entfernt");
		dataIsChanged.set(!checkForChangesAndSave(Checkmode.ONLY_CHECK));
    }
	
	public final ObjectProperty<InSystem> inSystemProperty() {
		return inSystem;
	}
	
	public final InSystem getInSystem() {
		return inSystem.get() ;
	}
	
	public final void setInSystem(InSystem inSystem) {
		this.inSystem.set(inSystem);
	}
    
	final static String fxmlFilename = "InSystem.fxml";
	final static String fxmlErrortxt = "' was not injected: check FXML-file '" + fxmlFilename + "'.";
    void checkFieldsFromView() {
    	assert tfBezeichnung 			!= null : "fx:id='tfBezeichnung"  			+ fxmlErrortxt;
    	assert taBeschreibung 			!= null : "fx:id='taBeschreibung"  			+ fxmlErrortxt;
    	assert tcInNr 					!= null : "fx:id='tcInNr"  				+ fxmlErrortxt;
    	assert tcSender 				!= null : "fx:id='tcSender"  				+ fxmlErrortxt;
    	assert tcEmpfaenger 			!= null : "fx:id='tcEmpfaenger"  			+ fxmlErrortxt;
    	assert tcGeschaeftsobjekt		!= null : "fx:id='tcGeschaeftsobjekt"		+ fxmlErrortxt;
    	assert tcDatumAb 				!= null : "fx:id='tcDatumAb"  				+ fxmlErrortxt;
    	assert tcDatumBis 				!= null : "fx:id='tcDatumBis"  				+ fxmlErrortxt;
    	assert lvAnsprechpartner    	!= null : "fx:id='lvAnsprechpartner"  		+ fxmlErrortxt;
    	assert tvVerwendungen 			!= null : "fx:id='tvVerwendungen"  			+ fxmlErrortxt;
    	assert btnSpeichern 			!= null : "fx:id='btnSpeichern"  			+ fxmlErrortxt;
    	assert btnLoeschen 				!= null : "fx:id='btnLoeschen"  			+ fxmlErrortxt;
    	assert btnRemoveAnsprechpartner	!= null : "fx:id='btnRemoveAnsprechpartner" 	+ fxmlErrortxt;
    }
}
