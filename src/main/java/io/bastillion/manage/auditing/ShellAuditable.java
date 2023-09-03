package io.bastillion.manage.auditing;

import io.bastillion.manage.auditing.rules.Trigger;

import java.util.concurrent.ConcurrentLinkedDeque;

public class ShellAuditable {

    static ConcurrentLinkedDeque<Trigger> warn = new ConcurrentLinkedDeque<>();


    public static void addWarning(Trigger trigger){
        warn.add(trigger);
    }

    public static Trigger getNextWarning(){
        return warn.isEmpty() ? null : warn.pop();
    }
}
