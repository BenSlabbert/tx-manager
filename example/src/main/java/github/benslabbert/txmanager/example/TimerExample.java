/* Licensed under Apache-2.0 2024. */
package github.benslabbert.txmanager.example;

import github.benslabbert.txmanager.annotation.TimerAdvice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TimerExample {

  private static final Logger log = LoggerFactory.getLogger(TimerExample.class);

  @TimerAdvice
  public void run() {
    System.out.println("in run");
  }
}
