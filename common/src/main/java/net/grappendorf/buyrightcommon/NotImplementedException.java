package net.grappendorf.buyrightcommon;

public class NotImplementedException extends RuntimeException {
  public NotImplementedException() {
    super("This method is not (yet) implemented");
  }
}
