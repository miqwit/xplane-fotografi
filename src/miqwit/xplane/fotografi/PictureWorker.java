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
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTextArea;
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
  private JLabel jLabelPicture = null;
  private int rotationTimeInSeconds = 0;
  private JTextField jLatitude = null;
  private JTextField jLongitude = null;
  private JTextPane jLegend = null;
  private Flickr flickr = null;
  private JTextArea console = null;
  
  private PhotoList<Photo> current_photos = new PhotoList<Photo>();
  private ArrayList<String> already_displayed = new ArrayList<String>();
  
  public PictureWorker(int timeSpanInMinutes, int rotationTimeInSeconds, 
          JLabel jLabelPicture, JTextField latitude, JTextField longitude,
          JTextPane jLegend, JTextArea console) {
    this.timeSpanInMilliseconds = timeSpanInMinutes * 60 * 1000;
    this.jLabelPicture = jLabelPicture;
    this.rotationTimeInSeconds = rotationTimeInSeconds;
    this.jLatitude = latitude;
    this.jLongitude = longitude;
    this.jLegend = jLegend;
    this.flickr = new Flickr(this.apiKey, this.sharedSecret, new REST());
    this.console = console;
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
      this.console.append("Do not download photos now\n");
      return;
    }
    
    try {
      this.console.append("Download photos now\n");
      String longitude = this.jLongitude.getText().replace(",", ".").trim();
      String latitude = this.jLatitude.getText().replace(",", ".").trim();
      
      // Cast in float to check valid floats
      float f1 = new Float(longitude);
      float f2 = new Float(latitude);
      
      // Set params
      SearchParameters searchParams = new SearchParameters();
      searchParams.setLatitude(longitude);
      searchParams.setLongitude(latitude);
      searchParams.setRadius(20); // km
      
      // Download pictures
      this.downloadFreshPhotos(searchParams);
      
      // Update lastDate
      this.lastDateDL = new Date();
    } catch (FlickrException ex) {
      Logger.getLogger(PictureWorker.class.getName()).log(Level.SEVERE, null, ex);
    } catch (java.lang.NumberFormatException ex) {
      // The cast in float failed. Not a valid float. Do nothing.
    }
  }
  
  /**
   * Will call the Flickr API several times until new pictures (never been
   * displayed before) are downloaded.
   * Populates this.current_photos.
   * @param searchParams
   * @throws FlickrException 
   */
  private void downloadFreshPhotos(SearchParameters searchParams) throws FlickrException {
    this.console.append("Make call with " + searchParams.getLatitude()
            + " " + searchParams.getLongitude() + "\n");
    PhotosInterface photosInterface = this.flickr.getPhotosInterface();

    // Download fresh photos
    boolean download_new_photos = true;
    int page = 1;
    PhotoList<Photo> previous_photos = new PhotoList<Photo>();
    PhotoList<Photo> photos;

    while (download_new_photos) {
      photos = photosInterface.search(searchParams, 10, page);

      // If no photos in this page, use previous photos
      if (photos.size() == 0) {
        photos = previous_photos;
        break;
      }

      // Adds only to current_photos the photos not already displayed
      for (Photo photo : photos) {
        String id = photo.getId();
        if (!this.already_displayed.contains(id)) {
          this.current_photos.add(photo);
        }
      }

      // If no photos added, try next page
      if (this.current_photos.size() == 0) {
        page++;
        this.console.append("No new photos. Try page " + page + "\n");
        previous_photos = photos;
      } else {
        download_new_photos = false;
      }
    }

    this.console.append("Populated " + this.current_photos.size() + "\n");
  }
  
  public void displayNextPhoto() {
    while (this.current_photos.size() == 0) {
      try {
        if ("??".equals(this.jLatitude.getText())) {
          this.console.append("Invalid latitude. Wait 1 second to get proper data\n");
          Thread.sleep(1000);
          continue;
        }
        this.console.append("No photos in stock. Download more.\n");
        this.getMorePictures();

        if (this.current_photos.size() == 0) {
          // No photo found. Wait 10 seconds and retry
          this.console.append("No photos found.\n");
          Thread.sleep(10000);
        }
      } catch (InterruptedException ex) {
          Logger.getLogger(PictureWorker.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    // Pop photo
    Photo p = this.current_photos.get(0);
    this.current_photos.remove(0);
    
    // Add this photo to already displayed
    this.already_displayed.add(p.getId());
    
    String url = p.getLargeUrl();
    try {
      URL url_I = new URL(url);
      Image image = ImageIO.read(url_I);
      
      // Resize Image
      int w = this.jLabelPicture.getWidth();
      int h = this.jLabelPicture.getHeight();
      int iw = image.getWidth(null);
      int ih = image.getHeight(null);
      Image image_resized;
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
      this.console.append("Get Details\n");
      PhotosInterface photosInterface = this.flickr.getPhotosInterface();
      Photo pdetail = photosInterface.getInfo(p.getId(), this.sharedSecret);
      this.console.append("Get Details done\n");
      String legend = String.format("%s - %s - %s (%s, %s)", 
              (pdetail.getCountry() != null) ? pdetail.getCountry().getName() : "",
              (pdetail.getLocality() != null) ? pdetail.getLocality().getName() : "",
              pdetail.getDescription(), 
              (p.getOwner() != null) ? p.getOwner().getUsername() : "", 
              (pdetail.getDateTaken() != null) ? pdetail.getDateTaken().toString() : ""
      );
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
        
        if (h > this.jLabelPicture.getHeight()) {
          h = this.jLabelPicture.getHeight();
          w = (int) ((double) h / rapport);
        }
      } else if (w == 0) {
        double rapport = ((double) srcImg.getWidth(null) / (double) srcImg.getHeight(null));
        w = (int) ((double) h * rapport);
        
        if (w > this.jLabelPicture.getWidth()) {
          w = this.jLabelPicture.getWidth();
          h = (int) ((double) w * rapport);
        }
      }
      BufferedImage resizedImg = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
      Graphics2D g2 = resizedImg.createGraphics();

      g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
      g2.drawImage(srcImg, 0, 0, w, h, null);
      g2.dispose();

      return resizedImg;
  }
}
