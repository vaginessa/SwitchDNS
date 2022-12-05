package me.zaine.switchdns;

import static android.app.admin.DevicePolicyManager.PRIVATE_DNS_MODE_OPPORTUNISTIC;
import static android.app.admin.DevicePolicyManager.PRIVATE_DNS_SET_ERROR_FAILURE_SETTING;
import static android.app.admin.DevicePolicyManager.PRIVATE_DNS_SET_ERROR_HOST_NOT_SERVING;
import static android.app.admin.DevicePolicyManager.PRIVATE_DNS_SET_NO_ERROR;
import static android.app.admin.DevicePolicyManager.WIPE_EUICC;
import static android.app.admin.DevicePolicyManager.WIPE_SILENTLY;
import static android.content.pm.PackageManager.FEATURE_DEVICE_ADMIN;
import static android.service.controls.ControlsProviderService.TAG;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.LinkAddress;
import android.net.LinkProperties;
import android.net.Network;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    DevicePolicyManager devicePolicyManager;
    ComponentName componentName;
    Button b_device_admin, b_d_device_admin, b_getdns, b_setdns;
    EditText t_dns_server;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        b_device_admin = findViewById(R.id.device_admin);
        b_d_device_admin = findViewById(R.id.disable_device_admin);
        b_getdns = findViewById(R.id.get_dns_button);
        t_dns_server = findViewById(R.id.text_dns_server);
        b_setdns = findViewById(R.id.set_dns_button);

        devicePolicyManager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);

        componentName = new ComponentName(MainActivity.this, Controller.class);
        boolean active = devicePolicyManager.isAdminActive(componentName);

        if(active){
            b_device_admin.setVisibility(View.GONE);
            b_getdns.setVisibility(View.VISIBLE);
            t_dns_server.setVisibility(View.VISIBLE);
            b_setdns.setVisibility(View.VISIBLE);
        }else {
            b_d_device_admin.setVisibility(View.GONE);
            b_getdns.setVisibility(View.GONE);
            t_dns_server.setVisibility(View.GONE);
            b_setdns.setVisibility(View.GONE);
        }

        b_device_admin.setOnClickListener(v -> {
            try {
                Log.i(TAG,getApplicationContext().getPackageName());
                Runtime.getRuntime().exec("dpm set-device-owner "+getApplicationContext().getPackageName()+"/.Controller");
            } catch (Exception e) {
                Log.e(TAG, "device owner not set");
                Log.e(TAG, e.toString());
                e.printStackTrace();
            }
            /* Intent intent=new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
            intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, componentName);
            intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION,"This app needs this " +
                    "permissions to modify DNS Settings. You can check the source code if you're " +
                    "skeptical.");
            startActivityForResult(intent,1);*/

        });

        b_getdns.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("The global private DNS is "+devicePolicyManager.getGlobalPrivateDnsHost(componentName)+
                    "\nThe private DNS mode is "+devicePolicyManager.getGlobalPrivateDnsMode(componentName))
                    .setCancelable(false)
                    .setPositiveButton("OK", (dialog, id) -> {
                        //do nothing
                    });
            AlertDialog alert = builder.create();
            alert.show();
        });

        b_setdns.setOnClickListener( v -> {
            Thread setDnsThread = new Thread(() -> {
                try {
                    int result = devicePolicyManager.setGlobalPrivateDnsModeSpecifiedHost(componentName, String.valueOf(t_dns_server.getText()));
                    runOnUiThread( () -> {
                        switch (result) {
                            case PRIVATE_DNS_SET_ERROR_FAILURE_SETTING:
                                Controller.showToast(getApplicationContext(), "❌ Your DNS Server could not be set");
                                break;
                            case PRIVATE_DNS_SET_ERROR_HOST_NOT_SERVING:
                                Controller.showToast(getApplicationContext(), "❌ Your DNS Server does not implement RFC7858");
                                break;
                            case PRIVATE_DNS_SET_NO_ERROR:
                                Controller.showToast(getApplicationContext(), "✅ Your DNS Server was set successfully");
                                break;
                            default:
                                Controller.showToast(getApplicationContext(), "❌ Unknown error, contact developer!");
                                break;
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            setDnsThread.start();
        });

        b_d_device_admin.setOnClickListener( v-> {
            devicePolicyManager.clearDeviceOwnerApp(getApplicationContext().getPackageName());
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            if (resultCode == Activity.RESULT_OK) {
                b_device_admin.setVisibility(View.GONE);
                b_getdns.setVisibility(View.VISIBLE);
                t_dns_server.setVisibility(View.VISIBLE);
                b_setdns.setVisibility(View.VISIBLE);
            } else {
                Toast.makeText(this, "Failed", Toast.LENGTH_SHORT).show();
            }
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}