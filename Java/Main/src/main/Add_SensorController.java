package main;

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
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;

/*
 * Add_SensorController.java
 *
 * This is the window for adding a sensor to the database
 * 
 * Created: 2018/02/27
 * @author Rilind Hasanaj <rilind.hasanaj0018@stud.hkr.se>
 */
public class Add_SensorController implements Initializable {

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
    private Text label_i2c_addr;
    @FXML
    private Text label_i2c_data;
    @FXML
    private CheckBox checkbox_a;
    @FXML
    private CheckBox checkbox_b;
    @FXML
    private CheckBox checkbox_c;
    @FXML
    private Text label_i2c_read;
    @FXML
    private Text label_spi_data1;
    @FXML
    private Text label_spi_data;
    @FXML
    private Text label_spi_read;
    @FXML
    private Button button_i2c_write;
    @FXML
    private Button button_i2c_read;
    @FXML
    private Button button_spi_write;
    @FXML
    private Button button_spi_read;
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
    private TextField tfc1;
    @FXML
    private TextField tfc2;
    @FXML
    private TextField tfc3;
    @FXML
    private TextField tfc31;
    @FXML
    private TextField tfc4;
    @FXML
    private ChoiceBox cba;

    private String i2c_write_reg = "";
    private String i2c_write_data = "";
    private String i2c_read_reg = "";
    private String spi_write_reg = "";
    private String spi_write_data = "";
    private String spi_read_reg = "";

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        cba.setItems(FXCollections.observableArrayList(
                "Simple Read", "Pulse"));
        try {
            sql.start(MainController.UserName, MainController.UserPass, MainController.PortNr, MainController.IP_address, MainController.Schema);
        } catch (ClassNotFoundException | SQLException ex) {
            Logger.getLogger(Add_SensorController.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    // button handler for the next button
    @FXML
    private void next(ActionEvent event) throws SQLException {
        Button button = ((Button) event.getSource());
        String id = button.getId();
        byte[] b;
        if (id.equalsIgnoreCase("button_i2c_write")) {
            if (tfb4.getText().length() == 2 && tfb5.getText().length() == 2) {
                try {
                    b = new BigInteger(tfb4.getText(), 16).toByteArray();
                    b = new BigInteger(tfb5.getText(), 16).toByteArray();
                    int num = Integer.parseInt(button.getText().charAt(0) + "");
                    if (num < 7) {
                        label_i2c_addr.setText(num + 1 + "" + ". Write address");
                        label_i2c_data.setText(num + 1 + "" + ". Write data");
                        i2c_write_reg += tfb4.getText();
                        i2c_write_data += tfb5.getText();
                        button.setText(num + 1 + "" + ". Next");
                        tfb4.setText("");
                        tfb5.setText("");
                    }
                } catch (Exception ex) {

                }
            }
        } else if (id.equalsIgnoreCase("button_i2c_read")) {
            if (tfb6.getText().length() == 2) {
                try {
                    b = new BigInteger(tfb6.getText(), 16).toByteArray();
                    int num = Integer.parseInt(button.getText().charAt(0) + "");
                    if (num < 7) {
                        button.setText(num + 1 + "" + ". Next");
                        label_i2c_read.setText(num + 1 + "" + ". Read address");
                        i2c_read_reg += tfb6.getText();
                        tfb6.setText("");
                    }
                } catch (Exception ex) {

                }
            }

        } else if (id.equalsIgnoreCase("button_spi_write")) {
            if (tfc3.getText().length() == 2 && tfc31.getText().length() == 2) {
                try {
                    b = new BigInteger(tfc3.getText(), 16).toByteArray();
                    b = new BigInteger(tfc31.getText(), 16).toByteArray();
                    int num = Integer.parseInt(button.getText().charAt(0) + "");
                    if (num < 7) {
                        label_spi_data.setText(num + 1 + "" + ". Write address");
                        label_spi_data1.setText(num + 1 + "" + ". Write data");
                        spi_write_reg += tfc3.getText();
                        spi_write_data += tfc31.getText();
                        button.setText(num + 1 + "" + ". Next");
                        tfc3.setText("");
                        tfc31.setText("");
                    }
                } catch (Exception ex) {

                }
            }
        } else if (id.equalsIgnoreCase("button_spi_read")) {
            if (tfc4.getText().length() == 2) {
                try {
                    b = new BigInteger(tfc4.getText(), 16).toByteArray();
                    int num = Integer.parseInt(button.getText().charAt(0) + "");
                    if (num < 7) {
                        button.setText(num + 1 + "" + ". Next");
                        label_spi_read.setText(num + 1 + "" + ". Read address");
                        spi_read_reg += tfc4.getText();
                        tfc4.setText("");
                    }
                } catch (Exception ex) {

                }
            }
        }
    }

    // Button for adding Analog_digital sensor
    @FXML
    private void Button_AD(ActionEvent event) throws SQLException {
        String s = (String) cba.getValue();
        if (tfa1.getText().equalsIgnoreCase("") || tfa2.getText().equalsIgnoreCase("")
                || s.equalsIgnoreCase("")) {
            //InvocationTargetException
            la.setText("Insufficent data!");
        } else {
            String reference = "no";
            if (checkbox_a.isSelected()) {
                reference = "yes";
            }
            sql.add_sensor(tfa1.getText(), tfa2.getText(), (String) cba.getValue(), reference);
            la.setText("Sensor added!");
        }
    }
    // Button for adding I2c sensor
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
                    && tfb6.getText().length() == 2 || tfb6.getText().length() == 0) {
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
                    i2c_write_reg += tfb4.getText();
                    i2c_write_data += tfb5.getText();
                    i2c_read_reg += tfb6.getText();
                    String reference = "no";
                    if (checkbox_b.isSelected()) {
                        reference = "yes";
                    }
                    sql.add_sensor(tfb1.getText(), tfb2.getText(), tfb3.getText(), i2c_write_reg, i2c_write_data, i2c_read_reg,reference);
                    lb.setText("Sensor added!");
                } catch (Exception ex) {
                    ex.printStackTrace();
                    lb.setText("Error!");
                }
            }
        }
    }
    // Button for adding SPI sensor
    @FXML
    private void Button_SPI(ActionEvent event) throws SQLException {
        if (tfc1.getText().equalsIgnoreCase("") || tfc2.getText().equalsIgnoreCase("") || tfc4.getText().equalsIgnoreCase("")) {
            //InvocationTargetException
            lc.setText("Insufficent data!");
        } else {

            if (tfc3.getText().length() == 2 || tfc3.getText().length() == 0
                    && tfc31.getText().length() == 2 || tfc31.getText().length() == 0
                    && tfc4.getText().length() == 2 || tfc4.getText().length() == 0) {
                try {
                    byte[] b;
                    if (tfc3.getText().length() == 2) {
                        b = new BigInteger(tfc3.getText(), 16).toByteArray();
                    }
                    if (tfc31.getText().length() == 2) {
                        b = new BigInteger(tfc31.getText(), 16).toByteArray();
                    }
                    if (tfc4.getText().length() == 2) {
                        b = new BigInteger(tfc4.getText(), 16).toByteArray();
                    }
                    spi_read_reg += tfc4.getText();
                    spi_write_data += tfc3.getText();
                    spi_write_reg += tfc31.getText();
                    String reference = "no";
                    if (checkbox_c.isSelected()) {
                        reference = "yes";
                    }
                    sql.add_sensor(tfc1.getText(), tfc2.getText(), spi_write_reg, spi_write_data, spi_read_reg,reference);
                    lc.setText("Sensor added!");
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    lc.setText("Error!");
                }
            }
        }
    }

}
