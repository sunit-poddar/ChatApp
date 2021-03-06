package com.demo.demochatapp.utilities;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import org.eclipse.paho.client.mqttv3.MqttConnectOptions;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class Hashdefine {

    public static final String CHAT_SERVER = "tcp://iot.eclipse.org:1883";

    public static final String USER_SUBSCRIPTION_TOPIC = "test234";
    public static final String CHAT_PUBLISH_TOPIC = "test234";


    public static MqttConnectOptions mqttConnectOptions(Context applicationContext) {
        SharedPrefs sharedPrefs = new SharedPrefs(applicationContext);

        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setCleanSession(false);
        mqttConnectOptions.setAutomaticReconnect(true);
        mqttConnectOptions.setWill(Hashdefine.CHAT_PUBLISH_TOPIC, ("User \'"+ sharedPrefs.getUsername().substring(0, sharedPrefs.getUsername().lastIndexOf("_")) + "\' is offline").getBytes(), 0, true);
        return mqttConnectOptions;
    }

    //send date in UTC
    public static String getCurrentDateTimeInUTC(){
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy kk:mm:ss");
        formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
        Calendar currentDateTime = Calendar.getInstance();
        long currentMillis = currentDateTime.getTimeInMillis();
        return formatter.format(new Date(currentMillis));
    }

    //convert received datetime in utc to device's current locale
    public static String getReceivedDateTimeInCurrentLocale(String date){
        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy kk:mm:ss");
            simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date receivedDate = simpleDateFormat.parse(date);

            SimpleDateFormat simpleDateFormat1 = new SimpleDateFormat("dd MMM hh:mm a");

            simpleDateFormat1.setTimeZone(TimeZone.getDefault());
            return simpleDateFormat1.format(receivedDate);
        } catch (Exception e) {
            e.printStackTrace();
            try {
                return new SimpleDateFormat("dd/MM/yyyy kk:mm:ss").parse(date).toLocaleString();
            } catch (ParseException e1) {
                e1.printStackTrace();
            }

            // in case unable to parse date in the right format; it returns current datetime as last option
            return new SimpleDateFormat("dd/MM/yyyy kk:mm:ss").format(new Date());
        }
    }

    //check if network is available
    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        if (null != networkInfo && networkInfo.isConnected()) {
            //network is connected
            return true;
        }else {
            //no working network connected to device
            return false;
        }
    }

}
