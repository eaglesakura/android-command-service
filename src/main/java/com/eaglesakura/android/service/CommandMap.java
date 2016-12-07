package com.eaglesakura.android.service;

import com.eaglesakura.android.service.data.Payload;

import android.os.RemoteException;

import java.util.HashMap;
import java.util.Map;

public class CommandMap {

    private Map<String, Action> actions = new HashMap<>();

    public void addAction(String cmd, Action action) {
        synchronized (this) {
            actions.put(cmd, action);
        }
    }

    public Payload execute(Object sender, String cmd, byte[] buffer) throws RemoteException {
        return execute(sender, cmd, new Payload(buffer));
    }

    public Payload execute(Object sender, String cmd, Payload payload) throws RemoteException {
        Action action;
        synchronized (this) {
            action = actions.get(cmd);
        }

        try {
            if (action != null) {
                return action.execute(sender, cmd, payload);
            } else {
                return null;
            }
        } catch (RemoteException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RemoteException();
        }
    }

    public interface Action {
        Payload execute(Object sender, String cmd, Payload payload) throws Exception;
    }
}
