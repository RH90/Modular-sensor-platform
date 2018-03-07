/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;

/**
 * FXML Controller class
 *
 * @author Rilind
 */
public class Get_sensorController implements Initializable {

    @FXML
    private ListView lv;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
        SQL sql = new SQL();
        ObservableList<String> list = null;
        try {
            sql.start();
        } catch (ClassNotFoundException | SQLException ex) {
            Logger.getLogger(Get_sensorController.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            list = sql.list();
        } catch (SQLException ex) {
            Logger.getLogger(Get_sensorController.class.getName()).log(Level.SEVERE, null, ex);
        }
        lv.setItems(list);

    }

    @FXML
    private void search(ActionEvent event) {

    }

    @FXML
    private void get(ActionEvent event) {
        System.out.println(lv.getSelectionModel().getSelectedItem());
    }

}
