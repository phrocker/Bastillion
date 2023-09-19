package io.bastillion.manage.jit;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class JITStatus {

    Long id;

    JITRequest request;

    Long approverId;

    @Builder.Default
    boolean approved=false;

    @Builder.Default
    boolean needMoreInfo=false;
}
