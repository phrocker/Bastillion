package io.bastillion.common.util;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BastillionOptions {

    @Builder.Default
    public String systemLogoName = "Bastillion";

    @Builder.Default
    public boolean jitRequiresTicket = false;

    @Builder.Default
    public Integer approvedJITPeriod = 60;
}
