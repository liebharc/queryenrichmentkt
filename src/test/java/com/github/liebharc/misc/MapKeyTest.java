package com.github.liebharc.misc;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.Before;
import org.junit.Test;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class MapKeyTest {

    private DomainMap<Long> mMap = new DomainMap<>();

    @Before
    public void createData() {
        List<String> types = Arrays.asList("AB", "BC", "CD");
        List<String> subTypes = Arrays.asList("100", "200", "300", "400", "500");
        for (long i = 1; i < 100000; i++) {
            for (String type : types) {
                for (String subType : subTypes) {
                    mMap.put(new MapKey(i, type, subType), i);
                }
            }
        }
    }

    @Test
    public void mapKeyGetTest() {
        long start = System.currentTimeMillis();
        for (long i = 0; i < 100000; i++) {
            mMap.get(new MapKey(1000L + i, "AB", "200"));
            mMap.get(new MapKey(2000L + i, "CD", "400"));
            mMap.get(new MapKey(3000L + i, "BC", "100"));
        }
        System.out.println(System.currentTimeMillis() - start);
    }

    @Test
    public void mapKeyClearDomainTest() {
        long start = System.currentTimeMillis();
        mMap.removeAll(1001L);
        mMap.removeAll(2001L);
        mMap.removeAll(3001L);
        System.out.println(System.currentTimeMillis() - start);
    }

    private void clearKey(long domainId) {
        for (MapKey mapKey : new HashSet<>(mMap.keySet())) {
            if (mapKey.domainId == domainId) {
                mMap.remove(mapKey);
            }
        }
    }

    private static class DomainMap<V> implements Map<MapKey, V> {

        private Map<Long, Map<MapKey, V>> mInnerMap = new ConcurrentHashMap<>();

        @Override
        public int size() {
            int size = 0;
            for (Map<MapKey, V> mapKeyVMap : mInnerMap.values()) {
                size += mapKeyVMap.size();
            }
            return size;
        }

        @Override
        public boolean isEmpty() {
            return mInnerMap.isEmpty();
        }

        @Override
        public boolean containsKey(Object key) {
            MapKey mapKey = (MapKey) key;
            Map<MapKey, V> innerMap = mInnerMap.get(mapKey.domainId);
            if (innerMap == null) {
                return false;
            }

            return innerMap.containsKey(key);
        }

        @Override
        public boolean containsValue(Object value) {
            for (Map<MapKey, V> m : mInnerMap.values()) {
                if (m.containsValue(value)) {
                    return true;
                }
            }

            return false;
        }

        @Override
        public V get(Object key) {
            MapKey mapKey = (MapKey) key;
            Map<MapKey, V> innerMap = mInnerMap.get(mapKey.domainId);
            if (innerMap == null) {
                return null;
            }

            return innerMap.get(mapKey);
        }

        @Nullable
        @Override
        public V put(MapKey key, V value) {
            Map<MapKey, V> innerMap = mInnerMap.computeIfAbsent(key.domainId, k -> new ConcurrentHashMap<>());
            return innerMap.put(key, value);
        }

        @Override
        public V remove(Object key) {

            MapKey mapKey = (MapKey) key;
            Map<MapKey, V> innerMap = mInnerMap.get(mapKey.domainId);
            if (innerMap == null) {
                return null;
            }

            V previousValue = innerMap.remove(mapKey);
            if (previousValue == null) {
                return null;
            }

            if (innerMap.isEmpty()) {
                mInnerMap.remove(mapKey.domainId, innerMap);
            }

            return previousValue;
        }

        public void removeAll(long domainId) {
            mInnerMap.remove(domainId);
        }

        @Override
        public void putAll(@NotNull Map<? extends MapKey, ? extends V> m) {
            for (Entry<? extends MapKey, ? extends V> entry : m.entrySet()) {
                put(entry.getKey(), entry.getValue());
            }
        }

        @Override
        public void clear() {
            mInnerMap.clear();
        }

        @NotNull
        @Override
        public Set<MapKey> keySet() {
            Set<MapKey> set = new HashSet<>();
            for (Map<MapKey, V> values : mInnerMap.values()) {
                set.addAll(values.keySet());
            }
            return set;
        }

        @NotNull
        @Override
        public Collection<V> values() {
            Set<V> set = new HashSet<>();
            for (Map<MapKey, V> values : mInnerMap.values()) {
                set.addAll(values.values());
            }
            return set;
        }

        @NotNull
        @Override
        public Set<Entry<MapKey, V>> entrySet() {
            Set<Entry<MapKey, V>> set = new HashSet<>();
            for (Map<MapKey, V> values : mInnerMap.values()) {
                set.addAll(values.entrySet());
            }
            return set;
        }
    }

    private static class MapKey {

        private final long domainId;
        private final String type;
        private final String[] coIds;
        private final int mHashCode;

        public MapKey(long domainId, String type, String... coIds) {
            this.domainId = domainId;
            this.type = type;
            this.coIds = coIds;
            mHashCode = this.calcHashCode();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            MapKey mapKey = (MapKey) o;
            return domainId == mapKey.domainId &&
                    Objects.equals(type, mapKey.type) &&
                    Arrays.equals(coIds, mapKey.coIds);
        }

        @Override
        public int hashCode() {
           return mHashCode;
        }

        private int calcHashCode() {
            int result = Objects.hash(domainId, type);
            result = 31 * result + Arrays.hashCode(coIds);
            return result;
        }
    }
}
