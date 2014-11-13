package de.vbl.im.controller.subs;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.controlsfx.dialog.Dialog.Actions;

import de.vbl.im.controller.IMController;
import de.vbl.im.model.Ansprechpartner;

public class AnsprechpartnerAuswaehlenController implements Initializable {
	private static final Logger logger = LogManager.getLogger(AnsprechpartnerAuswaehlenController.class.getName());

	private static Stage primaryStage = null;
    private static String applName = null;
	private static IMController managerController;
    private static EntityManager entityManager = null;
    
    private ObservableList<Ansprechpartner> ansprechpartnerList = FXCollections.observableArrayList();
	/**
	 * injection from 'AnsprechpartnerAuswaehlen.fxml'
	 */
    @FXML private TabPane tabPane;
    
    @FXML private Tab tabAnsprechpartnerListe;
    @FXML private TableView<Ansprechpartner> tableKontaktAuswahl;
    @FXML private TableColumn<Ansprechpartner, String> tColKontaktUserId;
    @FXML private TableColumn<Ansprechpartner, String> tColKontaktNachname;
    @FXML private TableColumn<Ansprechpartner, String> tColKontaktVorname;
    @FXML private TableColumn<Ansprechpartner, String> tColKontaktArt;
    @FXML private TableColumn<Ansprechpartner, String> tColKontaktAbteilung;
    @FXML private TableColumn<Ansprechpartner, String> tColKontaktTelefon;
    @FXML private TableColumn<Ansprechpartner, String> tColKontaktMailadresse;
    
    @FXML private Tab tabOutlookAuswahl;
	@FXML private ChoiceBox<String> m_OutlookArt; 
	@FXML private TextField tfNachnameOutlook; 
	@FXML private TextField tfNummerOutlook; 
	@FXML private TextField tfVornameOutlook; 
    @FXML private TextField tfAbteilungOutlook; 
    @FXML private TextField tfMailadresseOutlook; 
    @FXML private TextField tfTelefonOutlook;
    @FXML private Label     lbHinweisOutlook;
    @FXML private Label     lbHinweisNeueingabe;
    @FXML private Button    btnOK; 

    @FXML private Tab tabNeuanlage;
	@FXML private ChoiceBox<String> m_NeuanlageArt; 
    @FXML private TextField tfNachname; 
	@FXML private TextField tfVorname; 
    @FXML private TextField tfAbteilung; 
    @FXML private TextField tfMailadresse; 
    @FXML private TextField tfTelefon;
    
    private BooleanProperty listIsNotSelected = new SimpleBooleanProperty();
    private BooleanProperty outlookIsNotSelected = new SimpleBooleanProperty(false);
    private BooleanProperty newPersonNotEntered = new SimpleBooleanProperty(false);

    
    private Ansprechpartner ansprechpartner = new Ansprechpartner();
    private BooleanProperty nachnameFilled = new SimpleBooleanProperty(false);
    private Actions retAction = Actions.CLOSE;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
			assert tabPane					!= null : "fx:id=\"tabPane\" was not injected: check your FXML file 'AnsprechpartnerAuswaehlen.fxml'.";
			assert tabAnsprechpartnerListe	!= null : "fx:id=\"tabAnsprechpartnerListe\" was not injected: check your FXML file 'AnsprechpartnerAuswaehlen.fxml'.";
			assert tableKontaktAuswahl		!= null : "fx:id=\"tableKontaktAuswahl\" was not injected: check your FXML file 'AnsprechpartnerAuswaehlen.fxml'.";
			assert tColKontaktUserId		!= null : "fx:id=\"tColKontaktUserId\" was not injected: check your FXML file 'AnsprechpartnerAuswaehlen.fxml'.";
			assert tColKontaktNachname		!= null : "fx:id=\"tColKontaktNachname\" was not injected: check your FXML file 'AnsprechpartnerAuswaehlen.fxml'.";
			assert tColKontaktVorname		!= null : "fx:id=\"tColKontaktVorname\" was not injected: check your FXML file 'AnsprechpartnerAuswaehlen.fxml'.";
			assert tColKontaktArt 			!= null : "fx:id=\"tColKontaktArt\" was not injected: check your FXML file 'AnsprechpartnerAuswaehlen.fxml'.";
			assert tColKontaktAbteilung 	!= null : "fx:id=\"tColKontaktAbteilung\" was not injected: check your FXML file 'AnsprechpartnerAuswaehlen.fxml'.";
			assert tColKontaktTelefon 		!= null : "fx:id=\"tColKontaktTelefon\" was not injected: check your FXML file 'AnsprechpartnerAuswaehlen.fxml'.";
			assert tColKontaktMailadresse 	!= null : "fx:id=\"tColKontaktMailadresse\" was not injected: check your FXML file 'AnsprechpartnerAuswaehlen.fxml'.";
			
