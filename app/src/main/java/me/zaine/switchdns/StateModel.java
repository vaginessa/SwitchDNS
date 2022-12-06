package me.zaine.switchdns;

import android.graphics.drawable.Icon;

public class StateModel {
    final boolean enabled;
    final String label;
    final Icon icon;

    public StateModel(boolean e, String l, Icon i) {
        enabled = e;
        label = l;
        icon = i;
    }
}
