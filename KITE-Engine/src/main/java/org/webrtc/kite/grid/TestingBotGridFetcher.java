/*
 * Copyright 2017 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     https://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.webrtc.kite.grid;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.net.ssl.HttpsURLConnection;
import org.webrtc.kite.config.Browser;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.List;

/**
 * testingbot implementation of RemoteGridFetcher.
 */
public class TestingBotGridFetcher extends RemoteGridFetcher {

  /**
   * Constructs a new TestingBotGridFetcher with the given pathToDB, remoteAddress and restApiUrl.
   *
   * @param pathToDB path to db.
   * @param remoteAddress string representation of the Selenium hub url.
   * @param restApiUrl string representation of the rest API url for fetching the supported
   *        browsers.
   */
  public TestingBotGridFetcher(String pathToDB, String remoteAddress, String restApiUrl) {
    super(pathToDB, "TESTING_BOT", remoteAddress, restApiUrl);
  }

  @Override
  public void fetchConfig() throws IOException {

    URL myurl = new URL(this.restApiUrl);

    JsonReader reader = null;
    List<JsonObject> availableConfigList = null;

    HttpsURLConnection con = null;
    InputStream is = null;
    InputStreamReader isr = null;
    BufferedReader br = null;
    try {
      con = (HttpsURLConnection) myurl.openConnection();
      is = con.getInputStream();
      isr = new InputStreamReader(is);
      br = new BufferedReader(isr);
      reader = Json.createReader(br);
      availableConfigList = reader.readArray().getValuesAs(JsonObject.class);
    } finally {
      if (reader != null)
        reader.close();
      if (br != null)
        try {
          br.close();
        } catch (IOException ioe) {
        }
      if (isr != null)
        try {
          isr.close();
        } catch (IOException ioe) {
        }
      if (is != null)
        try {
          is.close();
        } catch (IOException ioe) {
        }
      if (con != null)
        con.disconnect();
    }

    /* might be not necessary, depending on data format it DB */
    for (JsonObject jsonObject : availableConfigList) {
      String name = jsonObject.getString("name", "").trim().toLowerCase();
      if (name.equalsIgnoreCase("googlechrome"))
        name = "chrome";

      Browser browser = new Browser(name);
      browser.setVersion(jsonObject.getString("version", ""));

      String platform = jsonObject.getString("platform", "").toUpperCase();
      if (platform.equalsIgnoreCase("CAPITAN"))
        platform = "EL_CAPITAN";

      browser.setPlatform(platform);

      this.browserList.add(browser);
    }
  }

}
