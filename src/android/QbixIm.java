package com.q.users.cordova.plugin;

import org.json.JSONObject;

public class QbixIm {
    /**
     * TYPES
     *
     * TYPE_CUSTOM - 0 (actual type in {@link QbixIm#customType})
     * TYPE_HOME - 1
     * TYPE_WORK - 2
     * TYPE_OTHER - 3
     *
     * PROTOCOLS
     *
     * PROTOCOL_CUSTOM - -1 (actual protocol in {@link QbixIm#customProtocol})
     * PROTOCOL_AIM - 0
     * PROTOCOL_MSN - 1
     * PROTOCOL_YAHOO - 2
     * PROTOCOL_SKYPE - 3
     * PROTOCOL_QQ - 4
     * PROTOCOL_GOOGLE_TALK - 5
     * PROTOCOL_ICQ - 6
     * PROTOCOL_JABBER - 7
     * PROTOCOL_NETMEETING - 8
     */

    String data;
    int type;
    String customType = null;
    String protocol;
    String customProtocol = null;

    public JSONObject toJson() {
        try {
            JSONObject jsonIm = new JSONObject();
            jsonIm.put("data", data);
            jsonIm.put("type", type);
            jsonIm.put("customType", customType);
            jsonIm.put("protocol", protocol);
            jsonIm.put("customProtocol", customProtocol);
            return jsonIm;
        } catch (Exception e) {
            return null;
        }
    }
}
