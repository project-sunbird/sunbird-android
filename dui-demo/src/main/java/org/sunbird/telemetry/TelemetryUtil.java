package org.sunbird.telemetry;

import android.text.TextUtils;

import org.ekstep.genieservices.commons.bean.CorrelationData;
import org.ekstep.genieservices.commons.bean.HierarchyInfo;
import org.sunbird.models.CurrentGame;
import org.sunbird.models.enums.ContentType;
import org.sunbird.utils.Util;

import java.util.ArrayList;
import java.util.List;


public class TelemetryUtil {

    public static List<CorrelationData> computeCData(List<HierarchyInfo> contentInfoList) {
        List<CorrelationData> cdata = null;

        if (!Util.getCdataStatus()) {
            if (contentInfoList != null && contentInfoList.size() > 0) {
                cdata = new ArrayList<>();

                List<String> idList = new ArrayList<>();

                for (int i = 0; i < contentInfoList.size(); i++) {
                    idList.add(contentInfoList.get(i).getIdentifier());
                }

                cdata.add(new CorrelationData(TextUtils.join("/", idList), contentInfoList.get(0).getContentType()));
            } else {
                cdata = Util.getCoRelationList();
            }
        } else {
            Util.setCdataStatus(false);
        }

        return cdata;
    }

    public static boolean isFromCollectionOrTextBook(List<HierarchyInfo> contentInfoList) {
        if (contentInfoList != null && contentInfoList.size() > 0) {
            HierarchyInfo contentInfo = contentInfoList.get(0);

            return (contentInfo.getContentType().toLowerCase().equalsIgnoreCase(ContentType.COLLECTION)
                    || contentInfo.getContentType().toLowerCase().equalsIgnoreCase(ContentType.TEXTBOOK)
                    || contentInfo.getContentType().toLowerCase().equalsIgnoreCase(ContentType.TEXTBOOKUNIT));
        } else {
            return false;
        }
    }

    public static void addCurrentGame(CurrentGame currentGame) {
        List<CurrentGame> currentGameList = Util.getCurrentGameList();

        currentGameList.add(currentGame);

        Util.saveCurrentGame(currentGameList);
    }

    public static void removeCurrentGame(String identifier) {
        List<CurrentGame> currentGameList = Util.getCurrentGameList();

        if (currentGameList != null) {
            for (int i = 0; i < currentGameList.size(); i++) {
                if (currentGameList.get(i).getIdentifier().equalsIgnoreCase(identifier)) {
                    currentGameList.remove(i);
                }
            }

            Util.saveCurrentGame(currentGameList);
        }
    }

    public static void removeAllCurrentGame() {
        Util.saveCurrentGame(new ArrayList<CurrentGame>());
    }

    public static boolean isContent() {
        List<CurrentGame> currentGameList = Util.getCurrentGameList();

        if (currentGameList != null && currentGameList.size() > 0) {
            CurrentGame currentGame = currentGameList.get(currentGameList.size() - 1);

            return (!currentGame.getMediaType().equalsIgnoreCase(ContentType.COLLECTION) &&
                    !currentGame.getMediaType().equalsIgnoreCase(ContentType.TEXTBOOK) &&
                    !currentGame.getMediaType().equalsIgnoreCase(ContentType.TEXTBOOKUNIT));
        }

        return false;
    }

    public static CurrentGame getCurrentGame() {
        List<CurrentGame> currentGameList = Util.getCurrentGameList();
        if (currentGameList != null && currentGameList.size() > 0) {
            return currentGameList.get(currentGameList.size() - 1);
        }
        return null;
    }

}
