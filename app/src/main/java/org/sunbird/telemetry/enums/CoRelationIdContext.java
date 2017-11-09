package org.sunbird.telemetry.enums;

import android.support.annotation.StringDef;

@StringDef({CoRelationIdContext.COURSE_AND_RESOURCE_SEARCH, CoRelationIdContext.COURSE_SEARCH, CoRelationIdContext.RESOURCE_SEARCH,
        CoRelationIdContext.HOME_PAGE, CoRelationIdContext.COURSE_PAGE, CoRelationIdContext.RESOURCE_PAGE,
        CoRelationIdContext.NONE})
public @interface CoRelationIdContext {
    String COURSE_AND_RESOURCE_SEARCH = "course_and_resource_search";
    String COURSE_SEARCH = "course_search";
    String RESOURCE_SEARCH = "resource_search";
    String HOME_PAGE = "home_page";
    String COURSE_PAGE = "course_page";
    String RESOURCE_PAGE = "resource_page";
    String NONE = "";
}
