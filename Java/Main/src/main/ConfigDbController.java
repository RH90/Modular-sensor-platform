package main;

import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.TextField;

/*
 * ConfigDBController.java
 *
 * This is the window for configuring the Database
 * 
 * Created: 2018/02/27
 * @author Ahmed
 */
public class ConfigDbController implements Initializable {

    /**
     * Initializes the controller class.
     */
    @FXML
    private TextField UserName;
    @FXML
    private TextField UserPass;
    @FXML
    private TextField PortNr;
    @FXML
    private TextField IP_address;
    @FXML
    private TextField Schema;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }

    @FXML
    private void Configure_Button(ActionEvent event) throws ClassNotFoundException, SQLException {
        MainController.UserName = UserName.getText();
        MainController.UserPass = UserPass.getText();
        try {
            MainController.PortNr = Integer.parseInt(PortNr.getText());
        } catch (Exception ex) {
            MainController.PortNr = 0;
        }

        MainController.IP_address = IP_address.getText();
        MainController.Schema = Schema.getText();
        System.out.println(Schema.getText());
        ((Node) (event.getSource())).getScene().getWindow().hide();
    }

}
