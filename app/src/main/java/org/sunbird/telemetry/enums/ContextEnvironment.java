package org.sunbird.telemetry.enums;

import android.support.annotation.StringDef;

/**
 * Created by nikith.shetty on 10/01/18.
 */

@StringDef({ContextEnvironment.HOME, ContextEnvironment.USER})
public @interface ContextEnvironment {
    String HOME = "home";
    String USER = "user";
}
