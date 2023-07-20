package com.mena97villalobos.versioncatalogmigrator.compiler.visitors

import com.mena97villalobos.versioncatalogmigrator.compiler.ast.Visitor
import com.mena97villalobos.versioncatalogmigrator.compiler.ast.implementations.dependencies.DependencyBlockDeclaration
import com.mena97villalobos.versioncatalogmigrator.compiler.ast.implementations.dependencies.ImplementationDeclaration
import com.mena97villalobos.versioncatalogmigrator.compiler.ast.implementations.dependencies.ModuleIdentifier
import com.mena97villalobos.versioncatalogmigrator.compiler.ast.implementations.dependencies.VariableDeclaration
import com.mena97villalobos.versioncatalogmigrator.compiler.ast.implementations.terminals.Identifier

class VersionCatalogGenerator: Visitor {

    data class Dependency(
        val group: String,
        val name: String,
        var version: String,
        var isVersionVariable: Boolean,
    )

    data class ModuleVersionIdentifier(
        val moduleIndex: Int,
        var moduleVersionName: String
    )

    private val variables = hashMapOf<String, String>()
    private val modules = arrayListOf<Dependency>()
    private var dependencyBlockVisited = false

    val versionReference = mutableListOf<VersionReference>()

    private lateinit var finalVersionCatalogue: HashMap<String, MutableList<ModuleVersionIdentifier>>

    override fun visitIdentifier(declaration: Identifier, o: Any): Any {
        println("Visiting Identifier")
        return Unit
    }

    override fun visitImplementationDeclaration(declaration: ImplementationDeclaration, o: Any): Any {
        return declaration.dependencyIdentifier.visit(this, o)
    }

    override fun visitVariableDeclaration(variable: VariableDeclaration, o: Any): Any {
        variables[variable.identifier.spelling] = variable.value.spelling
        return variables
    }

    override fun visitDependencyBlock(block: DependencyBlockDeclaration, o: Any): Any {
        block.implementations.forEach {
            it.visit(this, o)
        }
        block.variables.forEach {
            it.visit(this, o)
        }
        dependencyBlockVisited = true
        return block
    }

    override fun visitModuleIdentifier(module: ModuleIdentifier, o: Any): Any {
        modules.add(
            Dependency(
                module.identifiers.joinToString(".") { it.spelling },
                module.moduleName.spelling,
                module.versionName.spelling,
                module.versionName.spelling.startsWith("$")
            )
        )
        return modules
    }

    fun generateVersionCatalogue(): String {
        if (dependencyBlockVisited) {
            mapVariableNamesToActualNumber()
            reviewAndAdaptVersions()
            return "${generateVersions()}${generateModules()}"
        }
        return ""
    }

    private fun mapVariableNamesToActualNumber() {
        modules.filter { it.isVersionVariable }
            .forEach {
                val versionKey = it.version.removePrefix("$")
                val actualVersion = variables.getOrDefault(versionKey, it.version)
                it.version = actualVersion
                it.isVersionVariable = false
            }
    }

    private fun reviewAndAdaptVersions() {
        val finalVersionsByVersion = hashMapOf<String, MutableList<ModuleVersionIdentifier>>()
        modules.forEachIndexed { index, dependency ->
            val version = ModuleVersionIdentifier(index, dependency.name)
            if(finalVersionsByVersion.contains(dependency.version)) {
                finalVersionsByVersion[dependency.version]?.add(version)
            } else {
                finalVersionsByVersion[dependency.version] = mutableListOf(version)
            }
        }
        val regex = Regex("[^a-z]")
        finalVersionsByVersion.forEach { (key, value) ->
            val normalizedStrings = value.map {
                it.moduleVersionName = regex.replace(it.moduleVersionName, "")
                it
            }

            val matchingVersionName = getMaximumSharedSubstring(normalizedStrings.map { it.moduleVersionName })
            var versionNames = value
            if (matchingVersionName.isNotEmpty()) {
                versionNames = versionNames.map {
                    it.moduleVersionName = matchingVersionName
                    it
                }.toMutableList()
            }

            finalVersionsByVersion[key] = versionNames.map {
                it.moduleVersionName = "${it.moduleVersionName}-version"
                it
            }.toMutableList()
        }

        finalVersionCatalogue = finalVersionsByVersion
    }

    private fun generateVersions(): String {
        val versionsSection = arrayListOf<String>()
        finalVersionCatalogue.forEach { (versionNumber, dependenciesMap) ->
            dependenciesMap.distinctBy { it.moduleVersionName }.forEach {
                versionsSection.add("${it.moduleVersionName} = \"$versionNumber\"")
            }
        }
        return "[versions]\n${versionsSection.joinToString("\n")}\n\n"
    }

    private fun generateModules(): String {
        val librariesSection = arrayListOf<String>()
        val versionsSection = arrayListOf<String>()

        finalVersionCatalogue.forEach { (versionNumber, dependenciesMap) ->
            dependenciesMap.forEach {
                with(modules[it.moduleIndex]) {
                    versionsSection.add("${it.moduleVersionName} = \"$versionNumber\"")
                    librariesSection.add("$name = { group = \"$group\", name = \"$name\", version.ref = \"${it.moduleVersionName}\"}")

                    val versionCatalogReference = "libs.${name.replace("-", ".")}"
                    versionReference.add(VersionReference(versionCatalogReference, "$group:$name:"))
                }
            }
        }

        return "[libraries]\n${librariesSection.joinToString("\n")}\n\n"
    }

    private fun getMaximumSharedSubstring(strings: List<String>): String {
        val minNameLength = strings.minOfOrNull { it.length - 1 } ?: 0
        val stringBuilder = StringBuilder()

        var matchingChar: Char?
        var index = 0
        do {
            matchingChar = strings.map { name -> name[index] }.distinct().takeIf { it.size == 1 }?.get(0)
            if (matchingChar != null) {
                stringBuilder.append(matchingChar)
            }
            index++
        } while (matchingChar != null && index <= minNameLength)
        return stringBuilder.toString()
    }
}

data class VersionReference(
    val libsReference: String,
    val originalReference: String
)