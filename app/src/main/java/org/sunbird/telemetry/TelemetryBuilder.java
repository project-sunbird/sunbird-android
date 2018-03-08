package org.sunbird.telemetry;

import android.content.Context;
import android.text.TextUtils;

import org.ekstep.genieservices.GenieService;
import org.ekstep.genieservices.commons.ILocationInfo;
import org.ekstep.genieservices.commons.bean.CorrelationData;
import org.ekstep.genieservices.commons.bean.enums.InteractionType;
import org.ekstep.genieservices.commons.bean.telemetry.DeviceSpecification;
import org.ekstep.genieservices.commons.bean.telemetry.End;
import org.ekstep.genieservices.commons.bean.telemetry.Impression;
import org.ekstep.genieservices.commons.bean.telemetry.Interact;
import org.ekstep.genieservices.commons.bean.telemetry.Interrupt;
import org.ekstep.genieservices.commons.bean.telemetry.Log;
import org.ekstep.genieservices.commons.bean.telemetry.Rollup;
import org.ekstep.genieservices.commons.bean.telemetry.Start;
import org.ekstep.genieservices.commons.bean.telemetry.Visit;
import org.ekstep.genieservices.utils.DeviceSpec;
import org.sunbird.telemetry.enums.Workflow;
import org.sunbird.utils.Util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.Integer.parseInt;

//import android.util.Log;

/**
 * Created by anil on 5/17/2016.
 * <p/>
 * All the events related to interaction.
 */
public class TelemetryBuilder {
    private static String TAG = "TelemetryBuilder";

//    private static GEInteract buildGEInteract(InteractionType interactionType, String stageId, String subType, String id, List<Map<String, Object>> values, List<CorrelationData> coRelationList) {
//        GEInteract geInteract = new GEInteract.Builder()
//                .interActionType(interactionType)
//                .stageId(stageId)
//                .subType(subType)
//                .id(id)
//                .correlationData(coRelationList)
//                .values(values)
//                .build();
//        Log.i("GE_INTERACT", geInteract.toString());
//        return geInteract;
//    }
//
//    public static GEInteract buildGEInteractWithCoRelation(InteractionType interactionType, String stageId, String subType, String id, Map<String, Object> values, List<CorrelationData> coRelationList) {
//        return buildGEInteract(interactionType, stageId, subType, id, buildValueList(values), coRelationList);
//    }
//
//    public static GEInteract buildGEInteract(InteractionType interactionType, String stageId, String subType, String id, Map<String, Object> values) {
//        return buildGEInteract(interactionType, stageId, subType, id, buildValueList(values), new ArrayList<CorrelationData>());
//    }
//
//    public static GEInteract buildGEInteract(String stageId) {
//        return buildGEInteract(InteractionType.SHOW, stageId, null, null, new ArrayList<Map<String, Object>>(), new ArrayList<CorrelationData>());
//    }

    public static Interrupt buildInterruptEvent(String type, String env) {
        Interrupt.Builder interrupt = new Interrupt.Builder()
                .environment(env)
                .type(type);
        return interrupt.build();
    }



    public static Start buildStartEvent(Context context, String type, String mode, String pageId, String env, String objId, String objType, String objVersion) {
        // TODO: 1/10/2018  - Handle all the parameter
        Start.Builder start = new Start.Builder()
                .type(type)
                .mode(mode)
                .pageId(pageId)
                .environment(env);

        if (Workflow.APP.equals(type)) {
            DeviceSpecification deviceSpec = new DeviceSpecification();
            deviceSpec.setOs("Android " + DeviceSpec.getOSVersion());
            deviceSpec.setMake(DeviceSpec.getDeviceName());
            deviceSpec.setId(DeviceSpec.getAndroidId(context));

            String internalMemory = Util.bytesToHuman(DeviceSpec.getTotalInternalMemorySize());
            if (!TextUtils.isEmpty(internalMemory)) {
                deviceSpec.setIdisk(Double.valueOf(internalMemory));
            }

            String externalMemory = Util.bytesToHuman(DeviceSpec.getTotalExternalMemorySize());
            if (!TextUtils.isEmpty(externalMemory)) {
                deviceSpec.setEdisk(Double.valueOf(externalMemory));
            }

            String screenSize = DeviceSpec.getScreenInfoinInch(context);
            if (!TextUtils.isEmpty(screenSize)) {
                deviceSpec.setScrn(Double.valueOf(screenSize));
            }

            String[] cameraInfo = DeviceSpec.getCameraInfo(context);
            String camera = "";
            if (cameraInfo != null) {
                camera = TextUtils.join(",", cameraInfo);
            }
            deviceSpec.setCamera(camera);

            deviceSpec.setCpu(DeviceSpec.getCpuInfo());
            deviceSpec.setSims(-1);
            start.deviceSpecification(deviceSpec);

            ILocationInfo locationInfo = GenieService.getService().getLocationInfo();
            start.loc(locationInfo.getLocation());
        }
        android.util.Log.d(TAG, "buildStartEvent: " + start.build().toString());
        return start.build();
    }

