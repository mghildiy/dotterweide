/*
 *  LispAdviserTest.scala
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

import dotterweide.editor.{Adviser, AdviserTestBase}
import org.junit.Test

class LispAdviserTest extends AdviserTestBase(LispLexer, LispParser, LispAdviser) {
  @Test
  def coreSymbol(): Unit = {
    assertVariantsInclude("|")("def")
  }

  @Test
  def librarySymbol(): Unit = {
    assertVariantsInclude("|")("map")
  }

  @Test
  def userSymbol(): Unit = {
    assertVariantsInclude("(def someSymbol 1) |")("someSymbol")
  }

  @Test
  def anchorExclusion(): Unit = {
    assertVariantsExclude("(def |)")(Adviser.DefaultAnchor)
  }
}
