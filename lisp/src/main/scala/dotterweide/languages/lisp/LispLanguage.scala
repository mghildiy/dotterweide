/*
 *  LispLanguage.scala
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

package dotterweide.languages.lisp

import dotterweide.editor.{Adviser, ColorScheme, Styling}
import dotterweide.formatter.Format
import dotterweide.inspection.Inspection
import dotterweide.lexer.{Lexer, TokenKind}
import dotterweide.parser.Parser
import dotterweide.{Example, FileType, Language}

import scala.collection.immutable.{Seq => ISeq}

object LispLanguage extends Language {
  def name        = "Lisp"
  def description = "Clojure-like functional language"

  def lexer   : Lexer   = LispLexer
  def parser  : Parser  = LispParser
  def format  : Format  = LispFormat
  def adviser : Adviser = LispAdviser

  def stylings: Map[String, Styling] = Map(
    ColorScheme.LightName -> new LispStyling(ColorScheme.LightColors),
    ColorScheme.DarkName  -> new LispStyling(ColorScheme.DarkColors))

  def complements: ISeq[(TokenKind, TokenKind)] = List(LispTokens.Parens, LispTokens.Brackets)

  def lineCommentPrefix = ";"

  def inspections: ISeq[Inspection] = Nil

  def fileType = FileType("Lisp file", "lisp")

  def dispose(): Unit = ()

  def examples: ISeq[Example] = LispExamples.Values
}