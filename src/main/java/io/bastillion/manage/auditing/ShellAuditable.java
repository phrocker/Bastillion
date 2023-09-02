package io.bastillion.manage.auditing;

import io.bastillion.manage.model.SchSession;

public interface ShellAuditable {

    boolean onMessage(SchSession session, String message);
}
