package idv.kuan.libs.databases.models;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.StreamCorruptedException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

public class MetadataEntityUtil {


    private MetadataEntityUtil() {

    }

    public static byte[] serializeMetadata(DefaultMetadata metadata) {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(metadata);
            oos.flush();
            return bos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Serialization of metadata failed", e);
        }
    }

    public static MetadataBuilder metadataBuilder() {

        return new MetadataBuilder();

    }


    public static class MetadataBuilder {

        byte[] data;
        int versoion;

        /**
         * @param data byte array data from SQL column.
         * @return
         */
        public MetadataBuilder setData(byte[] data) {
            this.data = data;
            return this;
        }

        public MetadataBuilder setVersoion(int versoion) {
            this.versoion = versoion;
            return this;
        }

        public DefaultMetadata buildMetadata() {
            if (versoion == -1) {
                return null;
            }

            if (data != null) {

                try (ByteArrayInputStream bis = new ByteArrayInputStream(data); ObjectInputStream ois = new ObjectInputStream(bis)) {

                    Object o = ois.readObject();
                    Class<DefaultMetadata> metadataClass = (Class<DefaultMetadata>) MetadataRegister.getMetadata(versoion);
                    if (metadataClass.isInstance(o)) {
                        return metadataClass.cast(o);
                    }

                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                    System.out.println("dbg:" + e.getMessage());
                } catch (StreamCorruptedException e) {
                    System.out.println("dbg:" + e.getMessage());
                } catch (IOException e) {
                    //throw new RuntimeException("Failed to deserialize metadata", e);
                    e.printStackTrace();
                }
            }


            return null;
        }


    }


    public static abstract class DefaultMetadata implements Serializable, Cloneable {
        String serialVersionUID = "1";

        public static String ATUPDATED = "at_updated";
        public static String ATCREATED = "at_created";
        private Map<String, DataObject> dataObjectMap = new HashMap<>();

        public void setAtUpdated(Timestamp atUpdated) {
            this.dataObjectMap.put(ATUPDATED, new DataObject(ATUPDATED).setData(atUpdated));
        }

        public void setAtCreated(Timestamp atCreated) {
            this.dataObjectMap.put(ATCREATED, new DataObject(ATCREATED).setData(atCreated));
        }


        private void writeObject(ObjectOutputStream oos) {

        }

        @Override
        public String toString() {
            return null;
        }


        public DataObject getDataObject(String name) {
            return dataObjectMap.get(name);
        }

        public void addDataObject(String name, DataObject dataObject) {

            this.dataObjectMap.put(name, dataObject);
        }


        @Override
        public DefaultMetadata clone() throws CloneNotSupportedException {


            return (DefaultMetadata) super.clone();
        }
    }


    public static class DataObject implements Serializable {

        private String name;
        private Object data;

        public DataObject() {

        }


        public DataObject(String name) {
            this.name = name;
        }

        public DataObject setData(Object data) {
            this.data = data;
            return this;
        }

        public Object getData() {
            return this.data;
        }

        public String getName() {
            return this.name;
        }

        @Override
        public String toString() {
            return "DataObject{" +
                    "name='" + name + '\'' +
                    ", data=" + data +
                    '}';
        }
    }

}
