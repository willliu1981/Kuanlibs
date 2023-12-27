package idv.kuan.libs.interfaces.observers;

import java.util.ArrayList;
import java.util.List;

public interface Subject<T> {
    default List<Observer<T>> createObservers() {
        return new ArrayList<>();
    }

    List<Observer<T>> getActionObservers();


    default void registerObserver(Observer<T> observer) {
        this.getActionObservers().add(observer);
    }

    default void setDataAndNotifyObservers(T data) {
        notifyObservers();
    }

    T getData();

    default void removeObserver(Observer<T> observer) {
        this.getActionObservers().remove(observer);
    }

    default void notifyObservers() {
        this.getActionObservers().forEach(o -> {
            o.update(this.getData());
        });
    }
}
