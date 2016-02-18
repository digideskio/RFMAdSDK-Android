/*
 * Copyright (c) 2016. Rubicon Project. All rights reserved
 *
 */

package com.rfm.sdk.mediator;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.inmobi.commons.InMobi;
import com.inmobi.monetization.IMBanner;
import com.inmobi.monetization.IMBannerListener;
import com.inmobi.monetization.IMErrorCode;
import com.rfm.sdk.ui.mediator.RFMCustomBannerListener;
import com.rfm.sdk.ui.mediator.RFMCustomBanner;

import java.util.Map;
import java.util.Set;

public class RFMMediatorInMobiBanner implements RFMCustomBanner {
    Context mContext;
    private IMBanner bannerAd;
    RFMCustomBannerListener mListener;
    private static final String LOG_TAG = "RFMMediatorInMobiBanner";
    private static final String PARAM_AD_ID = "appId";

    /**
     * Implementation for requesting Banner Ad from InMobi via RFM Custom event
     *
     * @param context
     * @param params , will have appId, width, height and other parameters specified for Custom Event
     * @param listener
     */
   @Override
    public void requestAd(Context context, Map<String, String> params, RFMCustomBannerListener listener) {
        Log.v("RFMMediatorInMobiBanner", " Requesting Ad from RFMMediatorInMobiBanner ");
        mContext = context;
        mListener = listener;
       if(createBannerAd(params)) {
           createAdListener();
           loadAd();
       } else {
           if(mListener != null) {
               mListener.onAdFailed("Failed to request Inmobi Banner Ad, app Id missing");
           }
       }
   }

    /**
     * Called when Ad / UI containing Ad is closed
     * Method should include all the clean up code
     */
    @Override
    public void reset() {
        if(bannerAd != null) {
            bannerAd.setIMBannerListener(null);
            bannerAd.destroy();
            bannerAd.stopLoading();
            mContext=null;
            Log.v(LOG_TAG, "Clean up Inmobi Banner");
        }
    }

    /**
     * Method to do the needful for displaying Ad
     *
     * @return
     */
    @Override
    public boolean display() {
        if(bannerAd != null) {
            if(bannerAd.isShown()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Utility method to create Banner Ad
     *
     * @param adParams
     * @return true for success / false for failure
     */
    protected boolean createBannerAd(Map<String, String> adParams) {
        printAdParams(adParams);
        String appId=null;
        if(adParams != null) {
            appId = adParams.get(PARAM_AD_ID);
        }

        Log.v(LOG_TAG, "Requesting Banner Ad from InMobi with appId" + appId);
        if(appId == null) {
            return false;
        }


        InMobi.initialize(mContext, appId);
        bannerAd = new IMBanner((Activity)mContext, 0);
        bannerAd.setAppId(appId);
        bannerAd.setAdSize(getAdSize(adParams));

        return true;
    }

    /**
     * Utility method to add call back listener
     */
    protected void createAdListener() {
        bannerAd.setIMBannerListener(new IMBannerListener() {
            @Override
            public void onBannerInteraction(IMBanner arg0, Map<String, String> arg1) {
                Log.v(LOG_TAG, "InMobiMediatior banner interaction ");
                if (mListener != null) {
                    mListener.onAdClicked();
                }
            }

            @Override
            public void onBannerRequestFailed(IMBanner arg0, IMErrorCode eCode) {
                Log.v(LOG_TAG, "InMobiMediatior banner request Failed ");
                if (mListener != null) {
                    mListener.onAdFailed("Error code " + eCode.name());
                }
            }

            @Override
            public void onBannerRequestSucceeded(IMBanner arg0) {
                Log.v(LOG_TAG, "InMobiMediatior banner request Succeeded ");
                if (mListener != null) {
                    mListener.onAdLoaded(bannerAd);
                }
            }

            @Override
            public void onDismissBannerScreen(IMBanner arg0) {
                Log.v(LOG_TAG, "InMobiMediatior banner dismissed");
                if (mListener != null) {
                    mListener.onAdCollapsed();
                }
            }

            @Override
            public void onLeaveApplication(IMBanner arg0) {
                Log.v(LOG_TAG, "InMobiMediatior banner onLeaveApplication ");
            }

            @Override
            public void onShowBannerScreen(IMBanner arg0) {
                Log.v(LOG_TAG, "InMobiMediatior banner displayed ");
                if (mListener != null) {
                    //
                }
            }
        });
    }

    /**
     * Utility method to request Ad
     */
    protected void loadAd() {
        bannerAd.loadBanner();
    }

    /**
     * Utility method to set width and height of Ad
     *
     * @param params, RFM SDK will always include 'width' and 'height' in params
     *                width x height specified on RFMAdView will be available here
     * @return Ad Size value
     */
    private int getAdSize(Map<String, String> params){
        float width=320;
        float height=50;

        if(params !=null) {
            try {
                if (params.containsKey(PARAM_WIDTH)) {
                    width = Float.parseFloat(params.get(PARAM_WIDTH));
                }
                if (params.containsKey(PARAM_HEIGHT)) {
                    height = Float.parseFloat(params.get(PARAM_HEIGHT));
                }
            } catch (Exception e) {
                e.printStackTrace();
                Log.v(LOG_TAG, "Failed to get valid size params from RFM SDK, requesting ad with default banner size 320 x 50");
                width = 320;
                height=50;
            }
        }

        if(width == 120 && height == 600){
            return IMBanner.INMOBI_AD_UNIT_120X600;
        }
        if(width == 300 && height == 250){
            return IMBanner.INMOBI_AD_UNIT_300X250;
        }
        if(width == 468 && height == 60){
            return IMBanner.INMOBI_AD_UNIT_468X60;
        }
        if(width == 728 && height == 90){
            return IMBanner.INMOBI_AD_UNIT_728X90;
        }
        return 15;
    }

    /**
     * Utility method to print all the parameters sent from RFM SDK
     * @param params
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
