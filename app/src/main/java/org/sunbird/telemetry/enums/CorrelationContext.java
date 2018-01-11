package org.sunbird.telemetry.enums;

import android.support.annotation.StringDef;

@StringDef({CorrelationContext.ALL_CONTENT_SEARCH, CorrelationContext.CONTENT_PROFILE_SEARCH, CorrelationContext.COURSE_SEARCH, CorrelationContext.LIBRARY_SEARCH,
        CorrelationContext.HOME_PAGE, CorrelationContext.COURSE_PAGE, CorrelationContext.RESOURCE_PAGE,
        CorrelationContext.NONE})
public @interface CorrelationContext {
    String ALL_CONTENT_SEARCH = "all_content_search";
    String CONTENT_PROFILE_SEARCH = "content_profile_search";
    String COURSE_SEARCH = "course_search";
    String LIBRARY_SEARCH = "library_search";
    String HOME_PAGE = "home_page";
    String COURSE_PAGE = "course_page";
    String RESOURCE_PAGE = "resource_page";
    String NONE = "";
}
