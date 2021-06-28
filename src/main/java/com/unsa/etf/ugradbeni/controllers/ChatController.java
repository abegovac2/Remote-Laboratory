package com.unsa.etf.ugradbeni.controllers;

import com.unsa.etf.ugradbeni.StageHandler;
import com.unsa.etf.ugradbeni.alert.AlertMaker;
import com.unsa.etf.ugradbeni.bubble.BubbledLabel;
import com.unsa.etf.ugradbeni.models.*;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.HPos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONException;
import org.json.JSONObject;

import java.awt.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import static javafx.scene.control.PopupControl.USE_COMPUTED_SIZE;

public class ChatController {
    @FXML
    public Label currRoom;

    @FXML
    public VBox userList;

    @FXML
    public VBox roomList;

    @FXML
    public Button SendMessage;

    @FXML
    public TextField MessageField;

    @FXML
    public Label usernameLbl;

    @FXML
    public ScrollPane scrollChat;

    @FXML
    public Button btnUsersRefresh;

    @FXML
    public Button btnRoomRefresh;

    @FXML
    public Menu messageSounds;

    private MessagingClient chatUser;
    private final SimpleStringProperty messageContent = new SimpleStringProperty("");
    private final String currentChatTopic;
    private final User user;
    private final List<Message> last10;
    private final Room currentRoom;
    private int brojPoruke = 0;
    private GridPane Chat = new GridPane();
    private boolean sound = true;

