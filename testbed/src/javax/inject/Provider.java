package javax.inject;

/**
 * Created By: Connor Fraser
 */
public interface Provider<T> {
    public T get();
}
