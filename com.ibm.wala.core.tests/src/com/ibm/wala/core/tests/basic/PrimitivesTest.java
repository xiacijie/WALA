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
package com.ibm.wala.core.tests.basic;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.ibm.wala.core.tests.util.WalaTestCase;
import com.ibm.wala.util.collections.BimodalMap;
import com.ibm.wala.util.collections.Iterator2Collection;
import com.ibm.wala.util.collections.SmallMap;
import com.ibm.wala.util.debug.Trace;
import com.ibm.wala.util.graph.Dominators;
import com.ibm.wala.util.graph.Graph;
import com.ibm.wala.util.graph.NumberedGraph;
import com.ibm.wala.util.graph.impl.SlowSparseNumberedGraph;
import com.ibm.wala.util.graph.traverse.BFSPathFinder;
import com.ibm.wala.util.graph.traverse.BoundedBFSIterator;
import com.ibm.wala.util.intset.*;

/**
 * 
 * JUnit tests for some primitive operations.
 * 
 * @author sfink
 */
public class PrimitivesTest extends WalaTestCase {

  /**
   * 
   */
  public PrimitivesTest() {
    super("PrimitivesTest");
  }

  /**
   * @param arg0
   */
  public PrimitivesTest(String arg0) {
    super(arg0);
  }

