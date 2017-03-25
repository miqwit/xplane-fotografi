/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package miqwit.xplane.fotografi;

/**
 *
 * @author micka_000
 */
public class Coordinates {
  public float longitude;
  public float latitude;
  public float altitude;
  
  public Coordinates(float longitude, float latitude, float altitude) {
    this.longitude = longitude;
    this.latitude = latitude;
    this.altitude = altitude;
  }
  
  @Override
  public String toString() {
    return "Longitude: " + this.longitude +
            " Latitude: " + this.latitude + 
            " Altitude: " + this.altitude;
  }
}
