package net.grappendorf.buyright;

import android.app.Application;
import android.test.ApplicationTestCase;

public class ApplicationTest extends ApplicationTestCase<Application> {
  public ApplicationTest() {
    super(Application.class);
  }

  public void testFoo() throws Exception {
    assertEquals(1 + 1, 2);
  }
}