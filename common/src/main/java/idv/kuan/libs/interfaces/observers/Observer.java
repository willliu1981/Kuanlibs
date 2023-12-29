package idv.kuan.libs.interfaces.observers;

public interface Observer<T> {

    Subject getSubject();

    default void setData(T data) {
        this.getSubject().setData(data);
    }

    void update(T data);

}
