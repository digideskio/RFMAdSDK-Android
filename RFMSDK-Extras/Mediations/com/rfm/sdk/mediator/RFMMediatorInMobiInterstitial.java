/*
 * Copyright (c) 2016. Rubicon Project. All rights reserved
 *
 */

package com.rfm.sdk.mediator;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.inmobi.commons.InMobi;
import com.inmobi.monetization.IMErrorCode;
import com.inmobi.monetization.IMInterstitial;
import com.inmobi.monetization.IMInterstitialListener;
import com.rfm.sdk.ui.mediator.RFMCustomInterstitial;
import com.rfm.sdk.ui.mediator.RFMCustomInterstitialListener;

import java.util.Map;
import java.util.Set;

public class RFMMediatorInMobiInterstitial implements RFMCustomInterstitial {
    Context mContext;
    private IMInterstitial mInterstitial;
    RFMCustomInterstitialListener mListener;
    private static final String LOG_TAG = "InMobiInterstitial";
    private static final String PARAM_AD_ID = "appId";

    /**
     * Implementation for requesting Interstitial Ad from Admob via RFM Custom event
     *
     * @param context
     * @param params , will have appId and other parameters specified for Custom Event
     * @param listener
     **/
    @Override
    public void requestAd(Context context, Map<String, String> params, RFMCustomInterstitialListener listener) {
        Log.v("RFMMediatorInMobiBanner", " Requesting Ad from RFMMediatorInMobiBanner ");
        mContext = context;
        mListener = listener;
        if(createInterstitial(params)) {
            createAdListener();
            loadAd();
        } else {
            if(mListener != null) {
                mListener.onAdFailed("Failed to request InMobi Interstitial, app Id missing");
            }
        }
    }

    /**
     * Called when Ad / UI containing Ad is closed
     * Method should include all the clean up code
     */
    @Override
    public void reset() {
        if(mInterstitial != null) {
            mInterstitial.setIMInterstitialListener(null);
            mInterstitial.stopLoading();
            mContext=null;
            Log.v(LOG_TAG, "Clean up Inmobi Intersitital");
        }
    }

    /**
     * Method to do the needful for displaying Ad
     *
     * @return
     */
    @Override
    public boolean display() {
        if(mInterstitial != null) {
            mInterstitial.show();
            return true;
        }
        return false;
    }

    /**
     * Utility method to create Interstitial Ad
     *
     * @param adParams
     * @return true for success / false for failure
     */
    protected boolean createInterstitial(Map<String, String> adParams) {
        printAdParams(adParams);
        String appId = null;
        if(adParams != null) {
            appId = adParams.get(PARAM_AD_ID);
        }
        Log.v(LOG_TAG, "Requesting Interstitial from Inmobi with Id " + appId);
        if(appId == null) {
            return false;
        }
        
        InMobi.initialize(mContext, appId);
        mInterstitial = new IMInterstitial((Activity)mContext, appId);
        return true;
    }

    /**
     * Utility method to add call back listener
     */
    protected void createAdListener() {
        mInterstitial.setIMInterstitialListener(new IMInterstitialListener() {

            @Override
            public void onLeaveApplication(IMInterstitial arg0) {
                Log.v(LOG_TAG, "InMobiMediatior mInterstitial onLeaveApplication ");
            }
            @Override
            public void onDismissInterstitialScreen(IMInterstitial arg0) {
                Log.v(LOG_TAG, "InMobiMediatior mInterstitial dismissed ");
                if(mListener !=null) {
                    mListener.onAdClosed();
                }
            }

            @Override
            public void onInterstitialFailed(IMInterstitial arg0, IMErrorCode eCode) {
                Log.v(LOG_TAG, "InMobiMediatior mInterstitial failed");
                if(mListener != null) {
                    mListener.onAdFailed("Failed to display Interstitial from InMobi, InMobi "+eCode);
                }
            }

            @Override
            public void onInterstitialInteraction(IMInterstitial arg0,
                                                  Map<String, String> arg1) {
                // no-op
                Log.v(LOG_TAG, "InMobiMediatior mInterstitial on user Interaction");
                if(mListener !=null) {
                    mListener.onAdClicked();
                }
            }

            @Override
            public void onInterstitialLoaded(IMInterstitial arg0) {
                Log.v(LOG_TAG, "InMobiMediatior mInterstitial loaded");
                if(mListener !=null) {
                    mListener.onAdLoaded();
                }

            }

            @Override
            public void onShowInterstitialScreen(IMInterstitial arg0) {
                Log.v(LOG_TAG, "InMobiMediatior Interstitial on onShowInterstitialScreen");
                if(mListener !=null) {
                    mListener.onAdDisplayed();
                }
            }
        });
    }

    /**
     * Utility method to request Ad
     */
    protected void loadAd() {
        mInterstitial.loadInterstitial();
    }

    /**
     * Utility method to print all the parameters sent from RFM SDK
     */
    protected void printAdParams(Map<String, String> params) {
        if(params!= null) {
            Log.v(LOG_TAG, "Ad params from RFM Server");
            Set<String> keys = params.keySet();
            for(String keyStr:keys) {
                Log.v(LOG_TAG, "Key:"+keyStr+" | Value:"+params.get(keyStr));
            }
        } else {
            Log.v(LOG_TAG, "No additional Ad params available from RFM Server");
        }
    }

}
