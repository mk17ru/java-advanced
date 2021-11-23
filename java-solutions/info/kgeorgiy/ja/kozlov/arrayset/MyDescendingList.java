package info.kgeorgiy.ja.kozlov.arrayset;

import java.util.AbstractList;
import java.util.List;

public class MyDescendingList<P> extends AbstractList<P> {
    private final List<P> objs;
    private boolean isReversed;

    MyDescendingList(List<P> objs) {
        if (!(objs instanceof MyDescendingList)) {
            this.objs = objs;
            this.isReversed = false;
        } else {
            MyDescendingList<P> temp = (MyDescendingList<P>)objs;
            this.isReversed = temp.isReversed;
            this.objs = temp.objs;
        }
    }

    @Override
    public int size() {
        return objs.size();
    }

    public MyDescendingList<P> swapReverse() {
        this.isReversed = !this.isReversed;
        return this;
    }

    @Override
    public P get(int index) {
        return objs.get(!isReversed ? index : objs.size() - index - 1);
    }

}
