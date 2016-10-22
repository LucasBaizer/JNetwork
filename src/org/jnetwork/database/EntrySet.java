package org.jnetwork.database;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

public class EntrySet implements Serializable, Collection<Entry> {
	private static final long serialVersionUID = 6891274761747429375L;

	private ArrayList<Entry> entries = new ArrayList<>();
	private ColumnHeader[] columnHeaders;

	public EntrySet() {
	}

	public EntrySet(Entry... entries) {
		this.entries = new ArrayList<>(Arrays.asList(entries));
	}

	public void setEntries(Entry[] entries) {
		this.entries = new ArrayList<>(Arrays.asList(entries));
	}

	public Entry[] getEntries() {
		return this.toArray(new Entry[this.entries.size()]);
	}

	public ColumnHeader[] getTableColumnHeaders() {
		return columnHeaders;
	}

	EntrySet setTableColumnHeaders(ColumnHeader[] headers) {
		this.columnHeaders = headers;
		return this;
	}

	@Override
	public Iterator<Entry> iterator() {
		return entries.iterator();
	}

	@Override
	public int size() {
		return entries.size();
	}

	@Override
	public boolean isEmpty() {
		return entries.isEmpty();
	}

	@Override
	public boolean contains(Object o) {
		return entries.contains(o);
	}

	@Override
	public Object[] toArray() {
		return entries.toArray();
	}

	@Override
	public <T> T[] toArray(T[] a) {
		return entries.toArray(a);
	}

	@Override
	public boolean add(Entry e) {
		return entries.add(e);
	}

	@Override
	public boolean remove(Object o) {
		return entries.remove(o);
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return entries.containsAll(c);
	}

	@Override
	public boolean addAll(Collection<? extends Entry> c) {
		return entries.addAll(c);
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		return entries.retainAll(c);
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		return entries.removeAll(c);
	}

	@Override
	public void clear() {
		entries.clear();
	}
}
