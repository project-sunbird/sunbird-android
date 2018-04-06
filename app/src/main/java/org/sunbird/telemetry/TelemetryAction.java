package org.sunbird.telemetry;

public interface TelemetryAction {

    String BACKGROUND = "background";
    String RESUME = "resume";

    String LOGIN_INITIATE = "login-initiate";
    String LOGIN_SUCCESS = "login-success";
    String SIGNUP_INITIATE = "signup-initiate";
    String LOGOUT_INITIATE = "logout-initiate";
    String LOGOUT_SUCCESS = "logout-success";
    String SECTION_VIEWED = "section-viewed";
    String CANCEL = "cancel";
    String VIEWALL_CLICKED = "view-all-clicked";
    String TAB_CLICKED = "tab-clicked";
    String SHARE_COURSE_INITIATED = "share-course-initiated";
    String SHARE_LIBRARY_INITIATED = "share-library-initiated";
    String SHARE_COURSE_SUCCESS = "share-course-success";
    String SHARE_LIBRARY_SUCCESS = "share-library-success";
    String FLAG_INITIATE = "flag-initiated";
    String FLAG_SUCCESS = "flag-success";
    String FLAG_FAILED = "flag-failed";
    String CONTENT_PLAY = "content-play";
    String CONTENT_CLICKED = "content-clicked";
    String SEARCH_BUTTON_CLICKED = "search-buttonclicked";
    String FILTER_BUTTON_CLICKED = "filter-button-clicked";
    String QRCodeScanClicked = "qr-code-scanner-clicked";
    String QRCodeScanInitiate = "qr-code-scan-initiate";
    String QRCodeScanSuccess = "qr-code-scan-success";
    String QRCodeScanCancelled = "qr-code-scan-cancelled";
    String NOTIFICATION_CLICKED = "notification-clicked";

    String SETTINGS_CLICKED = "settings-clicked";
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

    String ANNOUNCEMENT_CLICKED = "announcement-clicked";
    String CONTINUE_CLICKED = "continue-clicked";
    String RATING_POPUP = "rating-popup";
}
