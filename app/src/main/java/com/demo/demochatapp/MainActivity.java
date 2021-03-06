package com.demo.demochatapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.demo.demochatapp.models.ChatItemModel;
import com.demo.demochatapp.utilities.Hashdefine;
import com.demo.demochatapp.utilities.SharedPrefs;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import org.json.JSONException;
import org.json.JSONObject;

import static com.demo.demochatapp.utilities.Hashdefine.CHAT_SERVER;

public class MainActivity extends AppCompatActivity {

    EditText username;
    Button submitBtn;

    SharedPrefs sharedPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sharedPrefs = new SharedPrefs(getApplicationContext());

        if (sharedPrefs.getIsUserSubscribed()) {
            Intent i = new Intent(MainActivity.this, ChatRoomActivity.class);
            startActivity(i);
            finish();
        }else {
            setContentView(R.layout.activity_main);

            username = (EditText) findViewById(R.id.userName);
            submitBtn = (Button) findViewById(R.id.submitBtn);


            submitBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!validateUsername()) {
                        return;
                    }

                    if (!Hashdefine.isNetworkAvailable(getApplicationContext())) {
                        Toast.makeText(getApplicationContext(), getString(R.string.label_no_internet_available), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    sharedPrefs.setUsername(username.getText().toString() + "_" + System.currentTimeMillis());
                    //saving username with timeinmillis server 2 purposes
                    //i get a unique ID for every user
                    //i can extract the username to show to other users without having to maintain 2 different sets of data i.e. username and uid

                    subscribeTopic();
                }
            });
        }

    }

    private void subscribeTopic() {
        final MqttAndroidClient mqttAndroidClient = new MqttAndroidClient(getApplicationContext(), CHAT_SERVER, sharedPrefs.getUsername());
        Log.e("starting ","subscription");
        try {
            mqttAndroidClient.connect(Hashdefine.mqttConnectOptions(getApplicationContext()), null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    DisconnectedBufferOptions disconnectedBufferOptions = new DisconnectedBufferOptions();
                    disconnectedBufferOptions.setBufferEnabled(true);
                    disconnectedBufferOptions.setBufferSize(100);
                    disconnectedBufferOptions.setPersistBuffer(false);
                    disconnectedBufferOptions.setDeleteOldestMessages(false);
                    mqttAndroidClient.setBufferOpts(disconnectedBufferOptions);

                    try {
                        mqttAndroidClient.subscribe(Hashdefine.USER_SUBSCRIPTION_TOPIC, 1, null, new IMqttActionListener() {
                            @Override
                            public void onSuccess(IMqttToken asyncActionToken) {
                                 if (!sharedPrefs.getIsUserSubscribed()) {
                                     if (!sharedPrefs.getIsNewJoineeBroadCasted()) {
                                         //send broadcast to other users
                                         publishUserMessage( "User " + "\'" +  sharedPrefs.getUsername().substring(0, sharedPrefs.getUsername().lastIndexOf("_")) + "\'" + " has joined the chat", "alert", mqttAndroidClient);
                                         sharedPrefs.setIsNewJoineeBroadcasted(true);
                                     }


                                    Log.e("subscription","success");
                                    Toast.makeText(getApplicationContext(),  getString(R.string.label_subscribed_to_topic, Hashdefine.USER_SUBSCRIPTION_TOPIC), Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(MainActivity.this, ChatRoomActivity.class);
                                    startActivity(intent);
                                    finish();
                                    sharedPrefs.setIsUserSubscribed(true);
                                }
                            }

                            @Override
                            public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                                Log.e("subscription","fail");
                                sharedPrefs.setIsUserSubscribed(false);
                                Toast.makeText(getApplicationContext(), getString(R.string.label_something_went_wrong), Toast.LENGTH_LONG).show();
                            }
                        });
                    } catch (MqttException e) {
                        Log.e("subscribe exception", e.toString());
                        Toast.makeText(getApplicationContext(), getString(R.string.label_something_went_wrong), Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.e("Failed to connect to: ", CHAT_SERVER);
                    sharedPrefs.setIsUserSubscribed(false);
                }
            });


        } catch (MqttException ex){
            Log.e("connect exception", ex.toString());
            sharedPrefs.setIsUserSubscribed(false);
        }

    }

    private boolean validateUsername(){
        if (username.getText().toString().trim().isEmpty()) {
            username.setError(getString(R.string.label_empty_text_field_required));
            username.requestFocus();
            return false;
        }
        return  true;
    }

    private void publishUserMessage(String content, String contentType, MqttAndroidClient mqttAndroidClient){
        MqttMessage message = new MqttMessage();

        try {
            int msgID = (int) System.currentTimeMillis();
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("message_id", msgID);
            jsonObject.put("sender", sharedPrefs.getUsername());
            jsonObject.put("message", content);
            jsonObject.put("datetime", Hashdefine.getCurrentDateTimeInUTC());
            jsonObject.put("content_type", contentType);

            message.setId(msgID);
            message.setQos(0);
            message.setPayload(jsonObject.toString().getBytes());

            mqttAndroidClient.publish(Hashdefine.CHAT_PUBLISH_TOPIC, message);

            try {
                if (!mqttAndroidClient.isConnected()) {
                    Log.e(" messages in buffer.", mqttAndroidClient.getBufferedMessageCount() + "");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        } catch (MqttPersistenceException e) {
            e.printStackTrace();
        } catch (MqttException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
