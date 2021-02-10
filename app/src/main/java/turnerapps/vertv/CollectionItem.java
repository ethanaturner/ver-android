package turnerapps.vertv;

/**
 * Created by ethanturner on 2/12/17.
 */

public class CollectionItem {
    public String id;
    public String type;

    public CollectionItem() {

    }

    public CollectionItem(String id, String type) {
        this.id = id;
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }

}