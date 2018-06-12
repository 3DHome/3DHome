package com.borqs.market.json;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class CommentJSONImpl extends Comment {

    private static final long serialVersionUID = -4902138822490387180L;

    public CommentJSONImpl(JSONObject obj) {
        if (obj == null)
            return;
        comment_id = obj.optString("id");
        commenter_id = obj.optString("commenter_id");
        commenter_name = obj.optString("commenter_name");
        created_time = obj.optLong("created_at");
        updated_time = obj.optLong("updated_at");
        message = obj.optString("message");
        product_id = obj.optString("product_id");
        version_code = obj.optInt("version");
        version_name = obj.optString("version_name");
        rating = (float)obj.optDouble("rating") * 5;
    }

    public static ArrayList<Comment> createCommentList(JSONArray array) {
        ArrayList<Comment> list = new ArrayList<Comment>();
        try {
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj;
                obj = array.getJSONObject(i);
                list.add(new CommentJSONImpl(obj));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return list;
    }

}
