/*
 * (c) Copyright 2017 Johannes Illenberger
 *
 * Project de.dbanalytics.spic.*
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.dbanalytics.spic.sim.data;

import de.dbanalytics.spic.data.Attributable;

import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.Map;

/**
 * @author johannes
 */
public abstract class CachedElement implements Attributable {

    private final Attributable delegate;
    private Map<Object, Object> cache;

    public CachedElement(Attributable delegate) {
        this.delegate = delegate;
    }

    protected Attributable getDelegate() {
        return delegate;
    }

    @Override
    public String getAttribute(String key) {
        synchronize(key);
        return delegate.getAttribute(key);
    }

    /*
    FIXME: experimental
     */
    public String getAttributeDirect(String key) {
        return delegate.getAttribute(key);
    }

    @Override
    public String setAttribute(String key, String value) {
        invalidateCache(key);
        return delegate.setAttribute(key, value);
    }

    @Override
    public String removeAttribute(String key) {
        invalidateCache(key);
        return delegate.removeAttribute(key);
    }

    @Override
    public Collection<String> keys() {
        return delegate.keys();
    }

    public Object getData(Object key) {
        initCache();
        Object value = cache.get(key);
        if (value == null) value = initObjectValue(key);
        return value;
    }

    public Object setData(Object key, Object value) {
        initCache();
        return cache.put(key, value);
    }

    //FIXME: As long as this is not synchronized with the delegate, the cache will be rebuild upon call of getData()
    public Object removeData(Object key) {
        initCache();
        return cache.remove(key);
    }

    private void initCache() {
        if (cache == null) cache = new IdentityHashMap<>(5);
    }

    private Object initObjectValue(Object key) {
        /*
        Check if there is a plain-object-key-pair. If not, this key is "standalone" and is not to be synchronized with
         the plain attribute.
         */
        String plainKey = Converters.getPlainKey(key);
        if (plainKey == null) return null;
        else {
            /*
            Check if the delegate stores a value for this key. If yes, convert it to an object value.
             */
            String plainValue = delegate.getAttribute(plainKey);
            if (plainValue == null) return null;
            else {
                Object value = Converters.toObject(plainKey, plainValue);
                setData(key, value);
                return value;
            }
        }
    }

    private void invalidateCache(String key) {
        /*
        Invalidate the cache if there is a plain-object-key-pair.
         */
        Object objKey = Converters.getObjectKey(key);
        if (objKey != null) removeData(objKey);
    }

    private void synchronize(String key) {
        if (cache != null) {
        /*
        Synchronize the cached data with the plain data, if there is a plain-object-key-pair. Do nothing if there is no
        data for a key, i.e. setting a data value to null does not affect the plain value.
         */
            Object objKey = Converters.getObjectKey(key);
            if (objKey != null) {
                Object value = cache.get(objKey);
                if (value != null) {
                    String plainValue = Converters.toString(objKey, value);
                    delegate.setAttribute(key, plainValue);
                }
            }
        }
    }
}
