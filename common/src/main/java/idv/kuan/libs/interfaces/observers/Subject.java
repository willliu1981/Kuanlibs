package idv.kuan.libs.interfaces.observers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public interface Subject<T> {
    default List<Observer<T>> createObservers() {
        return new ArrayList<>();
    }

    List<Observer<T>> getObservers();

    Map<String, Object> getOtherData();


    default void registerObserver(Observer<T> observer) {
        this.getObservers().add(observer);
    }

    default void setData(T data) {
        notifyObservers(data);
    }


    default void removeObserver(Observer<T> observer) {
        this.getObservers().remove(observer);
    }

    default void notifyObservers(T data) {
        this.getObservers().forEach(o -> {
            o.update(data);
        });
    }


}
