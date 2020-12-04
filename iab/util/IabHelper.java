package com.luolai.droidrender.iab.util;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;
import com.android.vending.billing.IInAppBillingService;
import com.android.vending.billing.IInAppBillingService.Stub;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.json.JSONException;

public class IabHelper {
    public static final int BILLING_RESPONSE_RESULT_BILLING_UNAVAILABLE = 3;
    public static final int BILLING_RESPONSE_RESULT_DEVELOPER_ERROR = 5;
    public static final int BILLING_RESPONSE_RESULT_ERROR = 6;
    public static final int BILLING_RESPONSE_RESULT_ITEM_ALREADY_OWNED = 7;
    public static final int BILLING_RESPONSE_RESULT_ITEM_NOT_OWNED = 8;
    public static final int BILLING_RESPONSE_RESULT_ITEM_UNAVAILABLE = 4;
    public static final int BILLING_RESPONSE_RESULT_OK = 0;
    public static final int BILLING_RESPONSE_RESULT_USER_CANCELED = 1;
    public static final String GET_SKU_DETAILS_ITEM_LIST = "ITEM_ID_LIST";
    public static final String GET_SKU_DETAILS_ITEM_TYPE_LIST = "ITEM_TYPE_LIST";
    public static final int IABHELPER_BAD_RESPONSE = -1002;
    public static final int IABHELPER_ERROR_BASE = -1000;
    public static final int IABHELPER_INVALID_CONSUMPTION = -1010;
    public static final int IABHELPER_MISSING_TOKEN = -1007;
    public static final int IABHELPER_REMOTE_EXCEPTION = -1001;
    public static final int IABHELPER_SEND_INTENT_FAILED = -1004;
    public static final int IABHELPER_SUBSCRIPTIONS_NOT_AVAILABLE = -1009;
    public static final int IABHELPER_UNKNOWN_ERROR = -1008;
    public static final int IABHELPER_UNKNOWN_PURCHASE_RESPONSE = -1006;
    public static final int IABHELPER_USER_CANCELLED = -1005;
    public static final int IABHELPER_VERIFICATION_FAILED = -1003;
    public static final String INAPP_CONTINUATION_TOKEN = "INAPP_CONTINUATION_TOKEN";
    public static final String ITEM_TYPE_INAPP = "inapp";
    public static final String ITEM_TYPE_SUBS = "subs";
    public static final String RESPONSE_BUY_INTENT = "BUY_INTENT";
    public static final String RESPONSE_CODE = "RESPONSE_CODE";
    public static final String RESPONSE_GET_SKU_DETAILS_LIST = "DETAILS_LIST";
    public static final String RESPONSE_INAPP_ITEM_LIST = "INAPP_PURCHASE_ITEM_LIST";
    public static final String RESPONSE_INAPP_PURCHASE_DATA = "INAPP_PURCHASE_DATA";
    public static final String RESPONSE_INAPP_PURCHASE_DATA_LIST = "INAPP_PURCHASE_DATA_LIST";
    public static final String RESPONSE_INAPP_SIGNATURE = "INAPP_DATA_SIGNATURE";
    public static final String RESPONSE_INAPP_SIGNATURE_LIST = "INAPP_DATA_SIGNATURE_LIST";
    boolean mAsyncInProgress = false;
    String mAsyncOperation = "";
    Context mContext;
    boolean mDebugLog = false;
    String mDebugTag = "IabHelper";
    boolean mDisposed = false;
    OnIabPurchaseFinishedListener mPurchaseListener;
    String mPurchasingItemType;
    int mRequestCode;
    IInAppBillingService mService;
    ServiceConnection mServiceConn;
    boolean mSetupDone = false;
    String mSignatureBase64 = null;
    boolean mSubscriptionsSupported = false;

    public interface OnConsumeFinishedListener {
        void onConsumeFinished(Purchase purchase, IabResult iabResult);
    }

    public interface OnConsumeMultiFinishedListener {
        void onConsumeMultiFinished(List<Purchase> list, List<IabResult> list2);
    }

    public interface OnIabPurchaseFinishedListener {
        void onIabPurchaseFinished(IabResult iabResult, Purchase purchase);
    }

    public interface OnIabSetupFinishedListener {
        void onIabSetupFinished(IabResult iabResult);
    }

    public interface QueryInventoryFinishedListener {
        void onQueryInventoryFinished(IabResult iabResult, Inventory inventory);
    }

    public IabHelper(Context context, String str) {
        this.mContext = context.getApplicationContext();
        this.mSignatureBase64 = str;
        logDebug("IAB helper created.");
    }

