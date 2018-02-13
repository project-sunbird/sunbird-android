package org.sunbird.utils;

import android.app.Activity;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;

import org.ekstep.genieservices.GenieService;
import org.ekstep.genieservices.async.GenieAsyncService;
import org.ekstep.genieservices.commons.IResponseHandler;
import org.ekstep.genieservices.commons.bean.ChildContentRequest;
import org.ekstep.genieservices.commons.bean.Content;
import org.ekstep.genieservices.commons.bean.ContentData;
import org.ekstep.genieservices.commons.bean.ContentDelete;
import org.ekstep.genieservices.commons.bean.ContentDeleteRequest;
import org.ekstep.genieservices.commons.bean.ContentDeleteResponse;
import org.ekstep.genieservices.commons.bean.ContentDetailsRequest;
import org.ekstep.genieservices.commons.bean.ContentExportRequest;
import org.ekstep.genieservices.commons.bean.ContentExportResponse;
import org.ekstep.genieservices.commons.bean.ContentFilterCriteria;
import org.ekstep.genieservices.commons.bean.ContentImport;
import org.ekstep.genieservices.commons.bean.ContentImportRequest;
import org.ekstep.genieservices.commons.bean.ContentImportResponse;
import org.ekstep.genieservices.commons.bean.ContentSearchCriteria;
import org.ekstep.genieservices.commons.bean.ContentSearchResult;
import org.ekstep.genieservices.commons.bean.CorrelationData;
import org.ekstep.genieservices.commons.bean.DownloadProgress;
import org.ekstep.genieservices.commons.bean.EcarImportRequest;
import org.ekstep.genieservices.commons.bean.GenieResponse;
import org.ekstep.genieservices.commons.bean.HierarchyInfo;
import org.ekstep.genieservices.commons.bean.ImportContentProgress;
import org.ekstep.genieservices.commons.bean.MasterData;
import org.ekstep.genieservices.commons.bean.MasterDataValues;
import org.ekstep.genieservices.commons.bean.Profile;
import org.ekstep.genieservices.commons.bean.SyncStat;
import org.ekstep.genieservices.commons.bean.enums.ContentImportStatus;
import org.ekstep.genieservices.commons.bean.enums.MasterDataType;
import org.ekstep.genieservices.commons.bean.telemetry.Telemetry;
import org.ekstep.genieservices.commons.utils.Base64Util;
import org.ekstep.genieservices.commons.utils.CollectionUtil;
import org.ekstep.genieservices.commons.utils.GsonUtil;
import org.ekstep.genieservices.commons.utils.StringUtil;
import org.ekstep.genieservices.utils.ContentPlayer;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.sunbird.BuildConfig;
import org.sunbird.GlobalApplication;
import org.sunbird.models.CurrentGame;
import org.sunbird.telemetry.TelemetryBuilder;
import org.sunbird.telemetry.TelemetryConstant;
import org.sunbird.telemetry.TelemetryHandler;
import org.sunbird.telemetry.TelemetryPageId;
import org.sunbird.telemetry.TelemetryUtil;
import org.sunbird.telemetry.enums.ContextEnvironment;
import org.sunbird.telemetry.enums.CorrelationContext;
import org.sunbird.telemetry.enums.ImpressionType;
import org.sunbird.ui.MainActivity;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import in.juspay.mystique.DynamicUI;

/**
 * Created by Vinay on 13/06/17.
 */
public class GenieWrapper extends Activity {

    private static final String TAG = GenieWrapper.class.getSimpleName();
    private static final int COURSE_AND_RESOURCE_SEARCH = 0;
    private static final int COURSE_SEARCH = 1;
    private static final int RESOURCE_SEARCH = 2;
    private GenieService mGenieService;
    private GenieAsyncService mGenieAsyncService;
    private String jsonInString;
    private MainActivity activity;
    private List<Content> list;
    private ContentSearchResult contentSearchResult;
    private DynamicUI dynamicUI;

    public GenieWrapper(MainActivity activity, DynamicUI dynamicUI) {
        this.activity = activity;
        mGenieService = GenieService.getService();
        mGenieAsyncService = GenieService.getAsyncService();
        this.dynamicUI = dynamicUI;
    }