			assert tabOutlookAuswahl 		!= null : "fx:id=\"tabOutlookAuswahl\" was not injected: check your FXML file 'AnsprechpartnerAuswaehlen.fxml'.";
			assert tfNummerOutlook 			!= null : "fx:id=\"tfNummerOutlook\" was not injected: check your FXML file 'AnsprechpartnerAuswaehlen.fxml'.";
			assert m_OutlookArt				!= null : "fx:id=\"m_OutlookArt\" was not injected: check your FXML file 'AnsprechpartnerAuswaehlen.fxml'.";
			assert tfVornameOutlook			!= null : "fx:id=\"tfVornameOutlook\" was not injected: check your FXML file 'AnsprechpartnerAuswaehlen.fxml'.";
			assert tfNachnameOutlook 		!= null : "fx:id=\"tfNachnameOutlook\" was not injected: check your FXML file 'AnsprechpartnerAuswaehlen.fxml'.";
			assert tfAbteilungOutlook		!= null : "fx:id=\"tfAbteilungOutlook\" was not injected: check your FXML file 'AnsprechpartnerAuswaehlen.fxml'.";
			assert tfMailadresseOutlook		!= null : "fx:id=\"tfMailadresseOutlook\" was not injected: check your FXML file 'AnsprechpartnerAuswaehlen.fxml'.";
			assert tfTelefonOutlook 		!= null : "fx:id=\"tfTelefonOutlook\" was not injected: check your FXML file 'AnsprechpartnerAuswaehlen.fxml'.";
			
