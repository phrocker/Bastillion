package io.bastillion.manage.auditing;

import java.util.concurrent.atomic.AtomicBoolean;

public class Auditor extends BaseAuditor {


    public Auditor(Long userId, Long sessionId) {
        super(userId,sessionId);
    }

    @Override
    protected void onPartial() {
        // do nothing

    }

}
