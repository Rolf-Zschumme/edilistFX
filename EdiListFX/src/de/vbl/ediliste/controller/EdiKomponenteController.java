package de.vbl.ediliste.controller;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
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
	private static TextField infoField;
	private static EntityManager entityManager;
	private final ObjectProperty<EdiKomponente> edikomponente;
	private final ObservableList<EdiEmpfaenger> ediKomponenteList = FXCollections.observableArrayList();
	private EdiKomponente aktKompo = null;
	
	@FXML private ResourceBundle resources;
    @FXML private URL location;
    @FXML private AnchorPane ediKomponente;
    @FXML private TextField tfBezeichnung;
    @FXML private TextArea taBeschreibung;
    @FXML private TableView<EdiEmpfaenger> tvVerwendungen;
    @FXML private TableColumn<EdiEmpfaenger, String> tcEmpfaenger;
    @FXML private TableColumn<EdiEmpfaenger, Integer> tcEdiNr;
    @FXML private TableColumn<EdiEmpfaenger, String> tcSender;
    @FXML private TableColumn<EdiEmpfaenger, ?> tcDatumBis;
    
    public EdiKomponenteController() {
    	this.edikomponente = new SimpleObjectProperty<>(this, "edikomponente", null);
    }

	public static void start(Stage primaryStage, TextField infoTextField, EntityManager entityManager) {
		EdiKomponenteController.primaryStage = primaryStage;
		EdiKomponenteController.infoField = infoTextField; 
		EdiKomponenteController.entityManager = entityManager;
	}

	@FXML
	public void initialize() {
		System.out.println("EdiKomponenteController.initialize()");
		checkFieldsFromView();
		
		edikomponente.addListener(new ChangeListener<EdiKomponente>() {
			@Override
			public void changed(ObservableValue<? extends EdiKomponente> ov,
					EdiKomponente oldKomponente, EdiKomponente newKomponente) {
				System.out.println("EdiKomponenteController.ChangeListener(edikomponente):"
									+ oldKomponente + " " +newKomponente);
				if (oldKomponente != null) {
					checkForChangesOk(true);
					ediKomponenteList.clear();
					if (newKomponente == null) {
						tfBezeichnung.setText("");
						taBeschreibung.setText("");
					}
				}
				if (newKomponente != null) {
					infoField.setText("");
					aktKompo = newKomponente;
					readEdiListeforKomponete(newKomponente);
					tfBezeichnung.setText(newKomponente.getName());
					taBeschreibung.setText(newKomponente.getBeschreibung());
				}
			}
	
		});
		tvVerwendungen.setItems(ediKomponenteList);
		
		tcEdiNr.setCellValueFactory(new PropertyValueFactory<EdiEmpfaenger, Integer>("ediNr") );
		tcSender.setCellValueFactory(new PropertyValueFactory<EdiEmpfaenger, String>("senderName"));
		tcEmpfaenger.setCellValueFactory(new PropertyValueFactory<EdiEmpfaenger, String>("empfaengerName"));
	}

	public boolean checkForChangesOk(boolean askForUpdate) {
		if (aktKompo == null ) {
			System.out.println(this.getClass().getName() + " checkForChanges() ohne aktKompo");
			return true;
		}
		String orgName = aktKompo.getName();
		String newName = tfBezeichnung.getText();
		String orgBeschreibung = (aktKompo.getBeschreibung()==null) ? "" : aktKompo.getBeschreibung();
		String newBeschreibung = (taBeschreibung.getText()==null) ? "" : taBeschreibung.getText();
		if (!orgName.equals(newName) ||
			!orgBeschreibung.equals(newBeschreibung) ) {
			if (askForUpdate) {
				Action response = Dialogs.create()
    				.owner(primaryStage).title(primaryStage.getTitle())
    				.actions(Dialog.Actions.OK, Dialog.Actions.CANCEL)
    				.message("Sollen die Änderungen an der Komponente " + orgName + " gespeichert werden")
    				.showConfirm();
	    		if (response != Dialog.Actions.OK) {
	    			return false;
	    		}	
			}	
			if (checkName(newName) == false) {
				infoField.setText("Fehler: Eine andere Komponente heißt bereits so!");
				return false;
			}
			System.out.println("checkForChanges() - Änderung erkannt -> update");
			entityManager.getTransaction().begin();
			aktKompo.setName(newName);
			aktKompo.setBeschreibung(newBeschreibung);
			entityManager.getTransaction().commit();
			infoField.setText("Komponente wurde gespeichert");
		}
		return true;
	}
	
	private boolean checkName(String newName) {
		TypedQuery<EdiKomponente> tq = entityManager.createQuery(
				"SELECT k FROM EdiKomponente k WHERE LOWER(k.name) = LOWER(:n)",EdiKomponente.class);
		tq.setParameter("n", newName);
		List<EdiKomponente> kompoList = tq.getResultList();
		for (EdiKomponente k : kompoList ) {
			if (k.getId() != aktKompo.getId() &&
				k.getEdiSystem().getId() == aktKompo.getEdiSystem().getId())  {
				if (k.getName().equalsIgnoreCase(newName)) {
					return false;
				}
			}
		}
		return true;
	}

	@FXML
	void speichern(ActionEvent event) {
		checkForChangesOk(false);
	}
	
	private void readEdiListeforKomponete( EdiKomponente selKomponente) {
		TypedQuery<EdiEintrag> tq = entityManager.createQuery(
				"SELECT e FROM EdiEintrag e WHERE e.komponente = :k", EdiEintrag.class);
		tq.setParameter("k", selKomponente);
		List<EdiEintrag> ediList = tq.getResultList();
		for(EdiEintrag e : ediList ) {
	    	ediKomponenteList.addAll(e.getEdiEmpfaenger());
		}
		System.out.println("KomponenteController:" + ediList.size() + " EDI-Einträge gelesen");
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
    
    void checkFieldsFromView() {
    	assert ediKomponente != null : "fx:id=\"ediKomponente\" was not injected: check your FXML file 'EdiKomponente.fxml'.";
    	assert tfBezeichnung != null : "fx:id=\"tfBezeichnung\" was not injected: check your FXML file 'EdiKomponente.fxml'.";
    	assert taBeschreibung != null : "fx:id=\"taBeschreibung\" was not injected: check your FXML file 'EdiKomponente.fxml'.";
    	assert tcEdiNr != null : "fx:id=\"tcEdiNr\" was not injected: check your FXML file 'EdiKomponente.fxml'.";
    	assert tcSender != null : "fx:id=\"tcSender\" was not injected: check your FXML file 'EdiKomponente.fxml'.";
        assert tcEmpfaenger != null : "fx:id=\"tcEmpfaenger\" was not injected: check your FXML file 'EdiKomponente.fxml'.";
        assert tcDatumBis != null : "fx:id=\"tcDatumBis\" was not injected: check your FXML file 'EdiKomponente.fxml'.";
        assert tvVerwendungen != null : "fx:id=\"tvVerwendungen\" was not injected: check your FXML file 'EdiKomponente.fxml'.";
    }
    
}