  /**
   * Test the MutableSparseIntSet implementation
   */
  private void doMutableIntSet(MutableIntSetFactory factory) {
    MutableIntSet v = factory.parse("{9,17}");
    MutableIntSet w = factory.make(new int[] {});
    MutableIntSet x = factory.make(new int[] { 7, 4, 2, 4, 2, 2 });
    MutableIntSet y = factory.make(new int[] { 7, 7, 7, 2, 7, 1 });
    MutableIntSet z = factory.parse("{ 9 }");

    Trace.println(w); // { }
    Trace.println(x); // { 2 4 7 }
    Trace.println(y); // { 1 2 7 }
    Trace.println(z); // { 9 }

    MutableIntSet temp = factory.makeCopy(x);
    temp.intersectWith(y);
    Trace.println(temp); // { 2 7 }
    temp.copySet(x);
    temp.addAll(y);
    Trace.println(temp); // { 1 2 4 7 }
    temp.copySet(x);
    Trace.println(IntSetUtil.diff(x, y, factory)); // { 4 }
    Trace.println(IntSetUtil.diff(v, z, factory)); // { 17 }
    Trace.println(IntSetUtil.diff(z, v, factory)); // { }

    // assertTrue(x.union(z).intersection(y.union(z)).equals(x.intersection(y).union(z)));
    MutableIntSet temp1 = factory.makeCopy(x);
    MutableIntSet temp2 = factory.makeCopy(x);
    MutableIntSet tempY = factory.makeCopy(y);
    temp1.addAll(z);
    tempY.addAll(z);
    temp1.intersectWith(tempY);
    temp2.intersectWith(y);
    temp2.addAll(z);
    assertTrue(temp1.sameValue(temp2));

    // assertTrue(x.union(z).diff(z).equals(x));
    assertTrue(w.isEmpty());
    assertTrue(IntSetUtil.diff(x, x, factory).isEmpty());
    assertTrue(IntSetUtil.diff(z, v, factory).isEmpty());
    assertTrue(IntSetUtil.diff(v, z, factory).sameValue(SparseIntSet.singleton(17)));
    assertTrue(IntSetUtil.diff(z, v, factory).isEmpty());
    assertTrue(z.isSubset(v));
    temp = factory.make();
    temp.add(4);
    Trace.println(temp); // { 4 }
    temp.add(7);
    Trace.println(temp); // { 4 7 }
    temp.add(2);
    Trace.println(temp); // { 2 4 7 }
    Trace.println(x); // { 2 4 7 }
    assertTrue(temp.sameValue(x));

    MutableIntSet a = factory.parse("{1,3,5,7,9,11,13,15,17,19,21,23,25,27,29,31,33,35,37,39,41,43,45,47,49,51,53,55,57,59}");
    Trace.println(a); // { 1 3 5 7 9 11 13 15 17 19 21 23 25 27 29 31 33
    // 35
    // 37 39 41 43 45 47 49 51 53 55 57 59 }
    assertTrue(a.sameValue(a));
    IntSet i = a.intersection(temp);
    assertTrue(i.sameValue(SparseIntSet.singleton(7)));
    a.add(100);
    assertTrue(a.sameValue(a));

    MutableIntSet b = factory.parse("{1,3,5,7,9,11,13,15,17,19,21,23,25,27,29,31,33,35,37,39,41,43,45,47,49,51,53,55,57,59,100}");
    assertTrue(a.sameValue(b));
    assertTrue(a.isSubset(b));

    IntSet f = IntSetUtil.diff(b, factory.parse("{7,8,9}"), factory);
    Trace.println(f);
    assertFalse(f.contains(7));
    assertFalse(f.contains(8));
    assertFalse(f.contains(9));
    assertFalse(f.sameValue(b));
    assertTrue(f.isSubset(b));

    IntSet tmp = factory.parse("{2,4,6,8,10,12,14,16,18,20,22,24,26,28,30,32,34,36,38,40,42,44,46,48,50,51,53,55,57,59,61,63}");
    f = IntSetUtil.diff(b, tmp, factory);
    Trace.println(f);
    assertFalse(f.sameValue(b));
    assertTrue(f.isSubset(b));
    assertFalse(f.contains(51));
    assertFalse(f.contains(53));
    assertFalse(f.contains(55));
    assertFalse(f.contains(57));
    assertFalse(f.contains(59));
    assertTrue(f.contains(100));

    tmp = factory.parse("{2,4,6,8,10,12,14,16,18,20,22,24,26,28,30,32,34,36,38,40,42,44,46,48,50,51,53,55,57,59,61,63,100}");
    f = IntSetUtil.diff(b, tmp, factory);
    Trace.println(f);
    assertFalse(f.sameValue(b));
    assertTrue(f.isSubset(b));
    assertFalse(f.contains(51));
    assertFalse(f.contains(53));
    assertFalse(f.contains(55));
    assertFalse(f.contains(57));
    assertFalse(f.contains(59));
    assertFalse(f.contains(100));

    b = factory.makeCopy(a);
    assertTrue(a.sameValue(b));
    b.remove(1);
    b.add(0);
    assertTrue(!a.sameValue(b));

    a = factory.parse("{1}");
    assertFalse(a.isSubset(b));
    b.remove(0);
    assertFalse(a.isSubset(b));
    a.remove(1);
    assertTrue(a.isEmpty());
    i = a.intersection(temp);
    assertTrue(a.isEmpty());

    temp2 = factory.make();
    assertTrue(temp2.sameValue(a));

    a = factory.parse("{2,4,6,8,10,12,14,16,18,20,22,24,26,28,30,32,34,36,38,40,42,44,46,48,50,51,53,55,57,59,61,63}");
    b = factory.parse("{2,4,6,8,10,12,14,16,18,20,22,24,26,28,30,32,34,36,38,40,42,44,46,48,50,52,54,56,58,60,62}");
    MutableIntSet c = factory.parse("{2,4,6,8,10,12,14,16,18,20,22,24,26,28,30,32,34,36,38,40,42,44,46,48,50}");
    MutableIntSet d = factory.parse("{2,4,6,8,10,12,14,16,18,20,22,24,26,28,30,32,34,36,38,40,42,44,46,48,50}");
    MutableIntSet e = factory.parse("{2,4,6,8,10,12,14,16,18,20,22,24,26,28,30,32,34}");

    assertTrue(e.isSubset(d));
    e.addAll(d);
    assertTrue(e.isSubset(d));
    e.remove(12);
    assertTrue(e.isSubset(d));
    e.add(105);
    assertFalse(e.isSubset(d));

    assertFalse(b.isSubset(a));

    b.add(53);
    assertFalse(b.isSubset(a));

    a.add(52);
    a.remove(52);
    assertFalse(b.isSubset(a));

    c.add(55);
    assertFalse(c.isSubset(b));

    d.add(53);
    assertTrue(d.isSubset(b));

    d = factory.make();
    d.copySet(c);
    assertFalse(d.isSubset(b));

    a = factory.parse("{2,4,6,8,10,12,14,16,18,20,22,24,26,28,30,32,34,36,38,40,42,44,46,48,50}");
    b = factory.parse("{2,4,6,8,10,12,14,16,18,20,22,24,26,28,30,32,34,36,38,40,42,44,46,48}");
    assertFalse(a.sameValue(b));
    b.add(50);
    assertTrue(a.sameValue(b));
    a.add(11);
    b.add(11);
    assertTrue(a.sameValue(b));

    a = factory.parse("{2,4,6,8,10,12,14,16,18,20,50}");
    b = factory.parse("{24,26,28,30,32,34,36,38,40,42,44,46,48}");
    c = factory.parse("{2,4,6,8,10,12,14,16,18,20,22,24,26,28,30,32,34,36,38,40,42,44,46,48,50}");
    a.addAll(b);
    a.add(22);
    assertTrue(a.sameValue(c));

    a = factory.parse("{2,4,6,8,10,12,14,16,18,20,50}");
    b = factory.parse("{24,26,28,30,32,34,36,38,40,42,44,46,48}");
    c = factory.parse("{2,4,6,8,10,12,14,16,18,20,22,24,26,28,30,32,34,36,38,40,42,44,46,48,50}");
    b.addAll(factory.parse("{22}"));
    a.addAll(b);
    assertTrue(a.sameValue(c));

    a = factory.parse("{2,4,6,8,10,12,14,16,18,20}");
    b = factory.parse("{22,24,26,28,30,32,34,36,38,40,42,44,46,48}");
    c = factory.parse("{2,4,6,8,10,12,14,16,18,20,22,24,26,28,30,32,34,36,38,40,42,44,46,48,50}");
    c.remove(22);
    a.addAll(b);
    assertFalse(a.sameValue(c));

    a = factory.parse("{1,3,5,7,9,11,13,15,17,19,21,23,25,27,29,31,33,35,37,39,41,43,45,47,49,51,53,55,57,59}");
    Trace.println(a); // { 1 3 5 7 9 11 13 15 17 19 21 23 25 27 29 31 33
    // 35
    // 37 39 41 43 45 47 49 51 53 55 57 59 }
    assertTrue(a.sameValue(a));
    i = a.intersection(temp);
    assertTrue(i.sameValue(SparseIntSet.singleton(7)));
    a.add(100);
    assertTrue(a.sameValue(a));

    b = factory.parse("{1,3,5,7,9,11,13,15,17,19,21,23,25,27,29,31,33,35,37,39,41,43,45,47,49,51,53,55,57,59,100}");
    assertTrue(a.sameValue(b));
    assertTrue(a.isSubset(b));

    b = factory.makeCopy(a);
    assertTrue(a.sameValue(b));
    b.remove(1);
    b.add(0);
    assertTrue(!a.sameValue(b));

    a = factory.parse("{1}");
    assertFalse(a.isSubset(b));
    b.remove(0);
    assertFalse(a.isSubset(b));
    a.remove(1);
    assertTrue(a.isEmpty());
    i = a.intersection(temp);
    assertTrue(a.isEmpty());

    temp2 = factory.make();
    assertTrue(temp2.sameValue(a));

    for(int idx = 500; idx < 550; ) {
      for(int xx = 0; xx < 50; xx++,idx++) {
	temp2.add(idx);
      }
      Trace.println(temp2);
    }

    for(int idx = 3000; idx < 3200; ) {
      for(int xx = 0; xx < 50; xx++,idx++) {
	temp2.add(idx);
      }
      Trace.println(temp2);
    }


    temp2 = factory.make();
    assertTrue(temp2.sameValue(a));

    for(int idx = 500; idx < 550; ) {
      for(int xx = 0; xx < 50; xx++,idx++) {
	temp2.add(idx);
      }
      Trace.println(temp2);
    }

    for(int idx = 0; idx < 25; idx++) {
      temp2.add(idx);
      Trace.println(temp2);
    }

  }

