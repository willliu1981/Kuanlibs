package idv.kuan.libs.databases.models;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class MetadataEntityUtil {


    private MetadataEntityUtil() {

    }

    public static byte[] serializeMetadata(Metadata metadata) {
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

        public Metadata buildMetadata() {

            if (data != null) {
                ByteArrayInputStream bis = new ByteArrayInputStream(data);
                try (ObjectInputStream ois = new ObjectInputStream(bis)) {

                    Object o = ois.readObject();
                    Class<Metadata> metadataClass = (Class<Metadata>) MetadataRegister.getMetadata(versoion);
                    if (metadataClass.isInstance(o)) {
                        return metadataClass.cast(o);
                    }

                } catch (IOException | ClassNotFoundException e) {
                    //throw new RuntimeException("Failed to deserialize metadata", e);
                    e.printStackTrace();
                }
            }


            return null;
        }


    }


    public interface Metadata extends Serializable {
        MetadataObject getMetadataObject(String name);

        void addMetadataObject(String name, MetadataObject metadataObject);

    }

    public static class MetadataObject implements Serializable {

        private String name;

        public MetadataObject() {

        }

        public MetadataObject(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return "MetadataObject{" +
                    "name='" + name + '\'' +
                    '}';
        }
    }

}
