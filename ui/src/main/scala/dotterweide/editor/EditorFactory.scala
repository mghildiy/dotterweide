/*
 *  EditorFactory.scala
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

import dotterweide.Language
import dotterweide.document.{Document, DocumentImpl}

object EditorFactory {
  def createEditorFor(language: Language, history: History, styling: Styling, font: FontSettings): Editor = {
    implicit val async: Async = new AsyncImpl()
    val document: Document    = new DocumentImpl()
    val data    : Data        = new DataImpl(document, language.lexer, language.parser, language.inspections)
    val holder  : ErrorHolder = new ErrorHolderImpl(document, data)

    createEditorFor(document, data, holder, language, history, styling, font)
  }

  def createEditorFor(document: Document, data: Data, holder: ErrorHolder, language: Language,
                      history: History, styling: Styling, font: FontSettings)(implicit async: Async): Editor = {

    val listRenderer  = new VariantCellRenderer(language.lexer, styling)
    val matcher       = new BraceMatcherImpl(language.complements)

    new EditorImpl(document, data, holder, language.lexer, styling, font, matcher, language.format,
      language.adviser, listRenderer, lineCommentPrefix = language.lineCommentPrefix, history = history)
  }
}