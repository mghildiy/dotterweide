/*
 *  MockData.scala
 *  (Dotterweide)
 *
 *  Copyright (c) 2019 the Dotterweide authors. All rights reserved.
 *
 *  This software is published under the GNU Lesser General Public License v2.1+
 *
 *  For further information, please contact Hanns Holger Rutz at
 *  contact@sciss.de
 */

/*
 * Original code copyright 2018 Pavel Fatin, https://pavelfatin.com
 * Licensed under the Apache License, Version 2.0 (the "License"): http://www.apache.org/licenses/LICENSE-2.0
 */

package dotterweide.editor

import dotterweide.lexer.Token
import dotterweide.node.Node

import scala.collection.immutable.{Seq => ISeq}
import scala.concurrent.Future

class MockData extends Data {
  def text = ""

  def tokens: ISeq[Token] = Nil

  def structure: Option[Node] = None

  def errors: ISeq[Error] = Nil

  def pass: Pass = Pass.Text

  def hasNextPass = false

  def nextPass(): Unit = ()

//  def compute(): Unit = ()

  def computeStructure(): Future[Option[Node]] = Future.successful(structure)

  def hasFatalErrors = false
}