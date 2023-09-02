package io.bastillion.manage.auditing;

import io.bastillion.manage.auditing.rules.AuditorRule;

import java.util.ArrayList;
import java.util.List;

public class RuleAlertAuditor extends BaseAuditor {

    public List<AuditorRule> rules = new ArrayList<>();

    public RuleAlertAuditor(Long userId, Long sessionId) {
        super(userId, sessionId);
    }

    @Override
    protected void onPartial() {
        final String cstr = get();
        for(AuditorRule rule : rules){

        }
        // do nothing
    }

}
