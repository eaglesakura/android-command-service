package com.eaglesakura.android.service;

import com.eaglesakura.android.service.aidl.ICommandClientCallback;
import com.eaglesakura.android.service.aidl.ICommandServerService;
import com.eaglesakura.android.service.data.Payload;
import com.eaglesakura.android.thread.UIHandler;

import android.app.Service;
import android.os.IBinder;
import android.os.RemoteException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class CommandServer {
    protected final Service mService;

    private final ICommandServerService mImpl;

    private Map<String, ServiceClient> mClients = new HashMap<>();

    public CommandServer(Service service) {
        this.mService = service;
        this.mImpl = new ServerImpl();
    }

    /**
     * 接続されているServiceを取得する
     */
    public Service getService() {
        return mService;
    }

    /**
     * 接続されているクライアント数を取得する
     */
    public int getClientNum() {
        synchronized (mClients) {
            return mClients.size();
        }
    }

    /**
     * Serviceの実体を返す
     */
    public IBinder getBinder() {
        if (mImpl instanceof ICommandServerService.Stub) {
            return (ICommandServerService.Stub) mImpl;
        } else {
            return null;
        }
    }


    /**
     * 指定したクライアントに接続されていればtrue
     */
    public boolean hasClient(String id) {
        synchronized (mClients) {
            return mClients.containsKey(id);
        }
    }

    private class ServerImpl extends ICommandServerService.Stub {

        @Override
        public Payload postToServer(String cmd, String clientId, Payload payload) throws RemoteException {
            return onReceivedDataFromClient(cmd, clientId, payload);
        }

        @Override
        public void registerCallback(final String id, final ICommandClientCallback callback) throws RemoteException {
            synchronized (mClients) {
                ServiceClient client = new ServiceClient(id, callback);
                mClients.put(id, client);
            }

            UIHandler.postUI(new Runnable() {
                @Override
                public void run() {
                    onRegisterClient(id, callback);
                }
            });
        }

        @Override
        public void unregisterCallback(ICommandClientCallback callback) throws RemoteException {
            final List<String> idList = new ArrayList<>();
            synchronized (mClients) {
                Iterator<Map.Entry<String, ServiceClient>> iterator = mClients.entrySet().iterator();
                while (iterator.hasNext()) {
                    Map.Entry<String, ServiceClient> item = iterator.next();
                    if (callback == item.getValue().callback) {
                        idList.add(item.getKey());
                        iterator.remove();
                    }
                }
            }

            UIHandler.postUI(new Runnable() {
                @Override
                public void run() {
                    for (String id : idList) {
                        onUnregisterClient(id);
                    }
                }
            });

        }
    }

    /**
     * コールバック登録が行われた
     */
    protected void onRegisterClient(String id, ICommandClientCallback callback) {

    }

    /**
     * コールバックが削除された
     */
    protected void onUnregisterClient(String id) {

    }

    /**
     * 指定したクライアントへデータを送信する
     *
     * @param clientId 送信先クライアントID
     * @param cmd      送信コマンド
     * @param buffer   データ
     */
    protected Payload postToClient(String clientId, String cmd, byte[] buffer) throws RemoteException {
        return postToClient(clientId, cmd, new Payload(buffer));
    }

    /**
     * 指定したクライアントへデータを送信する
     */
    protected Payload postToClient(String clientId, String cmd, Payload payload) throws RemoteException {
        ServiceClient client;
        synchronized (mClients) {
            client = mClients.get(clientId);
        }

        if (client != null) {
            return client.callback.postToClient(cmd, payload);
        } else {
            return null;
        }
    }

    /**
     * データを送信し、戻り値は無視する
     */
    protected void broadcastToClientNoResults(String cmd, byte[] buffer) throws RemoteException {
        broadcastToClientNoResults(cmd, new Payload(buffer));
    }

    /**
     * データを送信し、戻り値は無視する
     *
     * @param cmd     コマンド
     * @param payload 送信データ
     */
    protected void broadcastToClientNoResults(String cmd, Payload payload) throws RemoteException {
        Map<String, ServiceClient> clients;

        synchronized (this.mClients) {
            clients = new HashMap<>(this.mClients);
        }

        Iterator<Map.Entry<String, ServiceClient>> iterator = clients.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, ServiceClient> entry = iterator.next();
            entry.getValue().callback.postToClient(cmd, payload);
        }
    }


    /**
     * データを送信し、戻り値はコールバックで受け取る
     */
    protected void broadcastToClient(String cmd, byte[] buffer, ClientResultCallback callback) throws RemoteException {
        broadcastToClient(cmd, new Payload(buffer), callback);
    }

    /**
     * データを送信し、戻り値はコールバックで受け取る
     */
    protected void broadcastToClient(String cmd, Payload payload, ClientResultCallback callback) throws RemoteException {
        Map<String, ServiceClient> clients;

        synchronized (this.mClients) {
            clients = new HashMap<>(this.mClients);
        }

        Iterator<Map.Entry<String, ServiceClient>> iterator = clients.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, ServiceClient> entry = iterator.next();
            ServiceClient client = entry.getValue();
            Payload clientResult = client.callback.postToClient(cmd, payload);
            callback.onClientExecuted(client.id, client.callback, cmd, clientResult);
        }
    }

    /**
     * データを送信し、戻り値一覧を取得する
     */
    protected Map<String, Payload> broadcastToClient(String cmd, byte[] buffer) throws RemoteException {
        return broadcastToClient(cmd, new Payload(buffer));
    }

    /**
     * データを送信し、戻り値一覧を取得する
     */
    protected Map<String, Payload> broadcastToClient(String cmd, Payload payload) throws RemoteException {
        Map<String, ServiceClient> clients;

        synchronized (this.mClients) {
            clients = new HashMap<>(this.mClients);
        }

        Map<String, Payload> results = new HashMap<>();
        Iterator<Map.Entry<String, ServiceClient>> iterator = clients.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, ServiceClient> entry = iterator.next();
            results.put(entry.getKey(), entry.getValue().callback.postToClient(cmd, payload));
        }

        return results;
    }

    protected Payload onReceivedDataFromClient(String cmd, String clientId, Payload payload) throws RemoteException {
        return null;
    }

    public interface ClientResultCallback {
        void onClientExecuted(String id, ICommandClientCallback client, String cmd, Payload result);
    }

    private class ServiceClient {
        final String id;
        final ICommandClientCallback callback;

        public ServiceClient(String id, ICommandClientCallback callback) {
            this.id = id;
            this.callback = callback;
        }
    }
}
