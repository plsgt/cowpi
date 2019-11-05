package net.cowpi.oes;

import net.cowpi.oes.config.OesConfig;

public class TestOesConfig {

    private OesConfig oesConfig;

    private String routerLongtermPrivateKey;
    private String routerLongtermPublicKey;

    public OesConfig getOesConfig() {
        return oesConfig;
    }

    public void setOesConfig(OesConfig oesConfig) {
        this.oesConfig = oesConfig;
    }

    public String getRouterLongtermPrivateKey() {
        return routerLongtermPrivateKey;
    }

    public void setRouterLongtermPrivateKey(String routerLongtermPrivateKey) {
        this.routerLongtermPrivateKey = routerLongtermPrivateKey;
    }

    public String getRouterLongtermPublicKey() {
        return routerLongtermPublicKey;
    }

    public void setRouterLongtermPublicKey(String routerLongtermPublicKey) {
        this.routerLongtermPublicKey = routerLongtermPublicKey;
    }
}
