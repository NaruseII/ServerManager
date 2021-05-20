package fr.naruse.servermanager.core;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.netty.channel.unix.DomainSocketAddress;

import java.lang.reflect.Type;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Random;

public class Utils {

    public static final Gson GSON = new Gson();
    public static final Type MAP_TYPE = new TypeToken<Map<String, Object>>(){}.getType();
    public static final Type MAP_STRING_TYPE = new TypeToken<Map<String, String>>(){}.getType();

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

    public static SocketAddress getAddr(String hostline) {
        URI uri = null;
        try
        {
            uri = new URI( hostline );
        } catch ( URISyntaxException ex )
        {
        }

        if ( uri != null && "unix".equals( uri.getScheme() ) )
        {
            return new DomainSocketAddress( uri.getPath() );
        }

        if ( uri == null || uri.getHost() == null )
        {
            try
            {
                uri = new URI( "tcp://" + hostline );
            } catch ( URISyntaxException ex )
            {
                throw new IllegalArgumentException( "Bad hostline: " + hostline, ex );
            }
        }

        if ( uri.getHost() == null )
        {
            throw new IllegalArgumentException( "Invalid host/address: " + hostline );
        }

        return new InetSocketAddress( uri.getHost(), ( uri.getPort() ) == -1 ? 25565 : uri.getPort() );
    }

}
