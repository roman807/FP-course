package recfun

object RecFun extends RecFunInterface {

  def main(args: Array[String]): Unit = {
    println("Pascal's Triangle")
    for (row <- 0 to 10) {
      for (col <- 0 to row)
        print(s"${pascal(col, row)} ")
      println()
    }
  }

  /**
   * Exercise 1
   */
  def pascal(c: Int, r: Int): Int = {
    if (c==0) 1
    else if (c==r) 1
    else pascal(c-1, r-1) + pascal(c, r-1)
  }

  /**
   * Exercise 2
   */
  def balance(chars: List[Char]): Boolean = {
    def balance_(chars: List[Char], open: Int): AnyVal = {
      if (open < 0) false
      else if (chars.isEmpty) open
      else if (chars.head == '(') balance_(chars.tail, open + 1)
      else if (chars.head == ')') balance_(chars.tail, open - 1)
      else balance_(chars.tail, open)
    }
    if (balance_(chars, 0) == 0) true
    else false
  }


  /**
   * Exercise 3
   */
  def countChange(money: Int, coins: List[Int]): Int = {
    if (money == 0) 1
    else if (!coins.isEmpty && money > 0)
      countChange(money, coins.tail) + countChange(money - coins.head, coins)
    else 0
  }
}
