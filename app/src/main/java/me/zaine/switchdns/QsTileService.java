package me.zaine.switchdns;

import static android.app.admin.DevicePolicyManager.PRIVATE_DNS_MODE_PROVIDER_HOSTNAME;
import static android.app.admin.DevicePolicyManager.PRIVATE_DNS_SET_ERROR_FAILURE_SETTING;
import static android.app.admin.DevicePolicyManager.PRIVATE_DNS_SET_ERROR_HOST_NOT_SERVING;
import static android.app.admin.DevicePolicyManager.PRIVATE_DNS_SET_NO_ERROR;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.graphics.drawable.Icon;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;
import android.util.Log;
import android.view.View;

import java.lang.ref.WeakReference;

public class QsTileService extends TileService {

    DevicePolicyManager devicePolicyManager;
    ComponentName componentName;
    DnsController dnsController;

    //static inner class doesn't hold an implicit reference to the outer class
    private static class MyHandler extends Handler {
        //Using a weak reference means you won't prevent garbage collection
        private final WeakReference<QsTileService> myClassWeakReference;

        public MyHandler(QsTileService myClassInstance) {
            myClassWeakReference = new WeakReference<QsTileService>(myClassInstance);
        }

        @Override
        public void handleMessage(Message msg) {
            QsTileService myClass = myClassWeakReference.get();
            if (myClass != null) {
                //...do work here...
            }
        }
    }

    /**
     * An example getter to provide it to some external class
     * or just use 'new MyHandler(this)' if you are using it internally.
     * If you only use it internally you might even want it as final member:
     * private final MyHandler mHandler = new MyHandler(this);
     */
    public Handler getHandler() {
        return new MyHandler(this);
    }

    @Override
    public void onStartListening() {
        super.onStartListening();
        updateTile();
    }

    // Called when the user taps on your tile in an active or inactive state.
    @Override
    public void onClick() {
        super.onClick();
        devicePolicyManager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        componentName = new ComponentName(QsTileService.this, Controller.class);
        dnsController = new DnsController();


        if (dnsController.getDnsState(devicePolicyManager, componentName).enabled){
            dnsController.unsetDnsHost(devicePolicyManager, componentName);
            updateTile();
        }else{
            Thread setDnsThread = new Thread(() -> {
                Looper.prepare();
                try {
                    final MyHandler mHandler = new MyHandler(QsTileService.this){
                        @Override
                        public void handleMessage(Message msg) {
                            switch (msg.what) {
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
                        }
                    };
                    int result = dnsController.setDnsHost(devicePolicyManager,componentName, "dns.adguard.com");
                    mHandler.sendEmptyMessage(result);
                    updateTile();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            setDnsThread.start();
        }
    }

    // Called when the user adds your tile.
    @Override
    public void onTileAdded() {
        super.onTileAdded();
        updateTile();
    }

    protected void updateTile(){
        devicePolicyManager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        componentName = new ComponentName(QsTileService.this, Controller.class);
        dnsController = new DnsController();

        StateModel state = dnsController.getDnsState(devicePolicyManager, componentName);
        Log.i("QsTile","Tile current state is "+state.enabled);
        Tile tile = getQsTile();
        tile.setLabel(state.label);
        tile.setContentDescription(state.label);
        tile.setState(state.enabled ? Tile.STATE_ACTIVE : Tile.STATE_INACTIVE);
        tile.updateTile();
    }
}
