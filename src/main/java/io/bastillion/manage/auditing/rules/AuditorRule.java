package io.bastillion.manage.auditing.rules;

import java.util.List;

public interface AuditorRule {

    List<TriggerAction> trigger(String text);
}
