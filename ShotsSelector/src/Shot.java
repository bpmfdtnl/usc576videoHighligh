public class Shot implements Comparable<Shot>{
    int start;
    int end;

    public Shot(int start, int end) {
        this.start = start;
        this.end = end;
    }

    @Override
    public String toString() {
        return String.format("%d %d\n", start, end);
    }

    @Override
    public int compareTo(Shot o) {
        return Integer.compare(this.start, o.start);
    }
}