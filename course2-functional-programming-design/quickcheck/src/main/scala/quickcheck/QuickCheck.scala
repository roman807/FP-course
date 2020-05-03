package quickcheck

import org.scalacheck._
import Arbitrary._
import Gen._
import Prop._

abstract class QuickCheckHeap extends Properties("Heap") with IntHeap {


  lazy val genHeap: Gen[H] = oneOf(
    const(empty),
    for {
      x <- arbitrary[A]
      m <- oneOf(const(empty), genHeap)
    } yield insert(x, m)
  )

  implicit lazy val arbHeap: Arbitrary[H] = Arbitrary(genHeap)

  property("gen1") = forAll { (h: H) =>
    val m = if (isEmpty(h)) 0 else findMin(h)
    findMin(insert(m, h)) == m
  }

  //  If you insert any two elements into an empty heap, finding the minimum of the resulting
  //  heap should get the smallest of the two elements back.
  property("gen2") = forAll { (a: Int, b: Int) =>
    val h = insert(b, insert(a, empty))
    findMin(h) == Math.min(a, b)
  }

  //  If you insert an element into an empty heap, then delete the minimum, the resulting heap
  //  should be empty.
  property("gen3") = forAll { a: Int =>
    val h = insert(a, empty)
    deleteMin(h) == empty
  }

  //  Given any heap, you should get a sorted sequence of elements when continually finding
  //  and deleting minima. (Hint: recursion and helper functions are your friends.)
  def getList(h: H, l: List[A]): (H, List[A]) = {
    if (h == empty) (h, l)
    else
      getList(deleteMin(h), findMin(h) :: l)
  }

  property("gen4a") = forAll { h: H =>
    val (_, l1) = getList(h, List())
    l1 == l1.sorted(Ordering[A].reverse)
  }

  def toList1(h: H): List[A] = {
    if (h == empty) Nil
    else
      findMin(h) :: toList1(deleteMin(h))
  }

  property("gen4b") = forAll { h: H =>
    val l2 = toList1(h)
    l2 == l2.sorted
  }

  property("gen4c") = forAll { h: H =>
    if (isEmpty(h)) h == empty
    else insert(findMin(h), deleteMin(h)) == h
  }

  // Finding a minimum of the melding of any two heaps should return a minimum of one or the other.
  property("gen5") = forAll { (h1: H, h2: H) =>
    if (isEmpty(h1) & isEmpty(h2))
      isEmpty(h1)
    else if (isEmpty(h1))
      findMin(meld(h1, h2)) == findMin(h2)
    else if (isEmpty(h2))
      findMin(meld(h1, h2)) == findMin(h1)
    else
      (findMin(meld(h1, h2)) == findMin(h1)) | (findMin(meld(h1, h2)) == findMin(h2))
  }

  // My own: if element is deleted, heap must have less elements
  property("gen6") = forAll { h: H =>
    if (h != empty)
      deleteMin(h).toString.length != h.toString.length
    else
      isEmpty(h)

  }

}