  /**
   * Test the MutableSharedBitVectorIntSet implementation
   */
  public void testMutableSharedBitVectorIntSet() {
    doMutableIntSet(new MutableSharedBitVectorIntSetFactory());
  }

  /**
   * Test the MutableSparseIntSet implementation
   */
  public void testMutableSparseIntSet() {
    doMutableIntSet(new MutableSparseIntSetFactory());
  }

  /**
   * Test the BimodalMutableSparseIntSet implementation
   */
  public void testBimodalMutableSparseIntSet() {
    doMutableIntSet(new BimodalMutableIntSetFactory());
  }

  /**
   * Test the BitVectorIntSet implementation
   */
  public void testBitVectorIntSet() {
    doMutableIntSet(new BitVectorIntSetFactory());
  }

  /**
   * Test the SemiSparseMutableIntSet implementation
   */
  public void testSemiSparseMutableIntSet() {
    doMutableIntSet(new SemiSparseMutableIntSetFactory());
  }

  public void testSmallMap() {
    SmallMap<Integer, Integer> M = new SmallMap<Integer, Integer>();
    Integer I1 = new Integer(1);
    Integer I2 = new Integer(2);
    Integer I3 = new Integer(3);
    M.put(I1, I1);
    M.put(I2, I2);
    M.put(I3, I3);

    Integer I = (Integer) M.get(new Integer(2));
    assertTrue(I != null);
    assertTrue(I.equals(I2));

    I = (Integer) M.get(new Integer(4));
    assertTrue(I == null);

    I = (Integer) M.put(new Integer(2), new Integer(3));
    assertTrue(I.equals(I2));
    I = (Integer) M.get(I2);
    assertTrue(I.equals(I3));
  }

