package org.sunbird.notification.enums;

import android.support.annotation.IntDef;

/**
 * Created on 9/27/2016.
 *
 * @author anil
 */
@IntDef({NotificationActionId.DO_NOTHING, NotificationActionId.ANNOUNCEMENT_DETAIL, NotificationActionId.ANNOUNCEMENT_LIST})
public @interface NotificationActionId {

    int DO_NOTHING = -1;
    int ANNOUNCEMENT_DETAIL = 1;
    int ANNOUNCEMENT_LIST = 2;
}