    public void getMobileDeviceBearerToken(final String callback) {
        Log.e(TAG, "in token class");
        mGenieAsyncService.getAuthService().getMobileDeviceBearerToken(new IResponseHandler<String>() {
            @Override
            public void onSuccess(GenieResponse<String> genieResponse) {
                String javascript = String.format("window.callJSCallback('%s','%s');", callback, genieResponse.getResult());
                dynamicUI.addJsToWebView(javascript);
            }

            @Override
            public void onError(GenieResponse<String> genieResponse) {
                String javascript = String.format("window.callJSCallback('%s','%s','%s);", callback, "error", genieResponse.getResult());
                dynamicUI.addJsToWebView(javascript);
            }
        });

    }

    public void getContentDetails(final String callback, String content_id) {
        ContentDetailsRequest contentDetailsRequest = new ContentDetailsRequest.Builder().forContent(content_id).build();
        mGenieAsyncService.getContentService().getContentDetails(contentDetailsRequest, new IResponseHandler<Content>() {
            @Override
            public void onSuccess(GenieResponse<Content> genieResponse) {
                Content cd;
                cd = genieResponse.getResult();
                jsonInString = GsonUtil.toJson(cd);
                String enc = Base64Util.encodeToString(jsonInString.getBytes(), Base64Util.DEFAULT);
                String javascript = String.format("window.callJSCallback('%s','%s');", callback, enc);
                dynamicUI.addJsToWebView(javascript);
            }

            @Override
            public void onError(GenieResponse<Content> genieResponse) {
                String javascript = String.format("window.callJSCallback('%s','%s');", callback, "__failed");
                dynamicUI.addJsToWebView(javascript);
            }
        });
    }

    public void getImportStatus(final String id, final String callback) {
        mGenieAsyncService.getContentService().getImportStatus(id, new IResponseHandler<ContentImportResponse>() {
            @Override
            public void onSuccess(GenieResponse<ContentImportResponse> genieResponse) {
                jsonInString = GsonUtil.toJson(genieResponse.getResult());
                String javascript = String.format("window.callJSCallback('%s','%s');", callback, jsonInString);
                dynamicUI.addJsToWebView(javascript);
            }

            @Override
            public void onError(GenieResponse<ContentImportResponse> genieResponse) {

            }
        });
    }

    public void getCourseContent(final String callback, String content_id) {
        ChildContentRequest.Builder childContentBuilder = new ChildContentRequest.Builder();
        List<HierarchyInfo> hierarchyInfoList = new ArrayList<>();
        HierarchyInfo hierarchyInfo = new HierarchyInfo(content_id, "Course");
        hierarchyInfoList.add(hierarchyInfo);
        childContentBuilder.forContent(content_id).hierarchyInfo(hierarchyInfoList);
        mGenieAsyncService.getContentService().getChildContents(childContentBuilder.build(), new IResponseHandler<Content>() {
            @Override
            public void onSuccess(GenieResponse<Content> genieResponse) {
                Content cd;
                cd = genieResponse.getResult();
                Gson gson = new Gson();
                jsonInString = gson.toJson(cd);
                String enc = Base64Util.encodeToString(jsonInString.getBytes(), Base64Util.DEFAULT);
                String javascript = String.format("window.callJSCallback('%s','%s');", callback, enc);
                dynamicUI.addJsToWebView(javascript);
            }

            @Override
            public void onError(GenieResponse<Content> genieResponse) {
                String javascript = String.format("window.callJSCallback('%s','%s');", callback, "__failed");
                dynamicUI.addJsToWebView(javascript);
            }
        });
    }




    public void getLocalContentStatus(final String contentId, final String callback) {
        ContentDetailsRequest.Builder contentDetailBuilder = new ContentDetailsRequest.Builder();
        contentDetailBuilder.forContent(contentId);
        mGenieAsyncService.getContentService().getContentDetails(contentDetailBuilder.build(), new IResponseHandler<Content>() {
            @Override
            public void onSuccess(GenieResponse<Content> genieResponse) {
                Boolean status = false;
                Content content = genieResponse.getResult();
                if (content.isAvailableLocally()) {
                    status = true;
                }
                String javascript = String.format("window.callJSCallback('%s','%s');", callback, status);
                dynamicUI.addJsToWebView(javascript);
            }

            @Override
            public void onError(GenieResponse<Content> genieResponse) {
                Boolean status = false;
                String javascript = String.format("window.callJSCallback('%s','%s');", callback, status);
                dynamicUI.addJsToWebView(javascript);

            }
        });

    }

