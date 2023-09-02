package io.bastillion.manage.auditing.rules;

import org.thymeleaf.expression.Lists;

import java.util.ArrayList;
import java.util.List;

public class ForbiddenCommandsRule implements AuditorRule{
    @Override
    public List<TriggerAction> trigger(String text) {
        ArrayList<TriggerAction> lst = new ArrayList<>();
        if (text.startsWith("rm -rf")){
            lst.add( TriggerAction.WARN_ACTION );
        }

        return lst;

    }
}
