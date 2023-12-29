package idv.kuan.libs.interfaces.observers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
        List<Observer<T>> collect = this.getObservers().stream().collect(Collectors.toList());

        collect.forEach(o -> {
            o.onBeforeAllUpdate(data);
        });

        collect.forEach(o -> {
            o.update(data);
        });

        collect.forEach(o -> {
            o.onAfterAllUpdate(data);
        });
    }


}