    public void getContentType(final String contentId, final String callback) {
        ContentDetailsRequest.Builder builder1 = new ContentDetailsRequest.Builder();
        builder1.forContent(contentId);
        mGenieAsyncService.getContentService().getContentDetails(builder1.build(), new IResponseHandler<Content>() {
            @Override
            public void onSuccess(GenieResponse<Content> genieResponse) {
                Boolean isCourse = false;
                Content content = genieResponse.getResult();
                if (content.getContentType().equals("course")) {
                    isCourse = true;
                }
                String javascript = String.format("window.callJSCallback('%s','%s');", callback, isCourse);
                dynamicUI.addJsToWebView(javascript);


            }

            @Override
            public void onError(GenieResponse<Content> genieResponse) {

            }
        });
    }

    public void getAllLocalContent(final String callback) {

        ContentFilterCriteria contentFilterCriteria = new ContentFilterCriteria.Builder().build();
        ContentFilterCriteria.Builder builder = new ContentFilterCriteria.Builder();
        builder.contentTypes(new String[]{"Story", "Worksheet", "Collection", "Game", "TextBook", "Course", "Resource", "LessonPlan"});

        mGenieAsyncService.getContentService().getAllLocalContent(builder.build(), new IResponseHandler<List<Content>>() {
            @Override
            public void onSuccess(GenieResponse<List<Content>> genieResponse) {
                list = genieResponse.getResult();
                Gson gson = new Gson();
                String jsonInString = gson.toJson(list);
                String enc = Base64Util.encodeToString(jsonInString.getBytes(), Base64Util.DEFAULT);
                ;
                String javascript = String.format("window.callJSCallback('%s','%s');", callback, enc);
                dynamicUI.addJsToWebView(javascript);

            }

            @Override
            public void onError(GenieResponse<List<Content>> genieResponse) {

            }
        });
    }


