package turnerapps.vertv;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by ethanturner on 2/28/17.
 */

public interface JSONCallback {
    void onSuccessResponse(JSONArray results);
}