  public void testBimodalMap() {
    Map<Integer, Integer> M = new BimodalMap<Integer, Integer>(3);
    Integer I1 = new Integer(1);
    Integer I2 = new Integer(2);
    Integer I3 = new Integer(3);
    Integer I4 = new Integer(4);
    Integer I5 = new Integer(5);
    Integer I6 = new Integer(6);
    M.put(I1, I1);
    M.put(I2, I2);
    M.put(I3, I3);

    Integer I = M.get(new Integer(2));
    assertTrue(I != null);
    assertTrue(I.equals(I2));

    I = M.get(new Integer(4));
    assertTrue(I == null);

    I = M.put(new Integer(2), new Integer(3));
    assertTrue(I.equals(I2));
    I = M.get(I2);
    assertTrue(I.equals(I3));

    M.put(I4, I4);
    M.put(I5, I5);
    M.put(I6, I6);
    I = M.get(new Integer(4));
    assertTrue(I != null);
    assertTrue(I.equals(I4));

    I = M.get(new Integer(7));
    assertTrue(I == null);

    I = M.put(new Integer(2), new Integer(6));
    assertTrue(I.equals(I3));
    I = M.get(I2);
    assertTrue(I.equals(I6));
  }

  public void testBFSPathFinder() {
    NumberedGraph<Integer> G = makeBFSTestGraph();

    // path from 0 to 8
    BFSPathFinder<Integer> pf = new BFSPathFinder<Integer>(G, G.getNode(0), G.getNode(8));
    List<Integer> p = pf.find();

    // path should be 8, 6, 4, 2, 0
    Trace.println("Path is " + p);
    for (int i = 0; i < p.size(); i++) {
      assertTrue((p.get(i)).intValue() == new int[] { 8, 6, 4, 2, 0 }[i]);
    }
  }

  public void testBoundedBFS() {
    NumberedGraph<Integer> G = makeBFSTestGraph();

    BoundedBFSIterator<Integer> bfs = new BoundedBFSIterator<Integer>(G, G.getNode(0), 0);
    Collection<Integer> c = new Iterator2Collection<Integer>(bfs);
    assertTrue(c.size() == 1);

    bfs = new BoundedBFSIterator<Integer>(G, G.getNode(0), 1);
    c = new Iterator2Collection<Integer>(bfs);
    assertTrue(c.size() == 3);

    bfs = new BoundedBFSIterator<Integer>(G, G.getNode(0), 2);
    c = new Iterator2Collection<Integer>(bfs);
    assertTrue(c.size() == 5);

    bfs = new BoundedBFSIterator<Integer>(G, G.getNode(0), 3);
    c = new Iterator2Collection<Integer>(bfs);
    assertTrue(c.size() == 7);

    bfs = new BoundedBFSIterator<Integer>(G, G.getNode(0), 4);
    c = new Iterator2Collection<Integer>(bfs);
    assertTrue(c.size() == 9);

    bfs = new BoundedBFSIterator<Integer>(G, G.getNode(0), 5);
    c = new Iterator2Collection<Integer>(bfs);
    assertTrue(c.size() == 10);

    bfs = new BoundedBFSIterator<Integer>(G, G.getNode(0), 500);
    c = new Iterator2Collection<Integer>(bfs);
    assertTrue(c.size() == 10);
  }

  private NumberedGraph<Integer> makeBFSTestGraph() {
    // test graph
    NumberedGraph<Integer> G = new SlowSparseNumberedGraph<Integer>();

    // add 10 nodes
    Integer[] nodes = new Integer[10];
    for (int i = 0; i < nodes.length; i++)
      G.addNode(nodes[i] = new Integer(i));

    // edges to i-1, i+1, i+2
    for (int i = 0; i < nodes.length; i++) {
      if (i > 0) {
        G.addEdge(nodes[i], nodes[i - 1]);
      }
      if (i < nodes.length - 1) {
        G.addEdge(nodes[i], nodes[i + 1]);
        if (i < nodes.length - 2) {
          G.addEdge(nodes[i], nodes[i + 2]);
        }
      }
    }
    return G;
  }

