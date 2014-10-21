package de.vbl.ediliste.controller.subs;

import java.net.URL;
import java.util.Collection;
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import javax.persistence.EntityManager;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.controlsfx.dialog.Dialog.Actions;

import de.vbl.ediliste.controller.EdiMainController;
import de.vbl.ediliste.model.DokuLink;
import de.vbl.ediliste.model.Repository;

public class DokumentAuswaehlenController implements Initializable {
	private static final Logger logger = LogManager.getLogger(DokumentAuswaehlenController.class.getName());

	private static Stage primaryStage = null;
    private static String applName = null;
	private static EdiMainController mainController;
    private static EntityManager entityManager = null;
    
    private ObservableList<DokuLink> dokuLinkList = FXCollections.observableArrayList();
    private ObservableList<String> firstLevelList = FXCollections.observableArrayList();
	/**
	 * injection from 'DokumentAuswaehlen.fxml'
	 */
	@FXML private ComboBox<String> cmbRepository; 
	@FXML private ComboBox<String> cmbFirstLevel; 
	@FXML private TextField tfSearch; 
    
    @FXML private TableView<DokuLink> tableDokuLinkAuswahl;
    @FXML private TableColumn<DokuLink, String> tColDokumentVorhaben;
    @FXML private TableColumn<DokuLink, String> tColDokumentName;
    @FXML private TableColumn<DokuLink, String> tColDokumentPfad;
    @FXML private TableColumn<DokuLink, String> tColDokumentRevision;
    @FXML private TableColumn<DokuLink, String> tColDokumentDatum;
    
    @FXML private Label     lbHinweis;
    @FXML private Button    btnSearch; 

//  private BooleanProperty listIsNotSelected = new SimpleBooleanProperty();
    
    private String aktRepository = "QS-Akte";
    private String searchText = "TSpez_0";
    private Actions retAction = Actions.CLOSE;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
    	final String filename = "DokumentAuswaehlen.xml";
    	final String errtxt = "' was not injected: check FXML-file '" + filename + "'.";
        try {
        	assert tableDokuLinkAuswahl != null : "fixid='tableDokuLinkAuswahl" + errtxt; 
        	assert cmbRepository 		!= null : "fx:id='cmbRepository" 		+ errtxt;
        	assert cmbFirstLevel 		!= null : "fx:id='cmbFirstLevel" 	 	+ errtxt;
        	assert tfSearch      		!= null : "fx:id='tfsearch"      		+ errtxt;
        	assert btnSearch     		!= null : "fx:id='btnSearch"     		+ errtxt;
        	assert lbHinweis     		!= null : "fx:id='lbHinweis"     		+ errtxt;
		} catch (AssertionError e) {
			logger.error(e.getMessage(), e);
		}
        cmbRepository.getSelectionModel().select(aktRepository);
        getFirstLevels();
        cmbFirstLevel.setItems(firstLevelList);
        cmbFirstLevel.getSelectionModel().select(1);
        
        tfSearch.setText(searchText);
        
		tableDokuLinkAuswahl.setItems(dokuLinkList);
		tColDokumentVorhaben.setCellValueFactory(cellData -> cellData.getValue().vorhabenProperty());
		tColDokumentName.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
		tColDokumentPfad.setCellValueFactory(cellData -> cellData.getValue().pfadProperty());
//		tColDokumentDatum.setCellValueFactory(cellData -> cellData.getValue().);

		cmbRepository.valueProperty().addListener((ov, oldValue, newValue) -> {
			aktRepository = newValue;
		});

		
    	tfSearch.textProperty().addListener((ov, oldValue, newValue) ->  {
    		searchText = newValue.trim();
    		lbHinweis.setText("");
    	}); 
    	
//    	listIsNotSelected.bind(tabDokuLinkListe.selectedProperty().and(Bindings.isNull(tableKontaktAuswahl.getSelectionModel().selectedItemProperty())));
//    	outlookIsNotSelected.bind(tabOutlookAuswahl.selectedProperty().and(Bindings.isEmpty(tfNummerOutlook.promptTextProperty())));
//    	newPersonNotEntered.bind(tabNeueingabe.selectedProperty().and(Bindings.not(nachnameFilled)));
//    	
//    	btnOK.disableProperty().bind(listIsNotSelected.or(outlookIsNotSelected).or(newPersonNotEntered));
    	
    }

    public void start(Stage primaryStage, EdiMainController mainController, EntityManager entityManager) {

    	DokumentAuswaehlenController.primaryStage = primaryStage;
    	DokumentAuswaehlenController.entityManager = entityManager;
    	DokumentAuswaehlenController.mainController = mainController;
		applName = primaryStage.getTitle();
		
    	tfSearch.requestFocus();
	}

	@FXML
	private void searchPressed(ActionEvent event) {
		if (searchText.length() < 0) {
			lbHinweis.setText("Bitte mindesten ein Zeichen eingeben");
			return;
		}
		lbHinweis.setText("");
		Collection<DokuLink> dokuLinkCollection = null; 
		try {
			Repository repository = new Repository(aktRepository, entityManager);
			String aktFirstLevel = cmbFirstLevel.getSelectionModel().getSelectedItem();
			dokuLinkCollection = repository.findEntries(searchText, aktFirstLevel);
			dokuLinkList.clear();
			if (aktFirstLevel == "") {
				dokuLinkList.addAll(dokuLinkCollection);
			}
			else {
				for (DokuLink dok : dokuLinkCollection) {
					dok.setPfad(dok.getPfad().substring(aktFirstLevel.length()));
					dokuLinkList.add(dok);
				}
			}
			System.out.println("fertig");
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
    @FXML
    private void okPressed(ActionEvent event) {
    	if (applName == null && mainController == null && primaryStage == null ) {
    		// TODO just for suppressing "unused" warning;
    	}
//    	Tab akttab = tabPane.getSelectionModel().getSelectedItem();
    	
//    	if (tabDokuLinkListe.isSelected()) {
//    		kontaktperson = tableKontaktAuswahl.getSelectionModel().selectedItemProperty().get();
//    		retAction = Actions.OK;
//    		close(event);
//    	} else if ( tabOutlookAuswahl.isSelected()) {
//    		
//    		// TODO
//    		
//    	} else if ( tabNeueingabe.isSelected()) {
//    		if (saveInputData() == true) {
//    			retAction = Actions.OK;
//    			close(event);
//    		}
//    	}	
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
    
    public Actions getResponse () {
    	return retAction;
    }
    
	private void getFirstLevels() {
		firstLevelList.clear();
		firstLevelList.add("/01_xSpez_Reviews");
		firstLevelList.add("/02_xSpez_abgenommen");
		firstLevelList.add("");
	}
}
