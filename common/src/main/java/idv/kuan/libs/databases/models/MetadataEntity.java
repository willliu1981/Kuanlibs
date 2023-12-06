package idv.kuan.libs.databases.models;


import java.sql.Timestamp;

public abstract class MetadataEntity implements IAuditable {

    protected Integer id;
    protected MetadataEntityUtil.DefaultMetadata metadata;
    protected Integer version;//metadata version


    public MetadataEntity() {
    }


    @Override
    public Integer getId() {
        return id;
    }

    @Override
    public void setId(Integer id) {
        this.id = id;
    }


    public MetadataEntityUtil.DefaultMetadata getMetadata() {
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
    public Timestamp getAtCreated() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setAtCreated(Timestamp atCreated) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Timestamp getAtUpdated() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setAtUpdated(Timestamp atUpdated) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String toString() {
        return "MetadataEntity{" +
                "id=" + id +
                ", metadata=" + metadata +
                '}';
    }

}
