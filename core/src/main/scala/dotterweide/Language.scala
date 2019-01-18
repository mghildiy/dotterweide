/*
 *  Language.scala
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

package dotterweide

import dotterweide.editor.{Adviser, Coloring}
import dotterweide.formatter.Format
import dotterweide.inspection.Inspection
import dotterweide.lexer.{Lexer, TokenKind}
import dotterweide.parser.Parser

/** Access to a programming language, including description, lexer, parser, etc. */
trait Language {
  def name: String

  def description: String

  def lexer: Lexer

  def parser: Parser

  /** A map from color scheme names to the schemes. */
  def colorings: Map[String, Coloring]

  /** Pairs of tokens which are symmetric and can be highlighted together,
    * such as matching braces.
    */
  def complements: Seq[(TokenKind, TokenKind)]

  /** Default style for formatting the language with white space. */
  def format: Format

  /** The syntactic prefix for line comments. */
  def comment: String

  def inspections: Seq[Inspection]

  def adviser: Adviser

  def fileType: FileType

  def examples: Seq[Example]
}