			assert tabNeuanlage				!= null : "fx:id=\"tabNeuanlage\" was not injected: check your FXML file 'AnsprechpartnerAuswaehlen.fxml'.";
			assert m_NeuanlageArt 			!= null : "fx:id=\"m_NeuanlageArt\" was not injected: check your FXML file 'AnsprechpartnerAuswaehlen.fxml'.";
			assert tfNachname 				!= null : "fx:id=\"tfNachname\" was not injected: check your FXML file 'AnsprechpartnerAuswaehlen.fxml'.";
			assert tfVorname				!= null : "fx:id=\"tfVorname\" was not injected: check your FXML file 'AnsprechpartnerAuswaehlen.fxml'.";
			assert tfAbteilung				!= null : "fx:id=\"tfAbteilung\" was not injected: check your FXML file 'AnsprechpartnerAuswaehlen.fxml'.";
			assert tfMailadresse			!= null : "fx:id=\"tfMailadresse\" was not injected: check your FXML file 'AnsprechpartnerAuswaehlen.fxml'.";
			assert tfTelefon				!= null : "fx:id=\"tfTelefon\" was not injected: check your FXML file 'AnsprechpartnerAuswaehlen.fxml'.";
			assert tfNachnameOutlook		!= null : "fx:id=\"tfNachnameOutlook\" was not injected: check your FXML file 'AnsprechpartnerAuswaehlen.fxml'.";

	        
	        assert lbHinweisNeueingabe 		!= null : "fx:id=\"lbHinweisNeueingabe\" was not injected: check your FXML file 'AnsprechpartnerAuswaehlen.fxml'.";
	        assert lbHinweisOutlook 		!= null : "fx:id=\"lbHinweisOutlook\" was not injected: check your FXML file 'AnsprechpartnerAuswaehlen.fxml'.";
	        assert btnOK 					!= null : "fx:id=\"btnOK\" was not injected: check your FXML file 'AnsprechpartnerAuswaehlen.fxml'.";
        } catch (AssertionError e) {
			logger.error(e.getMessage(), e);
		}
		tableKontaktAuswahl.setItems(ansprechpartnerList);
		tColKontaktUserId.setCellValueFactory(cellData -> cellData.getValue().nummerProperty());
		tColKontaktNachname.setCellValueFactory(cellData -> cellData.getValue().nachnameProperty());
		tColKontaktVorname.setCellValueFactory(cellData -> cellData.getValue().vornameProperty());
		tColKontaktArt.setCellValueFactory(cellData -> cellData.getValue().artProperty());
		tColKontaktAbteilung.setCellValueFactory(cellData -> cellData.getValue().abteilungProperty());
		tColKontaktTelefon.setCellValueFactory(cellData -> cellData.getValue().telefonProperty());
		tColKontaktMailadresse.setCellValueFactory(cellData -> cellData.getValue().mailProperty());
		
		m_OutlookArt.getItems().addAll(Ansprechpartner.valuesOfArt);
		// TODO
		
		ansprechpartner.setVorname("");
		m_NeuanlageArt.getItems().addAll(Ansprechpartner.valuesOfArt);
		
		m_NeuanlageArt.valueProperty().addListener((observable, oldValue, newValue) -> {
			ansprechpartner.setArt(newValue);
    		lbHinweisNeueingabe.setText("");
		});
		
    	tfNachname.textProperty().addListener((ov, oldValue, newValue) ->  {
    		ansprechpartner.setNachname(newValue.trim());
    		nachnameFilled.set(newValue.trim().length() > 0);
    		lbHinweisNeueingabe.setText("");
    	}); 
    	
    	tfVorname.textProperty().addListener((ov, oldValue, newValue) -> {
    		ansprechpartner.setVorname(newValue.trim());
    		lbHinweisNeueingabe.setText("");
    	});
    	
    	tfAbteilung.textProperty().addListener((ov, oldValue, newValue) -> {
    		ansprechpartner.setAbteilung(newValue.trim());
    		lbHinweisNeueingabe.setText("");
    	});
    	
    	tfTelefon.textProperty().addListener((ov, oldValue, newValue) -> {
    		ansprechpartner.setTelefon(newValue.trim());
    	});
    	
    	tfMailadresse.textProperty().addListener((ov, oldValue, newValue) -> {
    		ansprechpartner.setMail(newValue.trim());
    	});
    	listIsNotSelected.bind(tabAnsprechpartnerListe.selectedProperty().and(Bindings.isNull(tableKontaktAuswahl.getSelectionModel().selectedItemProperty())));
    	outlookIsNotSelected.bind(tabOutlookAuswahl.selectedProperty().and(Bindings.isEmpty(tfNummerOutlook.promptTextProperty())));
    	newPersonNotEntered.bind(tabNeuanlage.selectedProperty().and(Bindings.not(nachnameFilled)));
    	
    	btnOK.disableProperty().bind(listIsNotSelected.or(outlookIsNotSelected).or(newPersonNotEntered));
    	
    }

    public void start(Stage primaryStage, IMController managerController, EntityManager entityManager) {

    	AnsprechpartnerAuswaehlenController.primaryStage = primaryStage;
    	AnsprechpartnerAuswaehlenController.entityManager = entityManager;
    	AnsprechpartnerAuswaehlenController.managerController = managerController;
		applName = primaryStage.getTitle();
		
		loadAnsprechpartnerListData();
	}

	public Actions getResponse () {
		return retAction;
	}

	public Ansprechpartner getAnsprechpartner() {
		return ansprechpartner;
	}
    
    @FXML
    private void okPressed(ActionEvent event) {
    	if (applName == null && managerController == null && primaryStage == null ) {
    		// TODO just for suppressing "unused" warning;
    	}
//    	Tab akttab = tabPane.getSelectionModel().getSelectedItem();
    	
    	if (tabAnsprechpartnerListe.isSelected()) {
    		ansprechpartner = tableKontaktAuswahl.getSelectionModel().selectedItemProperty().get();
    		retAction = Actions.OK;
    		close(event);
    	} else if ( tabOutlookAuswahl.isSelected()) {
    		
    		// TODO
    		
    	} else if ( tabNeuanlage.isSelected()) {
    		if (saveInputData() == true) {
    			retAction = Actions.OK;
    			close(event);
    		}
    	}	
    }

	@FXML
    private void escapePressed(ActionEvent event) {
		retAction = Actions.CANCEL;
    	close(event);
    }
    
    private void close(ActionEvent event) {
    	Node source = (Node) event.getSource();
    	Stage stage = (Stage) source.getScene().getWindow();
    	stage.close();
    }
    
    private boolean saveInputData() {
    	String ret = checkKontakt(ansprechpartner);
    	if (ret != null) {
    		lbHinweisNeueingabe.textProperty().set(ret);
    		return false;
    	}
		entityManager.getTransaction().begin();
		entityManager.persist(ansprechpartner);
		entityManager.getTransaction().commit();
		
		logger.info("Neuer Kontakt (" + ansprechpartner.getId() + ") erfolgreich angelegt");
    	return true;
    }

	private String checkKontakt(Ansprechpartner kp) {
		if ("".equals(kp.getNachname())) {
			return "Ein Nachname ist erforderlich";
		}
		TypedQuery<Ansprechpartner> tq = entityManager.createQuery(
				"SELECT k FROM Ansprechpartner k WHERE LOWER(k.nachname) = LOWER(:n)",Ansprechpartner.class);
		tq.setParameter("n", kp.getNachname());
		List<Ansprechpartner> ansprechpartnerList = tq.getResultList();
		for (Ansprechpartner k : ansprechpartnerList) {
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
	
	private void loadAnsprechpartnerListData() {
		TypedQuery<Ansprechpartner> tq = entityManager.createQuery(
				"SELECT k FROM Ansprechpartner k ORDER BY k.nachname", Ansprechpartner.class);
		List<Ansprechpartner> aktulist = tq.getResultList();
		ansprechpartnerList.retainAll(aktulist);
		for (Ansprechpartner k : aktulist) {
			if (ansprechpartnerList.contains(k) == false ) {
				ansprechpartnerList.add(aktulist.indexOf(k), k);
			}
		}
	}
}
