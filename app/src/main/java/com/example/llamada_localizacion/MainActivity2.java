package com.example.llamada_localizacion;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.PhoneNumberUtils;
import android.telephony.PhoneStateListener;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity2 extends AppCompatActivity {
    private static final int REQUEST_LOCATION_PERMISSION = 1;
    private static final int REQUEST_SEND_SMS_PERMISSION = 2;
    TextView tvtel, tvcoor;
    SharedPreferences sharedPreferences;
    LocationManager locationManager;
    LocationListener locationListener;
    TelephonyManager telephonyManager;
    PhoneStateListener phoneStateListener;
    private int secondsCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        tvtel = findViewById(R.id.tvtel);
        tvcoor = findViewById(R.id.tvcoor);
        sharedPreferences = getSharedPreferences("Telefono", MODE_PRIVATE);
        String number = sharedPreferences.getString("numero", "");
        tvtel.setText(number);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                // Aquí se actualiza el TextView con la ubicación actualizada
                tvcoor.setText("Latitud: " + location.getLatitude() + ", Longitud: " + location.getLongitude());
            }
        };

        telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        phoneStateListener = new PhoneStateListener() {
            @Override
            public void onCallStateChanged(int state, String incomingNumber) {
                super.onCallStateChanged(state, incomingNumber);
                if (state == TelephonyManager.CALL_STATE_RINGING) {
                    String displayedNumber = tvtel.getText().toString();
                    String last10DigitsIncoming = PhoneNumberUtils.extractNetworkPortion(incomingNumber);
                    if (last10DigitsIncoming.length() > 10) {
                        last10DigitsIncoming = last10DigitsIncoming.substring(last10DigitsIncoming.length() - 10);
                    }
                    if (displayedNumber.equals(last10DigitsIncoming)) {
                        Toast.makeText(MainActivity2.this, "El número coincide", Toast.LENGTH_SHORT).show();
                    }

                    // Comienza a contar los primeros 10 segundos
                    startCounting();
                } else if (state == TelephonyManager.CALL_STATE_IDLE) {
                    // Reinicia el contador cuando la llamada se finaliza o está en espera
                    resetCounting();
                    Toast.makeText(MainActivity2.this, "Llamada finalizada", Toast.LENGTH_SHORT).show();
                }
            }
        };

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
        } else {
            startLocationUpdates();
        }

        telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
    }

    private void startLocationUpdates() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocationUpdates();
            }
        } else if (requestCode == REQUEST_SEND_SMS_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                sendSMSWithLocation();
            } else {
                Toast.makeText(this, "No se concedieron permisos para enviar SMS", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void startCounting() {
        resetCounting(); // Reinicia el contador antes de comenzar
        secondsCount = 0; // Reinicia el contador
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(10000); // Espera 10 segundos
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity2.this, "Han pasado 10 segundos", Toast.LENGTH_SHORT).show();
                            checkAndRequestSmsPermission();
                        }
                    });
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void resetCounting() {
        secondsCount = 0; // Reinicia el contador
    }

    private void checkAndRequestSmsPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, REQUEST_SEND_SMS_PERMISSION);
        } else {
            sendSMSWithLocation();
        }
    }

    private void sendSMSWithLocation() {
        String phoneNumber = tvtel.getText().toString();
        String locationText = tvcoor.getText().toString();
        String message = "Mi ubicación actual es: " + locationText;

        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage(phoneNumber, null, message, null, null);

    }

    private void openGoogleMapsWithLocation() {
        String locationText = tvcoor.getText().toString();
        String[] coordinates = locationText.split(",");
        double latitude = Double.parseDouble(coordinates[0].substring(coordinates[0].indexOf(":") + 1).trim());
        double longitude = Double.parseDouble(coordinates[1].substring(coordinates[1].indexOf(":") + 1).trim());

        String uri = "geo:" + latitude + "," + longitude;

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (locationManager != null) {
            locationManager.removeUpdates(locationListener);
        }
        if (telephonyManager != null) {
            telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);
        }
        resetCounting(); // Detiene el conteo antes de destruir la actividad
    }
}