package idv.kuan.libs.interfaces.observers;

public interface Observer<T extends Observer> {

    void update(T data);

}
