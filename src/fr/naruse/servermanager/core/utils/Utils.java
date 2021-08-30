package fr.naruse.servermanager.core.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import fr.naruse.servermanager.core.ServerManager;
import fr.naruse.servermanager.core.logging.ServerManagerLogger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.lang.reflect.Type;
import java.net.*;
import java.nio.channels.FileChannel;
import java.util.*;

public class Utils {

    public static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .create();
    public static final Type MAP_TYPE = new TypeToken<Map<String, Object>>(){}.getType();
    public static final Type MAP_STRING_TYPE = new TypeToken<Map<String, String>>(){}.getType();
    public static final Type SET_TYPE = new TypeToken<Set<String>>(){}.getType();
    public static final Type LIST_GENERIC_TYPE = new TypeToken<List>(){}.getType();
    public static final Type MAP_GENERIC_TYPE = new TypeToken<Map>(){}.getType();
    public static final Type LIST_MAP_TYPE = new TypeToken<List<Map<String, Object>>>(){}.getType();

    public static final char[] ALPHABET = "abcdefghijklmnopqrstuvwxyz".toCharArray();
    public static final Random RANDOM = new Random();


    public static String randomLetters(int count){
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < count; i++) {
            stringBuilder.append(randomLetter());
        }
        return stringBuilder.toString();
    }

    public static char randomLetter(){
        return RANDOM.nextBoolean() ? ALPHABET[RANDOM.nextInt(ALPHABET.length)] : Character.toUpperCase(ALPHABET[RANDOM.nextInt(ALPHABET.length)]);
    }

    public static double getDoubleFromPacket(Object o) {
        return Double.parseDouble(o.toString());
    }

    public static int getIntegerFromPacket(Object o) {
        return (int) getDoubleFromPacket(o.toString());
    }


    public static boolean copyDirectory(File source, File dest) {
        if(!source.exists()){
            return false;
        }
        for (File file : source.listFiles()) {
            if(file.isDirectory()){
                if(!copyDirectory(file, new File(dest, file.getName()))){
                    return false;
                }
            }else{
                if(!copyFile(file, new File(dest, file.getName()))){
                    return false;
                }
            }
        }
        return true;
    }

    private static boolean copyFile(File sourceFile, File destFile) {
        try{
            if (!sourceFile.exists()) {
                return false;
            }
            if(!destFile.getParentFile().exists()){
                destFile.getParentFile().mkdirs();
            }
            if (destFile.exists()) {
                destFile.delete();
            }
            destFile.createNewFile();

            FileChannel source = null;
            FileChannel destination = null;
            source = new FileInputStream(sourceFile).getChannel();
            destination = new FileOutputStream(destFile).getChannel();
            if (destination != null && source != null) {
                destination.transferFrom(source, 0, source.size());
            }
            if (source != null) {
                source.close();
            }
            if (destination != null) {
                destination.close();
            }

            return true;
        }catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }

    public static void delete(File file) {
        List<File> list = new ArrayList<>();

        if(file.isDirectory()){
            if(file.listFiles() == null){
                file.delete();
            }else{
                for (File listFile : file.listFiles()) {
                    delete(listFile);
                }
                list.add(file);
            }
        }else{
            file.delete();
        }

        Collections.reverse(list);
        for (File file1 : list) {
            file1.delete();
        }
    }

    public static InetAddress getPacketManagerHost(){
        return findHost(ServerManager.get().getCoreData().getPacketManagerHost(), true);
    }

    public static InetAddress getLocalHost(){
        InetAddress inetAddress = findHost(null, false);
        if(inetAddress == null){
            inetAddress = findHost("127.0.0.1", false);
        }
        if(inetAddress == null){
            inetAddress = findHost("::1", true);
        }
        return inetAddress;
    }

    public static InetAddress findHost(String host, boolean trace){
        try {
            return host == null ? InetAddress.getLocalHost() : InetAddress.getByName(host);
        } catch (UnknownHostException unknownHostException) {
            if(trace){
                unknownHostException.printStackTrace();
            }
        }
        return null;
    }

    public static Object getStaticField(Class clazz, String methodName){
        try {
            return clazz.getDeclaredMethod(methodName).invoke(null);
        } catch (Exception e) {
            new ServerManagerLogger.Logger("Logger").error("Can't find color '"+methodName+"'!");
        }
        return null;
    }

    public static String getCurrentAddress() {
        try{
            final Socket socket = new Socket();
            socket.connect(new InetSocketAddress("google.com", 80));
            return socket.getLocalAddress().getHostAddress();
        }catch (Exception e){
            e.printStackTrace();
        }
        return "localhost";
    }
}