    public static String getResponseDesc(int i) {
        String str = "/";
        String[] split = "0:OK/1:User Canceled/2:Unknown/3:Billing Unavailable/4:Item unavailable/5:Developer Error/6:Error/7:Item Already Owned/8:Item not owned".split(str);
        String[] split2 = "0:OK/-1001:Remote exception during initialization/-1002:Bad response received/-1003:Purchase signature verification failed/-1004:Send intent failed/-1005:User cancelled/-1006:Unknown purchase response/-1007:Missing token/-1008:Unknown error/-1009:Subscriptions not available/-1010:Invalid consumption attempt".split(str);
        if (i <= -1000) {
            int i2 = -1000 - i;
            if (i2 >= 0 && i2 < split2.length) {
                return split2[i2];
            }
            StringBuilder sb = new StringBuilder();
            sb.append(i);
            sb.append(":Unknown IAB Helper Error");
            return sb.toString();
        } else if (i >= 0 && i < split.length) {
            return split[i];
        } else {
            StringBuilder sb2 = new StringBuilder();
            sb2.append(i);
            sb2.append(":Unknown");
            return sb2.toString();
        }
    }

    public void enableDebugLogging(boolean z, String str) {
        checkNotDisposed();
        this.mDebugLog = z;
        this.mDebugTag = str;
    }

    public void enableDebugLogging(boolean z) {
        checkNotDisposed();
        this.mDebugLog = z;
    }