    public static End buildEndEvent(long duration, String type, String mode, String pageId, String env, String objId, String objType, String objVersion) {
        // TODO: 1/10/2018  - Handle all the parameter
        End.Builder end = new End.Builder()
                .type(type)
                .mode(mode)
                .duration(duration)
                .pageId(pageId)
                .environment(env);
        android.util.Log.d(TAG, "buildEndEvent: " + end.build().toString());
        return end.build();
    }

    //////////////  IMPRESSION EVENT    ////////////////////////
    public static Impression buildImpressionEvent(String type, String pageId, String env) {
        Impression impression = new Impression.Builder().pageId(pageId).type(type).environment(env) //.addVisit(Visit)
                .build();  // .hierarchyLevel(rollup)
        android.util.Log.d(TAG, "buildImpressionEvent: " + impression.toString());
        return impression;
    }

    public static Impression buildImpressionEvent(String type, String subType, String pageId, String env) {
        Impression impression = new Impression.Builder().pageId(pageId).type(type).subType(subType).environment(env).build();
        android.util.Log.d(TAG, "buildImpressionEvent: " + impression.toString());
        return impression;
    }

    public static Impression buildImpressionEvent(String type, String subtype, String pageId, String env, String l1, String l2, String l3, String l4){
        Rollup rollup = new Rollup(l1,l2,l3,l4);
        Impression impression = new Impression.Builder().pageId(pageId).type(type).environment(env)  //.addVisit()
                .hierarchyLevel(rollup).build();
        android.util.Log.d(TAG, "buildImpressionEvent: " + impression.toString());
        return impression;
    }

    public static Impression buildSectionVisitImpressionEvent(String type, String pageId, String uri, String env, Map<String,String[]> sectionMap) {
        Map<String, Object> map = new HashMap<>();
        Impression.Builder impression = new Impression.Builder()
                .pageId(pageId)
                .type(type)
                .uri(uri)
                .environment(env);

        if(sectionMap != null) {
            for (Map.Entry mapItem : sectionMap.entrySet()) {
                String[] tagArray = (String[]) mapItem.getValue();
                Visit visit = new Visit(tagArray[2], "SECTION");
                visit.setSection(tagArray[0]);
                visit.setIndex(parseInt(tagArray[1]));
                impression.addVisit(visit);
            }
        }


        android.util.Log.d(TAG, "buildImpressionEvent: " + impression.build().toString());
        return impression.build();
    }

    public static Impression buildContentVisitImpressionEvent(String type, String pageId, String uri, String env, Map<String,String[]> contentMap) {
        Map<String, Object> map = new HashMap<>();
        Impression.Builder impression = new Impression.Builder()
                .pageId(pageId)
                .type(type)
                .uri(uri)
                .environment(env);

        if(contentMap != null) {
            for (Map.Entry mapItem : contentMap.entrySet()) {
                String [] value = (String[]) mapItem.getValue();
                Visit visit = new Visit(value[0], "CONTENT");
                visit.setSection(value[1]);
                visit.setIndex(parseInt((String) mapItem.getKey()));
                impression.addVisit(visit);
            }
        }


        android.util.Log.d(TAG, "buildImpressionEvent: " + impression.build().toString());
        return impression.build();
    }

    public static Impression buildImpressionEvent(String type, String subType, String pageId, String env, List<CorrelationData> cdata) {
        Impression impression = new Impression.Builder().pageId(pageId).type(type).subType(subType).correlationData(cdata).environment(env).build();
        android.util.Log.d(TAG, "buildImpressionEvent: " + impression.toString());
        return impression;
    }

    public static Impression buildImpressionEvent(String type, String subType, String pageId, String env, String objId, String objType, String objVersion) {
        Impression impression = new Impression.Builder()
                .pageId(pageId)
                .type(type)
                .subType(subType)
                .objectId(objId)
                .objectType(objType)
                .objectVersion(objVersion)
                .environment(env)
                .build();
        android.util.Log.d(TAG, "buildImpressionEvent: " + impression.toString());
        return impression;
    }

    public static Impression buildImpressionEvent(String type, String subType, String pageId, String env, String objId, String objType, String objVersion, List<CorrelationData> cdata) {
        Impression impression = new Impression.Builder()
                .pageId(pageId)
                .type(type)
                .subType(subType)
                .objectId(objId)
                .objectType(objType)
                .objectVersion(objVersion)
                .correlationData(cdata)
                .environment(env)
                .build();
        android.util.Log.d(TAG, "buildImpressionEvent: " + impression.toString());
        return impression;
    }

