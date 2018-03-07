/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.math.BigInteger;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

/**
 * FXML Controller class
 *
 * @author Rilind
 */
public class New_SensorController implements Initializable {

    /**
     * Initializes the controller class.
     */
    private SQL sql = new SQL();
    @FXML
    private Label la;
    @FXML
    private Label lb;
    @FXML
    private Label lc;
    @FXML
    private TextField tfa1;
    @FXML
    private TextField tfa2;
    @FXML
    private TextField tfb1;
    @FXML
    private TextField tfb2;
    @FXML
    private TextField tfb3;
    @FXML
    private TextField tfb4;
    @FXML
    private TextField tfb5;
    @FXML
    private TextField tfb6;
    @FXML
    private TextField tfb7;
    @FXML
    private TextField tfb8;
    @FXML
    private TextField tfc1;
    @FXML
    private TextField tfc2;
    @FXML
    private TextField tfc3;
    @FXML
    private TextField tfc4;
    @FXML
    private TextField tfc5;
    @FXML
    private TextField tfc6;
    @FXML
    private ChoiceBox cba;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        cba.setItems(FXCollections.observableArrayList(
                "Simple Read", "Pulse"));
        try {
            sql.start();
        } catch (ClassNotFoundException | SQLException ex) {
            Logger.getLogger(New_SensorController.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    @FXML
    private void Button_AD(ActionEvent event) throws SQLException {
        String s = (String) cba.getValue();
        if (tfa1.getText().equalsIgnoreCase("") || tfa2.getText().equalsIgnoreCase("")
                || s.equalsIgnoreCase("")) {
            //InvocationTargetException
            la.setText("Insufficent data!");
        } else {
            sql.add_sensor(tfa1.getText(), tfa2.getText(), (String) cba.getValue());
            la.setText("Sensor added!");
        }
    }

    @FXML
    private void Button_I2C(ActionEvent event) {
        if (tfb1.getText().equalsIgnoreCase("") || tfb2.getText().equalsIgnoreCase("")
                || tfb3.getText().equalsIgnoreCase("") || tfb6.getText().equalsIgnoreCase("")
                || tfb4.getText().equalsIgnoreCase("") && !tfb5.getText().equalsIgnoreCase("")
                || !tfb4.getText().equalsIgnoreCase("") && tfb5.getText().equalsIgnoreCase("")) {
            //InvocationTargetException
            lb.setText("Insufficent data!");
        } else {

            if (tfb3.getText().length() == 2 || tfb3.getText().length() == 0
                    && tfb4.getText().length() == 2 || tfb4.getText().length() == 0
                    && tfb5.getText().length() == 2 || tfb5.getText().length() == 0
                    && tfb6.getText().length() == 2 || tfb6.getText().length() == 0
                    && tfb7.getText().length() == 2 || tfb7.getText().length() == 0
                    && tfb8.getText().length() == 2 || tfb8.getText().length() == 0) {
                try {
                    byte[] b;
                    if (tfb3.getText().length() == 2) {
                        b = new BigInteger(tfb3.getText(), 16).toByteArray();
                    }
                    if (tfb4.getText().length() == 2) {
                        b = new BigInteger(tfb4.getText(), 16).toByteArray();
                    }
                    if (tfb5.getText().length() == 2) {
                        b = new BigInteger(tfb5.getText(), 16).toByteArray();
                    }
                    if (tfb6.getText().length() == 2) {
                        b = new BigInteger(tfb6.getText(), 16).toByteArray();
                    }
                    if (tfb7.getText().length() == 2) {
                        b = new BigInteger(tfb7.getText(), 16).toByteArray();
                    }
                    if (tfb8.getText().length() == 2) {
                        b = new BigInteger(tfb8.getText(), 16).toByteArray();
                    }
                    sql.add_sensor(tfb1.getText(), tfb2.getText(), tfb3.getText(), tfb4.getText(), tfb5.getText(), tfb6.getText(), tfb7.getText(), tfb8.getText());
                    lb.setText("Sensor added!");
                } catch (Exception ex) {
                    ex.printStackTrace();
                    lb.setText("Error!");
                }
            }
        }
    }

    @FXML
    private void Button_SPI(ActionEvent event) throws SQLException {
        if (tfc1.getText().equalsIgnoreCase("") || tfc2.getText().equalsIgnoreCase("") || tfc4.getText().equalsIgnoreCase("")) {
            //InvocationTargetException
            lc.setText("Insufficent data!");
        } else {

            if (tfc3.getText().length() == 2 || tfc3.getText().length() == 0 && tfc4.getText().length() == 2 || tfc4.getText().length() == 0
                    && tfc5.getText().length() == 2 || tfc5.getText().length() == 0 && tfc6.getText().length() == 2 || tfc6.getText().length() == 0) {
                try {
                    byte[] b;
                    if (tfc3.getText().length() == 2) {
                        b = new BigInteger(tfc3.getText(), 16).toByteArray();
                    }
                    if (tfc4.getText().length() == 2) {
                        b = new BigInteger(tfc4.getText(), 16).toByteArray();
                    }
                    if (tfc5.getText().length() == 2) {
                        b = new BigInteger(tfc5.getText(), 16).toByteArray();
                    }
                    if (tfc6.getText().length() == 2) {
                        b = new BigInteger(tfc6.getText(), 16).toByteArray();
                    }
                    sql.add_sensor(tfc1.getText(), tfc2.getText(), tfc3.getText(), tfc4.getText(), tfc5.getText(), tfc6.getText());
                    lc.setText("Sensor added!");
                } catch (SQLException ex) {
                    lc.setText("Error!");
                }
            }
        }
    }

}
