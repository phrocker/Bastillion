package io.bastillion.manage.auditing.rules;

import java.util.Optional;

public interface AuditorRule {

    Optional<Trigger> trigger(String text);

    boolean configure(String configuration);

    TriggerAction describeAction();
}
//