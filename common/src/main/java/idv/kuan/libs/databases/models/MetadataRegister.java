package idv.kuan.libs.databases.models;

import java.util.HashMap;
import java.util.Map;

public class MetadataRegister {

    private MetadataRegister(){

    }

    private static Map<Integer, MetadataEntityUtil.Metadata> metadataMap = new HashMap<>();

    public static void addMetadata(Integer version, MetadataEntityUtil.Metadata metadata) {
        MetadataRegister.metadataMap.put(version, metadata);

    }

    public static MetadataEntityUtil.Metadata getMetadata(Integer version) {
        return MetadataRegister.metadataMap.get(version);
    }

}
