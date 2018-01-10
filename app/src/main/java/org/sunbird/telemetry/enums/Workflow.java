package org.sunbird.telemetry.enums;

import android.support.annotation.StringDef;

/**
 * Created by nikith.shetty on 10/01/18.
 */

@StringDef({Workflow.APP, Workflow.SESSION, Workflow.COURSE, Workflow.TEXTBOOK, Workflow.CONTENT, Workflow.COLLECTION, Workflow.QR})
public @interface Workflow {
    String APP = "app";
    String SESSION = "session";
    String COURSE = "course";
    String TEXTBOOK = "textbook";
    String CONTENT = "content";
    String COLLECTION = "collection";
    String QR = "qr";
}
