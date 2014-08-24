package de.vbl.ediliste.controller;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
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
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.controlsfx.control.action.Action;
import org.controlsfx.dialog.Dialog;
import org.controlsfx.dialog.Dialogs;

import de.vbl.ediliste.model.EdiEintrag;
import de.vbl.ediliste.model.EdiEmpfaenger;
import de.vbl.ediliste.model.EdiKomponente;

public class EdiKomponenteController {
	private static Stage primaryStage = null;
	private static EdiMainController mainCtr;
	private static EntityManager entityManager;
	private final ObjectProperty<EdiKomponente> edikomponente;
	private ObservableList<EdiEmpfaenger> ediKomponenteList = FXCollections.observableArrayList();
	private EdiKomponente aktKomponente = null;
	
	@FXML private ResourceBundle resources;
    @FXML private URL location;
    @FXML private AnchorPane ediKomponentePane;
    @FXML private TextField tfBezeichnung;
    @FXML private TextArea taBeschreibung;
    @FXML private TableView<EdiEmpfaenger> tvVerwendungen;
    @FXML private TableColumn<EdiEmpfaenger, String> tcEmpfaenger;
    @FXML private TableColumn<EdiEmpfaenger, String> tcEdiNr;
    @FXML private TableColumn<EdiEmpfaenger, String> tcSender;
    @FXML private TableColumn<EdiEmpfaenger, String> tcGeschaeftsobjekt;
    @FXML private TableColumn<EdiEmpfaenger, String> tcDatumAb;
    @FXML private TableColumn<EdiEmpfaenger, String> tcDatumBis;
    
    @FXML private Button btnLoeschen;
    
    public EdiKomponenteController() {
    	this.edikomponente = new SimpleObjectProperty<>(this, "edikomponente", null);
    }

	public static void start(Stage 			   primaryStage, 
							 EdiMainController mainController, 
							 EntityManager     entityManager) {
		log("start","called");
		EdiKomponenteController.primaryStage = primaryStage;
		EdiKomponenteController.mainCtr = mainController;
		EdiKomponenteController.entityManager = entityManager;
	}

	@FXML
	public void initialize() {
		log("initialize","called");
		checkFieldsFromView();
		
		edikomponente.addListener(new ChangeListener<EdiKomponente>() {
			@Override
			public void changed(ObservableValue<? extends EdiKomponente> ov,
					EdiKomponente oldKomponente, EdiKomponente newKomponente) {
				log("ChangeListener<EdiKomponente>",
					((oldKomponente==null) ? "null" : oldKomponente.getFullname()) + " -> " 
				  + ((newKomponente==null) ? "null" : newKomponente.getFullname()) );
				if (oldKomponente != null && newKomponente == null) {
					ediKomponenteList.clear();
					tfBezeichnung.setText("");
					taBeschreibung.setText("");
				}
				if (newKomponente != null) {
					aktKomponente = newKomponente;
					readEdiListeforKomponete(newKomponente, CacheRefresh.FALSE);
					tfBezeichnung.setText(newKomponente.getName());
					taBeschreibung.setText(newKomponente.getBeschreibung());
				}
			}
		});
		
		tvVerwendungen.setItems(ediKomponenteList);
		
		tvVerwendungen.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<EdiEmpfaenger>() {
			@Override
			public void changed (ObservableValue<? extends EdiEmpfaenger> ov, EdiEmpfaenger oldValue, EdiEmpfaenger newValue) {
				log("tvVerwendungen.select.changed" ,"newValue" + newValue);
			}
		});
		
		btnLoeschen.disableProperty().bind(Bindings.isNotEmpty(ediKomponenteList));
		
