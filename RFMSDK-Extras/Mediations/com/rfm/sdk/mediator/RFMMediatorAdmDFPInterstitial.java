/*
 * Copyright (c) 2016. Rubicon Project. All rights reserved
 *
 */

package com.rfm.sdk.mediator;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.doubleclick.PublisherAdRequest;
import com.google.android.gms.ads.doubleclick.PublisherInterstitialAd;
import com.rfm.sdk.ui.mediator.RFMCustomInterstitial;
import com.rfm.sdk.ui.mediator.RFMCustomInterstitialListener;

import java.util.Map;
import java.util.Set;

public class RFMMediatorAdmDFPInterstitial implements RFMCustomInterstitial {
    private PublisherInterstitialAd mInterstitialAd;
    Context mContext;
    RFMCustomInterstitialListener mListener;
    private static final String LOG_TAG = "AdmDfpInterstitial";
    private static final String TEST_DEVICE_ID = "2CC6189A7D478F739F11622ECCB6EB5F";
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
        // Create an Interstitial Ad
        if(createInterstitial(params)) {
            // Add a listener for callbacks
            createAdListener();
            // request Ad
            loadAd();
        } else {
            if(mListener != null) {
                mListener.onAdFailed("Failed to request DFP Interstitial, site Id missing");
            }
        }
    }

    /**
     * Called when Ad / UI containing Ad is closed
     * Method should include all the clean up code
     */
    @Override
    public void reset() {
        if(mInterstitialAd != null) {
            mInterstitialAd.setAdListener(null);
            mInterstitialAd = null;
            mContext = null;
            Log.v(LOG_TAG, "Clean up Admob DFP Intersitital");
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
            if(mListener != null) {
                mListener.onAdDisplayed();
            }
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
    public boolean createInterstitial(Map<String, String> adParams) {
        String adUnitId = null;
        printAdParams(adParams);
        if(adParams != null) {
            adUnitId = adParams.get(PARAM_AD_ID);
        }
        Log.v(LOG_TAG, "Requesting Interstitial from DFP with adUnitId " + adUnitId);

        if(adUnitId == null) {
            return false;
        }

        mInterstitialAd = new PublisherInterstitialAd(mContext);
        mInterstitialAd.setAdUnitId(adUnitId);
        return true;
    }

    /**
     * Utility method to add call back listener
     */
    public void createAdListener() {
        // Set the AdListener.
        mInterstitialAd.setAdListener(new AdListener() {
            /** Called when an ad is loaded. */
            @Override
            public void onAdLoaded() {
                Log.d(LOG_TAG, "DFP Interstitial onAdLoaded");
                if (mListener != null) {
                    mListener.onAdLoaded();
                }
            }

            /** Called when an ad failed to load. */
            @Override
            public void onAdFailedToLoad(int errorCode) {
                String message = String.format("DFP InterstitialonAdFailedToLoad (%s)", getErrorReason(errorCode));
                Log.d(LOG_TAG, message);
                //Toast.makeText(InterstitialAdActivity.this, message, Toast.LENGTH_SHORT).show();
                if (mListener != null) {
                    mListener.onAdFailed("EDFP Interstitial error code " + errorCode);
                }
                // Change the button text and disable the show button.
            }

            /**
             * Called when an Activity is created in front of the app (e.g. an interstitial is shown, or an
             * ad is clicked and launches a new Activity).
             */
            @Override
            public void onAdOpened() {
                Log.d(LOG_TAG, "DFP Interstitial onAdOpened");
                //Toast.makeText(InterstitialAdActivity.this, "onAdOpened", Toast.LENGTH_SHORT).show();
                if (mListener != null) {
                    mListener.onAdClicked();
                }
            }

            /** Called when an ad is closed and about to return to the application. */
            @Override
            public void onAdClosed() {
                Log.d(LOG_TAG, "DFP Interstitial onAdClosed");
                if (mListener != null) {
                    mListener.onAdClosed();
                }
                //Toast.makeText(InterstitialAdActivity.this, "onAdClosed", Toast.LENGTH_SHORT).show();
            }

            /**
             * Called when an ad is clicked and going to start a new Activity that will leave the
             * application (e.g. breaking out to the Browser or Maps application).
             */
            @Override
            public void onAdLeftApplication() {
                Log.d(LOG_TAG, "DFP Interstitial onAdLeftApplication");
                //ast.makeText(InterstitialAdActivity.this, "onAdLeftApplication", Toast.LENGTH_SHORT).show();
            }

            /** Gets a string error reason from an error code. */
            private String getErrorReason(int errorCode) {
                switch (errorCode) {
                    case AdRequest.ERROR_CODE_INTERNAL_ERROR:
                        return "Internal error";
                    case AdRequest.ERROR_CODE_INVALID_REQUEST:
                        return "Invalid request";
                    case AdRequest.ERROR_CODE_NETWORK_ERROR:
                        return "Network Error";
                    case AdRequest.ERROR_CODE_NO_FILL:
                        return "No fill";
                    default:
                        return "Unknown error";
                }
            }
        });
    }

    /**
     * Utility method to request Ad
     */
    private void loadAd() {
        mInterstitialAd.loadAd(new PublisherAdRequest.Builder().addTestDevice(TEST_DEVICE_ID).
                build());
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
