package io.bastillion.common.util;

import loophole.mvc.annotation.Model;

public class BastillionOptions {

    String systemLogoName = "Bastillion";

    public String getSystemLogoName() {
        return systemLogoName;
    }

    public void setSystemLogoName(String systemLogoName){
        this.systemLogoName=systemLogoName;
    }
}
