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
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONException;
import org.json.JSONObject;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static javafx.scene.control.PopupControl.USE_COMPUTED_SIZE;

public class ChatController {
    @FXML
    public Label currRoom;

    @FXML
    public VBox UserList;

    @FXML
    public VBox RoomList;

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
    private GridPane chat = new GridPane();
    private ArrayList<String> listOfSounds = new ArrayList<String>();
    private File f = new File("");

    @FXML
    public void initialize() {
        listOfSounds.add("src/main/resources/sounds/definite-555.wav");
        listOfSounds.add("src/main/resources/sounds/graceful-558.wav");
        listOfSounds.add("src/main/resources/sounds/quite-impressed-565.wav");
        listOfSounds.add("src/main/resources/sounds/to-the-point-568.wav");

        f = new File("src/main/resources/sounds/definite-555.wav");

        for (int i = 0; i < 4; i++) {
            MenuItem mnit = new MenuItem("Sound " + (i + 1));
            mnit.setOnAction(event -> {
                f = new File(listOfSounds.get(Integer.parseInt(mnit.getText().substring(6, 7)) - 1));
                try {
                    AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(f);
                    Clip clip = AudioSystem.getClip();
                    clip.open(audioInputStream);
                    clip.start();
                } catch (UnsupportedAudioFileException | LineUnavailableException | IOException e) {
                    e.printStackTrace();
                }
            });
            messageSounds.getItems().add(mnit);
        }


        currRoom.setText("Current room: " + currentRoom.getRoomName());


        usernameLbl.setText(user.getUserName());
        MessageField.textProperty().bindBidirectional(messageContent);

        chat.setStyle("-fx-background-color: #2A2E37");
        chat.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        chat.setVgap(5.);
        ColumnConstraints c1 = new ColumnConstraints();
        c1.setPercentWidth(800);
        chat.getColumnConstraints().add(c1);
        scrollChat.setContent(chat);
        scrollChat.setFitToWidth(true);


        chat.heightProperty().addListener(observable -> scrollChat.setVvalue(1D));

        try {
            chatUserSetup();
        } catch (MqttException e) {
            e.printStackTrace();
        }

        for (int i = last10.size() - 1; i >= 0; --i) {
            BubbledLabel bl = new BubbledLabel();
            bl.getStyleClass().add("chat-bubble");

            String text = last10.get(i).getMessage();
            String mess = "";
            if (text.length() > 40) {
                for (int j = 0; j < text.length() - 40; j += 40) {
                    mess += text.substring(j, j + 40) + "\n";
                }
            } else
                mess = text;
            bl.setText(mess);

            GridPane.setHalignment(bl, HPos.LEFT);

            Image img = new Image("https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcR277JJUkU44zs2ln_Bw37bt5V_gY-XWpF3HQ&usqp=CAU");
            Circle cir2 = new Circle(40, 30, 20);
            cir2.setStroke(Color.SEAGREEN);
            cir2.setFill(Color.SNOW);
            cir2.setEffect(new DropShadow(+4d, 0d, +2d, Color.DARKSEAGREEN));
            cir2.setFill(new ImagePattern(img));

            HBox x = new HBox();
            x.setSpacing(3);
            x.getChildren().add(cir2);
            x.getChildren().add(bl);

            chat.addRow(brojPoruke++, x);


        }


        for (String user1 : user.getConnectedUsers()) {
            Button bl = new Button();
            bl.setText(user1);
            bl.setMinHeight(40);
            bl.setMinWidth(155);
            UserList.getChildren().add(bl);
        }

        for (Room room : user.getActiveRooms()) {
            if (room.equals(currentRoom)) continue;
            Button bl = new Button();
            bl.setMinHeight(40);
            bl.setMinWidth(155);
            bl.setText(room.getRoomName());
            bl.setOnAction((ActionEvent event) -> openNewChat(room));
            RoomList.getChildren().add(bl);
        }

        try {
            user.setupConnectDisconnect(UserList);
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
    public void aboutWind(ActionEvent actionEvent){
        StageHandler.openNewWindow(getClass().getResource("/views/AboutWindow.fxml"), "Remote Laboratory", new AboutAppController());
    }


    @FXML
    public void sendAction(ActionEvent actionEvent) {
        String message = messageContent.get().trim();
        System.out.println("DUZINA PORUKE: " + message.length());
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

                        BubbledLabel bl = new BubbledLabel();
                        bl.getStyleClass().add("chat-bubble");

                        JSONObject msg = new JSONObject(new String(mqttMessage.getPayload()));

                        String text = msg.getString("Message");

//                        System.out.println("poruka: " + text);
//                        System.out.println("SUBSTR: "+ text.substring(text.indexOf("[")+1,text.indexOf("]")));
//                        System.out.println("currUser: " + user.getUserName());


                        AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(f);
                        Clip clip = AudioSystem.getClip();
                        clip.open(audioInputStream);


                        if (!user.getUserName().equals(text.substring(text.indexOf("[") + 1, text.indexOf("]"))))
                            clip.start();


                        String mess = "";
                        if (text.length() > 40) {
                            for (int i = 0; i < text.length() - 40; i += 40) {
                                mess += text.substring(i, i + 40) + "\n";
                            }
                        } else
                            mess = text;
                        bl.setText(mess);
                        GridPane.setHalignment(bl, HPos.LEFT);

                        Image img = new Image("https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcR277JJUkU44zs2ln_Bw37bt5V_gY-XWpF3HQ&usqp=CAU");
                        Circle cir2 = new Circle(40, 30, 20);
                        cir2.setStroke(Color.SEAGREEN);
                        cir2.setFill(Color.SNOW);
                        cir2.setEffect(new DropShadow(+4d, 0d, +2d, Color.DARKSEAGREEN));
                        cir2.setFill(new ImagePattern(img));

                        HBox x = new HBox();
                        x.setSpacing(3);
                        x.getChildren().add(cir2);
                        x.getChildren().add(bl);

                        Platform.runLater(() -> chat.addRow(brojPoruke++, x));
                    } catch (JSONException | UnsupportedAudioFileException | IOException | LineUnavailableException e) {
                        e.printStackTrace();
                    }
                }).start();

