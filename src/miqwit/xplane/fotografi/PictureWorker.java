/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package miqwit.xplane.fotografi;

import com.flickr4java.flickr.Flickr;
import com.flickr4java.flickr.FlickrException;
import com.flickr4java.flickr.REST;
import com.flickr4java.flickr.photos.Photo;
import com.flickr4java.flickr.photos.PhotoList;
import com.flickr4java.flickr.photos.PhotosInterface;
import com.flickr4java.flickr.photos.SearchParameters;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.SwingWorker;

/**
 *
 * @author micka_000
 */
public class PictureWorker extends SwingWorker<Void, Coordinates> {
  private int timeSpanInMilliseconds = 0;
  private Date lastDateDL = null;
  private final String apiKey = "";
  private final String sharedSecret = "";
  PhotoList<Photo> current_photos = new PhotoList<Photo>();
  private JLabel jLabelPicture = null;
  private int rotationTimeInSeconds = 0;
  private JTextField jLatitude = null;
  private JTextField jLongitude = null;
  private JTextPane jLegend = null;
  private Flickr flickr = null;
  
  public PictureWorker(int timeSpanInMinutes, int rotationTimeInSeconds, 
          JLabel jLabelPicture, JTextField latitude, JTextField longitude,
          JTextPane jLegend) {
    this.timeSpanInMilliseconds = timeSpanInMinutes * 60 * 1000;
    this.jLabelPicture = jLabelPicture;
    this.rotationTimeInSeconds = rotationTimeInSeconds;
    this.jLatitude = latitude;
    this.jLongitude = longitude;
    this.jLegend = jLegend;
    this.flickr = new Flickr(this.apiKey, this.sharedSecret, new REST());
  }
  
  @Override
  public Void doInBackground() {
      while (this.timeSpanInMilliseconds > 0) {  // Always true
        try {
          this.displayNextPhoto();
          Thread.sleep(this.rotationTimeInSeconds * 1000);
        } catch (Exception ex) {
          ex.printStackTrace();
        }
      }
      return null;
  }
  
  private void getMorePictures() {
    // Check if timespan is reach
    Date now = new Date();
    if ((this.lastDateDL != null)
            && (now.getTime() - this.lastDateDL.getTime() < this.timeSpanInMilliseconds)
            && this.current_photos.size() > 1) {
      System.out.println("Do not download photos now");
      return;
    }
    
    try {
      System.out.println("Download photos now");
      SearchParameters searchParams = new SearchParameters();
      String longitude = this.jLongitude.getText().replace(",", ".").trim();
      String latitude = this.jLatitude.getText().replace(",", ".").trim();
      
      // Cast in float to check valid floats
      float f1 = new Float(longitude);
      float f2 = new Float(latitude);
      
      searchParams.setLatitude(longitude);
      searchParams.setLongitude(latitude);

      System.out.println("Make call with " + latitude + " " + longitude);
      PhotosInterface photosInterface = this.flickr.getPhotosInterface();
      
      this.current_photos = photosInterface.search(searchParams, 10, 1);
      System.out.println("Populated " + this.current_photos.size());
      
      // Update lastDate
      this.lastDateDL = new Date();
    } catch (FlickrException ex) {
      Logger.getLogger(PictureWorker.class.getName()).log(Level.SEVERE, null, ex);
    } catch (java.lang.NumberFormatException ex) {
      // The cast in float failed. Not a valid float. Do nothing.
    }
  }
  
  public void displayNextPhoto() {
    while (this.current_photos.size() == 0) {
      try {
        if ("??".equals(this.jLatitude.getText())) {
          System.out.println("Invalid latitude. Wait 1 second to get proper data");
          Thread.sleep(1000);
          continue;
        }
        System.out.println("No photos in stock. Download more.");
        this.getMorePictures();

        if (this.current_photos.size() == 0) {
          // No photo found. Wait 10 seconds and retry
          System.out.println("No photos found.");
          Thread.sleep(10000);
        }
      } catch (InterruptedException ex) {
          Logger.getLogger(PictureWorker.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    // Pop photo
    Photo p = this.current_photos.get(0);
    this.current_photos.remove(0);
    
    String url = p.getLargeUrl();
    try {
      URL url_I = new URL(url);
      Image image = ImageIO.read(url_I);
      
      // Resize Image
      int w = this.jLabelPicture.getWidth();
      int h = this.jLabelPicture.getWidth();
      int iw = image.getWidth(null);
      int ih = image.getHeight(null);
      Image image_resized = null;
      if ((w / h) > (iw / ih)) {
        // align on height
        image_resized = this.getScaledImage(image, 0, h);
      } else {
        // align on width
        image_resized = this.getScaledImage(image, w, 0);
      }

      // Apply it
      jLabelPicture.setIcon(new ImageIcon(image_resized));
      
      // Display legend
      System.out.println("Get Details");
      PhotosInterface photosInterface = this.flickr.getPhotosInterface();
      Photo pdetail = photosInterface.getInfo(p.getId(), this.sharedSecret);
      System.out.println("Get Details done");
      String legend = String.format("%s - %s - %s (%s, %s)", 
              pdetail.getCountry().getName(), 
              pdetail.getLocality().getName(),
              pdetail.getDescription(), 
              p.getOwner().getUsername(), 
              pdetail.getDateTaken().toString());
      jLegend.setText(legend);
      
    } catch (MalformedURLException ex) {
      Logger.getLogger(PictureWorker.class.getName()).log(Level.SEVERE, null, ex);
    } catch (IOException ex) {
      Logger.getLogger(PictureWorker.class.getName()).log(Level.SEVERE, null, ex);
    } catch (FlickrException ex) {
      Logger.getLogger(PictureWorker.class.getName()).log(Level.SEVERE, null, ex);
    }
  }
  
  private Image getScaledImage(Image srcImg, int w, int h){
      if (h == 0) {
        double rapport = ((double) srcImg.getHeight(null) / (double) srcImg.getWidth(null));
        h = (int) ((double) w * rapport);
      } else if (w == 0) {
        double rapport = ((double) srcImg.getWidth(null) / (double) srcImg.getHeight(null));
        w = (int) ((double) h * rapport);
      }
      BufferedImage resizedImg = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
      Graphics2D g2 = resizedImg.createGraphics();

      g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
      g2.drawImage(srcImg, 0, 0, w, h, null);
      g2.dispose();

      return resizedImg;
  }
}
