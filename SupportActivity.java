package com.luolai.droidrender;

import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.luolai.base.GAHelper;
import com.luolai.droidrender.iab.util.IabHelper;
import com.luolai.droidrender.iab.util.IabHelper.OnConsumeFinishedListener;
import com.luolai.droidrender.iab.util.IabHelper.OnIabPurchaseFinishedListener;
import com.luolai.droidrender.iab.util.IabHelper.OnIabSetupFinishedListener;
import com.luolai.droidrender.iab.util.IabHelper.QueryInventoryFinishedListener;
import com.luolai.droidrender.iab.util.IabResult;
import com.luolai.droidrender.iab.util.Inventory;
import com.luolai.droidrender.iab.util.Purchase;

public class SupportActivity extends BaseActivity implements ConnectionCallbacks, OnConnectionFailedListener {
    private static final String DEVELOPER_PAYLOAD = "DroidRenderDeveloperPayload";
    private static final String DIALOG_ERROR = "dialog_error";
    static final int RC_REQUEST = 10001;
    private static final int REQUEST_RESOLVE_ERROR = 1001;
    public static final String SKU_NOADS = "noads";
    public static final String SKU_PREMIUM = "premium";
    public static final String SKU_SUPPORT = "support";
    private static final String TAG = "SupportActivity";
    public static final String base64EncodedPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAtGiuhBDW62U3I9N04117SRt4I0Re5lQwSnrgI8kGmVkkfgX0oacmWkzEiSAGeye+RDnZVen6WUJm9tfrJM4CVo+rMwDvWTC+YliVoufAlsBOwoCzmOygyQNB5fLhCmWqJ/c8BmNLNYIimFjdWm1WLKdjl6z/aJ1ZOWEzBhYUsULO2JSRLH1JXTHI2Os0tvBECtNIiHATTTfT6lBV2/eJsfiN/Y3s07t8JjfiI6oBwa+0jAsyFbpmTJYW0Px+58cVTMEZin6+NRiWaJLAQhvpYWzbNvcFc/XTSaEWn3cfnHWB6AQgnPCJEvL1ZeGRhO5G+ILgJ+mKyWoxQInY4M5mtwIDAQAB";
    OnConsumeFinishedListener mConsumeFinishedListener = new OnConsumeFinishedListener() {
        public void onConsumeFinished(Purchase purchase, IabResult iabResult) {
            StringBuilder sb = new StringBuilder();
            sb.append("Consumption finished. Purchase: ");
            sb.append(purchase);
            sb.append(", result: ");
            sb.append(iabResult);
            String sb2 = sb.toString();
            String str = SupportActivity.TAG;
            Log.d(str, sb2);
            if (SupportActivity.this.mHelper != null) {
                if (iabResult.isSuccess()) {
                    SupportActivity supportActivity = SupportActivity.this;
                    Toast.makeText(supportActivity, supportActivity.getString(C0354R.string.support_completed), 0).show();
                }
                SupportActivity.this.updateUi();
                SupportActivity.this.setWaitScreen(false);
                Log.d(str, "End consumption flow.");
            }
        }
    };
    QueryInventoryFinishedListener mGotInventoryListener = new QueryInventoryFinishedListener() {
        public void onQueryInventoryFinished(IabResult iabResult, Inventory inventory) {
            String str = SupportActivity.TAG;
            Log.d(str, "Query inventory finished.");
            if (SupportActivity.this.mHelper != null) {
                boolean z = true;
                if (iabResult.isFailure()) {
                    SupportActivity supportActivity = SupportActivity.this;
                    supportActivity.complain(supportActivity.getString(C0354R.string.support_query_fail, new Object[]{iabResult}));
                    return;
                }
                Log.d(str, "Query inventory was successful.");
                Editor edit = SupportActivity.this.getSharedPreferences(Constants.PREF_NAME_SETTINGS, 0).edit();
                Purchase purchase = inventory.getPurchase(SupportActivity.SKU_PREMIUM);
                SupportActivity.this.mIsPremium = purchase != null && SupportActivity.verifyDeveloperPayload(purchase);
                edit.putBoolean(Constants.PREF_IS_VIP, SupportActivity.this.mIsPremium);
                StringBuilder sb = new StringBuilder();
                String str2 = "User is ";
                sb.append(str2);
                sb.append(SupportActivity.this.mIsPremium ? "PREMIUM" : "NOT PREMIUM");
                Log.d(str, sb.toString());
                Purchase purchase2 = inventory.getPurchase(SupportActivity.SKU_NOADS);
                SupportActivity supportActivity2 = SupportActivity.this;
                if (purchase2 == null || !SupportActivity.verifyDeveloperPayload(purchase2)) {
                    z = false;
                }
                supportActivity2.mIsNoADS = z;
                edit.putBoolean(Constants.PREF_IS_NOADS, SupportActivity.this.mIsNoADS);
                StringBuilder sb2 = new StringBuilder();
                sb2.append(str2);
                sb2.append(SupportActivity.this.mIsNoADS ? "NoADS" : "NOT NoADS");
                Log.d(str, sb2.toString());
                edit.apply();
                String str3 = SupportActivity.SKU_SUPPORT;
                Purchase purchase3 = inventory.getPurchase(str3);
                if (purchase3 == null || !SupportActivity.verifyDeveloperPayload(purchase3)) {
                    SupportActivity.this.updateUi();
                    SupportActivity.this.setWaitScreen(false);
                    Log.d(str, "Initial inventory query finished; enabling main UI.");
                    return;
                }
                SupportActivity.this.mHelper.consumeAsync(inventory.getPurchase(str3), SupportActivity.this.mConsumeFinishedListener);
            }
        }
    };
    IabHelper mHelper;
    boolean mIsNoADS = false;
    boolean mIsPremium = false;
    ProgressDialog mProgressDialog = null;
    OnIabPurchaseFinishedListener mPurchaseFinishedListener = new OnIabPurchaseFinishedListener() {
        public void onIabPurchaseFinished(IabResult iabResult, Purchase purchase) {
            StringBuilder sb = new StringBuilder();
            sb.append("Purchase finished: ");
            sb.append(iabResult);
            sb.append(", purchase: ");
            sb.append(purchase);
            String sb2 = sb.toString();
            String str = SupportActivity.TAG;
            Log.d(str, sb2);
            if (SupportActivity.this.mHelper != null) {
                if (iabResult.isFailure()) {
                    SupportActivity supportActivity = SupportActivity.this;
                    supportActivity.complain(supportActivity.getString(C0354R.string.support_purchase_fail, new Object[]{iabResult.toString()}));
                    SupportActivity.this.setWaitScreen(false);
                } else if (!SupportActivity.verifyDeveloperPayload(purchase)) {
                    SupportActivity supportActivity2 = SupportActivity.this;
                    supportActivity2.complain(supportActivity2.getString(C0354R.string.support_query_fail_payload));
                    SupportActivity.this.setWaitScreen(false);
                } else {
                    Log.d(str, "Purchase successful.");
                    Editor edit = SupportActivity.this.getSharedPreferences(Constants.PREF_NAME_SETTINGS, 0).edit();
                    if (purchase.getSku().equals(SupportActivity.SKU_SUPPORT)) {
                        Log.d(str, "Purchase support.");
                        SupportActivity.this.mHelper.consumeAsync(purchase, SupportActivity.this.mConsumeFinishedListener);
                    } else if (purchase.getSku().equals(SupportActivity.SKU_PREMIUM)) {
                        Log.d(str, "Purchase is premium upgrade. Congratulating user.");
                        SupportActivity.this.alert((int) C0354R.string.donate_big_message);
                        SupportActivity supportActivity3 = SupportActivity.this;
                        supportActivity3.mIsPremium = true;
                        edit.putBoolean(Constants.PREF_IS_VIP, supportActivity3.mIsPremium);
                        edit.apply();
                        SupportActivity.this.updateUi();
                        SupportActivity.this.setWaitScreen(false);
                    } else if (purchase.getSku().equals(SupportActivity.SKU_NOADS)) {
                        Log.d(str, "NoADS purchased.");
                        SupportActivity.this.alert((int) C0354R.string.donate_medium_message);
                        SupportActivity supportActivity4 = SupportActivity.this;
                        supportActivity4.mIsNoADS = true;
                        edit.putBoolean(Constants.PREF_IS_NOADS, supportActivity4.mIsNoADS);
                        edit.apply();
                        SupportActivity.this.updateUi();
                        SupportActivity.this.setWaitScreen(false);
                    }
                }
            }
        }
    };
    private boolean mResolvingError = false;

