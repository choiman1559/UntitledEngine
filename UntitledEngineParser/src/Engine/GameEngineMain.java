package Engine;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.*;
import java.util.Optional;

public class GameEngineMain extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    public final static String Version = "0.0.1";
    public static File ScriptFile;
    public static Stage window;
    public static Scene scene;

    @Override
    public void start(Stage primaryStage) {
        window = primaryStage;
        window.setTitle("Untitled Game Engine");

        VBox layout = new VBox(10);
        layout.setAlignment(Pos.TOP_CENTER);
        layout.setStyle("-fx-background-color: #000000;");

        scene = new Scene(layout, 1024, 720);
        window.setResizable(false);
        window.setScene(scene);
        window.show();

        if(getParameters().getRaw().size() < 1) {
            final FileChooser fileChooser = new FileChooser();
            File file = fileChooser.showOpenDialog(primaryStage);
            openFile(file);
        } else openFile(new File(getParameters().getRaw().get(1)));
    }

    private void openFile(File file) {
        if(file != null) {
            System.out.println("File input : " + file.getAbsolutePath());
            String[] var = file.getName().split("\\.");
            if (var[var.length - 1].toLowerCase().equals("ues")) {
                ScriptFile = file;
                processSplash();
            } else {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Error!");
                alert.setHeaderText("There was an error while opening the file.");
                alert.setContentText("The file is not a valid script file.");
                alert.setOnCloseRequest(event -> window.close());
                Optional<ButtonType> result = alert.showAndWait();
                if (result.isPresent() && result.get() == ButtonType.OK) {
                    window.close();
                }
            }
        } else {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Error!");
            alert.setHeaderText("There was an error while opening the file.");
            alert.setContentText("The file is not selected or not exist.");
            alert.setOnCloseRequest(event -> window.close());
            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                window.close();
            }
        }
    }

    private void processSplash() {
        try {
            GameEngineParser.processGame(ScriptFile);
        } catch (Exception e) {
            showExceptionThrowDialog(e);
        }
    }

    public static void showExceptionThrowDialog(Exception e) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Exception thrown");
        alert.setHeaderText("Exception Thrown while processing");
        alert.setContentText("Error Occurred while parsing script!");

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        String exceptionText = sw.toString();

        Label label = new Label("The exception stacktrace was:");

        TextArea textArea = new TextArea(exceptionText);
        textArea.setEditable(false);
        textArea.setWrapText(true);

        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);
        GridPane.setVgrow(textArea, Priority.ALWAYS);
        GridPane.setHgrow(textArea, Priority.ALWAYS);

        GridPane expContent = new GridPane();
        expContent.setMaxWidth(Double.MAX_VALUE);
        expContent.add(label, 0, 0);
        expContent.add(textArea, 0, 1);
        alert.getDialogPane().setExpandableContent(expContent);
        alert.showAndWait();
        alert.setOnCloseRequest(event -> window.close());
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            window.close();
        }
    }
}
