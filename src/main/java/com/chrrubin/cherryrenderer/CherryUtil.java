package com.chrrubin.cherryrenderer;

import com.chrrubin.cherryrenderer.prefs.ThemePreference;
import com.chrrubin.cherryrenderer.prefs.ThemePreferenceValue;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.util.Duration;
import uk.co.caprica.vlcj.factory.discovery.NativeDiscovery;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CherryUtil {
    public static final String VERSION = "2.0.2";
    public static final ThemePreferenceValue LOADED_THEME = new ThemePreference().get(); // Ensures theme is consistent throughout application runtime
    public static final NativeDiscovery VLC_NATIVE_DISCOVERY = new NativeDiscovery();
    public static final boolean FOUND_VLC = VLC_NATIVE_DISCOVERY.discover(); //FIXME: Edge case where a system with a valid VLC installation fail native discovery but can initialize MediaPlayerFactory. No extra information from tester and unable to reproduce myself.
    public static final String USER_AGENT = "CherryRenderer/" + CherryUtil.VERSION + " (" + System.getProperty("os.name") + "; " + System.getProperty("os.arch") + "; " + System.getProperty("os.version") + ")";

    public static String durationToString(Duration duration){
        int intSeconds = (int)Math.floor(duration.toSeconds());
        int hours = intSeconds / 60 / 60;

        if(hours != 0){
            intSeconds -= (hours * 60 * 60);
        }

        int minutes = intSeconds / 60;
        int seconds = intSeconds - (minutes * 60);

        if(duration.greaterThanOrEqualTo(Duration.ZERO)){
            return String.format("%d:%02d:%02d", hours, minutes, seconds);
        }
        else{
            return String.format("-%d:%02d:%02d",-hours, -minutes, -seconds);
        }
    }

    public static Duration stringToDuration(String strTime){
        String pattern = "(\\d+:)*\\d{2}:\\d{2}";
        Pattern p = Pattern.compile(pattern);
        Matcher m = p.matcher(strTime);
        if(!m.matches()){
            return null;
        }

        String[] splitTime = strTime.split(":");

        if(splitTime.length == 3){
            int hour = Integer.parseInt(splitTime[0]);
            int min = Integer.parseInt(splitTime[1]);
            int sec = Integer.parseInt(splitTime[2]);

            if(min <= 59 && sec <= 59){
                return Duration.hours(hour).add(Duration.minutes(min)).add(Duration.seconds(sec));
            }
            else{
                return null;
            }
        }
        else if(splitTime.length == 2){
            int min = Integer.parseInt(splitTime[0]);
            int sec = Integer.parseInt(splitTime[1]);

            if(min <= 59 && sec <= 59){
                return Duration.minutes(min).add(Duration.seconds(sec));
            }
            else{
                return null;
            }
        }
        else{
            return null;
        }
    }

    public static String getLatestVersion() throws IOException, RuntimeException{
        URL url = new URL("https://api.github.com/repos/chrrubin/cherryrenderer/releases/latest");

        HttpURLConnection connection = (HttpURLConnection)url.openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(5000);

        Reader reader = new InputStreamReader(connection.getInputStream());

        JsonObject jsonObject = new Gson().fromJson(reader, JsonObject.class);

        String latestVersion = jsonObject.get("tag_name").getAsString();
        if(latestVersion.isEmpty()){
            throw new RuntimeException("Could not get latest version.");
        }
        else{
            return latestVersion;
        }
    }

    public static boolean isOutdated(String latestVersion) {
        if(VERSION.split("-").length > 1) {
            String currentMetadata = VERSION.split("-")[1].toUpperCase();
            String currentWithoutMetadata = VERSION.split("-")[0];

            if ((currentMetadata.contains("SNAPSHOT") || currentMetadata.contains("RC")) && currentWithoutMetadata.equals(latestVersion)) {
                return true;
            }
        }

        int[] currentIntArray = semanticToIntArray(VERSION);
        int[] latestIntArray = semanticToIntArray(latestVersion);

        int maxLength = Math.max(currentIntArray.length, latestIntArray.length);
        for (int i = 0; i < maxLength; i++) {
            int current = i < currentIntArray.length ? currentIntArray[i] : 0;
            int latest = i < latestIntArray.length ? latestIntArray[i] : 0;
            if(current != latest){
                return current < latest;
            }
        }
        return false;
    }

    private static int[] semanticToIntArray(String semantic){
        String[] split = semantic.split("-")[0].split("\\.");
        int[] numbers = new int[split.length];

        for (int i = 0; i < split.length; i++) {
            numbers[i] = Integer.valueOf(split[i]);
        }

        return numbers;
    }

    public static Service<String> getLatestVersionJFXService(){
        return new Service<String>() {
            @Override
            protected Task<String> createTask() {
                return new Task<String>() {
                    @Override
                    protected String call() throws Exception {
                        return getLatestVersion();
                    }
                };
            }
        };
    }
}
