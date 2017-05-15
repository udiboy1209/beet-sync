/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package in.udiboy.beet_sync;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.nsd.NsdServiceInfo;
import android.net.nsd.NsdManager;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class NsdHelper {

    MainActivity mContext;
    Handler mUIThread;

    NsdManager mNsdManager;
    NsdManager.DiscoveryListener mDiscoveryListener;
    NsdManager.RegistrationListener mRegistrationListener;

    List<OnServiceChangedListener> onServiceChangedListeners = new ArrayList<>();

    ProgressDialog mRegistrationProgressDialog;

    public static final String SERVICE_TYPE = "_beetsync._tcp.";

    public static final String TAG = "NsdHelper";
    public String mServiceName = "";

    NsdServiceInfo mService;
    HashMap<String,NsdServiceInfo> resolvedServices = new HashMap<>();
    private int resolveTries=0;

    public NsdHelper(MainActivity context) {
        mContext = context;
        mUIThread = new Handler(context.getMainLooper());
        mNsdManager = (NsdManager) context.getSystemService(Context.NSD_SERVICE);
    }

    public void initializeNsd() {
        initializeDiscoveryListener();
        initializeRegistrationListener();

        //mNsdManager.init(mContext.getMainLooper(), this);

    }

    public void initializeDiscoveryListener() {
        mDiscoveryListener = new NsdManager.DiscoveryListener() {

            @Override
            public void onDiscoveryStarted(String regType) {
                Log.d(TAG, "Service discovery started");
            }

            @Override
            public void onServiceFound(NsdServiceInfo service) {
                Log.d(TAG, "Service discovery success: " + service);
                if (!service.getServiceType().equals(SERVICE_TYPE)) {
                    Log.d(TAG, "Unknown Service Type: " + service.getServiceType());
                } else if (service.getServiceName().equals(mServiceName)) {
                    Log.d(TAG, "Same machine: " + mServiceName);
                } else {
                    startResolveService(service);
                }
            }

            @Override
            public void onServiceLost(final NsdServiceInfo service) {
                Log.e(TAG, "service lost" + service);
                resolvedServices.remove(service.getServiceName());
                mUIThread.post(new Runnable() {
                    @Override
                    public void run() {
                        for(OnServiceChangedListener l : onServiceChangedListeners) l.onServiceRemoved(service);
                    }
                });
                if (mService == service) {
                    mService = null;
                }
            }
            
            @Override
            public void onDiscoveryStopped(String serviceType) {
                Log.i(TAG, "Discovery stopped: " + serviceType);        
            }

            @Override
            public void onStartDiscoveryFailed(String serviceType, int errorCode) {
                Log.e(TAG, "Discovery failed: Error code:" + errorCode);
                mNsdManager.stopServiceDiscovery(this);
            }

            @Override
            public void onStopDiscoveryFailed(String serviceType, int errorCode) {
                Log.e(TAG, "Discovery failed: Error code:" + errorCode);
                mNsdManager.stopServiceDiscovery(this);
            }
        };
    }

    public void initializeRegistrationListener() {
        mRegistrationListener = new NsdManager.RegistrationListener() {

            @Override
            public void onServiceRegistered(NsdServiceInfo NsdServiceInfo) {
                mServiceName = NsdServiceInfo.getServiceName();
                Log.d(TAG, "Service Registered: "+mServiceName);

                if(mRegistrationProgressDialog!=null)
                    if(mRegistrationProgressDialog.isShowing())
                        mRegistrationProgressDialog.dismiss();

                mUIThread.post(new Runnable() {
                    @Override
                    public void run() {
                        //mContext.onRegistered();
                    }
                });
            }
            
            @Override
            public void onRegistrationFailed(NsdServiceInfo arg0, int arg1) {
                if(mRegistrationProgressDialog!=null)
                    if(mRegistrationProgressDialog.isShowing())
                        mRegistrationProgressDialog.dismiss();

                Toast.makeText(mContext, "Registration Failed. Error Code: "+arg1,Toast.LENGTH_LONG).show();
            }

            @Override
            public void onServiceUnregistered(NsdServiceInfo arg0) {
            }
            
            @Override
            public void onUnregistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
            }
            
        };
    }

    public void registerService(int port, String name) {
        NsdServiceInfo serviceInfo  = new NsdServiceInfo();
        serviceInfo.setPort(port);
        serviceInfo.setServiceName(name);
        serviceInfo.setServiceType(SERVICE_TYPE);

        mRegistrationProgressDialog = ProgressDialog.show(mContext, "","Starting server, please wait", true, false);
        
        mNsdManager.registerService(
                serviceInfo, NsdManager.PROTOCOL_DNS_SD, mRegistrationListener);
        
    }

    public void unregisterService(){
        mNsdManager.unregisterService(mRegistrationListener);
    }

    private void startResolveService(NsdServiceInfo serviceInfo){
        NsdManager.ResolveListener newResolveListener = new NsdManager.ResolveListener() {
            @Override
            public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {
                Log.e(TAG, "Resolve Failed: " + serviceInfo + "\tError Code: " + errorCode);
                resolveTries++;
                switch (errorCode) {
                    case NsdManager.FAILURE_ALREADY_ACTIVE:
                        Log.e(TAG, "FAILURE_ALREADY_ACTIVE");
                        // Just try again...
                        if(resolveTries < 30)
                            startResolveService(serviceInfo);
                        else
                            resolveTries=0;
                        break;
                    case NsdManager.FAILURE_INTERNAL_ERROR:
                        Log.e(TAG, "FAILURE_INTERNAL_ERROR");
                        break;
                    case NsdManager.FAILURE_MAX_LIMIT:
                        Log.e(TAG, "FAILURE_MAX_LIMIT");
                        break;
                }
            }

            @Override
            public void onServiceResolved(final NsdServiceInfo serviceInfo) {
                Log.i(TAG, "Service Resolved: " + serviceInfo);
                resolveTries=0;
                if(resolvedServices.put(serviceInfo.getServiceName(), serviceInfo)==null) {
                    mUIThread.post(new Runnable() {
                        @Override
                        public void run() {
                            for (OnServiceChangedListener l : onServiceChangedListeners)
                                l.onServiceAdded(serviceInfo);
                        }
                    });
                }
            }
        };
        mNsdManager.resolveService(serviceInfo, newResolveListener);
    }

    public void discoverServices() {
        mNsdManager.discoverServices(
                SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, mDiscoveryListener);
    }
    
    public void stopDiscovery() {
        mNsdManager.stopServiceDiscovery(mDiscoveryListener);
    }

    public NsdServiceInfo getChosenServiceInfo() {
        return mService;
    }
    
    public void tearDown() {
        try{
            mNsdManager.unregisterService(mRegistrationListener);
        } catch (Exception e){
        }
    }

    public void registerServiceChangedListener(OnServiceChangedListener lstn){
        onServiceChangedListeners.add(lstn);
    }

    public void unregisterServiceChangedListener(OnServiceChangedListener lstn){
        onServiceChangedListeners.remove(lstn);
    }

    public NsdServiceInfo getConnectedService() {
        return mService;
    }

    public void setConnectedService(NsdServiceInfo service) {
        mService = service;
    }

    public interface OnServiceChangedListener {
        void onServiceAdded(NsdServiceInfo service);
        void onServiceRemoved(NsdServiceInfo service);
    }
}