  public void testDominatorsA() {
    // test graph
    Graph<Object> G = new SlowSparseNumberedGraph<Object>();

    // add nodes
    Object[] nodes = new Object[11];
    for (int i = 0; i < nodes.length; i++)
      G.addNode(nodes[i] = new Integer(i));

    // add edges
    G.addEdge(nodes[10], nodes[0]);
    G.addEdge(nodes[10], nodes[1]);
    G.addEdge(nodes[0], nodes[2]);
    G.addEdge(nodes[1], nodes[3]);
    G.addEdge(nodes[2], nodes[5]);
    G.addEdge(nodes[3], nodes[5]);
    G.addEdge(nodes[4], nodes[2]);
    G.addEdge(nodes[5], nodes[8]);
    G.addEdge(nodes[6], nodes[3]);
    G.addEdge(nodes[7], nodes[4]);
    G.addEdge(nodes[8], nodes[7]);
    G.addEdge(nodes[8], nodes[9]);
    G.addEdge(nodes[9], nodes[6]);

    // compute dominators
    Dominators<Object> D = new Dominators<Object>(G, nodes[10]);

    // assertions
    int i = 0;
    Object[] desired4 = new Object[] { nodes[4], nodes[7], nodes[8], nodes[5], nodes[10] };
    for (Iterator<Object> d4 = D.dominators(nodes[4]); d4.hasNext();)
      assertTrue(d4.next() == desired4[i++]);

    int j = 0;
    Object[] desired5 = new Object[] { nodes[8] };
    for (Iterator<? extends Object> t4 = D.dominatorTree().getSuccNodes(nodes[5]); t4.hasNext();)
      assertTrue(t4.next() == desired5[j++]);

    assertTrue(D.dominatorTree().getSuccNodeCount(nodes[10]) == 5);
  }

  public void testBinaryIntegerRelation() {
    byte[] impl = new byte[] { BasicNaturalRelation.SIMPLE, BasicNaturalRelation.TWO_LEVEL,
        BasicNaturalRelation.SIMPLE };
    IBinaryNaturalRelation R = new BasicNaturalRelation(impl, BasicNaturalRelation.TWO_LEVEL);
    R.add(3, 5);
    R.add(3, 7);
    R.add(3, 9);
    R.add(3, 11);
    R.add(5, 1);
    int count = 0;
    for (Iterator<IntPair> it = R.iterator(); it.hasNext();) {
      Trace.println(it.next());
      count++;
    }
    assertTrue(count == 5);

    IntSet x = R.getRelated(3);
    assertTrue(x.size() == 4);

    x = R.getRelated(5);
    assertTrue(x.size() == 1);

    R.remove(5, 1);
    x = R.getRelated(5);
    assertTrue(x == null);

    R.add(2, 1);
    R.add(2, 2);
    R.remove(2, 1);
    x = R.getRelated(2);
    assertTrue(x.size() == 1);

    R.removeAll(3);
    x = R.getRelated(3);
    assertTrue(x == null);

    x = R.getRelated(0);
    assertTrue(x == null);

    for (int i = 0; i < 100; i++) {
      R.add(1, i);
    }
    assertTrue(R.getRelated(1).size() == 100);
    R.remove(1, 1);
    assertTrue(R.getRelated(1).size() == 99);
  }

  public void testUnionFind() {
    int SIZE = 10000;
    IntegerUnionFind uf = new IntegerUnionFind(SIZE);
    int count = countEquivalenceClasses(uf);
    assertTrue("Got count " + count, count == SIZE);

    uf.union(3, 7);
    assertTrue(uf.find(3) == uf.find(7));
    assertTrue("Got uf.find(3)=" + uf.find(3), uf.find(3) == 3 || uf.find(3) == 7);

    uf.union(7, SIZE - 1);
    assertTrue(uf.find(3) == uf.find(SIZE - 1));
    assertTrue("Got uf.find(3)=" + uf.find(3), uf.find(3) == 3 || uf.find(3) == 7 || uf.find(3) == SIZE - 1);

    for (int i = 0; i < SIZE - 1; i++) {
      uf.union(i, i + 1);
    }
    count = countEquivalenceClasses(uf);
    assertTrue("Got count " + count, count == 1);

    uf = new IntegerUnionFind(SIZE);
    for (int i = 0; i < SIZE; i++) {
      if ((i % 2) == 0) {
        uf.union(i, 0);
      } else {
        uf.union(i, 1);
      }
    }
    count = countEquivalenceClasses(uf);
    assertTrue("Got count " + count, count == 2);
  }

  private int countEquivalenceClasses(IntegerUnionFind uf) {
    HashSet<Integer> s = new HashSet<Integer>();
    for (int i = 0; i < uf.size(); i++) {
      s.add(new Integer(uf.find(i)));
    }
    return s.size();
  }

  public void testBitVector() {
    testSingleBitVector(new BitVector());
  }

  public void testOffsetBitVector0_10() {
    testSingleBitVector(new OffsetBitVector(0, 10));
  }

  public void testOffsetBitVector10_10() {
    testSingleBitVector(new OffsetBitVector(10, 10));
  }

  public void testOffsetBitVector50_10() {
    testSingleBitVector(new OffsetBitVector(50, 10));
  }