    public void startSetup(final OnIabSetupFinishedListener onIabSetupFinishedListener) {
        checkNotDisposed();
        if (!this.mSetupDone) {
            logDebug("Starting in-app billing setup.");
            this.mServiceConn = new ServiceConnection() {
                public void onServiceDisconnected(ComponentName componentName) {
                    IabHelper.this.logDebug("Billing service disconnected.");
                    IabHelper.this.mService = null;
                }

                public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                    if (!IabHelper.this.mDisposed) {
                        IabHelper.this.logDebug("Billing service connected.");
                        IabHelper.this.mService = Stub.asInterface(iBinder);
                        String packageName = IabHelper.this.mContext.getPackageName();
                        try {
                            IabHelper.this.logDebug("Checking for in-app billing 3 support.");
                            int isBillingSupported = IabHelper.this.mService.isBillingSupported(3, packageName, IabHelper.ITEM_TYPE_INAPP);
                            if (isBillingSupported != 0) {
                                if (onIabSetupFinishedListener != null) {
                                    onIabSetupFinishedListener.onIabSetupFinished(new IabResult(isBillingSupported, "Error checking for billing v3 support."));
                                }
                                IabHelper.this.mSubscriptionsSupported = false;
                                return;
                            }
                            IabHelper iabHelper = IabHelper.this;
                            StringBuilder sb = new StringBuilder();
                            sb.append("In-app billing version 3 supported for ");
                            sb.append(packageName);
                            iabHelper.logDebug(sb.toString());
                            int isBillingSupported2 = IabHelper.this.mService.isBillingSupported(3, packageName, IabHelper.ITEM_TYPE_SUBS);
                            if (isBillingSupported2 == 0) {
                                IabHelper.this.logDebug("Subscriptions AVAILABLE.");
                                IabHelper.this.mSubscriptionsSupported = true;
                            } else {
                                IabHelper iabHelper2 = IabHelper.this;
                                StringBuilder sb2 = new StringBuilder();
                                sb2.append("Subscriptions NOT AVAILABLE. Response: ");
                                sb2.append(isBillingSupported2);
                                iabHelper2.logDebug(sb2.toString());
                            }
                            IabHelper.this.mSetupDone = true;
                            OnIabSetupFinishedListener onIabSetupFinishedListener = onIabSetupFinishedListener;
                            if (onIabSetupFinishedListener != null) {
                                onIabSetupFinishedListener.onIabSetupFinished(new IabResult(0, "Setup successful."));
                            }
                        } catch (RemoteException e) {
                            OnIabSetupFinishedListener onIabSetupFinishedListener2 = onIabSetupFinishedListener;
                            if (onIabSetupFinishedListener2 != null) {
                                onIabSetupFinishedListener2.onIabSetupFinished(new IabResult(IabHelper.IABHELPER_REMOTE_EXCEPTION, "RemoteException while setting up in-app billing."));
                            }
                            e.printStackTrace();
                        }
                    }
                }
            };
            Intent intent = new Intent("com.android.vending.billing.InAppBillingService.BIND");
            intent.setPackage("com.android.vending");
            List queryIntentServices = this.mContext.getPackageManager().queryIntentServices(intent, 0);
            if (queryIntentServices != null && !queryIntentServices.isEmpty()) {
                this.mContext.bindService(intent, this.mServiceConn, 1);
            } else if (onIabSetupFinishedListener != null) {
                onIabSetupFinishedListener.onIabSetupFinished(new IabResult(3, "Billing service unavailable on device."));
            }
        } else {
            throw new IllegalStateException("IAB helper is already set up.");
        }
    }

    public void dispose() {
        logDebug("Disposing.");
        this.mSetupDone = false;
        if (this.mServiceConn != null) {
            logDebug("Unbinding from service.");
            try {
                if (this.mContext != null) {
                    this.mContext.unbindService(this.mServiceConn);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        this.mDisposed = true;
        this.mContext = null;
        this.mServiceConn = null;
        this.mService = null;
        this.mPurchaseListener = null;
    }

    private void checkNotDisposed() {
        if (this.mDisposed) {
            throw new IllegalStateException("IabHelper was disposed of, so it cannot be used.");
        }
    }

    public boolean subscriptionsSupported() {
        checkNotDisposed();
        return this.mSubscriptionsSupported;
    }

    public void launchPurchaseFlow(Activity activity, String str, int i, OnIabPurchaseFinishedListener onIabPurchaseFinishedListener) {
        launchPurchaseFlow(activity, str, i, onIabPurchaseFinishedListener, "");
    }

    public void launchPurchaseFlow(Activity activity, String str, int i, OnIabPurchaseFinishedListener onIabPurchaseFinishedListener, String str2) {
        launchPurchaseFlow(activity, str, ITEM_TYPE_INAPP, i, onIabPurchaseFinishedListener, str2);
    }

    public void launchSubscriptionPurchaseFlow(Activity activity, String str, int i, OnIabPurchaseFinishedListener onIabPurchaseFinishedListener) {
        launchSubscriptionPurchaseFlow(activity, str, i, onIabPurchaseFinishedListener, "");
    }

    public void launchSubscriptionPurchaseFlow(Activity activity, String str, int i, OnIabPurchaseFinishedListener onIabPurchaseFinishedListener, String str2) {
        launchPurchaseFlow(activity, str, ITEM_TYPE_SUBS, i, onIabPurchaseFinishedListener, str2);
    }

    public void launchPurchaseFlow(Activity activity, String str, String str2, int i, OnIabPurchaseFinishedListener onIabPurchaseFinishedListener, String str3) {
        checkNotDisposed();
        String str4 = "launchPurchaseFlow";
        checkSetupDone(str4);
        flagStartAsync(str4);
        if (!str2.equals(ITEM_TYPE_SUBS) || this.mSubscriptionsSupported) {
            try {
                StringBuilder sb = new StringBuilder();
                sb.append("Constructing buy intent for ");
                sb.append(str);
                sb.append(", item type: ");
                sb.append(str2);
                logDebug(sb.toString());
                Bundle buyIntent = this.mService.getBuyIntent(3, this.mContext.getPackageName(), str, str2, str3);
                int responseCodeFromBundle = getResponseCodeFromBundle(buyIntent);
                if (responseCodeFromBundle != 0) {
                    StringBuilder sb2 = new StringBuilder();
                    sb2.append("Unable to buy item, Error response: ");
                    sb2.append(getResponseDesc(responseCodeFromBundle));
                    logError(sb2.toString());
                    flagEndAsync();
                    IabResult iabResult = new IabResult(responseCodeFromBundle, "Unable to buy item");
                    if (onIabPurchaseFinishedListener != null) {
                        onIabPurchaseFinishedListener.onIabPurchaseFinished(iabResult, null);
                    }
                    return;
                }
                PendingIntent pendingIntent = (PendingIntent) buyIntent.getParcelable(RESPONSE_BUY_INTENT);
                StringBuilder sb3 = new StringBuilder();
                sb3.append("Launching buy intent for ");
                sb3.append(str);
                sb3.append(". Request code: ");
                sb3.append(i);
                logDebug(sb3.toString());
                this.mRequestCode = i;
                this.mPurchaseListener = onIabPurchaseFinishedListener;
                this.mPurchasingItemType = str2;
                activity.startIntentSenderForResult(pendingIntent.getIntentSender(), i, new Intent(), Integer.valueOf(0).intValue(), Integer.valueOf(0).intValue(), Integer.valueOf(0).intValue());
            } catch (SendIntentException e) {
                StringBuilder sb4 = new StringBuilder();
                sb4.append("SendIntentException while launching purchase flow for sku ");
                sb4.append(str);
                logError(sb4.toString());
                e.printStackTrace();
                flagEndAsync();
                IabResult iabResult2 = new IabResult(IABHELPER_SEND_INTENT_FAILED, "Failed to send intent.");
                if (onIabPurchaseFinishedListener != null) {
                    onIabPurchaseFinishedListener.onIabPurchaseFinished(iabResult2, null);
                }
            } catch (RemoteException e2) {
                StringBuilder sb5 = new StringBuilder();
                sb5.append("RemoteException while launching purchase flow for sku ");
                sb5.append(str);
                logError(sb5.toString());
                e2.printStackTrace();
                flagEndAsync();
                IabResult iabResult3 = new IabResult(IABHELPER_REMOTE_EXCEPTION, "Remote exception while starting purchase flow");
                if (onIabPurchaseFinishedListener != null) {
                    onIabPurchaseFinishedListener.onIabPurchaseFinished(iabResult3, null);
                }
            }
        } else {
            IabResult iabResult4 = new IabResult(IABHELPER_SUBSCRIPTIONS_NOT_AVAILABLE, "Subscriptions are not available.");
            flagEndAsync();
            if (onIabPurchaseFinishedListener != null) {
                onIabPurchaseFinishedListener.onIabPurchaseFinished(iabResult4, null);
            }
        }
    }

    public boolean handleActivityResult(int i, int i2, Intent intent) {
        if (i != this.mRequestCode) {
            return false;
        }
        checkNotDisposed();
        checkSetupDone("handleActivityResult");
        flagEndAsync();
        if (intent == null) {
            logError("Null data in IAB activity result.");
            IabResult iabResult = new IabResult(IABHELPER_BAD_RESPONSE, "Null data in IAB result");
            OnIabPurchaseFinishedListener onIabPurchaseFinishedListener = this.mPurchaseListener;
            if (onIabPurchaseFinishedListener != null) {
                onIabPurchaseFinishedListener.onIabPurchaseFinished(iabResult, null);
            }
            return true;
        }
        int responseCodeFromIntent = getResponseCodeFromIntent(intent);
        String stringExtra = intent.getStringExtra(RESPONSE_INAPP_PURCHASE_DATA);
        String stringExtra2 = intent.getStringExtra(RESPONSE_INAPP_SIGNATURE);
        if (i2 == -1 && responseCodeFromIntent == 0) {
            logDebug("Successful resultcode from purchase activity.");
            StringBuilder sb = new StringBuilder();
            sb.append("Purchase data: ");
            sb.append(stringExtra);
            logDebug(sb.toString());
            StringBuilder sb2 = new StringBuilder();
            sb2.append("Data signature: ");
            sb2.append(stringExtra2);
            logDebug(sb2.toString());
            StringBuilder sb3 = new StringBuilder();
            String str = "Extras: ";
            sb3.append(str);
            sb3.append(intent.getExtras());
            logDebug(sb3.toString());
            StringBuilder sb4 = new StringBuilder();
            sb4.append("Expected item type: ");
            sb4.append(this.mPurchasingItemType);
            logDebug(sb4.toString());
            if (stringExtra == null || stringExtra2 == null) {
                logError("BUG: either purchaseData or dataSignature is null.");
                StringBuilder sb5 = new StringBuilder();
                sb5.append(str);
                sb5.append(intent.getExtras().toString());
                logDebug(sb5.toString());
                IabResult iabResult2 = new IabResult(IABHELPER_UNKNOWN_ERROR, "IAB returned null purchaseData or dataSignature");
                OnIabPurchaseFinishedListener onIabPurchaseFinishedListener2 = this.mPurchaseListener;
                if (onIabPurchaseFinishedListener2 != null) {
                    onIabPurchaseFinishedListener2.onIabPurchaseFinished(iabResult2, null);
                }
                return true;
            }
            try {
                Purchase purchase = new Purchase(this.mPurchasingItemType, stringExtra, stringExtra2);
                String sku = purchase.getSku();
                if (!Security.verifyPurchase(this.mSignatureBase64, stringExtra, stringExtra2)) {
                    StringBuilder sb6 = new StringBuilder();
                    sb6.append("Purchase signature verification FAILED for sku ");
                    sb6.append(sku);
                    logError(sb6.toString());
                    StringBuilder sb7 = new StringBuilder();
                    sb7.append("Signature verification failed for sku ");
                    sb7.append(sku);
                    IabResult iabResult3 = new IabResult(IABHELPER_VERIFICATION_FAILED, sb7.toString());
                    if (this.mPurchaseListener != null) {
                        this.mPurchaseListener.onIabPurchaseFinished(iabResult3, purchase);
                    }
                    return true;
                }
                logDebug("Purchase signature successfully verified.");
                OnIabPurchaseFinishedListener onIabPurchaseFinishedListener3 = this.mPurchaseListener;
                if (onIabPurchaseFinishedListener3 != null) {
                    onIabPurchaseFinishedListener3.onIabPurchaseFinished(new IabResult(0, "Success"), purchase);
                }
            } catch (JSONException e) {
                String str2 = "Failed to parse purchase data.";
                logError(str2);
                e.printStackTrace();
                IabResult iabResult4 = new IabResult(IABHELPER_BAD_RESPONSE, str2);
                OnIabPurchaseFinishedListener onIabPurchaseFinishedListener4 = this.mPurchaseListener;
                if (onIabPurchaseFinishedListener4 != null) {
                    onIabPurchaseFinishedListener4.onIabPurchaseFinished(iabResult4, null);
                }
                return true;
            }
        } else if (i2 == -1) {
            StringBuilder sb8 = new StringBuilder();
            sb8.append("Result code was OK but in-app billing response was not OK: ");
            sb8.append(getResponseDesc(responseCodeFromIntent));
            logDebug(sb8.toString());
            if (this.mPurchaseListener != null) {
                this.mPurchaseListener.onIabPurchaseFinished(new IabResult(responseCodeFromIntent, "Problem purchashing item."), null);
            }
        } else if (i2 == 0) {
            StringBuilder sb9 = new StringBuilder();
            sb9.append("Purchase canceled - Response: ");
            sb9.append(getResponseDesc(responseCodeFromIntent));
            logDebug(sb9.toString());
            IabResult iabResult5 = new IabResult(IABHELPER_USER_CANCELLED, "User canceled.");
            OnIabPurchaseFinishedListener onIabPurchaseFinishedListener5 = this.mPurchaseListener;
            if (onIabPurchaseFinishedListener5 != null) {
                onIabPurchaseFinishedListener5.onIabPurchaseFinished(iabResult5, null);
            }
        } else {
            StringBuilder sb10 = new StringBuilder();
            sb10.append("Purchase failed. Result code: ");
            sb10.append(i2);
            sb10.append(". Response: ");
            sb10.append(getResponseDesc(responseCodeFromIntent));
            logError(sb10.toString());
            IabResult iabResult6 = new IabResult(IABHELPER_UNKNOWN_PURCHASE_RESPONSE, "Unknown purchase response.");
            OnIabPurchaseFinishedListener onIabPurchaseFinishedListener6 = this.mPurchaseListener;
            if (onIabPurchaseFinishedListener6 != null) {
                onIabPurchaseFinishedListener6.onIabPurchaseFinished(iabResult6, null);
            }
        }
        return true;
    }

    public Inventory queryInventory(boolean z, List<String> list) throws IabException {
        return queryInventory(z, list, null);
    }

    public Inventory queryInventory(boolean z, List<String> list, List<String> list2) throws IabException {
        String str = ITEM_TYPE_SUBS;
        String str2 = ITEM_TYPE_INAPP;
        checkNotDisposed();
        checkSetupDone("queryInventory");
        try {
            Inventory inventory = new Inventory();
            int queryPurchases = queryPurchases(inventory, str2);
            if (queryPurchases == 0) {
                if (z) {
                    int querySkuDetails = querySkuDetails(str2, inventory, list);
                    if (querySkuDetails != 0) {
                        throw new IabException(querySkuDetails, "Error refreshing inventory (querying prices of items).");
                    }
                }
                if (this.mSubscriptionsSupported) {
                    int queryPurchases2 = queryPurchases(inventory, str);
                    if (queryPurchases2 != 0) {
                        throw new IabException(queryPurchases2, "Error refreshing inventory (querying owned subscriptions).");
                    } else if (z) {
                        int querySkuDetails2 = querySkuDetails(str, inventory, list);
                        if (querySkuDetails2 != 0) {
                            throw new IabException(querySkuDetails2, "Error refreshing inventory (querying prices of subscriptions).");
                        }
                    }
                }
                return inventory;
            }
            throw new IabException(queryPurchases, "Error refreshing inventory (querying owned items).");
        } catch (RemoteException e) {
            throw new IabException(IABHELPER_REMOTE_EXCEPTION, "Remote exception while refreshing inventory.", e);
        } catch (JSONException e2) {
            throw new IabException(IABHELPER_BAD_RESPONSE, "Error parsing JSON response while refreshing inventory.", e2);
        }
    }

    public void queryInventoryAsync(boolean z, List<String> list, QueryInventoryFinishedListener queryInventoryFinishedListener) {
        final Handler handler = new Handler();
        checkNotDisposed();
        checkSetupDone("queryInventory");
        flagStartAsync("refresh inventory");
        final boolean z2 = z;
        final List<String> list2 = list;
        final QueryInventoryFinishedListener queryInventoryFinishedListener2 = queryInventoryFinishedListener;
        C03652 r0 = new Runnable() {
            public void run() {
                final Inventory inventory;
                final IabResult iabResult = new IabResult(0, "Inventory refresh successful.");
                try {
                    inventory = IabHelper.this.queryInventory(z2, list2);
                } catch (IabException e) {
                    iabResult = e.getResult();
                    inventory = null;
                }
                IabHelper.this.flagEndAsync();
                if (!IabHelper.this.mDisposed && queryInventoryFinishedListener2 != null) {
                    handler.post(new Runnable() {
                        public void run() {
                            queryInventoryFinishedListener2.onQueryInventoryFinished(iabResult, inventory);
                        }
                    });
                }
            }
        };
        new Thread(r0).start();
    }

    public void queryInventoryAsync(QueryInventoryFinishedListener queryInventoryFinishedListener) {
        queryInventoryAsync(true, null, queryInventoryFinishedListener);
    }

    public void queryInventoryAsync(boolean z, QueryInventoryFinishedListener queryInventoryFinishedListener) {
        queryInventoryAsync(z, null, queryInventoryFinishedListener);
    }

    /* access modifiers changed from: 0000 */
    public void consume(Purchase purchase) throws IabException {
        checkNotDisposed();
        checkSetupDone("consume");
        if (purchase.mItemType.equals(ITEM_TYPE_INAPP)) {
            try {
                String token = purchase.getToken();
                String sku = purchase.getSku();
                if (token == null || token.equals("")) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("Can't consume ");
                    sb.append(sku);
                    sb.append(". No token.");
                    logError(sb.toString());
                    StringBuilder sb2 = new StringBuilder();
                    sb2.append("PurchaseInfo is missing token for sku: ");
                    sb2.append(sku);
                    sb2.append(" ");
                    sb2.append(purchase);
                    throw new IabException((int) IABHELPER_MISSING_TOKEN, sb2.toString());
                }
                StringBuilder sb3 = new StringBuilder();
                sb3.append("Consuming sku: ");
                sb3.append(sku);
                sb3.append(", token: ");
                sb3.append(token);
                logDebug(sb3.toString());
                int consumePurchase = this.mService.consumePurchase(3, this.mContext.getPackageName(), token);
                if (consumePurchase == 0) {
                    StringBuilder sb4 = new StringBuilder();
                    sb4.append("Successfully consumed sku: ");
                    sb4.append(sku);
                    logDebug(sb4.toString());
                    return;
                }
                StringBuilder sb5 = new StringBuilder();
                sb5.append("Error consuming consuming sku ");
                sb5.append(sku);
                sb5.append(". ");
                sb5.append(getResponseDesc(consumePurchase));
                logDebug(sb5.toString());
                StringBuilder sb6 = new StringBuilder();
                sb6.append("Error consuming sku ");
                sb6.append(sku);
                throw new IabException(consumePurchase, sb6.toString());
            } catch (RemoteException e) {
                StringBuilder sb7 = new StringBuilder();
                sb7.append("Remote exception while consuming. PurchaseInfo: ");
                sb7.append(purchase);
                throw new IabException(IABHELPER_REMOTE_EXCEPTION, sb7.toString(), e);
            }
        } else {
            StringBuilder sb8 = new StringBuilder();
            sb8.append("Items of type '");
            sb8.append(purchase.mItemType);
            sb8.append("' can't be consumed.");
            throw new IabException((int) IABHELPER_INVALID_CONSUMPTION, sb8.toString());
        }
    }

    public void consumeAsync(Purchase purchase, OnConsumeFinishedListener onConsumeFinishedListener) {
        checkNotDisposed();
        checkSetupDone("consume");
        ArrayList arrayList = new ArrayList();
        arrayList.add(purchase);
        consumeAsyncInternal(arrayList, onConsumeFinishedListener, null);
    }

    public void consumeAsync(List<Purchase> list, OnConsumeMultiFinishedListener onConsumeMultiFinishedListener) {
        checkNotDisposed();
        checkSetupDone("consume");
        consumeAsyncInternal(list, null, onConsumeMultiFinishedListener);
    }

    /* access modifiers changed from: 0000 */
    public void checkSetupDone(String str) {
        if (!this.mSetupDone) {
            StringBuilder sb = new StringBuilder();
            sb.append("Illegal state for operation (");
            sb.append(str);
            sb.append("): IAB helper is not set up.");
            logError(sb.toString());
            StringBuilder sb2 = new StringBuilder();
            sb2.append("IAB helper is not set up. Can't perform operation: ");
            sb2.append(str);
            throw new IllegalStateException(sb2.toString());
        }
    }

    /* access modifiers changed from: 0000 */
    public int getResponseCodeFromBundle(Bundle bundle) {
        Object obj = bundle.get(RESPONSE_CODE);
        if (obj == null) {
            logDebug("Bundle with null response code, assuming OK (known issue)");
            return 0;
        } else if (obj instanceof Integer) {
            return ((Integer) obj).intValue();
        } else {
            if (obj instanceof Long) {
                return (int) ((Long) obj).longValue();
            }
            logError("Unexpected type for bundle response code.");
            logError(obj.getClass().getName());
            StringBuilder sb = new StringBuilder();
            sb.append("Unexpected type for bundle response code: ");
            sb.append(obj.getClass().getName());
            throw new RuntimeException(sb.toString());
        }
    }

    /* access modifiers changed from: 0000 */
    public int getResponseCodeFromIntent(Intent intent) {
        Object obj = intent.getExtras().get(RESPONSE_CODE);
        if (obj == null) {
            logError("Intent with no response code, assuming OK (known issue)");
            return 0;
        } else if (obj instanceof Integer) {
            return ((Integer) obj).intValue();
        } else {
            if (obj instanceof Long) {
                return (int) ((Long) obj).longValue();
            }
            logError("Unexpected type for intent response code.");
            logError(obj.getClass().getName());
            StringBuilder sb = new StringBuilder();
            sb.append("Unexpected type for intent response code: ");
            sb.append(obj.getClass().getName());
            throw new RuntimeException(sb.toString());
        }
    }

    /* access modifiers changed from: 0000 */
    public void flagStartAsync(String str) {
        if (!this.mAsyncInProgress) {
            this.mAsyncOperation = str;
            this.mAsyncInProgress = true;
            StringBuilder sb = new StringBuilder();
            sb.append("Starting async operation: ");
            sb.append(str);
            logDebug(sb.toString());
            return;
        }
        StringBuilder sb2 = new StringBuilder();
        sb2.append("Can't start async operation (");
        sb2.append(str);
        sb2.append(") because another async operation(");
        sb2.append(this.mAsyncOperation);
        sb2.append(") is in progress.");
        throw new IllegalStateException(sb2.toString());
    }

    /* access modifiers changed from: 0000 */
    public void flagEndAsync() {
        StringBuilder sb = new StringBuilder();
        sb.append("Ending async operation: ");
        sb.append(this.mAsyncOperation);
        logDebug(sb.toString());
        this.mAsyncOperation = "";
        this.mAsyncInProgress = false;
    }

    /* access modifiers changed from: 0000 */
    public int queryPurchases(Inventory inventory, String str) throws JSONException, RemoteException {
        StringBuilder sb = new StringBuilder();
        sb.append("Querying owned items, item type: ");
        sb.append(str);
        logDebug(sb.toString());
        StringBuilder sb2 = new StringBuilder();
        sb2.append("Package name: ");
        sb2.append(this.mContext.getPackageName());
        logDebug(sb2.toString());
        int i = 0;
        String str2 = null;
        boolean z = false;
        while (true) {
            StringBuilder sb3 = new StringBuilder();
            sb3.append("Calling getPurchases with continuation token: ");
            sb3.append(str2);
            logDebug(sb3.toString());
            Bundle purchases = this.mService.getPurchases(3, this.mContext.getPackageName(), str, str2);
            int responseCodeFromBundle = getResponseCodeFromBundle(purchases);
            StringBuilder sb4 = new StringBuilder();
            sb4.append("Owned items response: ");
            sb4.append(responseCodeFromBundle);
            logDebug(sb4.toString());
            if (responseCodeFromBundle == 0) {
                String str3 = RESPONSE_INAPP_ITEM_LIST;
                if (!purchases.containsKey(str3)) {
                    break;
                }
                String str4 = RESPONSE_INAPP_PURCHASE_DATA_LIST;
                if (!purchases.containsKey(str4)) {
                    break;
                }
                String str5 = RESPONSE_INAPP_SIGNATURE_LIST;
                if (!purchases.containsKey(str5)) {
                    break;
                }
                ArrayList stringArrayList = purchases.getStringArrayList(str3);
                ArrayList stringArrayList2 = purchases.getStringArrayList(str4);
                ArrayList stringArrayList3 = purchases.getStringArrayList(str5);
                boolean z2 = z;
                for (int i2 = 0; i2 < stringArrayList2.size(); i2++) {
                    String str6 = (String) stringArrayList2.get(i2);
                    String str7 = (String) stringArrayList3.get(i2);
                    String str8 = (String) stringArrayList.get(i2);
                    if (Security.verifyPurchase(this.mSignatureBase64, str6, str7)) {
                        StringBuilder sb5 = new StringBuilder();
                        sb5.append("Sku is owned: ");
                        sb5.append(str8);
                        logDebug(sb5.toString());
                        Purchase purchase = new Purchase(str, str6, str7);
                        if (TextUtils.isEmpty(purchase.getToken())) {
                            logWarn("BUG: empty/null token!");
                            StringBuilder sb6 = new StringBuilder();
                            sb6.append("Purchase data: ");
                            sb6.append(str6);
                            logDebug(sb6.toString());
                        }
                        inventory.addPurchase(purchase);
                    } else {
                        logWarn("Purchase signature verification **FAILED**. Not adding item.");
                        StringBuilder sb7 = new StringBuilder();
                        sb7.append("   Purchase data: ");
                        sb7.append(str6);
                        logDebug(sb7.toString());
                        StringBuilder sb8 = new StringBuilder();
                        sb8.append("   Signature: ");
                        sb8.append(str7);
                        logDebug(sb8.toString());
                        z2 = true;
                    }
                }
                str2 = purchases.getString(INAPP_CONTINUATION_TOKEN);
                StringBuilder sb9 = new StringBuilder();
                sb9.append("Continuation token: ");
                sb9.append(str2);
                logDebug(sb9.toString());
                if (TextUtils.isEmpty(str2)) {
                    if (z2) {
                        i = IABHELPER_VERIFICATION_FAILED;
                    }
                    return i;
                }
                z = z2;
            } else {
                StringBuilder sb10 = new StringBuilder();
                sb10.append("getPurchases() failed: ");
                sb10.append(getResponseDesc(responseCodeFromBundle));
                logDebug(sb10.toString());
                return responseCodeFromBundle;
            }
        }
        logError("Bundle returned from getPurchases() doesn't contain required fields.");
        return IABHELPER_BAD_RESPONSE;
    }

    /* access modifiers changed from: 0000 */
    public int querySkuDetails(String str, Inventory inventory, List<String> list) throws RemoteException, JSONException {
        logDebug("Querying SKU details.");
        ArrayList arrayList = new ArrayList();
        arrayList.addAll(inventory.getAllOwnedSkus(str));
        if (list != null) {
            for (String str2 : list) {
                if (!arrayList.contains(str2)) {
                    arrayList.add(str2);
                }
            }
        }
        if (arrayList.size() == 0) {
            logDebug("queryPrices: nothing to do because there are no SKUs.");
            return 0;
        }
        Bundle bundle = new Bundle();
        bundle.putStringArrayList(GET_SKU_DETAILS_ITEM_LIST, arrayList);
        Bundle skuDetails = this.mService.getSkuDetails(3, this.mContext.getPackageName(), str, bundle);
        String str3 = RESPONSE_GET_SKU_DETAILS_LIST;
        if (!skuDetails.containsKey(str3)) {
            int responseCodeFromBundle = getResponseCodeFromBundle(skuDetails);
            if (responseCodeFromBundle != 0) {
                StringBuilder sb = new StringBuilder();
                sb.append("getSkuDetails() failed: ");
                sb.append(getResponseDesc(responseCodeFromBundle));
                logDebug(sb.toString());
                return responseCodeFromBundle;
            }
            logError("getSkuDetails() returned a bundle with neither an error nor a detail list.");
            return IABHELPER_BAD_RESPONSE;
        }
        Iterator it = skuDetails.getStringArrayList(str3).iterator();
        while (it.hasNext()) {
            SkuDetails skuDetails2 = new SkuDetails(str, (String) it.next());
            StringBuilder sb2 = new StringBuilder();
            sb2.append("Got sku details: ");
            sb2.append(skuDetails2);
            logDebug(sb2.toString());
            inventory.addSkuDetails(skuDetails2);
        }
        return 0;
    }

    /* access modifiers changed from: 0000 */
    public void consumeAsyncInternal(List<Purchase> list, OnConsumeFinishedListener onConsumeFinishedListener, OnConsumeMultiFinishedListener onConsumeMultiFinishedListener) {
        final Handler handler = new Handler();
        flagStartAsync("consume");
        final List<Purchase> list2 = list;
        final OnConsumeFinishedListener onConsumeFinishedListener2 = onConsumeFinishedListener;
        final OnConsumeMultiFinishedListener onConsumeMultiFinishedListener2 = onConsumeMultiFinishedListener;
        C03673 r0 = new Runnable() {
            public void run() {
                final ArrayList arrayList = new ArrayList();
                for (Purchase purchase : list2) {
                    try {
                        IabHelper.this.consume(purchase);
                        StringBuilder sb = new StringBuilder();
                        sb.append("Successful consume of sku ");
                        sb.append(purchase.getSku());
                        arrayList.add(new IabResult(0, sb.toString()));
                    } catch (IabException e) {
                        arrayList.add(e.getResult());
                    }
                }
                IabHelper.this.flagEndAsync();
                if (!IabHelper.this.mDisposed && onConsumeFinishedListener2 != null) {
                    handler.post(new Runnable() {
                        public void run() {
                            onConsumeFinishedListener2.onConsumeFinished((Purchase) list2.get(0), (IabResult) arrayList.get(0));
                        }
                    });
                }
                if (!IabHelper.this.mDisposed && onConsumeMultiFinishedListener2 != null) {
                    handler.post(new Runnable() {
                        public void run() {
                            onConsumeMultiFinishedListener2.onConsumeMultiFinished(list2, arrayList);
                        }
                    });
                }
            }
        };
        new Thread(r0).start();
    }

    /* access modifiers changed from: 0000 */
    public void logDebug(String str) {
        if (this.mDebugLog) {
            Log.d(this.mDebugTag, str);
        }
    }

    /* access modifiers changed from: 0000 */
    public void logError(String str) {
        String str2 = this.mDebugTag;
        StringBuilder sb = new StringBuilder();
        sb.append("In-app billing error: ");
        sb.append(str);
        Log.e(str2, sb.toString());
    }

    /* access modifiers changed from: 0000 */
    public void logWarn(String str) {
        String str2 = this.mDebugTag;
        StringBuilder sb = new StringBuilder();
        sb.append("In-app billing warning: ");
        sb.append(str);
        Log.w(str2, sb.toString());
    }
}
