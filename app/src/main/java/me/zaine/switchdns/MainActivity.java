package me.zaine.switchdns;

import static android.app.admin.DevicePolicyManager.PRIVATE_DNS_MODE_OFF;
import static android.app.admin.DevicePolicyManager.PRIVATE_DNS_MODE_OPPORTUNISTIC;
import static android.app.admin.DevicePolicyManager.PRIVATE_DNS_MODE_PROVIDER_HOSTNAME;
import static android.app.admin.DevicePolicyManager.PRIVATE_DNS_MODE_UNKNOWN;
import static android.app.admin.DevicePolicyManager.PRIVATE_DNS_SET_ERROR_FAILURE_SETTING;
import static android.app.admin.DevicePolicyManager.PRIVATE_DNS_SET_ERROR_HOST_NOT_SERVING;
import static android.app.admin.DevicePolicyManager.PRIVATE_DNS_SET_NO_ERROR;
import static android.service.controls.ControlsProviderService.TAG;

import static androidx.constraintlayout.widget.ConstraintSet.PARENT_ID;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

public class MainActivity extends AppCompatActivity {
    DevicePolicyManager devicePolicyManager;
    ComponentName componentName;
    Button b_device_admin;
    EditText t_dns_server;
    TextView dns_status;
    Switch toggle_dns;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        b_device_admin = findViewById(R.id.device_admin);
        t_dns_server = findViewById(R.id.text_dns_server);
        dns_status = findViewById(R.id.dns_status);
        toggle_dns = findViewById(R.id.switch1);

        devicePolicyManager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);

        componentName = new ComponentName(MainActivity.this, Controller.class);
        boolean active = devicePolicyManager.isAdminActive(componentName);

        if (active) {
            b_device_admin.setText("Disable device admin");
            dns_status.setVisibility(View.VISIBLE);
            t_dns_server.setVisibility(View.VISIBLE);
            toggle_dns.setVisibility(View.VISIBLE);

            switch (devicePolicyManager.getGlobalPrivateDnsMode(componentName)){
                case PRIVATE_DNS_MODE_OFF:
                    dns_status.setText("Private DNS is OFF.");
                case PRIVATE_DNS_MODE_OPPORTUNISTIC:
                    dns_status.setText("Private DNS is set to Automatic.");
                case PRIVATE_DNS_MODE_PROVIDER_HOSTNAME:
                    dns_status.setText("Private DNS is set to ON and the DNS host is "+devicePolicyManager.getGlobalPrivateDnsHost(componentName)+".");
                case PRIVATE_DNS_MODE_UNKNOWN:
                    dns_status.setText("Private DNS status is unknown");
            }

        }else{
            b_device_admin.setText("Enable device admin");

            ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) b_device_admin.getLayoutParams();
            layoutParams.topToTop = R.id.parent_layout;
            layoutParams.bottomToBottom = R.id.parent_layout;
            b_device_admin.setLayoutParams(layoutParams);

            dns_status.setVisibility(View.GONE);
            t_dns_server.setVisibility(View.GONE);
            toggle_dns.setVisibility(View.GONE);
        }

        b_device_admin.setOnClickListener(v -> {
            if (active){
                devicePolicyManager.clearDeviceOwnerApp(getApplicationContext().getPackageName());
                this.recreate();
            }else{
                try {
                    Log.i(TAG,getApplicationContext().getPackageName());
                    Runtime.getRuntime().exec("dpm set-device-owner "+getApplicationContext().getPackageName()+"/.Controller");
                } catch (Exception e) {
                    Log.e(TAG, "device owner not set");
                    Log.e(TAG, e.toString());
                    e.printStackTrace();
                }}
        });

        toggle_dns.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
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
            } else {
                devicePolicyManager.setGlobalPrivateDnsModeOpportunistic(componentName);
                Controller.showToast(getApplicationContext(), "0️ Your DNS Server was unset successfully");
            }
            this.recreate();
        });
    }
}