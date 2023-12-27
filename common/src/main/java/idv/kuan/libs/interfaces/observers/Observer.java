package idv.kuan.libs.interfaces.observers;

public interface Observer<T> {

    Subject getSubject();

    default void setData(T data) {
        this.getSubject().setDataAndNotifyObservers(data);
    }

    void update(T data);

}
