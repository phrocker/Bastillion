package io.bastillion.manage.auditing.rules;

import java.util.Optional;

public class ForbiddenCommandsRule implements AuditorRule{
    @Override
    public Optional<Trigger> trigger(String text) {
        System.out.println("checking " + text);
        if (text.startsWith("rm -r")){
            System.out.println("ahhhh");
            return Optional.of(new Trigger(TriggerAction.WARN_ACTION, "rm -rf can cause irreperable harm. please use cautiously" ));
        }


        return Optional.empty();
    }
}
