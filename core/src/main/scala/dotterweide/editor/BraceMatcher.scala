/*
 *  BraceMatcher.scala
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

import dotterweide.lexer.{Token, TokenKind}

import scala.collection.immutable.{Seq => ISeq}

private trait BraceMatcher {
  def braceTypeOf(token: Token, tokens: ISeq[Token], offset: Int): BraceType

  def complementIn(tail: ISeq[Token], opening: TokenKind, closing: TokenKind): Option[Token]
}

private abstract sealed class BraceType

private case object Inapplicable  extends BraceType
private case object Paired        extends BraceType
private case object Unbalanced    extends BraceType
