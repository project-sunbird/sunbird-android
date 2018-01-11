package org.sunbird.telemetry;

public interface TelemetryAction {

    String BACKGROUND = "background";
    String RESUME = "resume";

    String LOGIN_INITIATE = "Login-Initiate";
    String LOGIN_SUCCESS = "Login-Success";
    String SIGNUP_INITIATE = "Signup-Initiate";
    String SIGNUP_SUCCESS = "Signup-Success";
    String LOGOUT_INITIATE = "Logout-Initiate";
    String LOGOUT_SUCCESS = "Logout-Success";
    String SECTION_VIEWED = "Section-Viewed";
    String CANCEL = "Cancel";
    String VIEWALL_CLICKED = "ViewAll-Clicked";
    String TAB_CLICKED = "Tab-Clicked";
    String SHARE_CONTENT_LINK = "ShareContent-Link";
    String SHARE_COURSE_INITIATED = "ShareCourse-Initiated";
    String SHARE_LIBRARY_INITIATED = "ShareLibrary-Initiated";
    String SHARE_COURSE_SUCCESS = "ShareCourse-Success";
    String SHARE_LIBRARY_SUCCESS = "ShareLibrary-Success";
    String PREVIEW_LOGIN = "Preview-Login";
    String FLAG_INITIATE = "Flag-Initiate";
    String FLAG_SUCCESS = "Flag-Success";
    String FLAG_FAILED = "Flag-Failed";
    String CONTENT_PLAY = "ContentPlay";
    String CONTENT_CLICKED = "ContentClicked";
    String SEARCH_PHRASE = "SearchPhrase";
    String FILTER_PHRASE = "FilterPhrase";

    String NOTIFICATION_RECEIVED = "Notification-Received";
    String NOTIFICATION_DISPLAYED = "Notification-Displayed";
    String NOTIFICATION_CLICKED = "Notification-Clicked";

    String ANNOUNCEMENT_CLICKED = "Announcement-Clicked";
}
