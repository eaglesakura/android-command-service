// ICommandClientCallback.aidl
package com.eaglesakura.android.service.aidl;

import android.os.Parcelable;
import com.eaglesakura.android.service.data.Payload;

interface ICommandClientCallback {
    /**
     * エンコードデータを受け取り、同じくそれを返す
     */
    Payload postToClient(String cmd, in Payload payload);
}
