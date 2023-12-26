package idv.kuan.libs.interfaces.observers;

public interface Subject<T extends Observer> {
    void registerObserver(Observer<T> observer);

    void removeObserver(Observer<T> observer);

    void notifyObservers();
}
