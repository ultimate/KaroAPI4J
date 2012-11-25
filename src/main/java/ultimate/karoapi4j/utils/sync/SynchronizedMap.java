package ultimate.karoapi4j.utils.sync;

import java.util.Map;

import ultimate.karoapi4j.enums.EnumRefreshMode;

/**
 * Implementation of synchronized maps managing the standard access operations.<br>
 * Note:<br>
 * Modifications of the underlying Map are not allowed. Therefore several operations like
 * put, remove, clear, etc. will throw an {@link UnsupportedOperationException}
 * 
 * @author ultimate
 * @param <K> - the type of the keys inside the Map
 * @param <V> - the type of the values inside the Map
 * @param <M> - the map type
 * @param <S> - the Type of Entity to be synchronized (= extending Class)
 */
public class SynchronizedMap<K, V, M extends Map<K, V>, S extends SynchronizedMap<K, V, M, S>> extends BaseSynchronizedMap<K, V, Map<K, V>, Map<K, V>, S> implements
		Map<K, V>, Synchronized<Map<K, V>, S>
{
	/**
	 * Construct a new synchronized Map.
	 * 
	 * @see BaseSynchronized#BaseSynchronized(Loader, EnumRefreshMode)
	 * @param loader - the Loader used to load the Content to synchronize from
	 * @param refreshMode - the RefreshMode used for auto refreshing the synchronized entity
	 * @param map - the underlying map to refresh
	 * @param clearOnRefresh - should the content of the map be cleared on refresh
	 */
	@SuppressWarnings("unchecked")
	public SynchronizedMap(Loader<? extends Map<K, V>> loader, EnumRefreshMode refreshMode, M map, boolean clearOnRefresh)
	{
		super((Loader<Map<K, V>>) loader, refreshMode, map, clearOnRefresh);
	}

	/*
	 * (non-Javadoc)
	 * @see ultimate.karoapi4j.utils.sync.BaseSynchronized#update(java.lang.Object)
	 */
	@Override
	protected synchronized void update(Map<K, V> content)
	{
		if(clearOnRefresh)
		{
			this.map.clear();
		}
		else
		{
			this.map.values().removeAll(content.values());
		}
		// TODO merge types
		this.map.putAll((Map<? extends K, ? extends V>) content);
	}
}
