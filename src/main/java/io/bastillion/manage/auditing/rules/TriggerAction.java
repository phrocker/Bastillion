package io.bastillion.manage.auditing.rules;

public enum TriggerAction {
    NO_ACTION,
    LOG_ACTION,
    ALERT_ACTION,
    WARN_ACTION,
    DENY_ACTION,
    JIT_ACTION;

    public static TriggerAction valueOfStr(String action){
        if ("WARN".equals(action)) {
            return WARN_ACTION;
        }
        if ("ALERT".equals(action)) {
            return ALERT_ACTION;
        }
        if ("JIT".equals(action)) {
            return JIT_ACTION;
        }
        if ("DENY".equals(action)) {
            return DENY_ACTION;
        }
        if ("LOG".equals(action)) {
            return LOG_ACTION;
        }
        return NO_ACTION;
    }
}
