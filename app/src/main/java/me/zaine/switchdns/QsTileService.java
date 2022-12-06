package me.zaine.switchdns;

import static android.app.admin.DevicePolicyManager.PRIVATE_DNS_MODE_PROVIDER_HOSTNAME;
import static android.app.admin.DevicePolicyManager.PRIVATE_DNS_SET_ERROR_FAILURE_SETTING;
import static android.app.admin.DevicePolicyManager.PRIVATE_DNS_SET_ERROR_HOST_NOT_SERVING;
import static android.app.admin.DevicePolicyManager.PRIVATE_DNS_SET_NO_ERROR;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.graphics.drawable.Icon;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;
import android.view.View;

public class QsTileService extends TileService {

    DevicePolicyManager devicePolicyManager;
    ComponentName componentName;
    DnsController dnsController;

    @Override
    public void onStartListening() {
        super.onStartListening();
        devicePolicyManager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        componentName = new ComponentName(QsTileService.this, Controller.class);
        dnsController = new DnsController();
        StateModel state = dnsController.getDnsState(devicePolicyManager, componentName);
        Tile tile = getQsTile();
        tile.setLabel(state.label);
        tile.setContentDescription(state.label);
        tile.setState(state.enabled ? Tile.STATE_ACTIVE : Tile.STATE_INACTIVE);
        tile.setIcon(state.icon);
        tile.updateTile();
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
        }else{
            switch (dnsController.setDnsHost(devicePolicyManager, componentName, devicePolicyManager.getGlobalPrivateDnsHost(componentName))) {
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
    }

    // Called when your app can no longer update your tile.
    @Override
    public void onStopListening() {
        super.onStopListening();
    }

    // Called when the user adds your tile.
    @Override
    public void onTileAdded() {
        super.onTileAdded();
    }
    // Called when the user removes your tile.
    @Override
    public void onTileRemoved() {
        super.onTileRemoved();
    }
}
