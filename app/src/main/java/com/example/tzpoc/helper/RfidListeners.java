package com.example.tzpoc.helper;

public interface RfidListeners {
    void onFailure(Exception exc);

    void onFailure(String str);

    void onSuccess(Object obj);
}