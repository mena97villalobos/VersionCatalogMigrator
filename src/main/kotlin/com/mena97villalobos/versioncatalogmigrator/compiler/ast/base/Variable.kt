package com.mena97villalobos.versioncatalogmigrator.compiler.ast.base

import com.mena97villalobos.versioncatalogmigrator.compiler.ast.AST
import com.mena97villalobos.versioncatalogmigrator.compiler.syntax.SourcePosition

abstract class Variable(position: SourcePosition): AST(position)