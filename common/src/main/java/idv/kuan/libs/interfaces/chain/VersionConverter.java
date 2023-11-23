package idv.kuan.libs.interfaces.chain;

public interface VersionConverter {
    void setNext(VersionConverter converter);

    Convertible convert(Convertible input);
}
