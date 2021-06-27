package com.unsa.etf.ugradbeni.controllers;

import com.unsa.etf.ugradbeni.LoadWebPage;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;

public class AboutAppController {

    private final String LINK_OF_GITHUB_PROJECT = "https://github.com/abegovac2/Remote-Laboratory";

    @FXML
    public void sourceCodeLinkAction(ActionEvent actionEvent){
        LoadWebPage.loadWebpage(LINK_OF_GITHUB_PROJECT);
    }

}
