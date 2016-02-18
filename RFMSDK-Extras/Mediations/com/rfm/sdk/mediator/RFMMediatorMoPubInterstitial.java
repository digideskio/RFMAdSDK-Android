/*
 * Copyright (c) 2016. Rubicon Project. All rights reserved
 *
 */

package com.rfm.sdk.mediator;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.mopub.mobileads.MoPubErrorCode;
import com.mopub.mobileads.MoPubInterstitial;
import com.rfm.sdk.ui.mediator.RFMCustomInterstitial;
import com.rfm.sdk.ui.mediator.RFMCustomInterstitialListener;

import java.util.Map;
import java.util.Set;

public class RFMMediatorMoPubInterstitial implements RFMCustomInterstitial {
    Context mContext;
    RFMCustomInterstitialListener mListener;
    MoPubInterstitial mInterstitial;
    private static final String LOG_TAG = "MoPubInterstitial";
    private static final String PARAM_AD_ID = "adUnitId";

    /**
     * Implementation for requesting Interstitial Ad from Admob via RFM Custom event
     *
     * @param context
     * @param params , will have adUnitId and other parameters specified for Custom Event
     * @param listener
     **/
    @Override
    public void requestAd(Context context, Map<String, String> params, RFMCustomInterstitialListener listener) {
        mContext = context;
        mListener = listener;
        if(createInterstitialAd(params)) {
            createAdListener();
            loadAd();
        } else {
            if(mListener != null) {
                mListener.onAdFailed("Failed to request MoPub Interstitial, ad unit Id missing");
            }
        }
    }

    /**
     * Called when Ad / UI containing Ad is closed
     * Method should include all the clean up code
     */
    @Override
    public void reset() {
        if (mInterstitial != null) {
            mInterstitial.setInterstitialAdListener(null);
            mInterstitial.destroy();
            mInterstitial = null;
            mContext=null;
            Log.v(LOG_TAG, "Clean up MoPub Intersitital");
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
            return mInterstitial.show();
        }
        return false;
    }

    /**
     * Utility method to create Interstitial Ad
     *
     * @param adParams
     * @return true for success / false for failure
     */
    protected boolean createInterstitialAd(Map<String, String> adParams) {
        printAdParams(adParams);
        String adUnitId = null;
        if(adParams != null) {
            adUnitId = adParams.get(PARAM_AD_ID);
        }

        Log.v(LOG_TAG, "Requesting Interstitial from MoPub with AdUnit Id " + adUnitId);
        if(adUnitId == null) {
            return false;
        }

        mInterstitial= new MoPubInterstitial((Activity)mContext, adUnitId);
        mInterstitial.setKeywords(null);
        // Set location awareness and precision globally for your app:
//        MoPub.setLocationAwareness(locationAwareness);
//        MoPub.setLocationPrecision(locPrecision);

        return true;
    }

    /**
     * Utility method to add call back listener
     */
    protected void createAdListener() {
        mInterstitial.setInterstitialAdListener(new MoPubInterstitial.InterstitialAdListener() {
            @Override
            public void onInterstitialLoaded(MoPubInterstitial mInterstitial) {
                Log.i(LOG_TAG, "MoPub mInterstitial Ad loaded");
                if(mListener != null) {
                    mListener.onAdLoaded();
                }
            }

            @Override
            public void onInterstitialFailed(MoPubInterstitial mInterstitial, MoPubErrorCode errorCode) {
                Log.i(LOG_TAG, "MoPub mInterstitial Ad fetch request failed with error:" +errorCode.toString());
                if(mListener != null) {
                    mListener.onAdFailed( "MoPub mInterstitial Media Ad fetch request failed with error:" +errorCode.toString());
                }
            }

            @Override
            public void onInterstitialShown(MoPubInterstitial mInterstitial) {
                Log.i(LOG_TAG, "MoPub mInterstitial Ad displayed");
                if(mListener != null) {
                    mListener.onAdDisplayed();
                }
            }

            @Override
            public void onInterstitialClicked(MoPubInterstitial mInterstitial) {
                Log.i(LOG_TAG, "MoPub mInterstitial Ad clicked");
                if(mListener != null) {
                    mListener.onAdClicked();
                }
            }

            @Override
            public void onInterstitialDismissed(MoPubInterstitial mInterstitial) {
                Log.i(LOG_TAG, "MoPub mInterstitial Ad closed");
                if(mListener != null) {
                    mListener.onAdClosed();
                }
            }
        });
    }

    /**
     * Utility method to request Ad
     */
    protected void loadAd() {
        mInterstitial.load();
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