    public static class ErrorDialogFragment extends DialogFragment {
        public Dialog onCreateDialog(Bundle bundle) {
            return GooglePlayServicesUtil.getErrorDialog(getArguments().getInt(SupportActivity.DIALOG_ERROR), getActivity(), 1001);
        }

        public void onDismiss(DialogInterface dialogInterface) {
            ((SupportActivity) getActivity()).onDialogDismissed();
        }
    }

    public void onConnected(Bundle bundle) {
    }

    public void onConnectionSuspended(int i) {
    }

    public static boolean verifyDeveloperPayload(Purchase purchase) {
        return DEVELOPER_PAYLOAD.equals(purchase.getDeveloperPayload());
    }

    /* access modifiers changed from: protected */
    public void onDestroy() {
        super.onDestroy();
        IabHelper iabHelper = this.mHelper;
        if (iabHelper != null) {
            iabHelper.dispose();
            this.mHelper = null;
        }
    }

    /* access modifiers changed from: protected */
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(C0354R.layout.activity_support);
        String str = TAG;
        Log.d(str, "Creating IAB helper.");
        this.mHelper = new IabHelper(this, base64EncodedPublicKey);
        this.mHelper.enableDebugLogging(true);
        Log.d(str, "Starting setup.");
        this.mHelper.startSetup(new OnIabSetupFinishedListener() {
            public void onIabSetupFinished(IabResult iabResult) {
                String str = SupportActivity.TAG;
                Log.d(str, "Setup finished.");
                if (!iabResult.isSuccess()) {
                    SupportActivity supportActivity = SupportActivity.this;
                    supportActivity.complain(supportActivity.getString(C0354R.string.support_setup_fail, new Object[]{iabResult.toString()}));
                } else if (SupportActivity.this.mHelper != null) {
                    Log.d(str, "Setup successful. Querying inventory.");
                    SupportActivity.this.mHelper.queryInventoryAsync(SupportActivity.this.mGotInventoryListener);
                }
            }
        });
        ((Button) findViewById(C0354R.C0356id.btnbig)).setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                SupportActivity.this.setWaitScreen(true);
                Log.d(SupportActivity.TAG, "Launching purchase flow for SKU_PREMIUM.");
                IabHelper iabHelper = SupportActivity.this.mHelper;
                SupportActivity supportActivity = SupportActivity.this;
                iabHelper.launchPurchaseFlow(supportActivity, SupportActivity.SKU_PREMIUM, SupportActivity.RC_REQUEST, supportActivity.mPurchaseFinishedListener, SupportActivity.DEVELOPER_PAYLOAD);
            }
        });
        ((Button) findViewById(C0354R.C0356id.btnmedium)).setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                SupportActivity.this.setWaitScreen(true);
                Log.d(SupportActivity.TAG, "Launching purchase flow for SKU_NOADS.");
                IabHelper iabHelper = SupportActivity.this.mHelper;
                SupportActivity supportActivity = SupportActivity.this;
                iabHelper.launchPurchaseFlow(supportActivity, SupportActivity.SKU_NOADS, SupportActivity.RC_REQUEST, supportActivity.mPurchaseFinishedListener, SupportActivity.DEVELOPER_PAYLOAD);
            }
        });
        ((Button) findViewById(C0354R.C0356id.btnsmall)).setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                SupportActivity.this.setWaitScreen(true);
                Log.d(SupportActivity.TAG, "Launching purchase flow for SKU_SUPPORT.");
                IabHelper iabHelper = SupportActivity.this.mHelper;
                SupportActivity supportActivity = SupportActivity.this;
                iabHelper.launchPurchaseFlow(supportActivity, SupportActivity.SKU_SUPPORT, SupportActivity.RC_REQUEST, supportActivity.mPurchaseFinishedListener, SupportActivity.DEVELOPER_PAYLOAD);
            }
        });
        updateUi();
        GAHelper.recordScreen(this, str);
    }

    /* access modifiers changed from: 0000 */
    public void setWaitScreen(boolean z) {
        if (!z) {
            ProgressDialog progressDialog = this.mProgressDialog;
            if (progressDialog != null) {
                progressDialog.dismiss();
            }
        } else if (this.mProgressDialog == null) {
            this.mProgressDialog = ProgressDialog.show(this, getString(C0354R.string.support_progress_title), getString(C0354R.string.support_progress_message));
            this.mProgressDialog.setCancelable(false);
            this.mProgressDialog.setCanceledOnTouchOutside(false);
        }
    }

    public void updateUi() {
        SharedPreferences sharedPreferences = getSharedPreferences(Constants.PREF_NAME_SETTINGS, 0);
        this.mIsPremium = sharedPreferences.getBoolean(Constants.PREF_IS_VIP, false);
        this.mIsNoADS = sharedPreferences.getBoolean(Constants.PREF_IS_NOADS, false);
        Button button = (Button) findViewById(C0354R.C0356id.btnbig);
        button.setText(this.mIsPremium ? C0354R.string.donate_big_done : C0354R.string.donate_big);
        button.setEnabled(!this.mIsPremium);
        Button button2 = (Button) findViewById(C0354R.C0356id.btnmedium);
        button2.setText(this.mIsNoADS ? C0354R.string.donate_medium_done : C0354R.string.donate_medium);
        button2.setEnabled(!this.mIsNoADS);
    }

    /* access modifiers changed from: 0000 */
    public void complain(String str) {
        StringBuilder sb = new StringBuilder();
        sb.append("**** SupportActivity Error: ");
        sb.append(str);
        Log.e(TAG, sb.toString());
        alert(getString(C0354R.string.support_fail, new Object[]{str}));
    }

    /* access modifiers changed from: 0000 */
    public void alert(String str) {
        Builder builder = new Builder(this);
        builder.setMessage(str);
        builder.setNeutralButton(17039370, null);
        builder.create().show();
    }

    /* access modifiers changed from: 0000 */
    public void alert(int i) {
        Builder builder = new Builder(this);
        builder.setMessage(i);
        builder.setNeutralButton(17039370, null);
        builder.create().show();
    }

    /* access modifiers changed from: protected */
    public void onActivityResult(int i, int i2, Intent intent) {
        StringBuilder sb = new StringBuilder();
        sb.append("onActivityResult(");
        sb.append(i);
        String str = ",";
        sb.append(str);
        sb.append(i2);
        sb.append(str);
        sb.append(intent);
        String sb2 = sb.toString();
        String str2 = TAG;
        Log.d(str2, sb2);
        IabHelper iabHelper = this.mHelper;
        if (iabHelper != null) {
            if (!iabHelper.handleActivityResult(i, i2, intent)) {
                super.onActivityResult(i, i2, intent);
            } else {
                Log.d(str2, "onActivityResult handled by IABUtil.");
            }
        }
    }

    /* access modifiers changed from: protected */
    public void onStart() {
        super.onStart();
        boolean z = this.mResolvingError;
    }

    /* access modifiers changed from: protected */
    public void onStop() {
        super.onStop();
    }

    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (!this.mResolvingError) {
            if (connectionResult.hasResolution()) {
                try {
                    this.mResolvingError = true;
                    connectionResult.startResolutionForResult(this, 1001);
                } catch (SendIntentException unused) {
                }
            } else {
                showErrorDialog(connectionResult.getErrorCode());
                this.mResolvingError = true;
            }
        }
    }

    private void showErrorDialog(int i) {
        ErrorDialogFragment errorDialogFragment = new ErrorDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(DIALOG_ERROR, i);
        errorDialogFragment.setArguments(bundle);
        errorDialogFragment.show(getFragmentManager(), "errordialog");
    }

    public void onDialogDismissed() {
        this.mResolvingError = false;
    }
}
