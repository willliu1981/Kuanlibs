package idv.kuan.libs.databases.models;


import java.io.Serializable;
import java.sql.Timestamp;

public abstract class MetadataEntity implements IAuditable {

    protected Integer id;
    protected Metadata metadata;


    public MetadataEntity() {
        this.metadata = new Metadata(-1);
    }


    public static class Metadata implements Cloneable, Serializable {
        private Integer version;
        private String data;
        private Timestamp atCreated;
        private Timestamp atUpdated;

        private Metadata() {

        }


        @Override
        public Metadata clone() throws CloneNotSupportedException {
            try {
                return (Metadata) super.clone();
            } catch (AssertionError e) {
                throw new AssertionError();
            }

        }


        public Metadata(Integer version) {
            this.version = version;
        }

        public Integer getVersion() {
            return version;
        }

        public void setVersion(Integer version) {
            this.version = version;
        }

        public String getData() {
            return data;
        }


        @Override
        public String toString() {
            return "Metadata{" +
                    "version=" + version +
                    ", data='" + data + '\'' +
                    ", atCreated=" + atCreated +
                    ", atUpdated=" + atUpdated +
                    '}';
        }

        public void setData(String data) {
            this.data = data;
        }
    }

    @Override
    public Integer getId() {
        return id;
    }

    @Override
    public void setId(Integer id) {
        this.id = id;
    }

    @Override
    public Timestamp getAtCreated() {
        return metadata.atCreated;
    }

    @Override
    public void setAtCreated(Timestamp atCreated) {
        metadata.atCreated = atCreated;
    }

    @Override
    public Timestamp getAtUpdated() {
        return metadata.atUpdated;
    }

    @Override
    public void setAtUpdated(Timestamp atUpdated) {
        metadata.atUpdated = atUpdated;
    }


    public Metadata getMetadata() {
        return metadata;
    }

    //*
    public void setMetadata(Metadata metadata) {
        try {
            if(metadata==null){
                throw new NullPointerException();
            }
            Metadata metadata1 = metadata.clone();
            metadata1.atCreated = this.metadata.atCreated;
            metadata1.atUpdated = this.metadata.atUpdated;
            this.metadata = metadata1;
        } catch (CloneNotSupportedException |NullPointerException e) {
            //throw new AssertionError();
            e.printStackTrace();
        }
    }

    // */

    @Override
    public String toString() {
        return "MetadataEntity{" +
                "id=" + id +
                ", metadata=" + metadata +
                '}';
    }

}
