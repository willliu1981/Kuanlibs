package idv.kuan.libs.databases.models;

import java.util.HashMap;
import java.util.Map;

public class MetadataRegister {

    private MetadataRegister() {

    }

    private static Map<Integer, Class<? extends MetadataEntityUtil.Metadata>> metadataMap = new HashMap<>();

    public static void addMetadata(Integer version, Class<? extends MetadataEntityUtil.Metadata> metadataClass) {
        MetadataRegister.metadataMap.put(version, metadataClass);

    }

    public static Class<? extends MetadataEntityUtil.Metadata> getMetadata(Integer version) {
        return MetadataRegister.metadataMap.get(version);
    }

}
