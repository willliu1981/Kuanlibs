package idv.kuan.libs.databases.models;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * 需給定Integer getId();
 * 此CommonEntity擁有Timestamp atCreated 和 Timestamp atUpdated 需實作
 */
public interface CommonEntity extends Serializable {

    public Integer getId();

    public void setId(Integer id);

    public Timestamp getAtCreated();

    public void setAtCreated(Timestamp atCreated);


    default void setAtCreated(String atCreated) {
        try {
            setAtCreated(Timestamp.valueOf(atCreated));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Timestamp getAtUpdated();

    public void setAtUpdated(Timestamp atUpdated);

    default void setAtUpdated(String atUpdated) {

        try {
            setAtUpdated(Timestamp.valueOf(atUpdated));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

