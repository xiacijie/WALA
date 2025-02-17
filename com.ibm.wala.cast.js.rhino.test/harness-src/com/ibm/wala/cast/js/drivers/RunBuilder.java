/*
 * Copyright (c) 2007 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 */
package com.ibm.wala.cast.js.drivers;

import com.ibm.wala.cast.ipa.callgraph.CAstCallGraphUtil;
import com.ibm.wala.cast.js.ipa.callgraph.JSCFABuilder;
import com.ibm.wala.cast.js.translator.CAstRhinoTranslatorFactory;
import com.ibm.wala.cast.js.util.JSCallGraphBuilderUtil;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.callgraph.propagation.CPAContextSelector;
import com.ibm.wala.util.CancelException;
import com.ibm.wala.util.WalaException;
import java.io.IOException;

public class RunBuilder {

  public static void main(String[] args)
      throws IOException, WalaException, IllegalArgumentException, CancelException {

    com.ibm.wala.cast.js.ipa.callgraph.JSCallGraphUtil.setTranslatorFactory(
        new CAstRhinoTranslatorFactory());

    JSCFABuilder builder = JSCallGraphBuilderUtil.makeScriptCGBuilder(args[0], args[1]);
    builder.setContextSelector(new CPAContextSelector(builder.getContextSelector()));
    CallGraph CG = builder.makeCallGraph(builder.getOptions());
    CAstCallGraphUtil.AVOID_DUMP = false;
    CAstCallGraphUtil.dumpCG(builder.getCFAContextInterpreter(), builder.getPointerAnalysis(), CG);
  }
}
