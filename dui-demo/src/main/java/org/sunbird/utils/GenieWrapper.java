package org.sunbird.utils;

import android.app.Activity;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;

import org.ekstep.genieservices.GenieService;
import org.ekstep.genieservices.async.GenieAsyncService;
import org.ekstep.genieservices.commons.IResponseHandler;
import org.ekstep.genieservices.commons.bean.ChildContentRequest;
import org.ekstep.genieservices.commons.bean.Content;
import org.ekstep.genieservices.commons.bean.ContentData;
import org.ekstep.genieservices.commons.bean.ContentDeleteRequest;
import org.ekstep.genieservices.commons.bean.ContentDetailsRequest;
import org.ekstep.genieservices.commons.bean.ContentExportRequest;
import org.ekstep.genieservices.commons.bean.ContentExportResponse;
import org.ekstep.genieservices.commons.bean.ContentFilterCriteria;
import org.ekstep.genieservices.commons.bean.ContentImportRequest;
import org.ekstep.genieservices.commons.bean.ContentImportResponse;
import org.ekstep.genieservices.commons.bean.ContentSearchCriteria;
import org.ekstep.genieservices.commons.bean.ContentSearchResult;
import org.ekstep.genieservices.commons.bean.CorrelationData;
import org.ekstep.genieservices.commons.bean.DownloadProgress;
import org.ekstep.genieservices.commons.bean.EcarImportRequest;
import org.ekstep.genieservices.commons.bean.GenieResponse;
import org.ekstep.genieservices.commons.bean.HierarchyInfo;
import org.ekstep.genieservices.commons.bean.Profile;
import org.ekstep.genieservices.commons.bean.SyncStat;
import org.ekstep.genieservices.commons.bean.enums.InteractionType;
import org.ekstep.genieservices.commons.bean.telemetry.Telemetry;
import org.ekstep.genieservices.commons.utils.Base64Util;
import org.ekstep.genieservices.commons.utils.GsonUtil;
import org.ekstep.genieservices.commons.utils.StringUtil;
import org.ekstep.genieservices.utils.ContentPlayer;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;
import org.sunbird.models.CurrentGame;
import org.sunbird.telemetry.TelemetryAction;
import org.sunbird.telemetry.TelemetryBuilder;
import org.sunbird.telemetry.TelemetryConstant;
import org.sunbird.telemetry.TelemetryHandler;
import org.sunbird.telemetry.TelemetryStageId;
import org.sunbird.telemetry.TelemetryUtil;
import org.sunbird.telemetry.enums.CoRelationIdContext;
import org.sunbird.telemetry.enums.EntityType;
import org.sunbird.ui.MainActivity;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
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
                dynamicUI.addJsToWebView(javascript);
            }

            @Override
            public void onError(GenieResponse<String> genieResponse) {
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
        builder.contentTypes(new String[]{"Story", "Worksheet", "Collection", "Game", "TextBook", "Course"});

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


    public void searchContent(final String callback, final String filterParams, final String query, final String type, final String status, final int count) {
        try {
            ContentSearchCriteria.SearchBuilder builder = new ContentSearchCriteria.SearchBuilder();
            String[] strings;
            int stageId = -1;
            String filter_stageId, correlationId;
            if (type.equals("Combined")) {
                stageId = COURSE_AND_RESOURCE_SEARCH;
                filter_stageId = TelemetryStageId.COURSE_AND_RESOURSE_LIST;
                strings = new String[6];
                strings[0] = "Story";
                strings[1] = "Game";
                strings[2] = "TextBook";
                strings[3] = "Collection";
                strings[4] = "Worksheet";
                strings[5] = "Course";
            } else if (type.equals("Course")) {
                stageId = COURSE_SEARCH;
                filter_stageId = TelemetryStageId.COURSE_LIST;
                strings = new String[1];
                strings[0] = "Course";
            } else {
                stageId = RESOURCE_SEARCH;
                filter_stageId = TelemetryStageId.RESOURCE_LIST;
                strings = new String[5];
                strings[0] = "Story";
                strings[1] = "Game";
                strings[2] = "TextBook";
                strings[3] = "Collection";
                strings[4] = "Worksheet";
            }

            String fp;

            ContentSearchCriteria filters;
            if (filterParams.length() > 0 && filterParams.equals("userToken")){
                builder.contentTypes(strings).limit(count);
                builder.createdBy(query);
                filters = builder.build();
            } else if (filterParams.length() > 10 && status.equals("true")) {
                fp = filterParams.replaceAll("\"\\{", "{").replaceAll("\\}\"", "}").replaceAll("\\\\\"", "\"");
                filters = GsonUtil.fromJson(fp, ContentSearchCriteria.class);
//                Util.setCoRelationIdContext(correlationId);
//                Map<String, Object> eksMap = new HashMap<>();
//                eksMap.put(TelemetryConstant.FILTER_CRITERIA, filters);
////                TelemetryHandler.saveTelemetry(TelemetryBuilder.buildGEInteract(InteractionType.TOUCH,filter_stageId, TelemetryAction.FILTER_PHRASE,"",eksMap,Util.getCoRelationList()));
            } else {
                builder.contentTypes(strings).query(query).limit(count);
                builder.facets(new String[]{"language", "grade", "domain", "contentType", "subject", "medium"});
                filters = builder.build();
            }

            final int finalStageId = stageId;
            mGenieAsyncService.getContentService().searchContent(filters, new IResponseHandler<ContentSearchResult>() {
                @Override
                public void onSuccess(GenieResponse<ContentSearchResult> genieResponse) {

                    contentSearchResult = genieResponse.getResult();
                    Util.setCoRelationType(contentSearchResult.getId());

                    List<ContentData> list = contentSearchResult.getContentDataList();
                    String jsonInString = GsonUtil.toJson(list);
                    String filterCriteria = GsonUtil.toJson(contentSearchResult.getFilterCriteria());
                    String enc = Base64Util.encodeToString(jsonInString.getBytes(), Base64Util.DEFAULT);

                    String javascript = String.format("window.callJSCallback('%s','%s','%s');", callback, enc, filterCriteria);
                    dynamicUI.addJsToWebView(javascript);

                    String stageIdValue = null;
                    switch (finalStageId) {
                        case COURSE_AND_RESOURCE_SEARCH:
                            Util.setCoRelationIdContext(CoRelationIdContext.COURSE_AND_RESOURCE_SEARCH);
                            Util.setCourseandResourceSearchApiResponseMessageId(contentSearchResult.getResponseMessageId());
                            stageIdValue = TelemetryStageId.COURSE_AND_RESOURCE_SEARCH;
                            break;

                        case COURSE_SEARCH:
                            Util.setCoRelationIdContext(CoRelationIdContext.COURSE_SEARCH);
                            Util.setCourseSearchApiResponseMessageId(contentSearchResult.getResponseMessageId());
                            stageIdValue = TelemetryStageId.COURSE_SEARCH;
                            break;

                        case RESOURCE_SEARCH:
                            Util.setCoRelationIdContext(CoRelationIdContext.RESOURCE_SEARCH);
                            Util.setResourceSearchApiResponseMessageId(contentSearchResult.getResponseMessageId());
                            stageIdValue = TelemetryStageId.RESOURCE_SEARCH;
                            break;
                    }

                    Map<String, Object> values = new HashMap<>();
                    values.put(TelemetryConstant.SEARCH_RESULTS, list.size());
                    values.put(TelemetryConstant.SEARCH_CRITERIA, contentSearchResult.getRequest());

                    TelemetryHandler.saveTelemetry(TelemetryBuilder.buildGEInteractWithCoRelation(InteractionType.TOUCH, stageIdValue, EntityType.SEARCH_PHRASE, query, values, Util.getCoRelationList()));
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

    public void createUserProfile(final String uid) {
        Profile profile = new Profile(uid, "avatar", "en");
        profile.setUid(uid);

        mGenieAsyncService.getUserService().createUserProfile(profile, new IResponseHandler<Profile>() {
            @Override
            public void onSuccess(GenieResponse<Profile> genieResponse) {
                setUserProfile(uid);
            }

            @Override
            public void onError(GenieResponse<Profile> genieResponse) {
            }
        });
    }

    public void getAllUserProfiles(final String uid) {

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
                    createUserProfile(uid);
                }
            }

            @Override
            public void onError(GenieResponse<List<Profile>> genieResponse) {
            }
        });
    }

    public void importCourse(final String course_id, String isChild) {
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

        List<String> contentIds = new ArrayList<String>();
        contentIds.add(course_id);
        ContentImportRequest.Builder builder = new ContentImportRequest.Builder();
        builder.toFolder(String.valueOf(directory))
                .contentIds(contentIds);

        if (isChild.equals("true")) {
            builder.childContent();
        }
        builder.correlationData(Util.getCoRelationList());
        EventBus.getDefault().unregister(this);
        EventBus.getDefault().register(this);
        mGenieAsyncService.getContentService().importContent(builder.build(), new IResponseHandler<List<ContentImportResponse>>() {
            @Override
            public void onSuccess(GenieResponse<List<ContentImportResponse>> genieResponse) {
                List<ContentImportResponse> contentImportResponseList = genieResponse.getResult();
                for (ContentImportResponse contentImportResponse : contentImportResponseList) {
                    JSONObject jb = new JSONObject();
                    try {
                        jb.put("status", contentImportResponse.getStatus().toString());
                        jb.put("identifier", course_id);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    //{"downloadId":1727,"downloadProgress":51,"identifier":"do_21216425596451225628","status":1}
                    String date = String.format("window.__getDownloadStatus('%s');", jb.toString());
                    dynamicUI.addJsToWebView(date);

                }
            }

            @Override
            public void onError(GenieResponse<List<ContentImportResponse>> genieResponse) {
            }
        });


    }

    public void deleteContent(String content_id, final String callback) {
        ContentDeleteRequest.Builder contentDeleteBuilder = new ContentDeleteRequest.Builder();
        contentDeleteBuilder.contentId(content_id);
        mGenieAsyncService.getContentService().deleteContent(contentDeleteBuilder.build(), new IResponseHandler<Void>() {
            @Override
            public void onSuccess(GenieResponse<Void> genieResponse) {
                String result = GsonUtil.toJson(genieResponse.getResult());
                String javascript = String.format("window.callJSCallback('%s','%s');", callback, genieResponse.getMessage());
                dynamicUI.addJsToWebView(javascript);

            }

            @Override
            public void onError(GenieResponse<Void> genieResponse) {

            }
        });
    }

    public void stopEventBus() {
        EventBus.getDefault().unregister(this);
    }

    public void syncTelemetry() {
        mGenieAsyncService.getSyncService().sync(new IResponseHandler<SyncStat>() {
            @Override
            public void onSuccess(GenieResponse<SyncStat> genieResponse) {
            }

            @Override
            public void onError(GenieResponse<SyncStat> genieResponse) {
            }
        });
    }

    public void importEcarFile(String ecarFilePath) {
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
        EventBus.getDefault().unregister(this);
        EventBus.getDefault().register(this);
        EcarImportRequest ecarImportRequest = new EcarImportRequest.Builder().fromFilePath(ecarFilePath).toFolder(String.valueOf(directory)).build();
        mGenieAsyncService.getContentService().importEcar(ecarImportRequest, new IResponseHandler<List<ContentImportResponse>>() {
            @Override
            public void onSuccess(GenieResponse<List<ContentImportResponse>> genieResponse) {
            }

            @Override
            public void onError(GenieResponse<List<ContentImportResponse>> genieResponse) {
                String importResponse = String.format("window.__onContentImportResponse('%s');", "ALREADY_EXIST");
                dynamicUI.addJsToWebView(importResponse);
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

    public void playContent(String playContent) {

        EventBus.getDefault().unregister(this);
        EventBus.getDefault().register(this);
        Content content = GsonUtil.fromJson(playContent, Content.class);
        List<CorrelationData> cdata = TelemetryUtil.computeCData(content.getHierarchyInfo());
        CurrentGame currentGame = new CurrentGame(content.getIdentifier(), String.valueOf(System.currentTimeMillis()), content.getContentType());
        currentGame.setcData(cdata);
        TelemetryUtil.addCurrentGame(currentGame);
        TelemetryHandler.saveTelemetry(TelemetryBuilder.buildGEInteractWithCoRelation(InteractionType.TOUCH, TelemetryStageId.CONTENT_DETAIL, TelemetryAction.CONTENT_PLAY, content.getIdentifier(), null, Util.getCoRelationList()));
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onDownloadProgress(DownloadProgress downloadProgress) throws InterruptedException {

        String downloadResponse = GsonUtil.toJson(downloadProgress);
        Log.e(" PROGRESSS :>", downloadResponse.toString());
        String date = String.format("window.__getDownloadStatus('%s');", downloadResponse);
        dynamicUI.addJsToWebView(date);

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onContentImportResponse(ContentImportResponse contentImportResponse) throws InterruptedException {
        Log.e("File :--------->", "downloaded");
        Log.e("import ", GsonUtil.toJson(contentImportResponse));

        switch (contentImportResponse.getStatus()) {
            case IMPORT_COMPLETED:
                Map<String, Object> downloadResponse = new HashMap<>();
                downloadResponse.put("identifier", contentImportResponse.getIdentifier());
                downloadResponse.put("downloadProgress", "100");
                downloadResponse.put("FROM OUTSIDE1", 1);
                String date = String.format("window.__getDownloadStatus('%s');", GsonUtil.toJson(downloadResponse));

                String importResponse = String.format("window.__onContentImportResponse('%s');", GsonUtil.toJson(contentImportResponse));
                dynamicUI.addJsToWebView(importResponse);

                if (!StringUtil.isNullOrEmpty(contentImportResponse.getIdentifier())) {
                    dynamicUI.addJsToWebView(date);
                }
                break;
            case DOWNLOAD_COMPLETED:
                break;
            case DOWNLOAD_FAILED:
                break;
            case DOWNLOAD_STARTED:
                break;
            case ENQUEUED_FOR_DOWNLOAD:
                break;
            case IMPORT_FAILED:
                break;
            case IMPORT_STARTED:
                break;
            case NOT_FOUND:
                break;
        }
        // mPresenter.manageImportSuccess(contentImportResponse);

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onTelemetryEvent(Telemetry telemetryEvent) throws InterruptedException {
        String enc = Base64Util.encodeToString(telemetryEvent.toString().getBytes(), Base64Util.DEFAULT);
        ;
        String events = String.format("window.__getGenieEvents('%s');", enc);
        dynamicUI.addJsToWebView(events);
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

}