    @FXML
    public void initialize() {

        MenuItem mnit = new MenuItem("Turn off sound");
        mnit.setOnAction(event -> {
            if (sound) {
                sound = false;
                mnit.setText("Turn on sound");
            } else {
                sound = true;
                mnit.setText("Turn off sound");
            }
        });
        messageSounds.getItems().add(mnit);

        currRoom.setText("Current room: " + currentRoom.getRoomName());


        usernameLbl.setText(user.getUserName());
        MessageField.textProperty().bindBidirectional(messageContent);

        Chat.setStyle("-fx-background-color: #2A2E37");
        Chat.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        Chat.setVgap(5.);
        ColumnConstraints c1 = new ColumnConstraints();
        c1.setPercentWidth(800);
        Chat.getColumnConstraints().add(c1);
        scrollChat.setContent(Chat);
        scrollChat.setFitToWidth(true);


        Chat.heightProperty().addListener(observable -> scrollChat.setVvalue(1D));

        try {
            chatUserSetup();
        } catch (MqttException e) {
            e.printStackTrace();
        }

        //adds last 10 messages to the chat
        for (int i = last10.size() - 1; i >= 0; --i) {
            HBox message = createNewMessageForWindow(last10.get(i).getMessage());
            Chat.addRow(brojPoruke++, message);
        }

        //to add users to the list of active users
        for (String user1 : user.getConnectedUsers()) {
            Button bl = new Button();
            bl.setText(user1);
            bl.setMinHeight(40);
            bl.setMinWidth(155);
            userList.getChildren().add(bl);
        }

        //to add currently avalable rooms
        for (Room room : user.getActiveRooms()) {
            if (room.equals(currentRoom)) continue;
            Button bl = new Button();
            bl.setMinHeight(40);
            bl.setMinWidth(155);
            bl.setText(room.getRoomName());
            bl.setOnAction((ActionEvent event) -> openNewChat(room));
            roomList.getChildren().add(bl);
        }

        try {
            user.setupConnectDisconnect(userList);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public ChatController(User user, Room room) {
        this.last10 = user.getLast10();
        this.user = user;
        this.currentRoom = room;
        currentChatTopic = "" + ThemesMqtt.BASE + ThemesMqtt.MESSAGE + "/" + this.currentRoom.getRoomName();
    }


    @FXML
    public void aboutWindow(ActionEvent actionEvent) {
        StageHandler.openNewWindow(getClass().getResource("/views/AboutWindow.fxml"), "Remote Laboratory", new AboutAppController());
    }


    @FXML
    public void sendAction(ActionEvent actionEvent) {
        String message = messageContent.get().trim();

        if (message.length() > 255) {
            AlertMaker.alertINFORMATION("Notice", "Message can't have more than 255 character.");
            return;
        }
        if (message.isEmpty()) {
            AlertMaker.alertERROR("", "Message field is empty!");
            return;
        }

        messageContent.set("");

        if (message.startsWith("!port")) sendToMbed(message);
        else if (message.startsWith("!info")) sendForInfo();
        else sendRegular(message, null);

    }

    private void sendToMbed(String message) {
        StringBuilder port = new StringBuilder();
        char[] string = message.toCharArray();
        int i = 1;
        while (string[i] != ':') {
            port.append(string[i]);
            ++i;
        }
        String sendToTopic = "" + ThemesMqtt.BASE + ThemesMqtt.MESSAGE + "/" + this.currentRoom.getRoomName() + "/mbed/" + port.toString();
        StringBuilder value = new StringBuilder();
        ++i;
        while (i < message.length()) {
            if (string[i] != ' ') value.append(string[i]);
            ++i;
        }
        if (value.toString().isEmpty()) return;
        try {
            chatUser.sendMessage(sendToTopic, value.toString(), 0);
            sendRegular(message, "mbed");
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    private void sendForInfo() {
        String info = currentChatTopic + ThemesMqtt.SEND_INFO;
        try {
            chatUser.sendMessage(info, "{}", 0);
            sendRegular("!info", "mbed");
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    private void sendRegular(String message, String toMbed) {
        Message sendMessage = new Message(-1, "[" + user.getUserName() + "]: " + message, currentRoom.getId());
        try {
            String topic = currentChatTopic + (toMbed == null ? "" : "/" + toMbed);
            chatUser.sendMessage(topic, sendMessage.toString(), 0);
        } catch (JSONException | MqttException e) {
            e.printStackTrace();
        }

    }

    private void chatUserSetup() throws MqttException {
        HashMap<String, MqttOnRecive> functions = new HashMap<>();
        chatUser = new MessagingClient(user.getUserName(), functions);
        chatUser.subscribeToTopic(currentChatTopic + "/#", null, 0);
        MqttOnRecive recive = (String theme, MqttMessage mqttMessage) ->
                new Thread(() -> {
                    try {
                        if (theme.endsWith("info")) return;

                        JSONObject msg = new JSONObject(new String(mqttMessage.getPayload()));
                        String text = msg.getString("Message");
                        HBox message = createNewMessageForWindow(text);

                        Platform.runLater(() -> Chat.addRow(brojPoruke++, message));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }).start();

        functions.put("message", recive);
        functions.put(currentRoom.getRoomName(), recive);
        functions.put("mbed", recive);
    }

    private HBox createNewMessageForWindow(String text) {
        BubbledLabel bl = new BubbledLabel();
        bl.getStyleClass().add("chat-bubble");

        if (sound && text.contains("[") && text.contains("]") && !user.getUserName().equals(text.substring(text.indexOf("[") + 1, text.indexOf("]"))))
            Toolkit.getDefaultToolkit().beep();

        String message = "";
        if (text.length() > 50) {
            int x = (int) (Math.ceil(text.length() / 50));
            x++;
            int j = 0;
            while (x != 0) {
                if (j + 50 > text.length())
                    message += text.substring(j) + "\n";
                else
                    message += text.substring(j, j + 50) + "\n";
                j += 50;
                x--;
            }
        } else
            message = text;
        bl.setText(message);
        GridPane.setHalignment(bl, HPos.LEFT);

        Image img = new Image("https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcR277JJUkU44zs2ln_Bw37bt5V_gY-XWpF3HQ&usqp=CAU");
        Circle cir2 = new Circle(40, 30, 20);
        cir2.setStroke(Color.SEAGREEN);
        cir2.setFill(Color.SNOW);
        cir2.setEffect(new DropShadow(+4d, 0d, +2d, Color.DARKSEAGREEN));
        cir2.setFill(new ImagePattern(img));

        HBox messageBox = new HBox();
        messageBox.setSpacing(3);
        messageBox.getChildren().add(cir2);
        messageBox.getChildren().add(bl);

        return messageBox;
    }

    public void openNewChat(Room room) {
        Stage newChatStage = new Stage();
        newChatStage.getIcons().add(new Image("/pictures/etf_logo.png"));
        newChatStage.setTitle("Remote Laboratory");
        newChatStage.setResizable(false);
        closeWindow();

        final Parent[] roots = {null};

        Task<Boolean> loadingTask = new Task<Boolean>() {
            @Override
            protected Boolean call() {
                try {
                    user.getMessagesForRoom(room);
                } catch (MqttException e) {
                    e.printStackTrace();
                }
                //check to see if the selected room was deleted
                if (user.getLast10().get(user.getLast10().size() - 1).getId() == -404) return false;

                FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/ChatWindow.fxml"));
                ChatController chat = new ChatController(user, room);
                loader.setController(chat);
                try {
                    chatUser.disconnect();
                    roots[0] = loader.load();
                } catch (IOException | MqttException e) {
                    e.printStackTrace();
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                newChatStage.setOnCloseRequest((event) -> {
                    user.disconnectUser();
                    Platform.exit();
                });

                return true;
            }
        };

        loadingTask.setOnSucceeded(workerStateEvent -> {
            if (roots[0] == null) {
                newChatStage.close();
                AlertMaker.alertERROR("An error has occured", "Room \"" + room.getRoomName() + "\" no longer exists!");
            } else {
                closeWindow();
                newChatStage.setScene(new Scene(roots[0], USE_COMPUTED_SIZE, USE_COMPUTED_SIZE));
                newChatStage.show();
            }
        });

        Parent secRoot = null;
        try {
            secRoot = FXMLLoader.load(getClass().getResource("/views/LoadingWindow.fxml"));
            secRoot.setVisible(true);
        } catch (IOException e) {
            e.printStackTrace();
        }

        newChatStage.setScene(new Scene(secRoot, USE_COMPUTED_SIZE, USE_COMPUTED_SIZE));
        newChatStage.initModality(Modality.APPLICATION_MODAL);
        newChatStage.show();
        Thread thread = new Thread(loadingTask);
        thread.start();

    }

    public void closeWindow() {
        ((Stage) Chat.getScene().getWindow()).close();
    }

    public void closeAction() {
        ((Stage) Chat.getScene().getWindow()).close();
        user.disconnectUser();
        Platform.exit();
    }

    @FXML
    public void roomRefresh(ActionEvent event) {
        new Thread(() -> {
            user.refreshActiveRooms();
            Platform.runLater(() -> {
                roomList.getChildren().clear();
                roomList.getChildren().add(btnRoomRefresh);
            });
            for (Room room : user.getActiveRooms()) {
                if (room.equals(currentRoom)) continue;
                Button bl = new Button();
                bl.setMinHeight(40);
                bl.setMinWidth(155);
                bl.setText(room.getRoomName());
                bl.setOnAction((ActionEvent event1) -> openNewChat(room));
                Platform.runLater(() -> roomList.getChildren().add(bl));
            }
        }).start();
    }

    @FXML
    public void onlineUserRefresh(ActionEvent event) {
        new Thread(() -> {
            user.refreshConnectedUsers();
            Platform.runLater(() -> {
                userList.getChildren().clear();
                userList.getChildren().add(btnUsersRefresh);
            });
            for (String user1 : user.getConnectedUsers()) {
                Button bl = new Button();
                bl.setMinHeight(40);
                bl.setMinWidth(155);
                bl.setText(user1);
                Platform.runLater(() -> userList.getChildren().add(bl));
            }
        }).start();
    }


}
