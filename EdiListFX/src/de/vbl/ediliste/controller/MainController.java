package de.vbl.ediliste.controller;

import javafx.scene.control.Dialogs;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Dialogs.DialogOptions;
import javafx.scene.control.Dialogs.DialogResponse;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;

import de.vbl.ediliste.controller.KomponentenAuswahlController.KomponentenTyp;
import de.vbl.ediliste.model.EdiEintrag;
import de.vbl.ediliste.model.EdiKomponente;
import de.vbl.ediliste.model.EdiPartner;
import de.vbl.ediliste.model.EdiSystem;
import de.vbl.ediliste.view.EdiNrListElement;
import de.vbl.ediliste.view.PartnerListElement;

public class MainController {
	private static final String PERSISTENCE_UNIT_NAME = "EdiListFX";
	private static final String SICHERHEITSABFRAGE = "Sicherheitsabfrage";

    @FXML private ResourceBundle resources;
    @FXML private URL location;

    @FXML private TabPane TabPaneObjekte;    
    @FXML private Tab tabEdiNr;
    @FXML private TableView<EdiNrListElement> tableEdiNrAuswahl;
    @FXML private TableColumn<EdiNrListElement, String> tColAuswahlEdiNr;
    @FXML private TableColumn<EdiNrListElement, String> tColAuswahlEdiNrBezeichnung;

    @FXML private Tab tabPartner;
    @FXML private TableView<PartnerListElement> tablePartnerAuswahl;
    @FXML private TableColumn<PartnerListElement, String> tColAuswahlPartnerName;
    @FXML private TableColumn<PartnerListElement, String> tColAuswahlPartnerSysteme;
    @FXML private TableColumn<PartnerListElement, String> tColAuswahlPartnerKomponenten;

    @FXML private Tab tabSysteme;

    @FXML private TitledPane paneSzenario;
    @FXML private TitledPane paneAnbindung;
    @FXML private TitledPane paneEdiEintrag;
    
    @FXML private TextField tfEdiBezeichnung;
    @FXML private TextField tfDatenart1;
    @FXML private TextField ediLastChange;

    @FXML private Button btnEmpfaenger1;
    @FXML private Button btnSender;
    @FXML private Button btnNewEdiNr;
    @FXML private Button btnDeleteEdiEintrag;


    
    private EntityManager em;
    private ObservableList<EdiNrListElement> ediNrArrayList = FXCollections.observableArrayList();
    private ObservableList<PartnerListElement> ediPartnerList = FXCollections.observableArrayList();
    private int maxEdiNr;
    private Stage primaryStage;

    private BooleanProperty senderIsSelected = new SimpleBooleanProperty(false);
    
    private EdiEintrag aktEdi;
    
    public void setStage(Stage temp) {
    	primaryStage = temp;
    }
    
    /* ------------------------------------------------------------------------
     * initialize() is the controllers "main"-method 
     * it is called after loading "EdiListe.fxml" 
     * ----------------------------------------------------------------------*/
    @FXML
    void initialize() {
    	checkFieldFromView();
        setupEntityManager();
        loadEdiNrListData();
        loadPartnerListData();
        
        setupBindings();
        
        tableEdiNrAuswahl.getSelectionModel().selectedItemProperty().addListener(
        		new ChangeListener<EdiNrListElement>() {
        			
        			@Override
        			public void changed(
        					ObservableValue<? extends EdiNrListElement> observable,
        					EdiNrListElement oldValue, EdiNrListElement newValue) {
        				System.out.println("oldValue=" + ((oldValue == null) ? "null" : oldValue.ediNrProperty().get()) 
        							   + "  newValue=" + ((newValue == null) ? "null" : newValue.ediNrProperty().get()) ); 
        				final EdiEintrag defEdi = new EdiEintrag();
        				if (oldValue != null) {
        					tfEdiBezeichnung.textProperty().unbindBidirectional(defEdi.kurzBezProperty());
        					tfEdiBezeichnung.textProperty().unbindBidirectional(aktEdi.kurzBezProperty());;
        				}
        				if (newValue != null) {
        					aktEdi = em.find(EdiEintrag.class, newValue.getEdiId());
        					tfEdiBezeichnung.textProperty().bindBidirectional(aktEdi.kurzBezProperty());
        		    		btnSender.setText(aktEdi.getKomponente()==null ? "" : aktEdi.getKomponente().getFullname());
        		    		senderIsSelected.set(aktEdi.getKomponente()!=null);
        				}
        				else {
        					tfEdiBezeichnung.textProperty().bindBidirectional(defEdi.kurzBezProperty());
        					btnSender.setText("");
        		    		senderIsSelected.set(false);
        				}
        			}
				}
        );
    }
    
