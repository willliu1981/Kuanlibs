package idv.kuan.libs.databases.models;

import java.util.HashMap;
import java.util.Map;

public class MetadataRegister {

    private MetadataRegister() {

    }

    private static Map<Integer, Class<? extends MetadataEntityUtil.DefaultMetadata>> metadataMap = new HashMap<>();

    public static void addMetadata(Integer version, Class<? extends MetadataEntityUtil.DefaultMetadata> metadataClass) {
        MetadataRegister.metadataMap.put(version, metadataClass);

    }

    public static Class<? extends MetadataEntityUtil.DefaultMetadata> getMetadata(Integer version) {
        return MetadataRegister.metadataMap.get(version);
    }

}
