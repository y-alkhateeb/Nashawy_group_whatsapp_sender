package ga.nashawygroup.whatsappsender;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import ga.nashawygroup.whatsappsender.sender.WhatsappApi;
import android.widget.RadioGroup;

public class Sender extends AppCompatActivity {
    // this is the action code we use in our intent,
    // this way we know we're looking at the response from our own action
    private static final int SELECT_PICTURE = 1;

    static public String messageToSend;

    static public Uri uriSelectedImagePath;



    Uri uri = null;
    SharedPreferences sp;
    Context activityContext = this;
    Button browsebtn, sendbtn, accbtn;
    EditText messageTxt;
    RadioGroup variantsopt;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sender);
        sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        stopFgService();
        browsebtn = findViewById(R.id.browsebtn);
        messageTxt = findViewById(R.id.messageTxt);
        messageTxt.setSingleLine(false);
        messageTxt.setImeOptions(EditorInfo.IME_FLAG_NO_ENTER_ACTION);
        sendbtn = findViewById(R.id.sendbtn);
        accbtn = findViewById(R.id.accbtn);
        variantsopt = findViewById(R.id.variantsopt);
        switch (sp.getString("variant","com.whatsapp")){
            case "com.whatsapp":
                variantsopt.check(R.id.waradio);
            case "com.whatsapp.w4b":
                variantsopt.check(R.id.wa4bradio);
        }
        variantsopt.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                switch (radioGroup.getCheckedRadioButtonId()){
                    case R.id.waradio:
                        sp.edit().putString("variant","com.whatsapp").apply();
                    case R.id.wa4bradio:
                        sp.edit().putString("variant","com.whatsapp.w4b").apply();
                }
            }
        });
        setDefaultOnClick(browsebtn);
        setDefaultOnClick(sendbtn);
        setDefaultOnClick(accbtn);
        checkUpdate();


        findViewById(R.id.selectPic)
                .setOnClickListener(new View.OnClickListener() {

                    public void onClick(View arg0) {

                        // in onCreate or any event where your want the user to
                        // select a file
                        Intent intent = new Intent();
                        intent.setType("image/*");
                        intent.setAction(Intent.ACTION_GET_CONTENT);
                        startActivityForResult(Intent.createChooser(intent,
                                "Select Picture"), SELECT_PICTURE);
                    }
                });


    }
    private void setOnClick(View v){

        switch (v.getId()){

            case R.id.browsebtn:
                v.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                        intent.addCategory(Intent.CATEGORY_OPENABLE);
                        intent.setType("*/*");
                        startActivityForResult(intent, 7);
                    }
                });
                break;

            case R.id.sendbtn:
                v.setOnClickListener(new View.OnClickListener() {
                    @SuppressLint("ApplySharedPref")
                    @Override
                    public void onClick(View view) {
                        if(messageTxt.getText().toString() == null
                                || messageTxt.getText().toString().length() == 0){
                            Toast.makeText(activityContext, "Please write a message to send", Toast.LENGTH_SHORT).show();
                        }
                        else{
                            messageToSend = messageTxt.getText().toString();
                            if (uri == null) {
                                Toast.makeText(activityContext, "No file selected :(", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(activityContext, "Checking for permissions", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(activityContext, WASenderFgSvc.class);
                                intent.putExtra("start", true);
                                intent.putExtra("uri", uri);
                                if (WhatsappApi.getInstance().isRootAvailable()) {
                                    Toast.makeText(activityContext, "Root Privileges Detected Switching to advanced mode :)", Toast.LENGTH_SHORT).show();
                                    intent.putExtra("rooted", true);
                                    sp.edit().putBoolean("running", true).commit();
                                    startService(intent);
                                } else {
//                                    Toast.makeText(activityContext, "Oh no root detected continuing with usual privileges :(", Toast.LENGTH_SHORT).show();
                                    intent.putExtra("rooted", false);
                                    sp.edit().putBoolean("running", true).commit();
                                    startService(intent);
                                }
                            }
                        }
                    }
                });
                break;

            case R.id.accbtn:
                v.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
                    }
                });
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_PICTURE) {
                uriSelectedImagePath = data.getData();
            }
        }
        if (requestCode == 7 && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                uri = data.getData();
            }
        }
    }
    @Override
    public void onResume() {
        stopFgService();
        super.onResume();
    }

    private void checkUpdate() {
        RequestQueue mRequestQueue = Volley.newRequestQueue(this);
        StringRequest request = new StringRequest("https://api.nikhilkumar.ga/version/whatsappsender/", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                JSONObject jsonObject;
                try {
                    jsonObject = new JSONObject(response);
                    if (jsonObject.getInt("latest") == 2) {
                        setOnClick(browsebtn);
                        setOnClick(sendbtn);
                        setOnClick(accbtn);
                    } else {
                        Toast.makeText(activityContext, "Update to proceed :(", Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    Toast.makeText(activityContext, "Server Error :( Try again later", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(activityContext, "Connectivity Error :(", Toast.LENGTH_SHORT).show();
            }
        });
        request.setShouldCache(false);
        mRequestQueue.add(request);
    }

    private void stopFgService() {
        Intent intent = new Intent(this, WASenderFgSvc.class);
        stopService(intent);
        sp.edit().putBoolean("running", false).apply();
    }

    private void setDefaultOnClick(View view) {
        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(activityContext, "Not Permitted :(", Toast.LENGTH_SHORT).show();
                checkUpdate();
            }
        };
        view.setOnClickListener(onClickListener);
    }
}
