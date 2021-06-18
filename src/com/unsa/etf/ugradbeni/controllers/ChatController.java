package com.unsa.etf.ugradbeni.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

public class ChatController {
    @FXML
    public VBox ChatList;
    @FXML
    public VBox UserList;
    @FXML
    public VBox RoomList;
    @FXML
    public Button SendMessage;
    @FXML
    public TextField MessageField;


    public ChatController(){



    }

    @FXML
    public void sendAction(ActionEvent actionEvent){
            RoomList.getChildren().add(new Button("Room " + (int)(RoomList.getChildren().size()+1)));

    }






}
