/*
 * This is the source code of Telegram for Android v. 5.x.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013-2018.
 */

package org.telegram.messenger;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;

import com.google.android.gms.auth.api.phone.SmsRetriever;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SmsReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null) {
            return;
        }
        try {
            String message = "";
            SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", Activity.MODE_PRIVATE);
            String hash = preferences.getString("sms_hash", null);
            if (SmsRetriever.SMS_RETRIEVED_ACTION.equals(intent.getAction())) {
                if (!AndroidUtilities.isWaitingForSms()) {
                    return;
                }
                Bundle bundle = intent.getExtras();
                message = (String) bundle.get(SmsRetriever.EXTRA_SMS_MESSAGE);
            } else if ("android.provider.Telephony.SMS_RECEIVED".equals(intent.getAction())) {
                //Wyng: plain SMS broadcast — no Google app hash needed
                if (!AndroidUtilities.isWaitingForSms()) {
                    return;
                }
                Bundle bundle = intent.getExtras();
                if (bundle == null) {
                    return;
                }
                Object[] pdus = (Object[]) bundle.get("pdus");
                if (pdus == null || pdus.length == 0) {
                    return;
                }
                StringBuilder sb = new StringBuilder();
                String format = bundle.getString("format");
                for (Object pdu : pdus) {
                    android.telephony.SmsMessage sms = android.telephony.SmsMessage.createFromPdu((byte[]) pdu, format);
                    if (sms != null && sms.getDisplayMessageBody() != null) {
                        sb.append(sms.getDisplayMessageBody());
                    }
                }
                message = sb.toString();
            }
            if (TextUtils.isEmpty(message)) {
                return;
            }
            //Wyng: match a standalone 3-8 digit code, prefer one on its own line (the SMS template starts with the code)
            String code = null;
            for (String line : message.split("\\n+")) {
                String t = line.trim();
                if (t.matches("[0-9]{3,8}")) {
                    code = t;
                    break;
                }
            }
            if (code == null) {
                Pattern pattern = Pattern.compile("[0-9\\-]+");
                final Matcher matcher = pattern.matcher(message);
                if (matcher.find()) {
                    code = matcher.group(0).replace("-", "");
                }
            }
            if (code != null && code.length() >= 3) {
                final String finalCode = code;
                if (hash != null) {
                    preferences.edit().putString("sms_hash_code", hash + "|" + finalCode).commit();
                }
                AndroidUtilities.runOnUIThread(() -> NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.didReceiveSmsCode, finalCode));
            }
        } catch (Throwable e) {
            FileLog.e(e);
        }
    }
}
