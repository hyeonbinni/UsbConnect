package com.tuto.usbconnect;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private static final String ACTION_USB_PERMISSION = "com.tuto.usbconnect.USB_PERMISSION";

    private static final int[] PERMITTED_VENDOR_IDS = {
            1027,
            9025
    };

    private BroadcastReceiver broadcastReceiver;

    private UsbManager usbManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        usbManager = (UsbManager)getSystemService(Context.USB_SERVICE);

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                UsbDevice attachedDevice;

                switch (action) {
                    case UsbManager.ACTION_USB_DEVICE_ATTACHED:
                        attachedDevice = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);

                        if(attachedDevice == null) {
                            return;
                        }

                        if (!isPermittedVenderId(attachedDevice.getVendorId())) {
                            return;
                        }

                        if (!usbManager.hasPermission(attachedDevice)) {
                            requestUsbPermission(attachedDevice);
                            return;
                        }

                        // todo : do something when the attached device has a permission
                        break;
                    case UsbManager.ACTION_USB_DEVICE_DETACHED:
                        attachedDevice = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);

                        if(attachedDevice == null) {
                            return;
                        }

                        if (!isPermittedVenderId(attachedDevice.getVendorId())) {
                            return;
                        }

                        // todo : do something when the device is detached
                        break;
                    case ACTION_USB_PERMISSION:
                        if (!intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                            // todo : do something when usb permission is denied
                            return;
                        }

                        // todo : do something when usb permission is permitted
                        break;
                    default:
                        break;
                }
            }
        };

        IntentFilter filter = new IntentFilter();
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        filter.addAction(ACTION_USB_PERMISSION);
        registerReceiver(broadcastReceiver, filter);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        checkAttachedUsbDevice();
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(broadcastReceiver);

        super.onDestroy();
    }

    private void checkAttachedUsbDevice() {
        HashMap<String, UsbDevice> map = usbManager.getDeviceList();
        Iterator itr = map.entrySet().iterator();
        while (itr.hasNext()) {
            Map.Entry<String, UsbDevice> entry = (Map.Entry) itr.next();
            UsbDevice device = entry.getValue();

            if (isPermittedVenderId(device.getVendorId())) {
                if (!usbManager.hasPermission(device)) {
                    requestUsbPermission(device);
                    return;
                }

                // todo : do something when the attached device has a permission
                break;
            }
        }
    }

    private void requestUsbPermission(UsbDevice device) {
        PendingIntent pi = PendingIntent.getBroadcast(getApplicationContext(), 0, new Intent(ACTION_USB_PERMISSION), 0);
        usbManager.requestPermission(device, pi);
    }

    // check attached device is permitted
    private boolean isPermittedVenderId(int venderId) {
        for(int permittedVenderId : PERMITTED_VENDOR_IDS) {
            if(venderId == permittedVenderId) return true;
        }

        return false;
    }
}
