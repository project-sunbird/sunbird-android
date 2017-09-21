package org.sunbird.telemetry.enums;

import android.support.annotation.StringDef;

@StringDef({EntityType.UID, EntityType.SEARCH_PHRASE, EntityType.CONTENT, EntityType.CONTENT_ID, EntityType.CHILD_ID,
        EntityType.LANGUAGE, EntityType.SECTION, EntityType.FILTER, EntityType.SORT, EntityType.FILTER_PHRASE})
public @interface EntityType {

    String UID = "UID";
    String SEARCH_PHRASE = "SearchPhrase";
    String CONTENT = "Content";
    String CONTENT_ID = "ContentID";
    String CHILD_ID = "ChildID";
    String LANGUAGE = "Language";
    String SECTION = "SectionId";
    String FILTER = "Filter";
    String SORT = "Sort";
    String FILTER_PHRASE = "FilterPhrase";
}
