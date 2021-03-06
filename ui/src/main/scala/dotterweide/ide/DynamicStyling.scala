/*
 *  DynamicColoring.scala
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

package dotterweide.ide

import dotterweide.editor.{Attributes, Color, Styling}
import dotterweide.lexer.TokenKind

import scala.collection.immutable.{Seq => ISeq}

/** A mutable color scheme, taking a map from scheme names to schemes
  * and notifying observers when the scheme is switched.
  */
class DynamicStyling(delegates: Map[String, Styling]) extends Styling {
  require (delegates.nonEmpty)

  private[this] var _name     : String   = delegates.head._1
  private[this] var _styling  : Styling  = delegates.head._2

  def names: ISeq[String] = delegates.keys.toList
  
  def name: String = _name 
  
  def name_=(name: String): Unit =
    if (_name != name) {
      _name     = name
      _styling  = delegates(name)

      notifyObservers()
    }

  def apply(id: String): Color = _styling(id)

  def attributesFor(kind: TokenKind): Attributes = _styling.attributesFor(kind)
}
