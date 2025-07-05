/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.rebeyond.behinder.ui;

import java.awt.*;
import java.io.ByteArrayInputStream;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import net.rebeyond.behinder.core.Constants;
import net.rebeyond.behinder.utils.Utils;
import javafx.scene.control.Label;
public class Main
extends Application {
    @Override
    public void start(Stage primaryStage) {
        try {
            Main.detectJDK();
            Parent root = (Parent)FXMLLoader.load(this.getClass().getResource("Main.fxml"));
            primaryStage.setTitle(String.format("\u51b0\u874e%s\u52a8\u6001\u4e8c\u8fdb\u5236\u52a0\u5bc6Web\u8fdc\u7a0b\u7ba1\u7406\u5ba2\u6237\u7aef", Constants.VERSION));
            primaryStage.getIcons().add(new Image(new ByteArrayInputStream(Utils.getResourceData("net/rebeyond/behinder/resource/logo.jpg"))));
            Scene scene = new Scene(root, 1200.0, 600.0);
            scene.getRoot().setStyle("-fx-font-family: 'Arial'");
            primaryStage.setScene(scene);
            primaryStage.show();
            Label mianshaBtn = (Label) root.lookup("#mianshaBtn");

            if (mianshaBtn != null) {
                mianshaBtn.setOnMouseClicked(event -> {
                    try {
                        FXMLLoader dialogLoader = new FXMLLoader(getClass().getResource("miansha.fxml"));
                        Parent dialogRoot = dialogLoader.load();
                        Stage dialogStage = new Stage();
                        dialogStage.setTitle("免杀生成");
                        dialogStage.getIcons().add(new Image(new ByteArrayInputStream(Utils.getResourceData("net/rebeyond/behinder/resource/logo.jpg"))));
                        dialogStage.setScene(new Scene(dialogRoot));
                        dialogStage.initOwner(primaryStage);
                        dialogStage.show();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                });
            }
        } catch (Exception e) {
            Utils.showErrorMessage("\u9519\u8bef", e.getMessage());
            e.printStackTrace();
        } catch (Error e) {
            e.printStackTrace();
        }
    }

    private static void detectJDK() throws Exception {
        Utils.getCompiler();
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        System.exit(0);
    }

    public static void main(String[] args2) {
        try {
            Main.launch(new String[0]);
        } catch (Throwable ex) {
            Utils.showErrorMessage("\u9519\u8bef", ex.getMessage());
            ex.printStackTrace();
        }
    }
}