		tcEdiNr.setCellValueFactory(cellData -> 
					Bindings.format(EdiEintrag.FORMAT_EDINR, cellData.getValue().ediNrProperty()));

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
//						log("tcSender.updateItem", "aktkombo:" + aktKomponente.getFullname() + 
//								" sender:" + sender);
						if (sender.equals(aktKomponente.getFullname()))
							setFont(Font.font(null, FontWeight.BOLD, getFont().getSize()));
						else
							setFont(Font.font(null, FontWeight.NORMAL,getFont().getSize()));
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
//						log("tcEmpfaengeItem","aktkombo:" + aktKomponente.getFullname() + 
//								" emfpaenger:" + empf);
						if (empf.equals(aktKomponente.getFullname()))
							setFont(Font.font(null, FontWeight.BOLD, getFont().getSize()));
						else
							setFont(Font.font(null, FontWeight.NORMAL,getFont().getSize()));
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
		if (ediKomponenteList.size() > 0) {
			mainCtr.setErrorText("Fehler: Komponente wird verwendet");
			return;
		}	
		String kompoName1 = "Komponente \"" + aktKomponente.getName() + "\"";
		String kompoName2 = kompoName1;
		if (aktKomponente.getName().equals(tfBezeichnung.getText()) == false) {
			kompoName2 = kompoName1 + " / \"" + tfBezeichnung.getText() + "\"";
		}
		Action response = Dialogs.create()
				.owner(primaryStage).title(primaryStage.getTitle())
				.message(kompoName2 + " wirklich löschen ?")
				.showConfirm();
		if (response == Dialog.Actions.YES) {
			try {
				entityManager.getTransaction().begin();
				entityManager.remove(aktKomponente);
				entityManager.getTransaction().commit();
				aktKomponente = null;
				mainCtr.loadKomponentenListData();
				mainCtr.setInfoText("Die " + kompoName1 + " wurde erfolgreich gelöscht !");
			} catch (RuntimeException er) {
				Dialogs.create()
					.owner(primaryStage).title(primaryStage.getTitle())
					.masthead("Datenbankfehler")
				    .message("Fehler beim Löschen der Komponente " + kompoName1)
				    .showException(er);
			}
		}
	}
	
	@FXML
	void speichern(ActionEvent event) {
		checkForChangesAndSave(false);
	}
	
	public boolean checkForChangesAndAskForSave() {
		return checkForChangesAndSave(true);
	}

	private boolean checkForChangesAndSave(boolean askForUpdate) {
		log("checkForChangesAndSave","aktKompo=" + (aktKomponente==null ? "null" : aktKomponente.getFullname()));
		if (aktKomponente == null ) {
			return true;
		}
		String orgName = aktKomponente.getName();
		String newName = tfBezeichnung.getText();
		String orgBeschreibung = aktKomponente.getBeschreibung()==null ? "" : aktKomponente.getBeschreibung();
		String newBeschreibung = taBeschreibung.getText()==null ? "" : taBeschreibung.getText();
		if (!orgName.equals(newName) ||
			!orgBeschreibung.equals(newBeschreibung) ) {
			if (askForUpdate) {
				Action response = Dialogs.create()
    				.owner(primaryStage).title(primaryStage.getTitle())
    				.actions(Dialog.Actions.YES, Dialog.Actions.NO, Dialog.Actions.CANCEL)
    				.message("Sollen die Änderungen an der Komponente " + orgName + " gespeichert werden ?")
    				.showConfirm();
	    		if (response == Dialog.Actions.CANCEL) 	
	    			return false;
	    		if (response == Dialog.Actions.NO) {
	    			aktKomponente = null;
	    			return true;
	    		}
			}	
			if (checkKomponentenName(newName) == false) {
				mainCtr.setErrorText("Eine andere Komponente des Systems heißt bereits so!");
				return false;
			}
			log("checkForChangesAndSave","Änderung erkannt -> update");
			entityManager.getTransaction().begin();
			aktKomponente.setName(newName);
			aktKomponente.setBeschreibung(newBeschreibung);
			entityManager.getTransaction().commit();
			readEdiListeforKomponete(aktKomponente, CacheRefresh.TRUE);
			mainCtr.setInfoText("Komponente wurde gespeichert");
		}
		else {
			log("checkForChangesAndSave", "Name und Bezeichnung unverändert");
		}
		return true;
	}
	
	private boolean checkKomponentenName(String newName) {
		TypedQuery<EdiKomponente> tq = entityManager.createQuery(
				"SELECT k FROM EdiKomponente k WHERE LOWER(k.name) = LOWER(:n)",EdiKomponente.class);
		tq.setParameter("n", newName);
		List<EdiKomponente> kompoList = tq.getResultList();
		for (EdiKomponente k : kompoList ) {
			if (k.getId() != aktKomponente.getId() &&
				k.getEdiSystem().getId() == aktKomponente.getEdiSystem().getId())  {
				if (k.getName().equalsIgnoreCase(newName)) {
					return false;
				}
			}
		}
		return true;
	}

	private enum CacheRefresh { TRUE, FALSE;
		CacheRefresh() {}
	}
	
	private void readEdiListeforKomponete( EdiKomponente selKomponente, CacheRefresh cache) {
		ediKomponenteList.clear();
		/* 1. lese alle EdiEinträge mit Sender = selekierter Komponente 
		 * 		-> zeige jeweils alle zugehörigen Empfänger, falls kein Empfänger vorhanden dummy erzeugen
		*/
		TypedQuery<EdiEintrag> tqS = entityManager.createQuery(
				"SELECT e FROM EdiEintrag e WHERE e.ediKomponente = :k", EdiEintrag.class);
		tqS.setParameter("k", selKomponente);
		if (cache == CacheRefresh.TRUE) {
			tqS.setHint("javax.persistence.cache.storeMode", "REFRESH");
		}	
		List<EdiEintrag> ediList = tqS.getResultList();
		for(EdiEintrag e : ediList ) {
			if (e.getEdiEmpfaenger().size() > 0) {
				ediKomponenteList.addAll(e.getEdiEmpfaenger());
//				for(EdiEmpfaenger ee : e.getEdiEmpfaenger() ) ediKomponenteList.add(ee); 
			} else {
				EdiEmpfaenger tmpE = new EdiEmpfaenger();
				tmpE.setEdiEintrag(e);
				ediKomponenteList.add(tmpE);
			}
		}
		log("readEdiListeforKomponete", "für "+ selKomponente.getName() + " " + 
			ediList.size() + " EDI-Einträge" + " mit insgesamt " + 
			ediKomponenteList.size() + " Empfänger gelesen (Refresh=" + cache+ ")");
		
		/* 2. lese alle Empfänger mit Empfänger = selektierte Komponente 
		 *    -> zeige alle Empfänger  
		 */
		
		TypedQuery<EdiEmpfaenger> tqE = entityManager.createQuery(
				"SELECT e FROM EdiEmpfaenger e WHERE e.komponente = :k", EdiEmpfaenger.class);
		tqE.setParameter("k", selKomponente);
		if (cache == CacheRefresh.TRUE) {
			tqE.setHint("javax.persistence.cache.storeMode", "REFRESH");
		}	
//		ediKomponenteList.addAll(tqE.getResultList());
		for(EdiEmpfaenger e : tqE.getResultList() ) {
			log("readEdiListeforKomponete", "Empfaenger:" + e.getKomponente().getFullname() + " add");
			ediKomponenteList.add(e);
		}
		log("readEdiListeforKomponete", "für " + selKomponente.getName() + " " + 
			tqE.getResultList().size() + " EDI-Empfänger gelesen (Refresh=" + cache+ ")");
	}

	public final ObjectProperty<EdiKomponente> komponenteProperty() {
		return edikomponente;
	}
	
	public final EdiKomponente getKomponente() {
		return edikomponente.get() ;
	}
	
	public final void setKomponente(EdiKomponente komponente) {
		this.edikomponente.set(komponente);
	}
    
	private static void log(String methode, String message) {
		String className = EdiKomponenteController.class.getName().substring(16);
		System.out.println(className + "." + methode + "(): " + message); 
	}

	void checkFieldsFromView() {
    	assert ediKomponentePane != null : "fx:id=\"ediKomponente\" was not injected: check your FXML file 'EdiKomponente.fxml'.";
    	assert tfBezeichnung != null : "fx:id=\"tfBezeichnung\" was not injected: check your FXML file 'EdiKomponente.fxml'.";
    	assert taBeschreibung != null : "fx:id=\"taBeschreibung\" was not injected: check your FXML file 'EdiKomponente.fxml'.";
    	assert tcEdiNr != null : "fx:id=\"tcEdiNr\" was not injected: check your FXML file 'EdiKomponente.fxml'.";
    	assert tcSender != null : "fx:id=\"tcSender\" was not injected: check your FXML file 'EdiKomponente.fxml'.";
        assert tcEmpfaenger != null : "fx:id=\"tcEmpfaenger\" was not injected: check your FXML file 'EdiKomponente.fxml'.";
        assert tcDatumBis != null : "fx:id=\"tcDatumBis\" was not injected: check your FXML file 'EdiKomponente.fxml'.";
        assert tvVerwendungen != null : "fx:id=\"tvVerwendungen\" was not injected: check your FXML file 'EdiKomponente.fxml'.";
        assert btnLoeschen != null : "fx:id=\"btnLoeschen\" was not injected: check your FXML file 'EdiKomponente.fxml'.";
    }
    
}
