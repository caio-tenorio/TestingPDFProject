package org.pdfquill.writer;

import java.util.Objects;

public final class SplitParts {
    private final String head;
    private final String tail;

    public SplitParts(String head, String tail) {
        this.head = head;
        this.tail = tail;
    }

    public String head() {
        return head;
    }

    public String tail() {
        return tail;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SplitParts)) {
            return false;
        }
        SplitParts that = (SplitParts) o;
        return Objects.equals(head, that.head) && Objects.equals(tail, that.tail);
    }

    @Override
    public int hashCode() {
        return Objects.hash(head, tail);
    }

    @Override
    public String toString() {
        return "SplitParts{" +
            "head='" + head + '\'' +
            ", tail='" + tail + '\'' +
            '}';
    }
}
