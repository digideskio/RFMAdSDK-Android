/*
 * Copyright (c) 2016. Rubicon Project. All rights reserved
 *
 */

package com.rfm.sdk.mediator;

import android.content.Context;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.widget.LinearLayout;

import com.millennialmedia.android.MMAd;
import com.millennialmedia.android.MMAdView;
import com.millennialmedia.android.MMException;
import com.millennialmedia.android.MMRequest;
import com.millennialmedia.android.MMSDK;
import com.millennialmedia.android.RequestListener;
import com.rfm.sdk.ui.mediator.RFMCustomBannerListener;
import com.rfm.sdk.ui.mediator.RFMCustomBanner;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class RFMMediatorMiMBanner implements RFMCustomBanner {
    private Context mContext;
    RFMCustomBannerListener mListener;
    private static final int IAB_LEADERBOARD_WIDTH = 728;
    private static final int IAB_LEADERBOARD_HEIGHT = 90;

    private static final int MED_BANNER_WIDTH = 480;
    private static final int MED_BANNER_HEIGHT = 60;
    private MMAdView mAdView;
    protected static final String LOG_TAG = "MiMBannerMediator";
    protected static final String PARAM_AD_ID = "apId";
    protected static final String PARAM_METADATA = "metadata";

    /**
     * Implementation for requesting Banner Ad from Millennial SDK via RFM Custom event
     *
     * @param context
     * @param params , will have apId, width, height and other parameters specified for Custom Event
     * @param listener
     */
    @Override
    public void requestAd(Context context, Map<String, String> params, RFMCustomBannerListener listener) {
        Log.v(LOG_TAG, "Requesting Ad from MIMiMediator ");
        mContext = context;
        mListener = listener;

        if(createBannerAd(params)) {
            createAdListener();
            loadAd();
        } else {
            if(mListener != null) {
                mListener.onAdFailed("Failed to request MIM Banner Ad, app Id missing");
            }
        }
    }

    /**
     * Called when Ad / UI containing Ad is closed
     * Method should include all the clean up code
     */
    @Override
    public void reset() {
        if(mAdView != null) {
            mAdView.destroyDrawingCache();
            mAdView = null;
            mContext=null;
            Log.v(LOG_TAG, "Clean up MIM Banner");
        }
    }

    /**
     * Method to do the needful for displaying Ad
     *
     * @return
     */
    @Override
    public boolean display() {
        if(mAdView != null) {
            if(mAdView.isShown()) {
                Log.v(LOG_TAG, " MIM - Mobi ad shown.");
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
    private boolean createBannerAd(Map<String, String> adParams) {
        printAdParams(adParams);
        String apId = null;
        if(adParams != null) {
            apId = adParams.get(PARAM_AD_ID);

            if(apId == null) {
                return false;
            }
     }

        /******** Millennial Media Ad View Integration ********/
        // Create the mAdView
        mAdView = new MMAdView(mContext);
        // Set your apid
        mAdView.setApid(apId);

        // (Highly Recommended) Set the id to preserve your ad on configuration changes. Save Battery!
        // Each MMAdView you give requires a unique id.
       // int defaultAdId = MMSDK.getDefaultAdId();
       // Log.v(LOG_TAG, " Setting ApId = "+defaultAdId);
       // mAdView.setId(defaultAdId);
        setAdSize(adParams);

        // (Optional/Recommended) Set meta data (will be applied to subsequent ad requests)
        Map<String, String> metaData = createMetaData();
        Log.v(LOG_TAG, " Requesting Ad from Millennial Media with ApId = " + apId);
        MMRequest request = new MMRequest();

        request.setMetaValues(metaData);

        mAdView.setMMRequest(request);

        return true;
    }

    /**
     * Utility method to add call back listener
     */
    protected void createAdListener() {
        // (Optional) Set the mListener to receive events about the adview
        mAdView.setListener(new RequestListener() {

            @Override
            public void MMAdOverlayLaunched(MMAd mmAd) {
                Log.i(MMSDK.SDKLOG, "Millennial Media Ad (" + mmAd.getApid() + ") overlay launched");
                if (mListener != null) {
                    mListener.onAdExpanded();
                }
            }

            @Override
            public void MMAdOverlayClosed(MMAd mmAd) {
                Log.i(MMSDK.SDKLOG, "Millennial Media Ad (" + mmAd.getApid() + ") overlay closed");
                if (mListener != null) {
                    mListener.onAdCollapsed();
                }
            }

            @Override
            public void MMAdRequestIsCaching(MMAd mmAd) {
                Log.i(MMSDK.SDKLOG, "Millennial Media Ad (" + mmAd.getApid() + ") caching started");
            }

            @Override
            public void requestCompleted(MMAd mmAd) {
                Log.i(MMSDK.SDKLOG, "Millennial Media Ad (" + mmAd.getApid() + ") request completed & succeeded");
                if (mListener != null) {
                    mListener.onAdLoaded(mAdView);
                }
            }

            @Override
            public void requestFailed(MMAd mmAd, MMException e) {
                Log.i(MMSDK.SDKLOG, String.format("Millennial Media Ad (" + mmAd.getApid() + ") request failed with error: %d %s.", e.getCode(), e.getMessage()));
                if (mListener != null) {
                    mListener.onAdFailed("Error code " + e.toString());
                }
            }

            @Override
            public void onSingleTap(MMAd mmAd) {
                Log.i(MMSDK.SDKLOG, "Millennial Media Ad (" + mmAd.getApid() + ") single tap");
                if (mListener != null) {
                    mListener.onAdClicked();
                }
            }
        });
    }

    /**
     * Utility method to request Ad
     */
    protected void loadAd() {
        mAdView.getAd();
    }

    /**
     * Utility method to set width and height of Ad
     *
     * @param params, RFM SDK will always include 'width' and 'height' in params
     *                width x height specified on RFMAdView will be available here
     */
    private void setAdSize(Map<String, String> params){
        float width = 320;
        float height = 50;
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


        // (Optional) Set the ad size
//        if(canFit(IAB_LEADERBOARD_WIDTH))
//        {
//            width = IAB_LEADERBOARD_WIDTH;
//            height = IAB_LEADERBOARD_HEIGHT;
//        }
//        else if(canFit(MED_BANNER_WIDTH))
//        {
//            width = MED_BANNER_WIDTH;
//            height = MED_BANNER_HEIGHT;
//        }

        // (Optional) Set the AdView size based on the placement size. You could use WRAP_CONTENT and not specify the placement size
        try {
            int layoutWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, width, mContext.getResources().getDisplayMetrics());
            int layoutHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, height, mContext.getResources().getDisplayMetrics());
            LinearLayout.LayoutParams lParams = new LinearLayout.LayoutParams(layoutWidth, layoutHeight);
            mAdView.setLayoutParams(lParams);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Determine if the requested adWidth can fit on the screen.
    protected boolean canFit(int adWidth)
    {
        int adWidthPx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, adWidth, mContext.getResources().getDisplayMetrics());
        DisplayMetrics metrics = mContext.getResources().getDisplayMetrics();
        return metrics.widthPixels >= adWidthPx;
    }

    protected Map<String, String> createMetaData()
    {
        Map<String, String> metaData = new HashMap<>();
        metaData.put(MMRequest.KEY_AGE, "45");
        metaData.put(MMRequest.KEY_GENDER, MMRequest.GENDER_MALE);
        metaData.put(MMRequest.KEY_ZIP_CODE, "21224");
        metaData.put(MMRequest.KEY_MARITAL_STATUS, MMRequest.MARITAL_SINGLE);
        metaData.put(MMRequest.KEY_ETHNICITY, MMRequest.ETHNICITY_HISPANIC);
        metaData.put(MMRequest.KEY_INCOME, "50000");
        metaData.put(MMRequest.KEY_CHILDREN, "yes");
        metaData.put(MMRequest.KEY_POLITICS, "other");
        metaData.put(MMRequest.KEY_KEYWORDS, "soccer");
        return metaData;
    }

    /**
     * Utility method to print parameters sent from RFM SDK
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
