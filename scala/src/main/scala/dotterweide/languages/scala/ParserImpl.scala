package dotterweide.languages.scala

import dotterweide.Span
import dotterweide.languages.scala.node.{AppliedTypeTreeNode, ApplyNode, AssignNode, AssignOrNamedArgNode, BindNode, BlockNode, CaseDefNode, ClassDefNode, DefDefNode, EmptyNode, FunctionNode, IdentNode, IfNode, ImportNode, IsRef, LabelDefNode, LiteralNode, MatchNode, ModifierNode, ModuleDefNode, NameNode, NewNode, PackageDefNode, RefNameNode, ReturnNode, SelectNode, SuperNode, TemplateNode, ThisNode, ThrowNode, TryNode, TypeApplyNode, TypeDefNode, TypeTreeNode, TypedNode, UnApplyNode, ValDefNode}
import dotterweide.node.NodeImpl

import scala.reflect.api.Position
import scala.reflect.internal.util.DefinedPosition
import scala.tools.nsc.interactive.DotterweidePeek.{reloadSource, typedTree}
import scala.tools.nsc.interactive.Global

private trait ParserImpl {
  _: CompilerActor =>

  private def compile(fullText: String): c.Tree = {
    val srcFile   = c.newSourceFile(fullText)
    // c.askReset()
    c.newTyperRun()
    reloadSource(c)(srcFile)
    reporter.reset()
    //      val respTree  = new c.Response[c.Tree]
    // c.askLoadedTyped(srcFile, keepLoaded = false, response = respTree)
    //      waitLoadedTyped(c)(srcFile, respTree, keepLoaded = false, onSameThread = true)
    //    val treeTyped: c.Tree = respTree.get.left.get
    val treeTyped: c.Tree = typedTree(c)(srcFile, forceReload = true)
    treeTyped
  }

