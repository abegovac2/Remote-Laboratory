package com.unsa.etf.ugradbeni.controllers;

import com.unsa.etf.ugradbeni.bubble.BubbledLabel;
import com.unsa.etf.ugradbeni.models.MqttOnRecive;
import com.unsa.etf.ugradbeni.models.Room;
import com.unsa.etf.ugradbeni.models.mqtt_components.MessagingClient;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;

public class GroupController {
    @FXML
    public Label user;

    @FXML
    public FlowPane pane;

    @FXML
    public TextField date;

    private String username;
    private DateTimeFormatter myFormatObj = DateTimeFormatter.ofPattern("dd-MMM-yyyy");



    @FXML
    public void initialize(){
        date.setText(LocalDate.now().format(myFormatObj));
        user.setText(username);

        //ovako se dodaje u pane
        for(int i=1;i<=10;i++){
            Button grupa = new Button("soba "+i);
            grupa.setMinWidth(200);
            grupa.setMinHeight(200);
            pane.getChildren().add(grupa);
        }

    }

    public GroupController(String username){
        this.username= username;
    }


}
