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
    String SEARCH_BUTTON_CLICKED = "SearchButtonClicked";
    String FILTER_BUTTON_CLICKED = "FilterButtonClicked";
    String NOTIFICATION_RECEIVED = "Notification-Received";
    String NOTIFICATION_DISPLAYED = "Notification-Displayed";
    String NOTIFICATION_CLICKED = "Notification-Clicked";

    String SETTINGS_CLICKED = "Settings-Clicked";
    String LANGUAGE_CLICKED = "language-clicked";
    String DATA_SYNC_CLICKED = "data-sync-clicked";
    String DEVICE_TAGS_CLICKED = "device-tags-clicked";
    String SUPPORT_CLICKED = "support-clicked";
    String SHARE_APP_CLICKED = "share-app-clicked";
    String ABOUT_APP_CLICKED = "about_app_clicked";
    String LANGUAGE_SETTINGS_SUCCESS = "language-settings-success";
    String MANUALSYNC_INITIATED = "manualsync-initiated";
    String MANUALSYNC_SUCCESS = "manualsync-success";
    String SHARE_APP_INITIATED = "share-app-initiated";
    String SHARE_APP_SUCCESS = "share-app-success";
    String BROWSE_AS_GUEST_CLICKED = "browse-as-guest-clicked";
    String SIGNIN_OVERLAY_CLICKED = "signin-overlay-clicked";

    String ANNOUNCEMENT_CLICKED = "Announcement-Clicked";
}