  public void testOffsetBitVector50_50() {
    testSingleBitVector(new OffsetBitVector(50, 50));
  }

  public void testOffsetBitVector100_10() {
    testSingleBitVector(new OffsetBitVector(100, 10));
  }

  @SuppressWarnings("unchecked")
  private void testSingleBitVector(BitVectorBase bv) {
    // does the following not automatically scale the bitvector to
    // a reasonable size?
    bv.set(55);

    assertTrue("bv.max() is " + bv.max(), bv.max() == 55);
    assertTrue(bv.get(55));

    bv.set(59);
    assertTrue(bv.max() == 59);
    assertTrue(bv.get(55));
    assertTrue(bv.get(59));

    {
      boolean[] gets = new boolean[] { false, true, true };
      int[] bits = new int[] { 0, 55, 59 };
      for (int i = 0, j = 0; i != -1; i = bv.nextSetBit(i + 1), j++) {
        assertTrue(i == bits[j]);
        assertTrue(bv.get(i) == gets[j]);
      }
    }

    bv.set(77);

    assertTrue("bv.max() is " + bv.max(), bv.max() == 77);
    {
      boolean[] gets = new boolean[] { false, true, true, true };
      int[] bits = new int[] { 0, 55, 59, 77 };
      for (int i = 0, j = 0; i != -1; i = bv.nextSetBit(i + 1), j++) {
        assertTrue(i == bits[j]);
        assertTrue(bv.get(i) == gets[j]);
      }
    }

    bv.set(3);
    assertTrue("bv.max() is " + bv.max(), bv.max() == 77);
    {
      boolean[] gets = new boolean[] { false, true, true, true, true };
      int[] bits = new int[] { 0, 3, 55, 59, 77 };
      for (int i = 0, j = 0; i != -1; i = bv.nextSetBit(i + 1), j++) {
        assertTrue(i == bits[j]);
        assertTrue(bv.get(i) == gets[j]);
      }
    }

    Trace.println(bv);
  }

  public void testBitVectors() {
    testBitVectors(new BitVector(), new BitVector());
  }

  public void testOffsetBitVectors150_10() {
    testBitVectors(new OffsetBitVector(150, 10), new OffsetBitVector(150, 10));
  }

  public void testOffsetBitVectors100_200_10() {
    testBitVectors(new OffsetBitVector(100, 10), new OffsetBitVector(200, 10));
  }

  public void testOffsetBitVectors100_25_10() {
    testBitVectors(new OffsetBitVector(100, 10), new OffsetBitVector(25, 10));
  }

  public void testOffsetBitVectors35_25_20_10() {
    testBitVectors(new OffsetBitVector(35, 20), new OffsetBitVector(25, 10));
  }

