package reductions

import scala.annotation._
import org.scalameter._

object ParallelParenthesesBalancingRunner {

  @volatile var seqResult = false

  @volatile var parResult = false

  val standardConfig = config(
    Key.exec.minWarmupRuns -> 40,
    Key.exec.maxWarmupRuns -> 80,
    Key.exec.benchRuns -> 120,
    Key.verbose -> true
  ) withWarmer(new Warmer.Default)

  def main(args: Array[String]): Unit = {
    val length = 100000000
    val chars = new Array[Char](length)
    val threshold = 10000
    val seqtime = standardConfig measure {
      seqResult = ParallelParenthesesBalancing.balance(chars)
    }
    println(s"sequential result = $seqResult")
    println(s"sequential balancing time: $seqtime")

    val fjtime = standardConfig measure {
      parResult = ParallelParenthesesBalancing.parBalance(chars, threshold)
    }
    println(s"parallel result = $parResult")
    println(s"parallel balancing time: $fjtime")
    println(s"speedup: ${seqtime.value / fjtime.value}")
  }
}

object ParallelParenthesesBalancing extends ParallelParenthesesBalancingInterface {

  /** Returns `true` iff the parentheses in the input `chars` are balanced.
   */
  def balance(chars: Array[Char]): Boolean = {
    @scala.annotation.tailrec
    def balanceInner(chars: Array[Char], open: Int): Int = {
      if (chars.isEmpty) open
      else {
        if (chars.head == '(') balanceInner(chars.tail, open + 1)
        else if (chars.head == ')'){
          if (open == 0) -1
          else balanceInner(chars.tail, open - 1)
        }
        else balanceInner(chars.tail, open)
      }
    }
    balanceInner(chars, 0) == 0
  }

  /** Returns `true` iff the parentheses in the input `chars` are balanced.
   */
  def parBalance(chars: Array[Char], threshold: Int): Boolean = {

    def traverse(idx: Int, until: Int, arg1: Int, arg2: Int): (Int, Int) = {
      // arg1: open, arg2: min
      if (idx < until){
        if (chars(idx) == '(') traverse(idx+1, until, arg1 + 1, arg2)
        else if (chars(idx) == ')') traverse(idx+1, until, arg1-1, math.min(arg1-1, arg2))
        else traverse(idx+1, until, arg1, arg2)
      }
      else (arg1, arg2)
    }

    def reduce(from: Int, until: Int): (Int, Int) = {
      if (until - from <= threshold) traverse(from, until, 0, 0)
      else {
        val mid = from + (until - from) / 2
        val (v1, v2) = parallel(
          reduce(from, mid),
          reduce(mid, until)
        )
        (v1._1 + v2._1, v1._1 + v2._2)
      }
    }
    reduce(0, chars.length) == (0, 0)
  }


//  def parBalance(chars: Array[Char], threshold: Int): Boolean = {
//
//    def traverse(idx: Int, until: Int, arg1: Int, arg2: Int) /*: ???*/ = {
//      ???
//    }
//
//    def reduce(from: Int, until: Int) /*: ???*/ = {
//      ???
//    }
//
//    reduce(0, chars.length) == ???
//  }


  // For those who want more:
  // Prove that your reduction operator is associative!

}
