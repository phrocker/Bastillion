package io.bastillion.manage.auditing;

import io.bastillion.manage.auditing.rules.AuditorRule;
import io.bastillion.manage.auditing.rules.Trigger;
import io.bastillion.manage.model.Rule;
import org.h2.tools.Shell;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RuleAlertAuditor extends BaseAuditor {

    public List<AuditorRule> rules = new ArrayList<>();


    public RuleAlertAuditor(Long userId, Long sessionId) {
        super(userId, sessionId);
    }

    @Override
    protected void onPartial() {
        final String cstr = get();
        for(AuditorRule rule : rules){
            Optional<Trigger> result = rule.trigger(cstr);
            if (result.isPresent()){
                Trigger trg = result.get();
                switch(trg.getAction()){
                    case WARN_ACTION:
                        System.out.println("Adding action");
                        ShellAuditable.addWarning(trg);
                        break;
                    default:
                        break;
                }
            }
        }
        // do nothing
    }

    public void setRules(List<Rule> rules) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        for(Rule rule : rules){
            Class<? extends AuditorRule> newRule = Class.forName(rule.getRuleClass()).asSubclass(AuditorRule.class);

            this.rules.add(newRule.getConstructor().newInstance());
        }
    }
}