  @SuppressWarnings("unchecked")
  private <T extends BitVectorBase> void testBitVectors(T v1, T v2) {
    v1.set(100);
    v1.set(101);
    v1.set(102);
    assertTrue("v1.max() is " + v1.max(), v1.max() == 102);

    v2.set(200);
    v2.set(201);
    v2.set(202);
    assertTrue("v2.max() is " + v2.max(), v2.max() == 202);

    assertTrue(v1.intersectionEmpty(v2));
    assertTrue(v2.intersectionEmpty(v1));

    v1.or(v2);

    Trace.println("v1 = " + v1 + ", v2 = " + v2);
    assertFalse("v1 = " + v1 + ", v2 = " + v2, v1.intersectionEmpty(v2));
    assertFalse("v1 = " + v1 + ", v2 = " + v2, v2.intersectionEmpty(v1));
    assertTrue("v1.max() is " + v1.max(), v1.max() == 202);

    {
      boolean[] gets = new boolean[] { false, true, true, true, true, true, true };
      int[] bits = new int[] { 0, 100, 101, 102, 200, 201, 202 };
      for (int i = 0, j = 0; i != -1; i = v1.nextSetBit(i + 1), j++) {
        assertTrue(i == bits[j]);
        assertTrue(v1.get(i) == gets[j]);
      }
    }

    v1.clearAll();
    v2.clearAll();

    v1.set(100);
    v1.set(101);
    v1.set(102);
    v1.set(103);
    v1.set(104);
    v1.set(105);
    assertTrue("v1.max() is " + v1.max(), v1.max() == 105);

    v2.set(103);
    v2.set(104);
    v2.set(200);
    v2.set(201);
    assertTrue("v2.max() is " + v2.max(), v2.max() == 201);

    v1.and(v2);
    assertTrue("v1.max() is " + v1.max(), v1.max() == 104);

    {
      boolean[] gets = new boolean[] { false, true, true };
      int[] bits = new int[] { 0, 103, 104 };
      for (int i = 0, j = 0; i != -1; i = v1.nextSetBit(i + 1), j++) {
        assertTrue(i == bits[j]);
        assertTrue(v1.get(i) == gets[j]);
      }
    }

    v1.set(100);
    v1.set(101);
    v1.set(102);
    v1.set(105);
    assertTrue("v1.max() is " + v1.max(), v1.max() == 105);

    {
      boolean[] gets = new boolean[] { false, true, true, true, true, true, true };
      int[] bits = new int[] { 0, 100, 101, 102, 103, 104, 105 };
      for (int i = 0, j = 0; i != -1; i = v1.nextSetBit(i + 1), j++) {
        assertTrue(i == bits[j]);
        assertTrue(v1.get(i) == gets[j]);
      }
    }

    v2.clear(103);
    v2.clear(104);
    v1.andNot(v2);

    {
      boolean[] gets = new boolean[] { false, true, true, true, true, true, true };
      int[] bits = new int[] { 0, 100, 101, 102, 103, 104, 105 };
      for (int i = 0, j = 0; i != -1; i = v1.nextSetBit(i + 1), j++) {
        assertTrue(i == bits[j]);
        assertTrue(v1.get(i) == gets[j]);
      }
    }

    v2.set(101);
    v2.set(102);

    Trace.println("v1 = " + v1 + ", v2 = " + v2);
    v1.andNot(v2);

    {
      boolean[] gets = new boolean[] { false, true, true, true, true };
      int[] bits = new int[] { 0, 100, 103, 104, 105 };
      for (int i = 0, j = 0; i != -1; i = v1.nextSetBit(i + 1), j++) {
        assertTrue(i == bits[j]);
        assertTrue(v1.get(i) == gets[j]);
      }
    }

    v1.clearAll();
    v2.clearAll();

    v1.set(35);
    v1.set(101);
    v1.set(102);
    v1.set(103);
    v1.set(104);
    v1.set(105);

    v2.set(101);
    v2.set(102);
    v2.set(104);
    v2.set(206);

    Trace.println("v1 = " + v1 + ", v2 = " + v2);
    v1.xor(v2);

    {
      boolean[] gets = new boolean[] { false, true, true, true, true };
      int[] bits = new int[] { 0, 35, 103, 105, 206 };
      for (int i = 0, j = 0; i != -1; i = v1.nextSetBit(i + 1), j++) {
        assertTrue(i == bits[j]);
        assertTrue(v1.get(i) == gets[j]);
      }
    }

    v2.set(35);
    v2.set(103);
    v2.set(105);

    Trace.println("v1 = " + v1 + ", v2 = " + v2);
    assertTrue(v1.isSubset(v2));

    v2.clearAll();
    v2.set(111);
    v2.or(v1);
    assertTrue(v1.isSubset(v2));

    v2.and(v1);

    assertTrue(v1.sameBits(v2));

    v1.clearAll();
  }

