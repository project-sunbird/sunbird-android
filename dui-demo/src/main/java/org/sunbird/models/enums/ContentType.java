package org.sunbird.models.enums;

import android.support.annotation.StringDef;

/**
 * Created by vinay.narayana on 18/08/17.
 */
@StringDef({ContentType.STORY, ContentType.WORKSHEET, ContentType.GAME, ContentType.COLLECTION,
        ContentType.TEXTBOOK, ContentType.TEXTBOOK, ContentType.COURSE, ContentType.LESSIONPLAN})
public @interface ContentType {

    String STORY = "Story";
    String WORKSHEET = "Worksheet";
    String GAME = "Game";
    String COLLECTION = "Collection";
    String TEXTBOOK = "TextBook";
    String TEXTBOOKUNIT = "TextbookUnit";
    String COURSE = "Course";
    String LESSIONPLAN = "LessonPlan";
}
