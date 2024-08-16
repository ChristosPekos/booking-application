package com.example.bookingapp.Utils;

import android.content.Context;
import android.util.Log;
import com.example.bookingapp.R;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class Helper {
    private static final String TAG = "Helper";

    public static void initializeConfigFile(Context context) {
        // Check if the config file already exists in internal storage
        File configFile = new File(context.getFilesDir(), "config.properties");
        if (configFile.exists()) {
            Log.d(TAG, "Config file already exists.");
            return; // Config file already exists, no need to initialize
        }

        // If the config file doesn't exist, copy it from raw resources
        InputStream inputStream = null;
        FileOutputStream outputStream = null;
        try {
            // Open the config file in raw resources
            inputStream = context.getResources().openRawResource(R.raw.config);

            // Create the config file in internal storage
            outputStream = context.openFileOutput("config.properties", Context.MODE_PRIVATE);

            // Copy the content of the config file from raw resources to internal storage
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }

            Log.d(TAG, "Config file initialized successfully.");
        } catch (IOException e) {
            Log.e(TAG, "Failed to initialize config file: " + e.getMessage());
        } finally {
            // Close the input and output streams
            try {
                if (inputStream != null) inputStream.close();
                if (outputStream != null) outputStream.close();
            } catch (IOException e) {
                Log.e(TAG, "Error closing streams: " + e.getMessage());
            }
        }
    }


    public static String getConfigValueString(Context context, String name) {
        Properties properties = new Properties();
        InputStream inputStream = null;
        try {
            inputStream = context.openFileInput("config.properties");
            properties.load(inputStream);
            return properties.getProperty(name);
        } catch (IOException e) {
            Log.e(TAG, "Failed to read config file: " + e.getMessage());
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                Log.e(TAG, "Error closing input stream: " + e.getMessage());
            }
        }
        return null;
    }

    public static int getConfigValueInt(Context context, String name) {
        String valueString = getConfigValueString(context, name);
        if (valueString != null) {
            try {
                return Integer.parseInt(valueString);
            } catch (NumberFormatException e) {
                Log.e(TAG, "Failed to parse int value from config file.");
            }
        }
        return -1;
    }

    public static String getConfigText(Context context) {
        StringBuilder stringBuilder = new StringBuilder();
        Properties properties = new Properties();
        InputStream inputStream = null;
        try {
            inputStream = context.openFileInput("config.properties");
            properties.load(inputStream);
            for (String key : properties.stringPropertyNames()) {
                String value = properties.getProperty(key);
                stringBuilder.append(key).append("=").append(value).append("\n");
            }
            stringBuilder.deleteCharAt(stringBuilder.length()-1);
            return stringBuilder.toString();
        } catch (IOException e) {
            Log.e(TAG, "Failed to read config file: " + e.getMessage());
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                Log.e(TAG, "Error closing input stream: " + e.getMessage());
            }
        }
        return null;
    }

    public static void updateConfigFile(Context context, String configFileText) {
        FileOutputStream outputStream = null;
        try {
            outputStream = context.openFileOutput("config.properties", Context.MODE_PRIVATE);
            outputStream.write(configFileText.getBytes());
            Log.d(TAG, "Config file updated successfully.");
        } catch (IOException e) {
            Log.e(TAG, "Failed to update config file: " + e.getMessage());
        } finally {
            try {
                if (outputStream != null) {
                    outputStream.close();
                }
            } catch (IOException e) {
                Log.e(TAG, "Error closing output stream: " + e.getMessage());
            }
        }
    }

}
