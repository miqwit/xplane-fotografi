package miqwit.xplane.fotografi;

import java.awt.BorderLayout;
import java.awt.Image;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JLabel;

/**
 * Will fetch pictures over the Internet from the latitude and longitude
 * @author micka_000
 */
public class PictureFetcher {
  /**
   * Will store a bench of pictures here
   */
  private Collection<String> pictures;
  
  public PictureFetcher() {
    
  }
  
  public void getPicture(Coordinates coords) {
    // TODO hardcoded
    String url = "https://api.flickr.com/services/rest/?method=flickr.photos.search&api_key=b9c772f6e0b5987e85dce3c72ddbf936&format=json&nojsoncallback=1";
    url += "&lat=" + coords.latitude;
    url += "&lon=" + coords.longitude;
    System.out.println("Call " + url);
    //this.callApi(url);
    
    // Individual photo
    url = "https://api.flickr.com/services/rest/?method=flickr.photos.getInfo&api_key=b9c772f6e0b5987e85dce3c72ddbf936&photo_id=33441361071&format=json&nojsoncallback=1";
    // this.callApi(url);
  }
  
  private String callApi(String url_call) {
    try {
      URL url = new URL(url_call);
      HttpURLConnection conn = (HttpURLConnection) url.openConnection();
      //conn.setRequestMethod("GET");
      // conn.setRequestProperty("Accept", "application/jsonp");

      if (conn.getResponseCode() != 200) {
        throw new RuntimeException("Failed : HTTP error code : "
                              + conn.getResponseCode());
      }

      BufferedReader br = new BufferedReader(new InputStreamReader(
              (conn.getInputStream()))
      );

      String output;
      System.out.println("Output from Server .... \n");
      while ((output = br.readLine()) != null) {
        System.out.println(output);
      }

      conn.disconnect();

    } catch (MalformedURLException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
    
    return "TODO";
  }

}