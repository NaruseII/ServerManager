package fr.naruse.servermanager.core;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class Utils {

    public static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .create();
    public static final Type MAP_TYPE = new TypeToken<Map<String, Object>>(){}.getType();
    public static final Type MAP_STRING_TYPE = new TypeToken<Map<String, String>>(){}.getType();
    public static final Type SET_TYPE = new TypeToken<Set<String>>(){}.getType();

    public static final char[] ALPHABET = "abcdefghijklmnopqrstuvwxyz".toCharArray();
    public static final Random RANDOM = new Random();


    public static String randomLetters(){
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < 4; i++) {
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

}
