package me.zaine.switchdns;

import static android.app.admin.DevicePolicyManager.PRIVATE_DNS_MODE_PROVIDER_HOSTNAME;
import static android.app.admin.DevicePolicyManager.PRIVATE_DNS_SET_ERROR_FAILURE_SETTING;
import static android.app.admin.DevicePolicyManager.PRIVATE_DNS_SET_ERROR_HOST_NOT_SERVING;
import static android.app.admin.DevicePolicyManager.PRIVATE_DNS_SET_NO_ERROR;

import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;

public class DnsController {

    protected int setDnsHost(DevicePolicyManager devicePolicyManager, ComponentName componentName, String host){
        try {
            return devicePolicyManager.setGlobalPrivateDnsModeSpecifiedHost(componentName, host);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    protected int unsetDnsHost(DevicePolicyManager devicePolicyManager, ComponentName componentName){
        return devicePolicyManager.setGlobalPrivateDnsModeOpportunistic(componentName);
    }

    protected StateModel getDnsState(DevicePolicyManager devicePolicyManager, ComponentName componentName){
        boolean active = devicePolicyManager.isAdminActive(componentName);

        if (active && devicePolicyManager.getGlobalPrivateDnsMode(componentName) == PRIVATE_DNS_MODE_PROVIDER_HOSTNAME)
            return new StateModel(true,"Ads blocked", null);
        else
            return new StateModel(false,"Ads allowed", null);
    }
}