    public void searchContent(final String callback, final String filterParams, final String query, final String type, final int count, boolean viewMoreClicked) {
        try {
            ContentSearchCriteria.SearchBuilder builder = new ContentSearchCriteria.SearchBuilder();
            String[] contentTypes;

            switch (type) {
                case "Course":
                    contentTypes = new String[1];
                    contentTypes[0] = "Course";
                    break;

                case "Library":
                    contentTypes = new String[7];
                    contentTypes[0] = "Story";
                    contentTypes[1] = "Game";
                    contentTypes[2] = "TextBook";
                    contentTypes[3] = "Collection";
                    contentTypes[4] = "Worksheet";
                    contentTypes[5] = "Resource";
                    contentTypes[6] = "LessonPlan";
                    break;

                case "Combined":
                default:
                    contentTypes = new String[8];
                    contentTypes[0] = "Story";
                    contentTypes[1] = "Game";
                    contentTypes[2] = "TextBook";
                    contentTypes[3] = "Collection";
                    contentTypes[4] = "Worksheet";
                    contentTypes[5] = "Course";
                    contentTypes[6] = "Resource";
                    contentTypes[7] = "LessonPlan";
                    break;
            }

            if (BuildConfig.FILTER_CONTENT_BY_CHANNEL_ID) {
                String channelId = PreferenceManager.getDefaultSharedPreferences(GlobalApplication.getInstance()).getString("channelId", "__failed");
                if (StringUtil.isNullOrEmpty(channelId) || channelId.equals("__failed")) {
                    channelId = BuildConfig.CHANNEL_ID;
                }
                builder.channel(new String[]{channelId});
            }

            boolean isProfileContent = false;
            String fp;
            ContentSearchCriteria filters;
            if (!StringUtil.isNullOrEmpty(filterParams) && !viewMoreClicked) {
                if (filterParams.equals("userToken")) {     // Get content created by user.
                    isProfileContent = true;
                    builder.contentTypes(contentTypes).limit(count);
                    builder.createdBy(new String[]{query});
                    filters = builder.build();
                } else {        // Filter applied
                    fp = filterParams.replaceAll("\"\\{", "{").replaceAll("\\}\"", "}").replaceAll("\\\\\"", "\"");
                    filters = GsonUtil.fromJson(fp, ContentSearchCriteria.class);
                }
            } else {
                builder.contentTypes(contentTypes).query(query).limit(count);
                builder.facets(new String[]{"language", "grade", "domain", "contentType", "subject", "medium"});
                filters = builder.build();
            }

            final boolean finalIsProfileContent = isProfileContent;
            mGenieAsyncService.getContentService().searchContent(filters, new IResponseHandler<ContentSearchResult>() {
                @Override
                public void onSuccess(GenieResponse<ContentSearchResult> genieResponse) {
                    contentSearchResult = genieResponse.getResult();

                    List<ContentData> list = contentSearchResult.getContentDataList();
                    String jsonInString = GsonUtil.toJson(list);
                    String filterCriteria = GsonUtil.toJson(contentSearchResult.getFilterCriteria());
                    String enc = Base64Util.encodeToString(jsonInString.getBytes(), Base64Util.DEFAULT);

                    String javascript = String.format("window.callJSCallback('%s','%s','%s');", callback, enc, filterCriteria);
                    dynamicUI.addJsToWebView(javascript);

                    String pageId = TelemetryPageId.HOME;
                    switch (type) {
                        case "Course":
                            Util.setCorrelationContext(CorrelationContext.COURSE_SEARCH);
                            pageId = TelemetryPageId.COURSES;
                            break;

                        case "Library":
                            Util.setCorrelationContext(CorrelationContext.LIBRARY_SEARCH);
                            pageId = TelemetryPageId.LIBRARY;
                            break;

                        case "Combined":
                            if (finalIsProfileContent) {
                                Util.setCorrelationContext(CorrelationContext.CONTENT_PROFILE_SEARCH);
                                pageId = TelemetryPageId.PROFILE;
                            } else {
                                Util.setCorrelationContext(CorrelationContext.ALL_CONTENT_SEARCH);
                                pageId = TelemetryPageId.HOME;
                            }
                            break;

                        default:
                            Util.setCorrelationContext(CorrelationContext.NONE);
                            break;
                    }

                    Util.setCorrelationId(contentSearchResult.getResponseMessageId());
                    Util.setCorrelationType(contentSearchResult.getId());

                    TelemetryHandler.saveTelemetry(TelemetryBuilder.buildImpressionEvent(ImpressionType.SEARCH, null, pageId, ContextEnvironment.USER, Util.getCorrelationList()));

                    Map<String, Object> params = new HashMap<>();
                    if (!StringUtil.isNullOrEmpty(query)) {
                        params.put(TelemetryConstant.SEARCH_QUERY, query);
                    }
                    params.put(TelemetryConstant.SEARCH_RESULTS, list.size());
                    params.put(TelemetryConstant.SEARCH_CRITERIA, contentSearchResult.getRequest());
                    TelemetryHandler.saveTelemetry(TelemetryBuilder.buildLogEvent(pageId, ImpressionType.SEARCH, pageId, ContextEnvironment.USER, params));
                }

                @Override
                public void onError(GenieResponse<ContentSearchResult> genieResponse) {
                    String javascript = String.format("window.callJSCallback('%s','%s','%s');", callback, "error", genieResponse.getError());
                    dynamicUI.addJsToWebView(javascript);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setUserProfile(String user_id) {
        mGenieAsyncService.getUserService().setCurrentUser(user_id, new IResponseHandler<Void>() {
            @Override
            public void onSuccess(GenieResponse<Void> genieResponse) {
            }

            @Override
            public void onError(GenieResponse<Void> genieResponse) {
            }
        });
    }

    public void createUserProfile(final String uid, final boolean isGuestMode) {
        Profile profile;
        if(!isGuestMode) {
            profile = new Profile(uid, "avatar", "en");
            profile.setUid(uid);
        } else {
            profile = new Profile("Guest1", "avatar", "en");
        }

        mGenieAsyncService.getUserService().createUserProfile(profile, new IResponseHandler<Profile>() {
            @Override
            public void onSuccess(GenieResponse<Profile> genieResponse) {
                if(isGuestMode) {
                    setUserProfile(genieResponse.getResult().getUid());
                } else {
                    setUserProfile(uid);
                }
//                genieResponse.getResult().getUid();
            }

            @Override
            public void onError(GenieResponse<Profile> genieResponse) {
            }
        });
    }

    public void getAllUserProfiles(final String uid, final boolean guestMode) {
        if (guestMode) {
            createUserProfile(uid, guestMode);
        } else {
            mGenieAsyncService.getUserService().getAllUserProfile(new IResponseHandler<List<Profile>>() {
                @Override
                public void onSuccess(GenieResponse<List<Profile>> genieResponse) {
                    Boolean status = true;
                    List<Profile> profileList = genieResponse.getResult();
                    for (Profile profile : profileList) {
                        if (uid.equals(profile.getUid())) {
                            setUserProfile(uid);
                            status = false;
                        }
                    }
                    if (status) {
                        createUserProfile(uid, guestMode);
                    }
                }

                @Override
                public void onError(GenieResponse<List<Profile>> genieResponse) {
                }
            });
        }
    }

    public void setAnonymousProfile() {
        mGenieAsyncService.getUserService().setAnonymousUser(new IResponseHandler<String>() {
            @Override
            public void onSuccess(GenieResponse<String> genieResponse) {

            }

            @Override
            public void onError(GenieResponse<String> genieResponse) {

            }
        });
    }

    public void importCourse(final String course_id, String isChild, final String[] callbacks) {
        File directory = new File(Environment.getExternalStorageDirectory() + File.separator + Constants.EXTERNAL_PATH);
        directory.mkdirs();

        File noMediaFile = new File(directory.getAbsolutePath() + "/" + ".nomedia");
        if (!noMediaFile.exists()) {
            try {
                noMediaFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        ContentImportRequest.Builder builder = new ContentImportRequest.Builder();

        ContentImport contentImport;
        if (isChild.equals("true")) {
            contentImport = new ContentImport(course_id, true, String.valueOf(directory));
        } else {
            contentImport = new ContentImport(course_id, String.valueOf(directory));
        }
        contentImport.setCorrelationData(Util.getCorrelationList());
        builder.add(contentImport);

        final CallbackContainer cbHandler = new CallbackContainer(course_id, callbacks);
//        EventBus.getDefault().unregister(cbHandler);
        EventBus.getDefault().register(cbHandler);
        mGenieAsyncService.getContentService().importContent(builder.build(), new IResponseHandler<List<ContentImportResponse>>() {
            @Override
            public void onSuccess(GenieResponse<List<ContentImportResponse>> genieResponse) {
//                EventBus.getDefault().unregister(cbHandler);

                List<ContentImportResponse> contentImportResponseList = genieResponse.getResult();
                if (contentImportResponseList.get(0).getStatus() == ContentImportStatus.NOT_FOUND){
                    EventBus.getDefault().unregister(cbHandler);
                }
            }

            @Override
            public void onError(GenieResponse<List<ContentImportResponse>> genieResponse) {
                List<ContentImportResponse> contentImportResponseList = genieResponse.getResult();
                String date = String.format("window.callJSCallback('%s', '%s', '%s');", callbacks[0], course_id, GsonUtil.toJson(contentImportResponseList.get(0)));
                dynamicUI.addJsToWebView(date);
                EventBus.getDefault().unregister(cbHandler);
            }
        });
    }

    public void downloadAllContent(final String[] mIdentifierList, final String[] callbacks) {
        File directory = new File(Environment.getExternalStorageDirectory() + File.separator + Constants.EXTERNAL_PATH);
        directory.mkdirs();

        File noMediaFile = new File(directory.getAbsolutePath() + "/" + ".nomedia");
        if (!noMediaFile.exists()) {
            try {
                noMediaFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        ContentImportRequest.Builder builder = new ContentImportRequest.Builder();
        for (String identifier : mIdentifierList) {
            ContentImport contentImport = new ContentImport(identifier, true, String.valueOf(directory));
            contentImport.setCorrelationData(Util.getCorrelationList());
            builder.add(contentImport);
        }

        CallbackContainer cbHandler = new CallbackContainer(mIdentifierList[0], callbacks);
        EventBus.getDefault().unregister(cbHandler);
        EventBus.getDefault().register(cbHandler);
        mGenieAsyncService.getContentService().importContent(builder.build(), new IResponseHandler<List<ContentImportResponse>>() {
            @Override
            public void onSuccess(GenieResponse<List<ContentImportResponse>> genieResponse) {
//                EventBus.getDefault().unregister(this);

//                List<ContentImportResponse> contentImportResponseList = genieResponse.getResult();
//                for (ContentImportResponse contentImportResponse : contentImportResponseList) {
//                    JSONObject jb = new JSONObject();
//                    try {
//                        jb.put("status", contentImportResponse.getStatus().toString());
//                        jb.put("identifier", contentImportResponse.getIdentifier() );
//                    } catch (JSONException e) {
//                        e.printStackTrace();
//                    }
//                    Log.d("download all status", jb.toString());
//                    String date = String.format("window.__getDownloadStatus('%s');", jb.toString());
//                    dynamicUI.addJsToWebView(date);
//                }
                String importResponse = GsonUtil.toJson(genieResponse);
                String javascript = String.format("window.callJSCallback('%s','%s','%s','%s');", callbacks[0], "importContentSuccessResponse", mIdentifierList[0], importResponse);
                dynamicUI.addJsToWebView(javascript);
            }

            @Override
            public void onError(GenieResponse<List<ContentImportResponse>> genieResponse) {
                String importResponse = GsonUtil.toJson(genieResponse);
                String javascript = String.format("window.callJSCallback('%s','%s','%s','%s');", callbacks[0], "importContentErrorResponse", mIdentifierList[0], importResponse);
                dynamicUI.addJsToWebView(javascript);
            }
        });
    }


    public void deleteContent(String content_id, final String callback) {
        ContentDeleteRequest.Builder contentDeleteBuilder = new ContentDeleteRequest.Builder();
        contentDeleteBuilder.add(new ContentDelete(content_id));
        mGenieAsyncService.getContentService().deleteContent(contentDeleteBuilder.build(), new IResponseHandler<List<ContentDeleteResponse>>() {
            @Override
            public void onSuccess(GenieResponse<List<ContentDeleteResponse>> genieResponse) {
                String result = GsonUtil.toJson(genieResponse.getResult());
                String javascript = String.format("window.callJSCallback('%s','%s');", callback, genieResponse.getMessage());
                dynamicUI.addJsToWebView(javascript);

            }

            @Override
            public void onError(GenieResponse<List<ContentDeleteResponse>> genieResponse) {

            }
        });
    }

    public void stopEventBus() {
        if (telemetryListener != null)
            EventBus.getDefault().unregister(telemetryListener);
    }

    public void syncTelemetry() {
        Log.d("telemetry auto","synced");
        mGenieAsyncService.getSyncService().sync(new IResponseHandler<SyncStat>() {
            @Override
            public void onSuccess(GenieResponse<SyncStat> genieResponse) {
            }

            @Override
            public void onError(GenieResponse<SyncStat> genieResponse) {
            }
        });
    }

    public void importEcarFile(String ecarFilePath, String[] callbacks) {
        File directory = new File(Environment.getExternalStorageDirectory() + File.separator + Constants.EXTERNAL_PATH);
        directory.mkdirs();
        File noMediaFile = new File(directory.getAbsolutePath() + File.separator + Constants.EXTERNAL_PATH + "/" + ".nomedia");
        if (!noMediaFile.exists()) {
            try {
                noMediaFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        String id = "1234567890";
        final CallbackContainer cbHandler = new CallbackContainer(id, callbacks);
        EventBus.getDefault().register(cbHandler);
        EcarImportRequest ecarImportRequest = new EcarImportRequest.Builder().fromFilePath(ecarFilePath).toFolder(String.valueOf(directory)).build();
        mGenieAsyncService.getContentService().importEcar(ecarImportRequest, new IResponseHandler<List<ContentImportResponse>>() {
            @Override
            public void onSuccess(GenieResponse<List<ContentImportResponse>> genieResponse) {

                List<ContentImportResponse> result = genieResponse.getResult();
                boolean launchDetail = true;
                if (!CollectionUtil.isNullOrEmpty(result)) {
                    for (ContentImportResponse contentImportResponse : result) {
                        switch (contentImportResponse.getStatus()) {
                            case ALREADY_EXIST:
                            case IMPORT_COMPLETED:
                                EventBus.getDefault().unregister(cbHandler);
                                break;
                        }
                    }
                }
            }

            @Override
            public void onError(GenieResponse<List<ContentImportResponse>> genieResponse) {
                EventBus.getDefault().unregister(cbHandler);
            }

        });
    }

    public void cancelDownload(String contentId) {
        try {
            mGenieAsyncService.getContentService().cancelDownload(contentId, new IResponseHandler<Void>() {
                @Override
                public void onSuccess(GenieResponse<Void> genieResponse) {
                }

                @Override
                public void onError(GenieResponse<Void> genieResponse) {

                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private CallbackContainer telemetryListener;

    public void playContent(String playContent, String cb) {

        Content content = GsonUtil.fromJson(playContent, Content.class);

        telemetryListener = new CallbackContainer(content.getIdentifier(), cb);
        EventBus.getDefault().unregister(telemetryListener);
        EventBus.getDefault().register(telemetryListener);

        List<CorrelationData> cdata = TelemetryUtil.computeCData(content.getHierarchyInfo());
        CurrentGame currentGame = new CurrentGame(content.getIdentifier(), String.valueOf(System.currentTimeMillis()), content.getContentType());
        currentGame.setcData(cdata);
        TelemetryUtil.addCurrentGame(currentGame);
        //TODO Telemetry
//        TelemetryHandler.saveTelemetry(TelemetryBuilder.buildGEInteractWithCoRelation(InteractionType.TOUCH, TelemetryPageId.CONTENT_DETAIL, TelemetryAction.CONTENT_PLAY, content.getIdentifier(), null, Util.getCorrelationList()));
        String mimeType = content.getMimeType();
        if (mimeType.equals("video/x-youtube")) {
            ContentPlayer.play(activity, content, null);
        } else if (content.isAvailableLocally()) {
            ContentPlayer.play(activity, content, null);
        } else {
            Toast.makeText(activity, "Content Not Available", Toast.LENGTH_LONG).show();
        }
    }

    public void endContent() {

    }

    public void exportEcar(String contentId, final String callback) {
        List<String> ContentIds = new ArrayList<String>();
        ContentIds.add(contentId);
        File directory = new File(Environment.getExternalStorageDirectory() + File.separator + "/Ecars");
        ContentExportRequest.Builder builder = new ContentExportRequest.Builder();
        builder.exportContents(ContentIds).toFolder(String.valueOf(directory));
        mGenieAsyncService.getContentService().exportContent(builder.build(), new IResponseHandler<ContentExportResponse>() {
            @Override
            public void onSuccess(GenieResponse<ContentExportResponse> genieResponse) {
                ContentExportResponse contentExportResponse = genieResponse.getResult();
                String ecarPath = contentExportResponse.getExportedFilePath();
                String javascript = String.format("window.callJSCallback('%s','%s');", callback, ecarPath);
                dynamicUI.addJsToWebView(javascript);
            }

            @Override
            public void onError(GenieResponse<ContentExportResponse> genieResponse) {
                String javascript = String.format("window.callJSCallback('%s','%s');", callback, "failure");
                dynamicUI.addJsToWebView(javascript);
            }
        });
    }

    private Profile currentProfile;

    public Profile getCurrentUserProfile() {
        return mGenieService.getUserService().getCurrentUser().getResult();
    }

    public String getBoards() {
        GenieResponse<MasterData> boardData = mGenieService.getConfigService().getMasterData(MasterDataType.BOARD);
        List<MasterDataValues> vals = boardData.getResult().getValues();
        JSONArray boards = new JSONArray();
        for (int i = 0; i < vals.size(); i++) {
            JSONObject board = new JSONObject();
            try {
                board.put("label", vals.get(i).getLabel());
                board.put("value", vals.get(i).getLabel());
                boards.put(board);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return Base64Util.encodeToString(boards.toString().getBytes(), Base64Util.DEFAULT);
    }

    public String getMediums() {
        GenieResponse<MasterData> mediumData = mGenieService.getConfigService().getMasterData(MasterDataType.MEDIUM);
        List<MasterDataValues> vals = mediumData.getResult().getValues();
        JSONArray mediums = new JSONArray();
        for (int i = 0; i < vals.size(); i++) {
            JSONObject medium = new JSONObject();
            try {
                medium.put("label", vals.get(i).getLabel());
                medium.put("value", vals.get(i).getLabel());
                mediums.put(medium);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return Base64Util.encodeToString(mediums.toString().getBytes(), Base64Util.DEFAULT);
    }

    public String getSubjects() {
        GenieResponse<MasterData> mediumData = mGenieService.getConfigService().getMasterData(MasterDataType.SUBJECT);
        List<MasterDataValues> vals = mediumData.getResult().getValues();
        JSONArray subjects = new JSONArray();
        for (int i = 0; i < vals.size(); i++) {
            JSONObject sub = new JSONObject();
            try {
                sub.put("label", vals.get(i).getLabel());
                sub.put("value", vals.get(i).getLabel());
                subjects.put(sub);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return Base64Util.encodeToString(subjects.toString().getBytes(), Base64Util.DEFAULT);
    }

    public String getGrades() {
        HashMap<String, String> mClassMap = new LinkedHashMap<String, String>() {{
            put("KG", "0");
            put("Grade 1", "1");
            put("Grade 2", "2");
            put("Grade 3", "3");
            put("Grade 4", "4");
            put("Grade 5", "5");
            put("Grade 6", "6");
            put("Grade 7", "7");
            put("Grade 8", "8");
            put("Grade 9", "9");
            put("Grade 10", "10");
            put("Grade 11", "11");
            put("Grade 12", "12");
            put("Others", "-1");
        }};
        JSONArray grades = new JSONArray();
        for (String key: mClassMap.keySet()) {
            JSONObject grade = new JSONObject();
            try {
                grade.put("label", key);
                grade.put("value", mClassMap.get(key));
                grades.put(grade);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return Base64Util.encodeToString(grades.toString().getBytes(), Base64Util.DEFAULT);
    }

    public String getCurrentProfileData() {
        if (currentProfile == null) {
            currentProfile = getCurrentUserProfile();
        }
        JSONObject profileData = new JSONObject();
        try {
            profileData.put("handle", currentProfile.getHandle());
            profileData.put("medium", currentProfile.getMedium());
            profileData.put("grade", currentProfile.getStandard());
            profileData.put("board", currentProfile.getBoard());

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return Base64Util.encodeToString(profileData.toString().getBytes(), Base64Util.DEFAULT);
    }

    public void updateProfile(String handle, String medium, String grade, String board) {
        Log.d(TAG, "updateProfile: " + handle + " " + medium + " " + grade + " " + board);
        if (currentProfile == null) {
            currentProfile = getCurrentUserProfile();
        }
        currentProfile.setHandle(handle);
        currentProfile.setMedium(medium);
        currentProfile.setStandard(Integer.parseInt(grade));
        currentProfile.setBoard(board);
        mGenieService.getUserService().updateUserProfile(currentProfile);
    }

    class CallbackContainer {
        private String id;
        private String downloadProgressCb, contentImportResponseCb, contentImportCb, telemetryCb;

        public CallbackContainer (String identifier, String[] callbacks) {
            this.id = identifier;
            this.downloadProgressCb = callbacks[0];
            this.contentImportCb = callbacks[1];
            this.contentImportResponseCb = callbacks[2];
        }

        public CallbackContainer (String identifier, String telemetryCallback) {
            this.id = identifier;
            this.telemetryCb = telemetryCallback;
        }

        @Subscribe(threadMode = ThreadMode.MAIN)
        public void onDownloadProgress(DownloadProgress downloadProgress) throws InterruptedException {

            String downloadResponse = GsonUtil.toJson(downloadProgress);
            String javascript = String.format("window.callJSCallback('%s', '%s', '%s', '%s');", this.downloadProgressCb, "onDownloadProgress", this.id, downloadResponse);
            dynamicUI.addJsToWebView(javascript);
        }

        @Subscribe(threadMode = ThreadMode.MAIN)
        public void onContentImportResponse(ContentImportResponse contentImportResponse) throws InterruptedException {
            String importResponse = GsonUtil.toJson(contentImportResponse);
            String javascript = String.format("window.callJSCallback('%s', '%s', '%s', '%s');", this.contentImportResponseCb, "onContentImportResponse", this.id, importResponse);
            dynamicUI.addJsToWebView(javascript);

        }

        @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
        public void onContentImport(ImportContentProgress importContentProgress) throws InterruptedException {
            String importResponse = GsonUtil.toJson(importContentProgress);
            String javascript = String.format("window.callJSCallback('%s', '%s', '%s', '%s');", this.contentImportCb, "onContentImportProgress", this.id, importResponse);
            dynamicUI.addJsToWebView(javascript);

        }

        @Subscribe(threadMode = ThreadMode.MAIN)
        public void onTelemetryEvent(Telemetry telemetryEvent) throws InterruptedException {
            String enc = Base64Util.encodeToString(telemetryEvent.toString().getBytes(), Base64Util.DEFAULT);
            String javascript = String.format("window.callJSCallback('%s', '%s', '%s', '%s');", this.telemetryCb, "onTelemetryEvent", this.id, enc);
            dynamicUI.addJsToWebView(javascript);
        }
    }
}
