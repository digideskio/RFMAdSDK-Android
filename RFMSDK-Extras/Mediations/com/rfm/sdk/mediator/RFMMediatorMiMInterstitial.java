/*
 * Copyright (c) 2016. Rubicon Project. All rights reserved
 *
 */

package com.rfm.sdk.mediator;

import android.content.Context;
import android.util.Log;

import com.millennialmedia.android.MMAd;
import com.millennialmedia.android.MMException;
import com.millennialmedia.android.MMInterstitial;
import com.millennialmedia.android.RequestListener;
import com.rfm.sdk.ui.mediator.RFMCustomInterstitial;
import com.rfm.sdk.ui.mediator.RFMCustomInterstitialListener;

import java.util.Map;
import java.util.Set;

public class RFMMediatorMiMInterstitial implements RFMCustomInterstitial {
    Context mContext;
    RFMCustomInterstitialListener mListener;
    private static final String LOG_TAG = "MiMInterstitial";
    private static final String PARAM_AD_ID = "apId";

    // The ad view object
    private MMInterstitial interstitial;

    /**
     * Implementation for requesting Interstitial Ad from Millennial via RFM Custom event
     *
     * @param context
     * @param params , will have apId and other parameters specified for Custom Event
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
                mListener.onAdFailed("Failed to request MIM Interstitial, App Id missing");
            }
        }
    }

    /**
     * Called when Ad / UI containing Ad is closed
     * Method should include all the clean up code
     */
    @Override
    public void reset() {
        if(interstitial != null) {
            interstitial = null;
            mContext=null;
            Log.v(LOG_TAG, "Clean up MIM Intersitital");
        }
    }

    /**
     * Method to do the needful for displaying Ad
     *
     * @return
     */
    @Override
    public boolean display() {
        if(interstitial != null) {
            return interstitial.display();
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
        String apId = null;
        if(adParams != null) {
           apId = adParams.get(PARAM_AD_ID);
        }
        //placement_id = "1111";
        Log.v(LOG_TAG, "Requesting Interstitial from Millennial Media with apId " + apId);
        if(apId == null) {
            return false;
        }
        // Create the adView
        interstitial = new MMInterstitial(mContext);

        // Set your apid
        interstitial.setApid(apId);
        return true;
    }

    /**
     * Utility method to add call back listener
     */
    protected void createAdListener() {
        if(interstitial != null) {
            interstitial.setListener(new RequestListener() {
                @Override
                public void MMAdOverlayLaunched(MMAd mmAd) {
                    Log.i(LOG_TAG, "Millennial Media Ad (" + mmAd.getApid() + ") Overlay Launched");
                    if(mListener != null) {
                        mListener.onAdDisplayed();
                    }
                }

                @Override
                public void MMAdOverlayClosed(MMAd mmAd) {
                    Log.i(LOG_TAG, "Millennial Media Ad (" + mmAd.getApid() + ") Overlay closed");
                    if(mListener != null) {
                        mListener.onAdClosed();
                    }

                }

                @Override
                public void MMAdRequestIsCaching(MMAd mmAd) {
                    Log.i(LOG_TAG, "Millennial Media Ad (" + mmAd.getApid() + ") caching started");
                }

                @Override
                public void requestCompleted(MMAd mmAd) {
                    Log.i(LOG_TAG, "Millennial Media Ad (" + mmAd.getApid() + ") caching completed successfully.");
                    if(mListener != null) {
                        mListener.onAdLoaded();
                    }
                }

                @Override
                public void requestFailed(MMAd mmAd, MMException e) {
                    Log.i(LOG_TAG, String.format("Millennial Media Ad (" + mmAd.getApid() + ") fetch request failed with error: %d %s.", e.getCode(), e.getMessage()));
                    if(mListener != null) {
                        mListener.onAdFailed(String.format("Millennial Media Ad (" + mmAd.getApid() + ") fetch request failed with error: %d %s.", e.getCode(), e.getMessage()));
                    }
                }

                @Override
                public void onSingleTap(MMAd mmAd) {
                    Log.i(LOG_TAG, "Millennial Media Ad (" + mmAd.getApid() + ") single tap");
                    if(mListener !=null) {
                        mListener.onAdClicked();
                    }
                }
            });
        }
    }

    /**
     * Utility method to request Ad
     */
    protected void loadAd() {
        interstitial.fetch();
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