	private void setupBindings() {
    	
    	tableEdiNrAuswahl.setItems(ediNrArrayList);
    
    	tColAuswahlEdiNr.setCellValueFactory(new PropertyValueFactory<EdiNrListElement,String>("ediNr"));
    	tColAuswahlEdiNrBezeichnung.setCellValueFactory(new PropertyValueFactory<EdiNrListElement,String>("bezeichnung"));
    	
    	tfEdiBezeichnung.disableProperty().bind(Bindings.isNull(tableEdiNrAuswahl.getSelectionModel().selectedItemProperty()));
    	btnDeleteEdiEintrag.disableProperty().bind(Bindings.isNull(tableEdiNrAuswahl.getSelectionModel().selectedItemProperty()));
    	btnSender.disableProperty().bind(Bindings.isNull(tableEdiNrAuswahl.getSelectionModel().selectedItemProperty()));
    	btnEmpfaenger1.disableProperty().bind(Bindings.not(senderIsSelected));

    	//		paneSzenario.textProperty().bind(ediEintrag.szenarioNameProperty());
    	
    	tablePartnerAuswahl.setItems(ediPartnerList);
    	tColAuswahlPartnerName.setCellValueFactory(new PropertyValueFactory<PartnerListElement,String>("name"));
    	tColAuswahlPartnerSysteme.setCellValueFactory(new PropertyValueFactory<PartnerListElement,String>("anzSysteme"));
    	tColAuswahlPartnerKomponenten.setCellValueFactory(new PropertyValueFactory<PartnerListElement,String>("anzKomponenten"));
	}

	private void loadEdiNrListData() {
    	Query query = em.createQuery("SELECT e.id, e.ediNr, e.kurzBez FROM EdiEintrag e ORDER BY e.ediNr");

    	ediNrArrayList.clear();
    	Integer max = 0;
    	for (Object zeile  : query.getResultList()) {
    		Object[] obj = (Object[]) zeile;
			ediNrArrayList.add(new EdiNrListElement( (Long) obj[0], (Integer) obj[1], (String) obj[2]));
			max = (Integer) obj[1]; 
    	}	
    	maxEdiNr = max;
	}
	
	private void loadPartnerListData() {
    	Query query = em.createQuery("SELECT p FROM EdiPartner p ORDER BY p.name");

    	ediPartnerList.clear();
    	for (Object p : query.getResultList()) {
    		ediPartnerList.add(new PartnerListElement( (EdiPartner) p));
    	}
	}
	
	
    	
