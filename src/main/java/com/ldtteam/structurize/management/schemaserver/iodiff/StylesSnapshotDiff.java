package com.ldtteam.structurize.management.schemaserver.iodiff;

import java.util.List;
import com.google.common.collect.ForwardingList;
import com.google.common.collect.ImmutableList;
import com.ldtteam.structurize.management.schemaserver.iodiff.StylesSnapshotDiff.Diff.DiffType;

public class StylesSnapshotDiff extends ForwardingList<StylesSnapshotDiff.Diff>
{
    private final List<Diff> delegate;
    private final int[] counters;

    private StylesSnapshotDiff(final ImmutableList<Diff> delegate, final int[] counters)
    {
        this.delegate = delegate;
        this.counters = counters;
    }

    @Override
    protected List<Diff> delegate()
    {
        return delegate;
    }

    public int getCountOf(final DiffType diffType)
    {
        return counters[diffType.ordinal()];
    }

    public static Builder getBuilder()
    {
        return new Builder();
    }

    public static class Builder
    {
        private final ImmutableList.Builder<Diff> builder = ImmutableList.builder();
        private final int[] counters = new int[DiffType.values().length];

        private Builder()
        {
        }

        public void addAdded(final String path, final long newLastModifiedTime)
        {
            builder.add(new Diff(path, DiffType.ADDED, newLastModifiedTime));
            counters[DiffType.ADDED.ordinal()]++;
        }

        public void addUpdated(final String path, final long newLastModifiedTime)
        {
            builder.add(new Diff(path, DiffType.UPDATED, newLastModifiedTime));
            counters[DiffType.UPDATED.ordinal()]++;
        }

        public void addRemoved(final String path)
        {
            builder.add(new Diff(path, DiffType.REMOVED));
            counters[DiffType.REMOVED.ordinal()]++;
        }

        public StylesSnapshotDiff build()
        {
            return new StylesSnapshotDiff(builder.build(), counters);
        }
    }

    public static class Diff
    {
        public enum DiffType
        {
            ADDED("structurize.gui.ssfilediff.added"),
            UPDATED("structurize.gui.ssfilediff.updated"),
            REMOVED("structurize.gui.ssfilediff.removed");

            private final String tKey;

            private DiffType(final String tKey)
            {
                this.tKey = tKey;
            }

            public String getTranslationKey()
            {
                return tKey;
            }
        }

        private final String path;
        private final long newLastModifiedTime;
        private final DiffType status;

        private Diff(final String path, final DiffType status)
        {
            this(path, status, 0);
        }

        private Diff(final String path, final DiffType status, final long newLastModifiedTime)
        {
            this.path = path;
            this.status = status;
            this.newLastModifiedTime = newLastModifiedTime;
        }

        public String getPath()
        {
            return path;
        }

        public long getNewLastModifiedTime()
        {
            return newLastModifiedTime;
        }

        public DiffType getStatus()
        {
            return status;
        }
    }
}
