package com.openpipe.app;

import android.app.Activity;
import android.os.Bundle;

/** Invisible activity used by home-screen shortcuts: runs one rule, shows a toast, closes. */
public class RunActivity extends Activity {
    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        final Rule r = Store.find(this, getIntent().getStringExtra("id"));
        if (r == null) {
            U.toast(this, "This rule no longer exists");
            finish();
            return;
        }
        if (!r.enabled) {
            U.toast(this, r.name + " is turned off");
            finish();
            return;
        }
        if (!Perm.has(this)) {
            U.toast(this, "Open OpenPipe and allow file access first");
            finish();
            return;
        }
        final android.content.Context app = getApplicationContext();
        new Thread(() -> {
            final Run run = Mover.run(app, r, "Shortcut");
            runOnUiThread(() -> {
                String msg;
                if (run.error != null) msg = run.error;
                else if (run.done == 0 && run.failed == 0) msg = r.name + ": no matching files";
                else msg = r.name + ": " + (run.copy ? "copied " : "moved ") + run.done
                            + (run.done == 1 ? " file" : " files")
                            + (run.failed > 0 ? ", " + run.failed + " failed" : "");
                U.toast(app, msg);
                finish();
            });
        }).start();
    }
}
