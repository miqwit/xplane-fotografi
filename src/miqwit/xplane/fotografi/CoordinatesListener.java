/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package miqwit.xplane.fotografi;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JProgressBar;

/**
 * ProgressListener listens to "progress" property
 * changes in the SwingWorkers that search and load
 * images.
 */
class CoordinatesListener implements PropertyChangeListener {
  CoordinatesListener(JProgressBar progressBar) {
    this.progressBar = progressBar;
    this.progressBar.setValue(0);
  }
  
  public void propertyChange(PropertyChangeEvent evt) {
    String strPropertyName = evt.getPropertyName();
    if ("progress".equals(strPropertyName)) {
      progressBar.setIndeterminate(false);
      int progress = (Integer)evt.getNewValue();
      progressBar.setValue(progress);
    }
  }
  
  private JProgressBar progressBar;
}