package io.bastillion.manage.jit;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class JITRequest {

    Long id;

    // command being run
    String command;

    JITReason reason;

    Long userId;

    Long systemId;

    boolean isApproved;

}
