/*
 *  PrefixedApplicationTest.scala
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

package dotterweide.languages.toy.inspection

import org.junit.Test
import dotterweide.inspection.MarkData
import dotterweide.Helpers._

class PrefixedApplicationTest extends InspectionTestBase(PrefixApplication) {
  @Test
  def applicable(): Unit = {
    assertMatches(marksIn("println(-1);")) {
      case Nil =>
    }
  }

  @Test
  def unknownExpressionType(): Unit = {
    assertMatches(marksIn("println(-v);")) {
      case Nil =>
    }
  }

  @Test
  def inapplicable(): Unit = {
    val Message = PrefixApplication.Message("-", "boolean")

    assertMatches(marksIn("println(-true);")) {
      case MarkData(Text("-true"), Message) :: Nil =>
    }
  }
}