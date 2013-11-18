package javax.inject;

/**
 * Created By: Connor Fraser
 */
public class ProviderImpl<T> implements Provider<T> {

    private Class<T> clazz;

    public ProviderImpl(Class<T> clazz) {

        this.clazz = clazz;
    }

    public T get() {
        try {
            return clazz.newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (IllegalAccessException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return null;
    }
}
