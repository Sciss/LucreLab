package de.sciss.lucre.lab
package impl

import TSet.Mod

import scala.annotation.tailrec
import scala.collection.generic.CanBuildFrom

object TSetImpl {
  def empty[S <: Sys[S], A](implicit tx: S#Tx): Mod[A] = {
    val head = tx.newVar(Option.empty[Cell[S#Tx, A]])
    new Impl(head)
  }

  private class Cell[Tx, A](val elem: A, val next: Var[Tx, Option[Cell[Tx, A]]])

  private final class Impl[S <: Sys[S], A](val head: Var[S#Tx, Option[Cell[S#Tx, A]]])(implicit tx: S#Tx)
    extends TSet.Mod[A] {

    def add(elem: A): Boolean = !contains(elem) && {
      head() = Some(new Cell(elem, tx.newVar(head())))
      true
    }

    def remove(elem: A): Boolean = {
      @tailrec def loop(curr: Var[S#Tx, Option[Cell[S#Tx, A]]]): Boolean = curr() match {
        case Some(cell: Cell[S#Tx, A]) =>
          if (cell.elem == elem) {
            curr() = cell.next()
            true
          } else {
            loop(cell.next)
          }
        case _ => false
      }
      loop(head)
    }

    def filter(p: A => Boolean): TSet[A] = ???

    def to[That](implicit cbf: CanBuildFrom[Nothing, A, That]): That = ???

    def contains[A1 >: A](elem: A1): Boolean = {
      @tailrec def loop(opt: Option[Cell[S#Tx, A]]): Boolean = opt match {
        case Some(cell: Cell[S#Tx, A]) => cell.elem == elem || loop(cell.next())
        case _ => false
      }
      loop(head())
    }
  }
}