  protected def runCompile(text0: String): NodeImpl = {
    val fullText      = prelude + text0 + postlude
    val mainStart     = prelude.length
    val mainStop      = mainStart + text0.length
    val tree: c.Tree  = compile(fullText)

    type Parents = List[Global#Tree]

    def setPosition(n: NodeImpl, pos: Position, inclusive: Boolean = false): n.type = {
      val start     = math.max(0, pos.start - mainStart)
      val stop      = math.min(mainStop, if (inclusive) pos.end + 1 else pos.end) - mainStart
      if (stop >= start) {
        val spanText  = text0.substring(start, stop)
        n.span        = Span(spanText, start, stop)
      }
      n
    }

    // we re-use name-nodes for first use of a symbol
    var symCache = Map.empty[Global#Symbol, NameNode]

    // we collect all references to symbols here,
    // and in the end we try to resolve them by looking
    // up the corresponding name nodes and storing their
    // parents in the reference's target
    var refMem   = List.empty[(IsRef, Global#Symbol)]

    def complete(p: Global#Tree, parents: Parents, n: NodeImpl): n.type = {
      p.pos match {
        case dp: DefinedPosition => setPosition(n, dp)
        case _ =>
      }
      n
    }

    def mkMods(mods: Global#Modifiers): List[ModifierNode] =
      mods.positions.iterator.filter(_._2.isDefined).toList.sortBy(_._2.start).map {
        case (code, pos) =>
          val nm = new ModifierNode(code)
          setPosition(nm, pos, inclusive = true /* WTF */)
      }

    def mkDefName(sym: Global#Symbol): NameNode = {
      // note: do not reuse the name-node, because we'll get
      // in hell with breaking 1:1 children <-> parent, siblings relationships
      //            val n = symCache.getOrElseUpdate(sym,
      //              mkName(sym.pos, sym.name)
      //            )
      val n = completeDefName(sym.pos, new NameNode(nameString(sym.name)))
      if (!symCache.contains(sym)) symCache += sym -> n
      n
    }

    def mkRefName(p: Global#Tree, name: Global#Name): NameNode = {
      val n   = new RefNameNode(nameString(name))
      val pp  = p.pos
      completeRefName(pp, n)
      addRef(p.symbol, n)
    }

    def nameString(name: Global#Name): String =
      name.decoded.trim // WTF `trim`, there are dangling trailing spaces

    def completeRefName(pos: Position, n: NameNode): n.type = {
      pos match {
        case pd: DefinedPosition =>
          val start = pd.point - mainStart
          val stop  = pd.end   - mainStart
          if (start >= 0 && stop <= text0.length) {
            val spanText = text0.substring(start, stop)
            n.span = Span(spanText, start, stop)
          }
        case _ =>
      }
      n
    }

    // since it's a definition, it can't be renamed.
    // in val-def, we need to use n.name.length
    // for the span, not pos.end!
    def completeDefName(pos: Position, n: NameNode): n.type = {
      pos match {
        case pd: DefinedPosition =>
          val spanText  = n.name
          val start     = pd.point - mainStart
          val stop      = start + spanText.length
          if (start >= 0 && stop <= text0.length) {
            n.span = Span(spanText, start, stop)
          }
        case _ =>
      }
      n
    }

    def addRef(sym: Global#Symbol, n: IsRef): n.type = {
      refMem ::= ((n, sym))
      n
    }

    def parseCaseDef(p: Global#CaseDef, parents: Parents): CaseDefNode = {
      // c.CaseDef(pat: Tree, guard: Tree, body: Tree)
      val patNode   = parseChild(p, p.pat   , parents)
      val guardNode = parseChild(p, p.guard , parents)
      val bodyNode  = parseChild(p, p.body  , parents)
      val n         = new CaseDefNode(patNode, guardNode, bodyNode)
      complete(p, parents, n)
    }

    def parseIdent(p: Global#Ident, parents: Parents): IdentNode = {
      // c.Ident(_ /* name: Name */)
      val nameNode = completeRefName(p.pos, new NameNode(nameString(p.name)))
      if (!symCache.contains(p.symbol)) symCache += p.symbol -> nameNode

      val n         = new IdentNode(nameNode)
      complete(p, parents, n)
    }

    //          def parseImportSelector(p: Global#ImportSelector, parents: Parents): IdentNode = {
    //            // c.ImportSelector(_ /* name: Name */, _ /* namePos: Int */, _ /* rename: Name */, _ /* renamePos: Int */)
    //            val n = new ImportSelectorNode
    //            complete(p, parents, n)
    //          }

    def parseTemplate(p: Global#Template, parents: Parents): TemplateNode = {
      val parents1 = p :: parents
      // c.Template(parents /* :List[Tree] */, valDef /* :ValDef */, stats /* :List[Tree] */) =>
      val parentNodes = p.parents.map { child =>
        parseChild(p, child, parents)
      }
      val selfNode  = parseValDef(p.self , parents1)
      val bodyNodes = p.body.map { child =>
        parse(child, parents1)
      }
      val n         = new TemplateNode(parentNodes, selfNode, bodyNodes)
      complete(p, parents, n)
    }

    def parseTypeDef(p: Global#TypeDef, parents: Parents): TypeDefNode = {
      val parents1 = p :: parents
      // c.TypeDef(_ /* mods: Modifiers */, _ /* name: TypeName */, tParams /* : List[TypeDef] */, rhs /* : Tree */)
      val tParamNodes = p.tparams.map(parseTypeDef(_, parents1))
      val rhsNode     = parseChild(p, p.rhs, parents)
      val n           = new TypeDefNode(tParamNodes, rhsNode)
      complete(p, parents, n)
    }

    def parseValDef(p: Global#ValDef, parents: Parents): ValDefNode = {
      // c.ValDef(_ /* mods: Modifiers */, _ /* name: TermName */, tpt /* :Tree */, rhs /* :Tree */) =>
      // println(s"VAL DEF ${p.name} - mods = ${p.mods}; name.start ${p.name.start}; name.len ${p.name.length()}")

      // XXX TODO --- need to add defaults
      val modNodes  = mkMods(p.mods)
      val nameNode  = mkDefName(p.symbol)
      val tptNode   = parseChild(p, p.tpt, parents)
      val rhsNode   = parseChild(p, p.rhs, parents)
      val n         = new ValDefNode(modNodes, nameNode, tptNode, rhsNode)
      complete(p, parents, n)
    }

    def parseChild(p: Global#Tree, c: Global#Tree, parents: List[Global#Tree]): NodeImpl = {
      val parents1  = p :: parents
      val n         = parse(c, parents1)
      complete(c, parents1, n)
    }

    def parse(p: Global#Tree, parents: List[Global#Tree]): NodeImpl = {
      @inline def parents1: Parents = p :: parents

      @inline
      def parseChild1(c: Global#Tree): NodeImpl =
        parseChild(p, c, parents)

      //            log.info(s"-- ${"  " * indent}${p.productPrefix} | ${p.pos} ${p.pos.getClass.getSimpleName}")
      val res: NodeImpl = p match {
        //              case c.Alternative      (_)     =>
        //              case c.Annotated        (_, _)  =>
        //              case c.ApplyDynamic     (_, _)  =>
        //              case c.ArrayValue       (_, _)  =>

        case c.AppliedTypeTree(tpt /* :Tree */, args /* :List[Tree] */) =>
          val tgtNode   = parseChild1(tpt)
          val argNodes  = args.map { child =>
            parseChild1(child)
          }
          new AppliedTypeTreeNode(tgtNode, argNodes)

        case c.Apply(receiver /* :Tree */, args /* :List[Tree] */) =>
          val rcvNode   = parseChild1(receiver)
          val argNodes  = args.map { child =>
            parseChild1(child)
          }
          new ApplyNode(rcvNode, argNodes)

        case c.Assign(lhs /* :Tree */, rhs /* :Tree */) =>
          val lhsNode = parseChild1(lhs)
          val rhsNode = parseChild1(rhs)
          new AssignNode(lhsNode, rhsNode)

        case /* c. */ NamedArg(lhs /* :Tree */, rhs /* :Tree */) =>
          val lhsNode = parseChild1(lhs)
          val rhsNode = parseChild1(rhs)
          new AssignOrNamedArgNode(lhsNode, rhsNode)

        case c.Bind(_ /* name: Name */, body /* :Tree */) =>
          val nameNode  = mkDefName(p.symbol)
          val bodyNode  = parseChild1(body)
          new BindNode(nameNode, bodyNode)

        case c.Block(init /* :List[Tree] */, last /* :Tree */) =>
          val initNodes = init.map { child =>
            parseChild1(child)
          }
          val lastNode = parseChild1(last)
          new BlockNode(initNodes, lastNode)

        case c.ClassDef(mods /* Modifiers */, _ /* name: TypeName */, tParams /* List[TypeDef] */,
        impl /* :Template */) =>
          val modNodes    = mkMods(mods)
          val nameNode    = mkDefName(p.symbol)
          val tParamNodes = tParams.map { child =>
            parseTypeDef(child, parents1)
          }
          val childNode = parseTemplate(impl, parents1)
          new ClassDefNode(modNodes, nameNode, tParamNodes, childNode)

        case c.DefDef(mods /* :Modifiers */, _ /* name: TermName */, tParams /* :List[TypeDef] */,
        vParamsS /* :List[List[ValDef]] */, tpt /* :Tree */, rhs /* :Tree */) =>
          // println(s"def-def symbol = ${p.symbol}")
          // p.symbol.pos
          val modNodes    = mkMods(mods)
          val nameNode    = mkDefName(p.symbol)
          val tParamNodes = tParams.map { child =>
            parseTypeDef(child, parents1)
          }
          val vParamNodesS = vParamsS.map { vParams =>
            vParams.map { child =>
              parseValDef(child, parents1)
            }
          }
          val tptNode = parseChild1(tpt)
          val rhsNode = parseChild1(rhs)
          new DefDefNode(modNodes, nameNode, tParamNodes, vParamNodesS, tptNode, rhsNode)

        case c.EmptyTree =>
          new EmptyNode

        case c.Function(vParams /* :List[ValDef] */, body /* :Tree */) =>
          val vParamNodes = vParams.map { child =>
            parseValDef(child, parents1)
          }
          val bodyNode = parseChild1(body)
          new FunctionNode(vParamNodes, bodyNode)

        case in: c.Ident => parseIdent(in, parents)

        case c.If(cond /* :Tree */, thenP /* :Tree */, elseP /* :Tree */) =>
          val condNode  = parseChild1(cond )
          val thenNode  = parseChild1(thenP)
          val elseNode  = parseChild1(elseP)
          new IfNode(condNode, thenNode, elseNode)

        case c.Import(expr /* :Tree */, _ /* sel: List[ImportSelector] */) =>
          val exprNode  = parseChild1(expr)
          //                val selNodes = sel.map { child =>
          //                  parseImportSelector(child, parents1)
          //                }
          new ImportNode(exprNode)

        case c.LabelDef(_ /* name: TermName */, params /* :List[Ident] */, rhs /* :Tree */) =>
          val paramNodes = params.map { child =>
            parseIdent(child, parents1)
          }
          val rhsNode = parseChild1(rhs)
          new LabelDefNode(paramNodes, rhsNode)

        case c.Literal(const /* :Constant */) =>
          new LiteralNode(const.value)

        case c.Match(sel /* :Tree */, cases /* :List[CaseDef] */) =>
          val selNode = parseChild1(sel)
          val caseNodes = cases.map { child =>
            parseCaseDef(child, parents1)
          }
          new MatchNode(selNode, caseNodes)

        case c.ModuleDef(mods /* Modifiers */, _ /* name: TermName */, child /* :Template */) =>
          val modNodes  = mkMods(mods)
          val nameNode  = mkDefName(p.symbol)
          val childNode = parseTemplate(child, parents1)
          new ModuleDefNode(modNodes, nameNode, childNode)

        case c.New(tpt /* :Tree */ ) =>
          val tptNode = parseChild1(tpt)
          new NewNode(tptNode)

        case c.PackageDef(pid /* :RefTree */, stats /* :List[Tree] */) =>
          val pidNode     = parseChild1(pid)
          val statNodes   = stats.map { child =>
            parseChild1(child)
          }
          new PackageDefNode(pidNode, statNodes)

        case c.Return(expr /* :Tree */) =>
          val exprNode = parseChild1(expr)
          new ReturnNode(exprNode)

        case c.Select(qualifier /* :Tree */, name /* :Name */) =>
          val nameNode      = mkRefName(p, name)
          val qualifierNode = parseChild1(qualifier)
          new SelectNode(qualifierNode, nameNode)

        case c.Super(qualifier /* :Tree */, _ /* mix: TypeName */) =>
          val qNode = parseChild1(qualifier)
          new SuperNode(qNode)

        case tn: c.Template => parseTemplate(tn, parents)

        case c.This(_ /* qualifier: TypeName */) =>
          new ThisNode

        case c.Throw(expr /* :Tree */) =>
          val exprNode = parseChild1(expr)
          new ThrowNode(exprNode)

        case c.Try(block /* :Tree */, cases /* :List[CaseDef] */, finalizer /* :Tree */) =>
          val blockNode = parseChild1(block)
          val caseNodes = cases.map { child =>
            parseCaseDef(child, parents1)
          }
          val finalizerNode = parseChild1(finalizer)
          new TryNode(blockNode, caseNodes, finalizerNode)

        case c.TypeApply(fun /* :Tree */, args /* :List[Tree] */) =>
          val funNode     = parseChild1(fun)
          val argNodes    = args.map { child =>
            parseChild1(child)
          }
          new TypeApplyNode(funNode, argNodes)

        case c.Typed(expr /* :Tree */, tpt /* :Tree */) =>
          val exprNode  = parseChild1(expr)
          val tptNode   = parseChild1(tpt)
          new TypedNode(exprNode, tptNode)

        case td: c.TypeDef => parseTypeDef(td, parents)

        case c.TypeTree() =>
          new TypeTreeNode

        case c.UnApply(fun /* :Tree */, args /* :List[Tree] */) =>
          val funNode     = parseChild1(fun)
          val argNodes    = args.map { child =>
            parseChild1(child)
          }
          new UnApplyNode(funNode, argNodes)

        case vd: c.ValDef => parseValDef(vd, parents)

        case _ =>
          log.info(s"-- SKIP ${"  " * parents.size}${p.productPrefix} | ${p.pos} ${p.pos.getClass.getSimpleName}")
          new NodeImpl("<unknown>")
      }
      res
    }

    val programNode = parse(tree, Nil)
    complete(tree, Nil, programNode)
    // log.info("done tree")

    // now assign the reference targets
    for {
      (rn, sym) <- refMem
      name      <- symCache.get(sym)
      dn        <- name.parent
    } {
      rn.target = Some(dn)
    }

    var moreErrors = List.empty[NodeImpl]
    var errorCount = 0

    reporter.infos.iterator.filter(info => info.pos.isDefined && info.severity.id >= 2).foreach { info =>
      val n   = new NodeImpl("leaf")
      setPosition(n, info.pos)
      val sp  = n.span

      // XXX TODO inefficient
      val childOpt = programNode.elements.find(_.span.matches(sp))
      val child = childOpt.getOrElse {
        moreErrors ::= n
        n
      }
      child.problem = Some(info.msg)
      errorCount += 1
    }

    log.debug(s"done errors ($errorCount)")

    if (moreErrors.isEmpty) {
      programNode
    } else {
      val top       = new NodeImpl("top")
      top.children  = programNode :: moreErrors.reverse
      top
    }
  }
}
