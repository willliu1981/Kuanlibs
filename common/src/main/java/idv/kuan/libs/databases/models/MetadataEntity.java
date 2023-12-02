package idv.kuan.libs.databases.models;


import java.sql.Timestamp;

public abstract class MetadataEntity implements IAuditable {

    protected Integer id;
    protected MetadataEntityUtil.DefaultMetadata metadata;
    protected Integer version;//metadata version


    public MetadataEntity() {
        //this.metadata = new MetadataEntityUtil.Metadata();
    }


    /*
    public static class Metadata implements Cloneable, Serializable {
        private static final long serialVersionUID = 1L;
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


        public String getData() {
            return data;
        }


        @Override
        public String toString() {
            return "Metadata{" +
                    "data='" + data + '\'' +
                    ", atCreated=" + atCreated +
                    ", atUpdated=" + atUpdated +
                    '}';
        }

        public void setData(String data) {
            this.data = data;
        }
    }

         */


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
        //return metadata.atCreated;
        return null;
    }

    @Override
    public void setAtCreated(Timestamp atCreated) {
        //metadata.atCreated = atCreated;
    }

    @Override
    public Timestamp getAtUpdated() {
        //return metadata.atUpdated;
        return null;
    }

    @Override
    public void setAtUpdated(Timestamp atUpdated) {
        //metadata.atUpdated = atUpdated;
    }


    public MetadataEntityUtil.DefaultMetadata getMetadata() {
        //return metadata;
        return this.metadata;
    }


    public void setMetadata(MetadataEntityUtil.DefaultMetadata metadata) {
        this.metadata = metadata;
    }


    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    @Override
    public String toString() {
        return "MetadataEntity{" +
                "id=" + id +
                ", metadata=" + metadata +
                '}';
    }

}
