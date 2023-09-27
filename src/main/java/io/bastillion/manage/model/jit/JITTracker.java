package io.bastillion.manage.model.jit;

import io.bastillion.manage.model.HostSystem;
import io.bastillion.manage.model.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class JITTracker extends JITRequest {

    User user;
    HostSystem hostSystem;
}
