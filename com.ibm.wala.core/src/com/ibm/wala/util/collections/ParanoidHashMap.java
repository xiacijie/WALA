/*******************************************************************************
 * Copyright (c) 2002 - 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.ibm.wala.util.collections;

import java.util.LinkedHashMap;
import java.util.Map;

import com.ibm.wala.annotations.Internal;
import com.ibm.wala.util.debug.Assertions;

/**
 * 
 * a debugging aid. This implementation complains if you stick an object in here
 * which appears to use System.identityHashCode()
 * 
 * @author sfink
 */
@Internal
public class ParanoidHashMap<K, V> extends LinkedHashMap<K, V> {
  public static final long serialVersionUID = 909018793791787198L;

  /**
   * @param t
   * @throws NullPointerException
   *           if t is null
   */
  public ParanoidHashMap(Map<K, V> t) throws NullPointerException {
    super(t.size());
    putAll(t);
  }

  public ParanoidHashMap(int size) {
    super(size);
  }

  public ParanoidHashMap() {
  }

  /*
   * @see java.util.Map#put(java.lang.Object, java.lang.Object)
   */
  @Override
  public V put(K arg0, V arg1) {
    if (arg0 != null && arg0.hashCode() == System.identityHashCode(arg0)) {
      Assertions._assert(false, arg0.getClass().toString());
    }
    return super.put(arg0, arg1);
  }

  /*
   * @see java.util.Map#putAll(java.util.Map)
   */
  @Override
  public void putAll(Map<? extends K, ? extends V> arg0) {
    if (arg0 == null) {
      throw new IllegalArgumentException("arg0 is null");
    }
    for (Map.Entry<? extends K, ? extends V> E: arg0.entrySet()) {
      put(E.getKey(), E.getValue());
    }
  }

}