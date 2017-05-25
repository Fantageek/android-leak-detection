package com.khanhtm.leakcanary;

import com.squareup.leakcanary.AnalysisResult;
import com.squareup.leakcanary.ExcludedRefs;
import com.squareup.leakcanary.HeapAnalyzer;

import java.io.File;
import java.lang.ref.PhantomReference;
import java.lang.ref.WeakReference;

public class LeakCanaryJavaMain {
  public static void main(String[] args) {
    if (args.length!=2) {
      System.out.println("-------------DETECT-JAVA-MEMORY-LEAK-@KHANH--------------");
      System.out.println("invalid argument: [1] [2]");
      System.out.println("       [1]: HPROF input path");
      System.out.println("       [2]: class name to detect");
      return;
    }
    System.out.println("-------------DETECT-JAVA-MEMORY-LEAK-@KHANH--------------");
    File heapDumpFile = new File(args[0]);
    ExcludedRefs.BuilderWithParams excludedRefs;
    excludedRefs = ExcludedRefs.builder() //
            .clazz(WeakReference.class.getName())
            .alwaysExclude()
            .clazz("java.lang.ref.FinalizerReference")
            .alwaysExclude()
            .clazz(PhantomReference.class.getName())
            .alwaysExclude();
    HeapAnalyzer analyzer = new HeapAnalyzer(excludedRefs.build());
    AnalysisResult[] results = analyzer.checkLeakByClass(heapDumpFile, args[1]);
    for (AnalysisResult result : results) {
      System.out.println("----------------------------------------------------------");
      System.out.print(leakInfo(result));
      System.out.println("----------------------------------------------------------");
    }
    System.out.println("-------------DETECT-JAVA-MEMORY-LEAK-@KHANH---------------");
  }

  private static String leakInfo(AnalysisResult result) {
    String info = "";
    if (result.leakFound) {
      if (result.excludedLeak) {
        info += "* EXCLUDED LEAK.\n";
      }
      info += "* " + result.className;
      info += " has leaked:\n" + result.leakTrace.toString() + "\n";
      info += "* Retaining: " + result.retainedHeapSize + ".\n";
      info += "\n* Details:\n" + result.leakTrace.toDetailedString();
      info += "\n";
    } else if (result.failure != null) {
      // We duplicate the library version & Sha information because bug reports often only contain
      // the stacktrace.
      result.failure.printStackTrace();
      info += "* FAILURE in " + result.failure + "\n";
    } else {
      info += "* NO LEAK FOUND.\n\n";
    }
    return info;
  }
}
