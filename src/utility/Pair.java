package utility;

public class Pair<U, V> {
    private U first;
    private V second;

    public Pair(U u, V v) {
        this.first = u;
        this.second = v;
    }

    public U getFirst() { return first; }
    public V getSecond() { return second; }
}
