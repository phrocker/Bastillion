package io.bastillion.manage.model.proxy;

import io.bastillion.manage.model.HostSystem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@Getter
@AllArgsConstructor
public class ProxyHost {

    long proxyId;

    String host;
    Integer port;

    HostSystem hostSystem;

    String errorMsg;
}
