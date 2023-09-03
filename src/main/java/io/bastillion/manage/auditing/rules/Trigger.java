package io.bastillion.manage.auditing.rules;

public class Trigger {

    TriggerAction action;

    String description;

    public Trigger(TriggerAction action, String description){
        this.action=action;
        this.description=description;
    }
    public TriggerAction getAction() {
        return action;
    }

    public String getDescription() {
        return description;
    }
}
