package idv.kuan.libs.interfaces.observers;

import java.util.ArrayList;
import java.util.List;

public interface Subject<T> {
    default List<Observer<T>> createObservers() {
        return new ArrayList<>();
    }

    List<Observer<T>> getObservers();


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
