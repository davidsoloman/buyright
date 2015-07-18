package net.grappendorf.buyright;

import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.support.v7.app.AppCompatActivity;

public class BuyrightPreferences extends AppCompatActivity {

  public static final String DRAW_CARD_ANIMATIONS = "drawCardAnimations";
  public static final String SINGLE_LINE_CARDS = "singleLineCards";

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    getFragmentManager().beginTransaction().replace(android.R.id.content,
        new PrefsFragment()).commit();
  }

  public static class PrefsFragment extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      addPreferencesFromResource(R.xml.buyright_preferences);
    }
  }
}
