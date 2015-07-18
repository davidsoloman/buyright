package net.grappendorf.buyrightcommon;

import android.content.Context;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class JSONHelper {

  public static int getInt(JSONObject json, String name, int defaultValue) {
    try {
      return json.getInt(name);
    } catch (JSONException e) {
      return defaultValue;
    }
  }

  public static long getLong(JSONObject json, String name, long defaultValue) {
    try {
      return json.getLong(name);
    } catch (JSONException e) {
      return defaultValue;
    }
  }

  public static String getString(JSONObject json, String name, String defaultValue) {
    try {
      return json.getString(name);
    } catch (JSONException e) {
      return defaultValue;
    }
  }

  public static boolean getBoolean(JSONObject json, String name, boolean defaultValue) {
    try {
      return json.getBoolean(name);
    } catch (JSONException e) {
      return defaultValue;
    }
  }

  public static JSONArray getArray(JSONObject json, String name, JSONArray defaultValue) {
    try {
      return json.getJSONArray(name);
    } catch (JSONException e) {
      return defaultValue;
    }
  }

  public static JSONObject readFromFile(Context context, String filename) throws IOException, JSONException {
    FileInputStream in = null;
    BufferedReader reader = null;
    try {
      in = context.openFileInput(filename);
      reader = new BufferedReader(new InputStreamReader(in));
      StringBuilder sb = new StringBuilder();
      String line;
      while ((line = reader.readLine()) != null) {
        sb.append(line);
      }
      reader.close();
      return new JSONObject(sb.toString());
    } finally {
      if (reader != null) {
        reader.close();
      }
      if (in != null) {
        in.close();
      }
    }
  }
}
