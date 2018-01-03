package org.sunbird.telemetry.enums;

import android.support.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created on 11/22/2017.
 *
 * @author anil
 */
@Retention(RetentionPolicy.SOURCE)
@StringDef({ObjectType.CONTENT, ObjectType.USER, ObjectType.ANNOUNCEMENT})
public @interface ObjectType {
    String CONTENT = "Content";
    String USER = "User";
    String ANNOUNCEMENT = "Announcement";
}
