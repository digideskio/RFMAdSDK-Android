/*
 * Copyright (c) 2016. Rubicon Project. All rights reserved
 *
 */

package com.rfm.sdk.mediator;

import android.content.Context;
import android.util.Log;

import com.rfm.sdk.ui.mediator.RFMCustomInterstitial;
import com.rfm.sdk.ui.mediator.RFMCustomInterstitialListener;
import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.InterstitialAd;
import com.facebook.ads.InterstitialAdListener;

import java.util.Map;
import java.util.Set;

public class RFMMediatorFBANInterstitial implements RFMCustomInterstitial {
    Context mContext;
    RFMCustomInterstitialListener mListener;
    private InterstitialAd mInterstitialAd;
    private static final String LOG_TAG = "FBANInterstitial";
    private static final String PARAM_AD_ID = "placementId";

    /**
     * Implementation for requesting Interstitial Ad from FBAN via RFM Custom event
     *
     * @param context
     * @param params , will have placementId and other parameters specified for Custom Event
     * @param listener
     **/
    @Override
    public void requestAd(Context context, Map<String, String> params, RFMCustomInterstitialListener listener) {
        mContext = context;
        mListener = listener;
        if(createInterstitial(params)) {
            createAdListener();
            loadAd();
        } else {
            if(mListener != null) {
                mListener.onAdFailed("Failed to request FBAN Interstitial, placement Id missing");
            }
        }
    }

    /**
     * Called when Ad / UI containing Ad is closed
     * Method should include all the clean up code
     */
    @Override
    public void reset() {
        if (mInterstitialAd != null) {
            mInterstitialAd.destroy();
            mInterstitialAd = null;
            mContext=null;
            Log.v(LOG_TAG, "Clean up FBAN Intersitital");
        }
    }

    /**
     * Method to do the needful for displaying Ad
     *
     * @return
     */
    @Override
    public boolean display() {
        if(mInterstitialAd != null) {
            mInterstitialAd.show();
            return true;
        }
        return false;
    }


    /**
     * Utility method to create Interstitial Ad
     *
     * @param  params
     * @return true for success / false for failure
     */
    private boolean createInterstitial(Map<String, String> params) {
        // Create the interstitial unit with a placement ID (generate your own on the Facebook app settings).
        // Use different ID for each ad placement in your app.
        String placementId = null;
        printAdParams(params);


        if(params != null) {
            placementId = params.get(PARAM_AD_ID);
        }
        Log.v(LOG_TAG, "Requesting Interstitial from FBAN with placement Id " + placementId);

        if(placementId == null) {
           return false;
        }

        mInterstitialAd = new InterstitialAd(mContext,  placementId);
        return true;
    }

    /**
     * Utility method to add call back listener
     */
    private void createAdListener() {
        mInterstitialAd.setAdListener(new InterstitialAdListener() {
            @Override
            public void onInterstitialDisplayed(Ad ad) {
                Log.d(LOG_TAG, "FBAN Interstitial Ad displayed");
                if(mListener != null) {
                    mListener.onAdDisplayed();
                }
            }

            @Override
            public void onInterstitialDismissed(Ad ad) {
                Log.d(LOG_TAG, "FBAN Interstitial Ad closed");
                if(mListener != null) {
                    mListener.onAdClosed();
                }
            }

            @Override
            public void onError(Ad ad, AdError adError) {
                Log.d(LOG_TAG, "FBAN Interstitial Ad failed");
                if (mListener != null) {
                    mListener.onAdFailed("FBAN Interstitial failed with error code " + adError.getErrorCode()+ " and message "+adError.getErrorMessage());
                }
            }

            @Override
            public void onAdLoaded(Ad ad) {
                Log.d(LOG_TAG, "FBAN Interstitial Ad loaded");
                if(mListener != null) {
                    mListener.onAdLoaded();
                }
            }

            @Override
            public void onAdClicked(Ad ad) {
                Log.d(LOG_TAG, "FBAN Interstitial Ad clicked");
                if(mListener != null) {
                    mListener.onAdClicked();
                }
            }
        });
    }

    /**
     * Utility method to request Ad
     */
    private void loadAd() {
        mInterstitialAd.loadAd();
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
