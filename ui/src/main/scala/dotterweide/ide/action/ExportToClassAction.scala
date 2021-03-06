/*
 *  ExportToClassAction.scala
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

package dotterweide.ide.action

import java.io.{File, FileOutputStream}

import dotterweide.compiler.Assembler
import dotterweide.editor.{Async, Data, StructureAction}
import dotterweide.node.Node
import javax.swing.JOptionPane
import javax.swing.filechooser.FileNameExtensionFilter

import scala.swing.{Action, Component, FileChooser}

class ExportToClassAction(title0: String, mnemonic0: Char, val data: Data, parent: Component)
                         (implicit val async: Async)
  extends Action(title0) with StructureAction {

  mnemonic = mnemonic0

  def applyWithStructure(root: Node): Unit =
    if (!data.hasFatalErrors) {
      val chooser = new FileChooser()
      chooser.title = "Export to Class"
      chooser.fileFilter = new FileNameExtensionFilter("JVM class", "class")
      chooser.showSaveDialog(parent) match {
        case FileChooser.Result.Approve => try {
          save(root, chooser.selectedFile)
        } catch {
          case e: Exception => JOptionPane.showMessageDialog(parent.peer,
            e.getMessage, "Export error", JOptionPane.ERROR_MESSAGE)
        }
        case _ =>
      }
    }

  private def save(root: Node, file: File): Unit = {
    val (name, path) = if (file.getName.endsWith(".class")) (file.getName.dropRight(6), file.getPath)
      else (file.getName, "%s.class".format(file.getPath))

    val byteCode  = Assembler.assemble(root, name)
    val stream    = new FileOutputStream(path)
    try {
      stream.write(byteCode)
      stream.flush()
    } finally {
      stream.close()
    }
  }
}