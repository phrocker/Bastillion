package io.bastillion.manage.model.proxy;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@Getter
@AllArgsConstructor
public class ProxyURL {

    ProxyHost proxyHost;

    String path;
}
