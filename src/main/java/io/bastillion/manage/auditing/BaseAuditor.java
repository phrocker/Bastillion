package io.bastillion.manage.auditing;

import io.bastillion.manage.auditing.rules.AuditorRule;
import io.bastillion.manage.model.Rule;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class BaseAuditor {


    private final Long userId;
    private final Long sessionId;
    StringBuilder builder = new StringBuilder();

    AtomicBoolean receiveFromServer = new AtomicBoolean(false);

    public BaseAuditor(Long userId, Long sessionId) {
        this.userId = userId;
        this.sessionId = sessionId;
    }

    public synchronized String clear(){
        String currentOutput = this.builder.toString();
        this.builder = new StringBuilder();
        return currentOutput;
    }

    public synchronized  String append(String strToAppend){
        this.builder.append(strToAppend);
        try {
            onPartial();
        }catch(Exception e){

        }
        return this.builder.toString();
    }

    protected abstract void onPartial();

    public synchronized  String backspace(){
        if (!this.builder.toString().isEmpty()){
            this.builder.deleteCharAt(this.builder.length()-1);
        }
        return this.builder.toString();
    }

    public synchronized  String get(){
        return this.builder.toString();
    }

    public synchronized boolean shouldReceiveFromServer(){
        return receiveFromServer.get();
    }

    public synchronized void receiveFromServer(String srvResponse){
        this.append(srvResponse);
        this.receiveFromServer.set(false);
    }

    public synchronized void setReceiveFromServer(){
        this.receiveFromServer.set(true);
    }


    public synchronized void keycode(Integer keyCode) {
        switch(keyCode){
            case 9:
                setReceiveFromServer();
                break;
            case 8:
                backspace();
                break;
            case 13:
                System.out.println("on message " + get().toString());
            case 67:
                clear();
            default:
                break;

        }
    }


}
