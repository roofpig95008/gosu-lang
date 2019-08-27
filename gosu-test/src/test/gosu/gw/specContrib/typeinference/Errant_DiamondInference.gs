package gw.specContrib.typeinference

// IDE-4166 replaced by ISSTUDIO-12: Gosu supported reverse inference in 1.13 but it was taken out of 1.14+

class Errant_DiamondInference {

    function hello<T>(t : T) : T { return null}

    function foo() {
      // GitHub #126 BG: Fixed
      var bar: ArrayList<Integer> = hello(new ArrayList())  //## issuekeys: The type "java.util.ArrayList<java.lang.Object>" cannot be converted to "java.util.ArrayList<java.lang.Integer>"
      var bar2: ArrayList<Integer> = hello(new ArrayList<String>( ))  //## issuekeys: The type "java.util.ArrayList<java.lang.String>" cannot be converted to "java.util.ArrayList<java.lang.Integer>"
    }

}
