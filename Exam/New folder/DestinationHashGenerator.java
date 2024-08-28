import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonObject;
import java.io.FileReader;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

public class DestinationHashGenerator {
    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Usage: java -jar DestinationHashGenerator.jar <PRN Number> <path to JSON file>");
            return;
        }

        String prnNumber = args[0];
        String jsonFilePath = args[1];

       
        String randomString = generateRandomString(8);

       
        String destinationValue = readDestinationValue(jsonFilePath);

        if (destinationValue == null) {
            System.out.println("Key 'destination' not found in JSON file.");
            return;
        }

        String toHash = prnNumber + destinationValue + randomString;

       
        String hash = generateMD5Hash(toHash);

        
        System.out.println(hash + ";" + randomString);
    }

    private static String readDestinationValue(String filePath) {
        try (FileReader reader = new FileReader(filePath)) {
            JsonElement element = JsonParser.parseReader(reader);
            JsonObject jsonObject = traverseJson(element.getAsJsonObject());
            if (jsonObject != null) {
                return jsonObject.get("destination").getAsString();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static JsonObject traverseJson(JsonObject jsonObject) {
        for (String key : jsonObject.keySet()) {
            JsonElement value = jsonObject.get(key);
            if (key.equals("destination") && value.isJsonPrimitive()) {
                return jsonObject;
            }
            if (value.isJsonObject()) {
                JsonObject found = traverseJson(value.getAsJsonObject());
                if (found != null) {
                    return found;
                }
            } else if (value.isJsonArray()) {
                for (JsonElement element : value.getAsJsonArray()) {
                    if (element.isJsonObject()) {
                        JsonObject found = traverseJson(element.getAsJsonObject());
                        if (found != null) {
                            return found;
                        }
                    }
                }
            }
        }
        return null;
    }

    private static String generateMD5Hash(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(input.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : digest) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static String generateRandomString(int length) {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int index = random.nextInt(characters.length());
            sb.append(characters.charAt(index));
        }
        return sb.toString();
    }
}
