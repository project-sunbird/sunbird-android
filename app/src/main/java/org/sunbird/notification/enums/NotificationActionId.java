package org.sunbird.notification.enums;

import android.support.annotation.IntDef;

/**
 * Created on 9/27/2016.
 *
 * @author anil
 */
@IntDef({NotificationActionId.DO_NOTHING, NotificationActionId.GENIE_HOME, NotificationActionId.EXPLORE_CONTENT_SCREEN, NotificationActionId.LESSON_DETAIL_SCREEN,
        NotificationActionId.TRANSFER_SCREEN, NotificationActionId.MANAGE_CHILD_SCREEN, NotificationActionId.LIST_OF_STORIES, NotificationActionId.DATA_SYNC_SETTINGS_SCREEN,
        NotificationActionId.MY_CONTENT_SCREEN})
public @interface NotificationActionId {

    int DO_NOTHING = -1;
    int GENIE_HOME = 1;
    int EXPLORE_CONTENT_SCREEN = 2; // This screen does not exists after feb 2017 release
    int LESSON_DETAIL_SCREEN = 3;
    int TRANSFER_SCREEN = 4;
    int MANAGE_CHILD_SCREEN = 5;
    int LIST_OF_STORIES = 6; // Search result screen
    int DATA_SYNC_SETTINGS_SCREEN = 7;
    int MY_CONTENT_SCREEN = 8;
}