  private static OffsetBitVector makeBigTestOffsetVector() {
    OffsetBitVector v1 = new OffsetBitVector(50000096, 1024);
    v1.set(50000101);
    v1.set(50000103);
    v1.set(50000112);
    v1.set(50000114);
    v1.set(50000116);
    v1.set(50000126);
    v1.set(50000128);
    v1.set(50000129);
    v1.set(50000135);
    v1.set(50000137);
    v1.set(50000143);
    v1.set(50000148);
    v1.set(50000151);
    v1.set(50000158);
    v1.set(50000163);
    v1.set(50000167);
    v1.set(50000170);
    v1.set(50000173);
    v1.set(50000180);
    v1.set(50000182);
    v1.set(50000185);
    v1.set(50000187);
    v1.set(50000190);
    v1.set(50000194);
    v1.set(50000199);
    v1.set(50000201);
    v1.set(50000204);
    v1.set(50000206);
    v1.set(50000209);
    v1.set(50000210);
    v1.set(50000218);
    v1.set(50000221);
    v1.set(50000223);
    v1.set(50000227);
    v1.set(50000232);
    v1.set(50000234);
    v1.set(50000243);
    v1.set(50000246);
    v1.set(50000250);
    v1.set(50000257);
    v1.set(50000263);
    v1.set(50000268);
    v1.set(50000273);
    v1.set(50000275);
    v1.set(50000278);
    v1.set(50000282);
    v1.set(50000285);
    v1.set(50000290);
    v1.set(50000293);
    v1.set(50000298);
    v1.set(50000300);
    v1.set(50000303);
    v1.set(50000306);
    v1.set(50000308);
    v1.set(50000310);
    v1.set(50000315);
    v1.set(50000325);
    v1.set(50000327);
    v1.set(50000335);
    v1.set(50000337);
    v1.set(50000342);
    v1.set(50000345);
    v1.set(50000349);
    v1.set(50000360);
    v1.set(50000362);
    v1.set(50000369);
    v1.set(50000374);
    v1.set(50000375);
    v1.set(50000378);
    v1.set(50000380);
    v1.set(50000383);
    v1.set(50000384);
    v1.set(50000388);
    v1.set(50000391);
    v1.set(50000400);
    v1.set(50000405);
    v1.set(50000409);
    v1.set(50000414);
    v1.set(50000417);
    v1.set(50000421);
    v1.set(50000426);
    v1.set(50000431);
    v1.set(50000435);
    v1.set(50000437);
    v1.set(50000439);
    v1.set(50000442);
    v1.set(50000446);
    v1.set(50000450);
    v1.set(50000455);
    v1.set(50000459);
    v1.set(50000460);
    v1.set(50000463);
    v1.set(50000470);
    v1.set(50000471);
    v1.set(50000480);
    v1.set(50000497);
    v1.set(50000498);
    v1.set(50000500);
    v1.set(50000505);
    v1.set(50000508);
    v1.set(50000511);
    v1.set(50000513);
    v1.set(50000520);
    v1.set(50000526);
    v1.set(50000529);
    v1.set(50000536);
    v1.set(50000539);
    v1.set(50000551);
    v1.set(50000555);
    v1.set(50000559);
    v1.set(50000563);
    v1.set(50000564);
    v1.set(50000566);
    v1.set(50000568);
    v1.set(50000570);
    v1.set(50000576);
    v1.set(50000587);
    v1.set(50000590);
    v1.set(50000594);
    v1.set(50000601);
    v1.set(50000605);
    v1.set(50000608);
    v1.set(50000610);
    v1.set(50000619);
    v1.set(50000628);
    v1.set(50000630);
    v1.set(50000634);
    v1.set(50000644);
    v1.set(50000647);
    v1.set(50000654);
    v1.set(50000658);
    v1.set(50000661);
    v1.set(50000663);
    v1.set(50000669);
    v1.set(50000671);
    v1.set(50000673);
    v1.set(50000676);
    v1.set(50000677);
    v1.set(50000680);
    v1.set(50000689);
    v1.set(50000692);
    v1.set(50000695);
    v1.set(50000705);
    v1.set(50000707);
    v1.set(50000708);
    v1.set(50000714);
    v1.set(50000716);
    v1.set(50000721);
    v1.set(50000727);
    v1.set(50000732);
    v1.set(50000736);
    v1.set(50000739);
    v1.set(50000741);
    v1.set(50000750);
    v1.set(50000753);
    v1.set(50000755);
    v1.set(50000765);
    v1.set(50000769);
    v1.set(50000773);
    v1.set(50000779);
    v1.set(50000782);
    v1.set(50000784);
    v1.set(50000787);
    v1.set(50000791);
    v1.set(50000792);
    v1.set(50000793);
    v1.set(50000795);
    v1.set(50000801);
    v1.set(50000806);
    v1.set(50000809);
    return v1;
  }

  public void testSpecificBugsInOffsetBitVectors() {
    OffsetBitVector v1 = makeBigTestOffsetVector();
    Trace.println(v1);
    
    OffsetBitVector v2 = new OffsetBitVector(50000128, 512);
    v2.set(50000137);
    v2.set(50000204);
    v2.set(50000278);
    v2.set(50000315);
    v2.set(50000362);
    v2.set(50000450);
    v2.set(50000455);
    v2.set(50000471);
    Trace.println(v2);
       
    v1.andNot(v2);
    Trace.println(v1);
    assertTrue(v1.intersectionEmpty(v2));

    v1 = makeBigTestOffsetVector();
    v1.and(v2);
    Trace.println(v1);
    assertTrue(v1.sameBits(v2));
    assertTrue(v1.isSubset(v2));
    assertTrue(v2.isSubset(v1));
  }

  public void testSpecificBugsInSemiSparseMutableIntSets() {
    SemiSparseMutableIntSet v1 = new SemiSparseMutableIntSet();
    v1.add(54);
    v1.add(58);
    v1.add(59);
    v1.add(64);
    v1.add(67);
    v1.add(73);
    v1.add(83);
    v1.add(105);
    v1.add(110);
    v1.add(126);
    v1.add(136);
    v1.add(143);
    v1.add(150);
    v1.add(155);
    v1.add(156);
    v1.add(162);
    v1.add(168);
    v1.add(183);
    v1.add(191);
    v1.add(265);
    v1.add(294);
    v1.add(324); 
    v1.add(344);
    v1.add(397);
  }
}
