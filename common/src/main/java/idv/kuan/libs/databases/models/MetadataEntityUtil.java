package idv.kuan.libs.databases.models;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class MetadataEntityUtil {
    MetadataEntity.Metadata data;


    private MetadataEntityUtil() {

    }

    public static byte[] serializeMetadata(MetadataEntity.Metadata metadata) {
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

        public MetadataBuilder setData(byte[] data) {
            this.data = data;
            return this;
        }


        public MetadataEntity.Metadata buildMetadata() {

            if (data != null) {
                ByteArrayInputStream bis = new ByteArrayInputStream(data);
                try (ObjectInputStream ois = new ObjectInputStream(bis)) {
                    return (MetadataEntity.Metadata) ois.readObject();
                } catch (IOException | ClassNotFoundException e) {
                    //throw new RuntimeException("Failed to deserialize metadata", e);
                    e.printStackTrace();
                }
            }


            return null;
        }


    }


}
