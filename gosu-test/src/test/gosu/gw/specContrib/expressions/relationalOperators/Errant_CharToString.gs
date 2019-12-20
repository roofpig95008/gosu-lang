package gw.specContrib.expressions.relationalOperators

// ISSTUDIO-210
class Errant_CharToString {
  function foo() {

    var str = "abc"
    var b1 = (str.charAt(0) == "a")      //## issuekeys: OPERATOR '==' CANNOT BE APPLIED TO 'CHAR', 'JAVA.LANG.STRING'

    var c : char = 'a'
    var b2 = (str == c)      //## issuekeys: OPERATOR '==' CANNOT BE APPLIED TO 'JAVA.LANG.STRING', 'CHAR'

    var b3 = (str == 2)      //## issuekeys: OPERATOR '==' CANNOT BE APPLIED TO 'JAVA.LANG.STRING', 'INT'

    var i : int = 2
    var b4 = (str == i)      //## issuekeys: OPERATOR '==' CANNOT BE APPLIED TO 'JAVA.LANG.STRING', 'INT'

    var b5 = (i == "2")      //## issuekeys: OPERATOR '==' CANNOT BE APPLIED TO 'INT', 'JAVA.LANG.STRING'

    var d : double = 2.00000000000001
    var b6 = (str == d)      //## issuekeys: OPERATOR '==' CANNOT BE APPLIED TO 'JAVA.LANG.STRING', 'DOUBLE'

  }

}