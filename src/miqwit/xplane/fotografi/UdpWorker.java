/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package miqwit.xplane.fotografi;

import java.awt.Color;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingWorker;

/**
 *
 * @author micka_000
 */
public class UdpWorker extends SwingWorker<Boolean, Coordinates> {
  private final String ipFrom;
  private final int udpPort;
  private final JTextField sync;
  private JTextField jLatitude = null;
  private JTextField jLongitude = null;
  
  public UdpWorker(String ipFrom, int port, JTextField sync, JTextField latitude, JTextField longitude) {
    this.ipFrom = ipFrom;
    this.udpPort = port;
    this.sync = sync;
    this.jLatitude = latitude;
    this.jLongitude = longitude;
  }
  
  @Override
  public Boolean doInBackground() {
    this.ReadUdpPackets(this.ipFrom, this.udpPort);
    return true;
  }
    
  private void ReadUdpPackets(String ip, int port) {
    DatagramSocket serverSocket = null;
    try {
      InetAddress address = InetAddress.getByName(ip);
      serverSocket = new DatagramSocket(port);
      byte[] receiveData = new byte[41];

      DatagramPacket receivePacket = new DatagramPacket(receiveData,
                         receiveData.length);

      while(true) {
        serverSocket.receive(receivePacket);
        this.decodeMessage(receivePacket.getData());
      }
    } catch (IOException e) {
      System.out.println(e);
    } finally {
      if (serverSocket != null) {
        serverSocket.close();
      }
    }
  }
  
  private void decodeMessage(byte[] phrase) {
      // Message Type: 4
      String messageType = new String(Arrays.copyOfRange(phrase, 0, 4), 0, 4);
//      console.append(String.format("Message Type: %s%n", messageType));
      
      // Internal: 1
//      console.append(String.format("Internal use: %d%n", phrase[4] & 0xff));
      
      // Index number: 4
      int indexNumber = ByteBuffer.
              wrap(Arrays.copyOfRange(phrase, 5, 9)).
              order(ByteOrder.LITTLE_ENDIAN).getInt();
//      console.append(String.format("Index: %d%n", indexNumber));
      
      // Latitude (deg): 4
      float latitude = ByteBuffer.
              wrap(Arrays.copyOfRange(phrase, 9, 13)).
              order(ByteOrder.LITTLE_ENDIAN).getFloat();
//      console.append(String.format("Latitude: %f%n", latitude));
      
      // Longitude (deg): 4
      float longitude = ByteBuffer.
              wrap(Arrays.copyOfRange(phrase, 13, 17)).
              order(ByteOrder.LITTLE_ENDIAN).getFloat();
//      console.append(String.format("Longitude: %f%n", longitude));
      
      // Altitude (feet MSL): 4
      float altitude = ByteBuffer.
              wrap(Arrays.copyOfRange(phrase, 17, 21)).
              order(ByteOrder.LITTLE_ENDIAN).getFloat();
//      console.append(String.format("Altitude: %f%n", altitude));
//      console.append("\n");
      
      publish(new Coordinates(latitude, longitude, altitude));
    }
  
  @Override
  protected void process(List<Coordinates> coords) {
    for (Coordinates coord : coords) {
      sync.setText("SYNC");
      sync.setBackground(new Color(0, 153, 0)); // green
      this.jLatitude.setText(Float.toString(coord.latitude));
      this.jLongitude.setText(Float.toString(coord.longitude));
    }
  }
}
