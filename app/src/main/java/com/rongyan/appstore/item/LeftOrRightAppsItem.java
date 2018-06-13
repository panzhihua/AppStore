package com.rongyan.appstore.item;

/**
 * Created by panzhihua on 2017/11/7.
 */

public class LeftOrRightAppsItem {
    private Apps LeftApps;

    private Apps RightApps;

    public LeftOrRightAppsItem(Apps LeftApps,Apps RightApps){
        this.LeftApps=LeftApps;
        this.RightApps=RightApps;
    }

    public Apps getLeftApps() {
        return LeftApps;
    }

    public void setLeftApps(Apps leftApps) {
        LeftApps = leftApps;
    }

    public Apps getRightApps() {
        return RightApps;
    }

    public void setRightApps(Apps rightApps) {
        RightApps = rightApps;
    }
}
