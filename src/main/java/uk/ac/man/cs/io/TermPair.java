package uk.ac.man.cs.io;

/**
 * Created by slava on 09/10/17.
 */
public class TermPair {

    public final String first;

    public final String second;

    public TermPair(String first, String second) {
        this.first = first;
        this.second = second;
    }

    @Override
    public int hashCode() {
        return first.hashCode() * second.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof TermPair)) {
            return false;
        }
        TermPair pair = (TermPair) obj;
        return (pair.first.equals(first) && pair.second.equals(second))
                || (pair.first.equals(second) && pair.second.equals(first));
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return new TermPair(first, second);
    }

    @Override
    public String toString() {
        return "(" + first + ", " + second + ")";
    }
}
