package ro.umfst.newsapplication.model;
/**
 *
 * @author hunorszilvasi
 */
public abstract class BaseEntity {
    public abstract String businessKey();
    protected int id;

    public BaseEntity(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public abstract String getEntityType();
}