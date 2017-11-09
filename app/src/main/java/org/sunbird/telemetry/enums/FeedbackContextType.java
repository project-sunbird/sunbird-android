package org.sunbird.telemetry.enums;

import android.support.annotation.StringDef;

@StringDef({FeedbackContextType.CONTENT, FeedbackContextType.APP})
public @interface FeedbackContextType {
    String CONTENT = "Content";
    String APP = "App";
}
