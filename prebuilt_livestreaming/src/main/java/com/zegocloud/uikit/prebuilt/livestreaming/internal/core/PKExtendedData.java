package com.zegocloud.uikit.prebuilt.livestreaming.internal.core;

import androidx.annotation.NonNull;
import org.json.JSONException;
import org.json.JSONObject;

public class PKExtendedData {

    public String roomID;
    public String userName;
    public int type;

    public static final int START_PK = 91000;
    public static final int END_PK = 91001;
    public static final int RESUME_PK = 91002;

    public static PKExtendedData parse(String extendedData) {
        try {
            JSONObject jsonObject = new JSONObject(extendedData);
            if (jsonObject.has("type")) {
                int type = (int) jsonObject.get("type");
                if (type == START_PK || type == END_PK || type == RESUME_PK) {
                    PKExtendedData data = new PKExtendedData();
                    data.type = type;
                    data.roomID = jsonObject.getString("room_id");
                    data.userName = jsonObject.getString("user_name");
                    return data;
                }
            }
        } catch (JSONException e) {
        }
        return null;
    }

    @NonNull
    @Override
    public String toString() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("room_id", roomID);
            jsonObject.put("user_name", userName);
            jsonObject.put("type", type);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        return jsonObject.toString();
    }
}