        functions.put("message", recive);
        functions.put(currentRoom.getRoomName(), recive);
        functions.put("mbed", recive);
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
        ((Stage) chat.getScene().getWindow()).close();
    }

    public void closeAction() {
        ((Stage) chat.getScene().getWindow()).close();
        user.disconnectUser();
        Platform.exit();
    }

    @FXML
    public void roomRefresh(ActionEvent event) {
        new Thread(() -> {
            user.refreshActiveRooms();
            Platform.runLater(() -> {
                RoomList.getChildren().clear();
                RoomList.getChildren().add(btnRoomRefresh);
            });
            for (Room room : user.getActiveRooms()) {
                if (room.equals(currentRoom)) continue;
                Button bl = new Button();
                bl.setMinHeight(40);
                bl.setMinWidth(155);
                bl.setText(room.getRoomName());
                bl.setOnAction((ActionEvent event1) -> openNewChat(room));
                Platform.runLater(() -> RoomList.getChildren().add(bl));
            }
        }).start();

    }

    @FXML
    public void userRefresh(ActionEvent event) {
        new Thread(() -> {
            user.refreshConnectedUsers();
            Platform.runLater(() -> {
                UserList.getChildren().clear();
                UserList.getChildren().add(btnUsersRefresh);
            });
            for (String user1 : user.getConnectedUsers()) {
                Button bl = new Button();
                bl.setMinHeight(40);
                bl.setMinWidth(155);
                bl.setText(user1);
                Platform.runLater(() -> UserList.getChildren().add(bl));
            }
        }).start();
    }


}
