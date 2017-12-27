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
@StringDef({ImpressionType.LIST, ImpressionType.DETAIL, ImpressionType.VIEW,
        ImpressionType.EDIT, ImpressionType.WORKFLOW, ImpressionType.SEARCH})
public @interface ImpressionType {

    String LIST = "list";
    String DETAIL = "detail";
    String VIEW = "view";
    String EDIT = "edit";
    String WORKFLOW = "workflow";
    String SEARCH = "search";
}