    private void setupEntityManager() {
    	EntityManagerFactory factory = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME);
    	em = factory.createEntityManager();
    }
    
	/* *****************************************************************************
	 * 
	 * ****************************************************************************/
    @FXML
    void newEdiNr(ActionEvent event) {
    
    	FXMLLoader loader = new FXMLLoader();
    	loader.setLocation(getClass().getResource("../view/NeuerEdiEintrag.fxml"));
    	try {
			loader.load();
		} catch (IOException e) {
			e.printStackTrace();
		}
    	Parent root = loader.getRoot();
    	Scene scene = new Scene(root);
    	
    	Stage dialog = new Stage(StageStyle.UTILITY);
    	dialog.initModality(Modality.APPLICATION_MODAL);
    	dialog.initOwner(primaryStage);
    	
    	NeuerEdiEintragController dialogController = loader.getController();
    	
    	dialog.setScene(scene);
    	dialog.setX(primaryStage.getX() + 250);
    	dialog.setY(primaryStage.getY() + 100);
    	dialog.showAndWait();

    	if (dialogController.getResponse() == DialogResponse.OK) {
    		EdiEintrag newEE = dialogController.getNewEdiEintrag();
			EdiNrListElement newListElement = new EdiNrListElement(newEE.getId(),newEE.getEdiNr(),newEE.getKurzBez());
			ediNrArrayList.add(newListElement);
			if (newEE.getEdiNr() > maxEdiNr) 
				maxEdiNr = newEE.getEdiNr();
			tableEdiNrAuswahl.getSelectionModel().select(newListElement);
    	}
    }    
    
    @FXML
    void deleteEdiEintrag(ActionEvent event) {
    	EdiNrListElement selectedlistElement = tableEdiNrAuswahl.getSelectionModel().getSelectedItem();
    	if (selectedlistElement != null) {
    		int ediNr = selectedlistElement.ediNrProperty().get();
    		DialogResponse response = Dialogs.showConfirmDialog(primaryStage, 
    				"EDI-Eintrag mit der Nr. " + ediNr + " wirklich löschen?",
    				SICHERHEITSABFRAGE,"", DialogOptions.OK_CANCEL);
    		if (response == DialogResponse.OK) {
    			long id = selectedlistElement.getEdiId();
    			EdiEintrag ediEintrag = em.find(EdiEintrag.class, id);
    			if (ediEintrag==null) {
    				System.out.println("FEHLER: EDI-Eintrag mit der ID "+ id + " und der Nr. "+ 
    								 ediNr + " ist nicht (mehr) gespeichert");
    			}
    			else {
	        		em.getTransaction().begin();
	        		em.remove(ediEintrag);
	        		em.getTransaction().commit();
    			}	
        		ediNrArrayList.remove(selectedlistElement);
    			tableEdiNrAuswahl.getSelectionModel().clearSelection();
    		}
    	}
    }
    
    @FXML
    void senderButton(ActionEvent event) {

    	Stage dialog = new Stage(StageStyle.UTILITY);
    	FXMLLoader loader = loadKomponentenAuswahl(dialog); 

    	KomponentenAuswahlController komponentenAuswahlController = loader.getController();
    	Long aktSenderId = (aktEdi.getKomponente()==null) ? 0L : aktEdi.getKomponente().getId();
    	komponentenAuswahlController.setKomponente(KomponentenTyp.SENDER, aktSenderId);
    	dialog.showAndWait();

    	Long selKomponentenID = komponentenAuswahlController.selectedKomponentenId();
    	if (aktSenderId != selKomponentenID) {
    		aktEdi.setKomponente(em.find(EdiKomponente.class,
    				komponentenAuswahlController.selectedKomponentenId()));
    		btnSender.setText(aktEdi.getKomponente().getFullname());
    		senderIsSelected.set(true);
            loadPartnerListData();
    	}
    }

    
    private FXMLLoader loadKomponentenAuswahl(Stage dialog) {
    	FXMLLoader loader = new FXMLLoader();
    	loader.setLocation(getClass().getResource("../view/KomponentenAuswahl.fxml"));
    	try {
    		loader.load();
    	} catch (IOException e) {
    		e.printStackTrace();
    	}
    	Parent root = loader.getRoot();
//    	System.out.println(getClass().getName() + ".loaderKomponentenAuswahl --> vor new Scene");
    	Scene scene = new Scene(root);
//    	System.out.println(getClass().getName() + ".loaderKomponentenAuswahl --> vor Modality");
    	dialog.initModality(Modality.APPLICATION_MODAL);
    	dialog.initOwner(primaryStage);
    	dialog.setTitle(primaryStage.getTitle());
    	dialog.setScene(scene);
    	dialog.setX(primaryStage.getX() + 100);
    	dialog.setY(primaryStage.getY() + 250);
		return loader;
	}

	@FXML
    void empfaengerButton(ActionEvent event) {
    	Stage dialog = new Stage(StageStyle.UTILITY);
//
//    	FXMLLoader loader = loadKomponentenAuswahl(dialog); 

//    	KomponentenAuswahlController komponentenAuswahlController = loader.getController();
//    	komponentenAuswahlController.setKomponente(KomponentenTyp.RECEIVER, aktEdi.getEdiEmpfaenger().iterator().next().getId() );
    	
    	dialog.showAndWait();
    	
//    	Long selEmpfaengerId1 = komponentenAuswahlController.selectedKomponentenId();
//    	if (aktEdi.getKomponente().getId() != selKomponentenID) {
//    		aktEdi.setKomponente(em.find(EdiKomponente.class,
//    				komponentenAuswahlController.selectedKomponentenId()));
//    		btnSender.setText(aktEdi.getKomponente().getFullname());
//    		senderIsSelected.set(true);
//    	}
//    	EdiKomponente selKomponente = komponentenAuswahlController.selectedKomponenten();
//    	if (aktEdi.getKomponente() != selKomponente) {
//    		aktEdi.setKomponente(selKomponente);
//    	}

    }
    
    private void checkFieldFromView() {
    	assert TabPaneObjekte != null : "fx:id=\"TabPaneObjekte\" was not injected: check your FXML file 'Main.fxml'.";
        assert tabEdiNr != null : "fx:id=\"tabEdiNr\" was not injected: check your FXML file 'Main.fxml'.";
        assert tableEdiNrAuswahl != null : "fx:id=\"tableEdiNrAuswahl\" was not injected: check your FXML file 'Main.fxml'.";
        assert tColAuswahlEdiNr != null : "fx:id=\"tColAuswahlEdiNr\" was not injected: check your FXML file 'Main.fxml'.";
        assert tColAuswahlEdiNrBezeichnung != null : "fx:id=\"tColAuswahlEdiNrBezeichnung\" was not injected: check your FXML file 'Main.fxml'.";
        assert tColAuswahlPartnerName != null : "fx:id=\"tColAuswahlPartnerName\" was not injected: check your FXML file 'Main.fxml'.";
        assert tColAuswahlPartnerSysteme != null : "fx:id=\"tColAuswahlPartnerSysteme\" was not injected: check your FXML file 'Main.fxml'.";
        assert tColAuswahlPartnerKomponenten != null : "fx:id=\"tColAuswahlPartnerKomponenten\" was not injected: check your FXML file 'Main.fxml'.";
        assert tabSysteme != null : "fx:id=\"tabSysteme\" was not injected: check your FXML file 'Main.fxml'.";
        assert tabPartner != null : "fx:id=\"tabPartner\" was not injected: check your FXML file 'Main.fxml'.";
        assert btnDeleteEdiEintrag != null : "fx:id=\"btnDeleteEdiEintrag\" was not injected: check your FXML file 'Main.fxml'.";
        assert btnSender != null : "fx:id=\"btnSender\" was not injected: check your FXML file 'Main.fxml'.";
        assert btnNewEdiNr != null : "fx:id=\"btnNewEdiNr\" was not injected: check your FXML file 'Main.fxml'.";
        assert paneAnbindung != null : "fx:id=\"paneAnbindung\" was not injected: check your FXML file 'Main.fxml'.";
        assert paneSzenario != null : "fx:id=\"paneSzenario\" was not injected: check your FXML file 'Main.fxml'.";
        assert paneEdiEintrag != null : "fx:id=\"paneEdiEintrag\" was not injected: check your FXML file 'Main.fxml'.";
        assert tfEdiBezeichnung != null : "fx:id=\"tfEdiBezeichnung\" was not injected: check your FXML file 'Main.fxml'.";
        assert ediLastChange != null : "fx:id=\"ediLastChange\" was not injected: check your FXML file 'Main.fxml'.";
        assert tfDatenart1 != null : "fx:id=\"tfDatenart1\" was not injected: check your FXML file 'Main.fxml'.";
        assert btnEmpfaenger1 != null : "fx:id=\"btnEmpfaenger1\" was not injected: check your FXML file 'Main.fxml'.";
    }
}
