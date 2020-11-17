package ga.nashawygroup.whatsappsender;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Toast;

import java.util.List;

import static android.view.accessibility.AccessibilityNodeInfo.ACTION_CLICK;

public class WASenderAccSvc extends AccessibilityService {

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (!PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getBoolean("running", false)) {
            return;
        }
        if (AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED == event.getEventType()) {
            String actname = event.getClassName().toString();
            try {
                Log.e("actname", actname);
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            switch (actname) {
                case "com.whatsapp.Conversation":
                    List<AccessibilityNodeInfo> nodes = getRootInActiveWindow().findAccessibilityNodeInfosByViewId("com.whatsapp:id/send");
                    if (nodes.size() > 0) {
                        nodes.get(0).performAction(ACTION_CLICK);
                    }
                    performGlobalAction(GLOBAL_ACTION_BACK);
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    break;
                case "com.whatsapp.HomeActivity":
                    sendNext();
                    break;
                case "com.whatsapp.ContactPicker":
                    Toast.makeText(this, "Unable to find contacts in your list! Skipping!!!", Toast.LENGTH_SHORT).show();
                    performGlobalAction(GLOBAL_ACTION_BACK);
                    break;
            }
        }
    }

    @Override
    public void onInterrupt() {
        Toast.makeText(this, "Please allow accessibility permission to WhatsApp Sender", Toast.LENGTH_SHORT).show();
    }

    private void sendNext() {
        Intent intent = new Intent(this, WASenderFgSvc.class);
        intent.putExtra("start", false);
        startService(intent);
    }
}
