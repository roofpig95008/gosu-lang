package gw.specContrib.expressions.intervals

class Errant_CustomTypesIntervalExpressions {

  class Foo{}

  class FooComparable implements Comparable {
    override function compareTo(o: Object): int {
      return 0
    }
  }

  class FooIterable implements Iterable {
    override function iterator(): Iterator<Object> {
      return null
    }
  }

  class FooComparableIterable implements Iterable, Comparable {
    override function compareTo(o: Object): int {
      return 0
    }

    override function iterator(): Iterator<Object> {
      return null
    }
  }

  //custom types
  var foo1 = new Foo()..new Foo()            //## issuekeys: OPERATOR '..' CANNOT BE APPLIED TO 'GW.SPECCONTRIB.EXPRESSIONS.INTERVALS.ERRANT_CUSTOMTYPESINTERVALEXPRESSIONS.FOO', 'GW.SPECCONTRIB.EXPRESSIONS.INTERVALS.ERRANT_CUSTOMTYPESINTERVALEXPRESSIONS.FOO'
  var fooComparable1 = new FooComparable()..new FooComparable()
  var fooIterable1 = new FooIterable()..new FooIterable()            //## issuekeys: OPERATOR '..' CANNOT BE APPLIED TO 'GW.SPECCONTRIB.EXPRESSIONS.INTERVALS.ERRANT_CUSTOMTYPESINTERVALEXPRESSIONS.FOOITERABLE', 'GW.SPECCONTRIB.EXPRESSIONS.INTERVALS.ERRANT_CUSTOMTYPESINTERVALEXPRESSIONS.FOOITERABLE'
  var fooComparableIterable1 = new FooComparableIterable()..new FooComparableIterable()

  //with boolean
  var foo2 = new Foo()..true            //## issuekeys: OPERATOR '..' CANNOT BE APPLIED TO 'GW.SPECCONTRIB.EXPRESSIONS.INTERVALS.ERRANT_CUSTOMTYPESINTERVALEXPRESSIONS.FOO', 'BOOLEAN'
  var fooComparable2 = new FooComparable()..true      //## issuekeys: OPERATOR '..' CANNOT BE APPLIED TO 'GW.SPECCONTRIB.EXPRESSIONS.INTERVALS.ERRANT_CUSTOMTYPESINTERVALEXPRESSIONS.FOOCOMPARABLE', 'BOOLEAN'
  var fooIterable2 = new FooIterable()..true            //## issuekeys: OPERATOR '..' CANNOT BE APPLIED TO 'GW.SPECCONTRIB.EXPRESSIONS.INTERVALS.ERRANT_CUSTOMTYPESINTERVALEXPRESSIONS.FOOITERABLE', 'BOOLEAN'
  var fooComparableIterable2 = new FooComparableIterable()..true      //## issuekeys: OPERATOR '..' CANNOT BE APPLIED TO 'GW.SPECCONTRIB.EXPRESSIONS.INTERVALS.ERRANT_CUSTOMTYPESINTERVALEXPRESSIONS.FOOCOMPARABLEITERABLE', 'BOOLEAN'

  var fooComparable3 = new FooComparable()..new Foo()      //## issuekeys: OPERATOR '..' CANNOT BE APPLIED TO 'GW.SPECCONTRIB.EXPRESSIONS.INTERVALS.ERRANT_CUSTOMTYPESINTERVALEXPRESSIONS.FOOCOMPARABLE', 'GW.SPECCONTRIB.EXPRESSIONS.INTERVALS.ERRANT_CUSTOMTYPESINTERVALEXPRESSIONS.FOO'
  var fooComparableIterable3 = new FooComparableIterable()..new FooIterable()      //## issuekeys: OPERATOR '..' CANNOT BE APPLIED TO 'GW.SPECCONTRIB.EXPRESSIONS.INTERVALS.ERRANT_CUSTOMTYPESINTERVALEXPRESSIONS.FOOCOMPARABLEITERABLE', 'GW.SPECCONTRIB.EXPRESSIONS.INTERVALS.ERRANT_CUSTOMTYPESINTERVALEXPRESSIONS.FOOITERABLE'
}