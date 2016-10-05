package com.eaglesakura.android.service;

import com.eaglesakura.android.service.aidl.ICommandClientCallback;
import com.eaglesakura.android.service.aidl.ICommandServerService;
import com.eaglesakura.android.service.data.Payload;
import com.eaglesakura.android.thread.ui.UIHandler;
import com.eaglesakura.android.util.AndroidThreadUtil;
import com.eaglesakura.util.StringUtil;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;

/**
 * 別プロセスServiceと通信するためのインターフェース
 */
public abstract class CommandClient {
    protected final Context mContext;

    private ICommandServerService server;

    private final String id;

    public CommandClient(Context context) {
        this.mContext = context.getApplicationContext();
        this.id = context.getPackageName() + "@" + getClass().getName();
    }

    public String getId() {
        return id;
    }

    /**
     * クライアントを一意に識別するためのIDを任意に指定して生成する
     */
    public CommandClient(Context context, String uid) {
        if (!StringUtil.isEmpty(uid)) {
            if (uid.indexOf('@') >= 0) {
                throw new IllegalArgumentException();
            }
        } else {
            uid = getClass().getName();
        }
        this.mContext = context.getApplicationContext();
        this.id = context.getPackageName() + "@" + uid;
    }

    /**
     * Serviceに接続済みであればtrue
     */
    public boolean isConnected() {
        return server != null;
    }

    protected ICommandServerService getServer() {
        return server;
    }

    protected void connectToSever(Intent intent) {
        AndroidThreadUtil.assertUIThread();

        if (server != null) {
            return;
        }

        mContext.bindService(intent, connection, Context.BIND_AUTO_CREATE);
    }

    /**
     * 切断リクエストを送る
     */
    public void disconnect() {
        AndroidThreadUtil.assertUIThread();

        if (server == null) {
            // not connected
            return;
        }

        try {
            server.unregisterCallback(callback);
        } catch (Exception e) {
            e.printStackTrace();
        }

        mContext.unbindService(connection);
        server = null;

        // 正常な手段で切断した
        onDisconnected(0x00);
    }

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, final IBinder service) {
            UIHandler.postUIorRun(new Runnable() {
                @Override
                public void run() {
                    ICommandServerService newServer = ICommandServerService.Stub.asInterface(service);
                    try {
                        newServer.registerCallback(id, callback);
                    } catch (RemoteException e) {
                        throw new IllegalStateException(e);
                    }

                    server = newServer;
                    onConnected();
                }
            });
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            UIHandler.postUIorRun(new Runnable() {
                @Override
                public void run() {
                    if (server != null) {
                        server = null;
                        onDisconnected(FLAG_DISCONNECT_CRASH_SERVER);
                    }
                }
            });
        }
    };


    private ICommandClientCallback callback = new ICommandClientCallback.Stub() {
        @Override
        public Payload postToClient(String cmd, Payload payload) throws RemoteException {
            return onReceivedData(cmd, payload);
        }
    };

    /**
     * サーバーにデータを送信する
     */
    public Payload requestPostToServer(String cmd, Payload payload) throws RemoteException {
        if (server == null) {
            throw new IllegalStateException("Server not connected");
        }
        return server.postToServer(cmd, id, payload);
    }

    /**
     * サーバーからのデータ取得時のハンドリングを行う
     *
     * @param cmd     　処理するコマンド
     * @param payload 受け取ったデータペイロード
     */
    protected Payload onReceivedData(String cmd, Payload payload) throws RemoteException {
        return null;
    }

    /**
     * サーバーに接続完了した
     */
    protected void onConnected() {

    }

    /**
     * サーバーからデータ切断された
     */
    @Deprecated
    protected void onDisconnected() {

    }

    /**
     * 接続先のサーバーがクラッシュした
     */
    public static final int FLAG_DISCONNECT_CRASH_SERVER = 0x1 << 0;

    /**
     * サーバークラッシュ等のフラグを得られるようにした。
     * 互換のため、内部では引数無し版を呼び出す
     */
    protected void onDisconnected(int flags) {
        onDisconnected();
    }
}
