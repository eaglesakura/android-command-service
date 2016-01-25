// ICommandServerService.aidl
package com.eaglesakura.android.service.aidl;

import com.eaglesakura.android.service.aidl.ICommandClientCallback;
import com.eaglesakura.android.service.data.Payload;

interface ICommandServerService {
    /**
     * エンコードデータを受け取り、同じくそれを返す
     */
    Payload postToServer(String cmd, String senderId, in Payload payload);

    /**
     * コールバック登録を行う
     */
    void registerCallback(String id, ICommandClientCallback callback);

    /**
     * コールバック削除を行う
     */
    void unregisterCallback(ICommandClientCallback callback);
}
