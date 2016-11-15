package gw.specContrib.dynamicTypes

uses dynamic.Dynamic

class Errant_DynamicTypesTypeInfo {

  var PolicyLocation: Dynamic
  var a0 : List<Dynamic> = Arrays.asList(PolicyLocation)
  var aa : List<Dynamic> = Arrays.asList(PolicyLocation.LienHolders)
  var bb : List<Dynamic> = Arrays.asList(PolicyLocation.Lienholders.where(\po -> po.LocationBasedRU == null))


  function blah() {
    var name : Dynamic[]
    var ll : List<Dynamic> = Arrays.asList(name)

    var myarray : Dynamic
    var myll : List<Dynamic>  = Arrays.asList(myarray)

    var c : List<Dynamic> = Arrays.asList({1,2})

    var arrayVar1 : Dynamic = 1
    var arrayVar2 : int = 2
    var arrayMixedVar : List<Dynamic> = Arrays.asList({arrayVar1, arrayVar2})
  }
}