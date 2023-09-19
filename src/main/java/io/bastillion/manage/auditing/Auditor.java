package io.bastillion.manage.auditing;

public class Auditor extends BaseAuditor {


    public Auditor(Long userId, Long sessionId, Long systemId) {
        super(userId,sessionId,systemId);
    }

    @Override
    protected void onPartial() {
        // do nothing

    }

}
