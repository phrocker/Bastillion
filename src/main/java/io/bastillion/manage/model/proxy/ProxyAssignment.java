package io.bastillion.manage.model.proxy;

import io.bastillion.manage.model.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@Getter
@AllArgsConstructor
public class ProxyAssignment {

    long id;

    ProxyHost proxyHost;

    User user;
}