    //////////////  LOG EVENT    ////////////////////////

    /**
     * @param pageId
     * @param type
     * @param params
     * @return
     */
    public static Log buildLogEvent(String pageId, String type, String message, String env, Map<String, Object> params) {
        Log.Builder log = new Log.Builder();
        if (params != null) {
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                log.addParam(entry.getKey(), entry.getValue());
            }
        }
        log.pageId(pageId)
                .environment(env)
                .type(type)
                .level(Log.Level.INFO)
                .message(message);
        android.util.Log.d(TAG, "buildLogEvent: " + log.build().toString());
        return log.build();
    }

    //////////////  INTERACT EVENT    ////////////////////////

    /**
     * TODO: in all the interact events resource id value is pageId, should be replaced with actual expected value later.
     *
     * @param type
     * @param subType
     * @param pageId
     * @return
     */
    public static Interact buildInteractEvent(InteractionType type, String subType, String pageId, String env) {
        Interact interact = new Interact.Builder()
                .interactionType(type)
                .subType(subType)
                .pageId(pageId)
                .environment(env)
                .resourceId(pageId).build();
        android.util.Log.d(TAG, "buildInteractEvent: " + interact.toString());
        return interact;
    }

    public static Interact buildInteractEvent(InteractionType type, String subType, String pageId, String env, Map<String, Object> values) {
        Interact.Builder interact = new Interact.Builder()
                .interactionType(type)
                .subType(subType)
                .pageId(pageId)
                .environment(env)
                .resourceId(pageId);

        for (Map.Entry<String, Object> entry : values.entrySet()) {
            interact.addValue(entry.getKey(), entry.getValue());
        }

        Interact i = interact.build();
        android.util.Log.d(TAG, "buildInteractEvent: " + i.toString());
        return i;
    }

    public static Interact buildInteractEvent(InteractionType type, String subType, String pageId, String env, Map<String, Object> values, List<CorrelationData> cdata) {
        Interact.Builder interact = new Interact.Builder()
                .interactionType(type)
                .subType(subType)
                .pageId(pageId)
                .correlationData(cdata)
                .environment(env)
                .resourceId(pageId);

        for (Map.Entry<String, Object> entry : values.entrySet()) {
            interact.addValue(entry.getKey(), entry.getValue());
        }
        android.util.Log.d(TAG, "buildInteractEvent: " + interact.build().toString());
        return interact.build();
    }

    public static Interact buildInteractEvent(InteractionType type, String subType, String pageId, String env, String id, String objType, String objVersion) {
        Interact interact = new Interact.Builder()
                .interactionType(type)
                .subType(subType)
                .pageId(pageId)
                .objectId(id)
                .objectType(objType)
                .objectVersion(objVersion)
                .environment(env)
                .resourceId(pageId).build();
        android.util.Log.d(TAG, "buildInteractEvent: " + interact.toString());
        return interact;
    }

    public static Interact buildInteractEvent(InteractionType type, String subType, String pageId, String env, Map<String, Object> values, String objId, String objType, String objVersion) {
        Interact.Builder interact = new Interact.Builder()
                .interactionType(type)
                .subType(subType)
                .pageId(pageId)
                .objectId(objId)
                .objectType(objType)
                .objectVersion(objVersion)
                .environment(env)
                .resourceId(pageId);

        for (Map.Entry<String, Object> entry : values.entrySet()) {
            interact.addValue(entry.getKey(), entry.getValue());
        }
        android.util.Log.d(TAG, "buildInteractEvent: " + interact.build().toString());
        return interact.build();
    }

    public static Interact buildInteractEvent(InteractionType type, String subType, String pageId, String env, Map<String, Object> values, String id, String objType, String objVersion, List<CorrelationData> cdata) {
        Interact.Builder interact = new Interact.Builder()
                .interactionType(type)
                .subType(subType)
                .pageId(pageId)
                .objectId(id)
                .objectType(objType)
                .objectVersion(objVersion)
                .resourceId(pageId)
                .environment(env)
                .correlationData(cdata);

        if (values != null) {
            for (Map.Entry<String, Object> entry : values.entrySet()) {
                interact.addValue(entry.getKey(), entry.getValue());
            }
        }
        android.util.Log.d(TAG, "buildInteractEvent: " + interact.build().toString());
        return interact.build();
    }

    private static List<Map<String, Object>> buildValueList(Map<String, Object> values) {
        List<Map<String, Object>> valueList = new ArrayList<>();

        if (values == null) {
            values = new HashMap<>();
        }

        for (String key : values.keySet()) {
            Map<String, Object> eksMap = new HashMap<>();
            eksMap.put(key, values.get(key));
            valueList.add(eksMap);
        }
        return valueList;
    }

}
