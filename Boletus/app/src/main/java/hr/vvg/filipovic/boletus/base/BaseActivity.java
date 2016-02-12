package hr.vvg.filipovic.boletus.base;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

/**
 * Base Activity with support methods.
 *
 * Created by marko on 12/02/16.
 */
public class BaseActivity extends AppCompatActivity {
    ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public void showDialog() {
        dialog = new ProgressDialog(this);
        dialog.setIndeterminate(true);
        dialog.setMessage("Loading...");
        dialog.show();
    }

    public void hideDialog() {
        dialog.dismiss();
    }
}
