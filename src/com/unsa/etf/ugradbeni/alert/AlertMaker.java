package com.unsa.etf.ugradbeni.alert;


import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.stage.Stage;

import java.util.Optional;

public class AlertMaker {


    public static void alertERROR(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("Došlo je do greške");
        alert.setContentText(content);
        styleAlert(alert);
        alert.showAndWait();
    }
    public static void alertINFORMATION(String title, String content){
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Poruka");
        alert.setContentText(content);
        styleAlert(alert);
        alert.showAndWait();
    }
    public static Optional<ButtonType> alertCONFIRMATION(String title, String content){
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Potvrda");
        alert.setHeaderText(title);
        alert.setContentText(content);styleAlert(alert);

        return  alert.showAndWait();
    }





    private static void styleAlert(Alert alert) {
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().add(AlertMaker.class.getResource("/css/dark-theme.css").toExternalForm());
        dialogPane.getStyleClass().add("custom-alert");
    }
}
