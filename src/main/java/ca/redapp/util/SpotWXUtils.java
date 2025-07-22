package ca.redapp.util;

import java.awt.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class SpotWXUtils {

    public static List<String> fetchModelListFromAPI(double Latitude, double Longitude, String apiKey) {
        List<String> li = new ArrayList<>();
        String apiUrl = "https://spotwx.io/api.php?key=" + apiKey +"&lat=" + Latitude + "&lon=" + Longitude + "&model=inventory";
        try {
            URL url = new URL(apiUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            //conn.setReadTimeout(5000);

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    li.add(line);
                }
            }
        } catch (Exception e) {
            li.add(e.getMessage());
            e.printStackTrace();
        }
        if(li.size() == 1 && li.get(0).contains(",")){
            return Arrays.stream(li.get(0).split(","))
                    .map(String::trim)
                    .collect(Collectors.toList());
        }

        return li;
    }

    public static String DownloadSpotWXModelData(double Latitude, double Longitude, String modelName, Date date, String apiKey) {


        SimpleDateFormat formatter = new  SimpleDateFormat("yyyyMMdd");
        String modelDate = formatter.format(date) + "_00Z";
        String apiUrl = "https://spotwx.io/api.php?modelrun=" + modelDate + "&key=" + apiKey + "&lat=" + Latitude + "&lon=" + Longitude + "&model=" + modelName + "&format=prometheus";
        List<String> responses0Z = fetchModelDataFromAPI(apiUrl);

        modelDate = formatter.format(date) + "_12Z";
        apiUrl = "https://spotwx.io/api.php?modelrun=" + modelDate + "&key=" + apiKey + "&lat=" + Latitude + "&lon=" + Longitude + "&model=" + modelName + "&format=prometheus";
        List<String> responses12Z = fetchModelDataFromAPI(apiUrl);
        if(responses12Z.size() <= 1){
            responses12Z.clear();
            modelDate = formatter.format(date);
            apiUrl = "https://spotwx.io/api.php?modelrun=" + modelDate + "&key=" + apiKey + "&lat=" + Latitude + "&lon=" + Longitude + "&model=" + modelName + "&format=prometheus";
            responses12Z = fetchModelDataFromAPI(apiUrl);
        }


        return CreateTempFile(responses0Z, responses12Z);
    }

    public static String GetModelNameWithDescription(String modelName){
        switch (modelName.toLowerCase()) {
            case "hrdps_1km_west":
                return "HRDPS 1km West | 2 Day Forecast, 1km res.";
            case "hrdps_continental":
                return "HRDPS Continental | 2 Day Forecast, 2.5km res.";
            case "rdps":
                return "RDPS | 3.5 Day Forecast, 10km res.";
            case "gdps":
                return "GDPS | 10 Day Forecast, 15km res.";
            case "hrrr":
                return "HRRR | 18 Hr Forecast, 3km res.";
            case "geps":
                return "GEPS | 16 Day Forecast, 0.5 degree res.";
            case "rap":
                return "RAP | 21 Hr Forecast, 13km res.";
            case "nam":
                return "NAM | 3.5 Day Forecast, 12km res.";
            case "sref":
                return "SREF | 87 Hr Forecast,16km res.";
            case "gfs":
                return "GFS | 10 Day Forecast, 0.25 degree res.";
            case "gfs uv index":
                return "GFS UV Index | 5 Day Forecast, 0.5 degree res.";
        }

        return modelName;
    }

    public static String CreateTempFile(List<String> fileContents0Z, List<String> fileContents12Z) {
        File tempFile = null;
        try {
            tempFile = File.createTempFile("spotwximport", ".csv");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        List<String> datesIncluded = new ArrayList<String>();
        List<String> contentsToWrite = new ArrayList<>();

        for (int x = 1; x < fileContents12Z.size(); x++) {
            contentsToWrite.add(fileContents12Z.get(x));
            datesIncluded.add(fileContents12Z.get(x).substring(0, 13));
        }
        if (fileContents0Z != null && !fileContents0Z.isEmpty()) {
            for (int x = 1; x < fileContents0Z.size(); x++) {
                String dateTime = fileContents0Z.get(x).substring(0, 13);
                if (!datesIncluded.contains(dateTime)) {
                    contentsToWrite.add(fileContents0Z.get(x));
                }
            }
        }
        Collections.sort(contentsToWrite);

        contentsToWrite.add(0, fileContents12Z.get(0));

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {
            for (String s : contentsToWrite) {
                writer.write(s);
                writer.newLine(); // Writes a newline
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return tempFile.getAbsolutePath();
    }

    public static List<String> fetchModelDataFromAPI(String apiUrl) {
        List<String> li = new ArrayList<>();

        try {
            URL url = new URL(apiUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    li.add(line);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


        return li;
    }
}
