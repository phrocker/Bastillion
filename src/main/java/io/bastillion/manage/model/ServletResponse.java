package io.bastillion.manage.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@Getter
@AllArgsConstructor
public class ServletResponse {
    @Builder.Default
    String contentType = "text/plain";
    String utfHttpResponse;
    @Builder.Default
    ServletResponseType type = ServletResponseType.FORWARD;
}
