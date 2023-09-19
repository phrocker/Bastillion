package io.bastillion.manage.jit;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Link for why a request should be approved.
 *
 * Usually this corresponds with a ticket number
 */
@Builder
@NoArgsConstructor
@Getter
@AllArgsConstructor
public class JITRequestLink {

    public String identifier;

    public String uri;

    @Builder.Default
    public boolean isActive = false;
}
