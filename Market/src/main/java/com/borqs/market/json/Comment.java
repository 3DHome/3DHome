package com.borqs.market.json;

import java.io.Serializable;

public class Comment implements Serializable, Comparable<Comment> {
    /**
     * 
     */
    private static final long serialVersionUID = 5491013609222023947L;
    
    public String comment_id;
    public String commenter_id;
    public String commenter_name;
    public String message;
    public long created_time;
    public long updated_time;
    public String product_id;
    public float rating;
    public String version_name;
    public int version_code;

    @Override
    public int compareTo(Comment another) {
        return 0;
    }

    public void despose() {
        comment_id = null;
        commenter_id = null;
        message = null;
        commenter_name = null;
        product_id = null;
        version_name = null;
    }
    
    public Comment() {
        super();
    }


}
